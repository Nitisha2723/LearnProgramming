from datetime import date
from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from typing import List, Optional

from database import get_db
from auth import get_current_user, require_roles
import models
import schemas

router = APIRouter(prefix="/api/calves", tags=["calves"])

WRITE_ROLES = (models.UserRole.admin, models.UserRole.manager, models.UserRole.vet, models.UserRole.data_entry)


@router.get("/", response_model=List[schemas.CalfOut])
def list_calves(
    status: Optional[str] = None,
    sex: Optional[str] = None,
    is_active: Optional[bool] = True,
    limit: int = Query(100, le=500),
    db: Session = Depends(get_db),
    _: models.User = Depends(get_current_user),
):
    q = db.query(models.Calf).order_by(models.Calf.date_of_birth.desc())
    if status:
        q = q.filter(models.Calf.status == status)
    if sex:
        q = q.filter(models.Calf.sex == sex)
    if is_active is not None:
        q = q.filter(models.Calf.is_active == is_active)
    return q.limit(limit).all()


@router.get("/{calf_id}", response_model=schemas.CalfOut)
def get_calf(
    calf_id: int,
    db: Session = Depends(get_db),
    _: models.User = Depends(get_current_user),
):
    calf = db.query(models.Calf).filter(models.Calf.id == calf_id).first()
    if not calf:
        raise HTTPException(404, "Calf not found")
    return calf


@router.post("/", response_model=schemas.CalfOut, status_code=201)
def create_calf(
    payload: schemas.CalfCreate,
    db: Session = Depends(get_db),
    _: models.User = Depends(require_roles(*WRITE_ROLES)),
):
    if db.query(models.Calf).filter(models.Calf.tag_number == payload.tag_number).first():
        raise HTTPException(400, f"Tag number '{payload.tag_number}' already exists")
    dam = db.query(models.Animal).filter(models.Animal.id == payload.dam_id).first()
    if not dam:
        raise HTTPException(404, "Dam (mother animal) not found")
    calf = models.Calf(**payload.model_dump())
    db.add(calf)
    db.commit()
    db.refresh(calf)
    return calf


@router.put("/{calf_id}", response_model=schemas.CalfOut)
def update_calf(
    calf_id: int,
    payload: schemas.CalfUpdate,
    db: Session = Depends(get_db),
    _: models.User = Depends(require_roles(*WRITE_ROLES)),
):
    calf = db.query(models.Calf).filter(models.Calf.id == calf_id).first()
    if not calf:
        raise HTTPException(404, "Calf not found")
    for k, v in payload.model_dump(exclude_unset=True).items():
        setattr(calf, k, v)
    db.commit()
    db.refresh(calf)
    return calf


@router.delete("/{calf_id}", status_code=204)
def delete_calf(
    calf_id: int,
    db: Session = Depends(get_db),
    _: models.User = Depends(require_roles(models.UserRole.admin, models.UserRole.manager)),
):
    calf = db.query(models.Calf).filter(models.Calf.id == calf_id).first()
    if not calf:
        raise HTTPException(404, "Calf not found")
    db.delete(calf)
    db.commit()


# ─── Weight records ───────────────────────────────────────────────────────────

@router.get("/{calf_id}/weights", response_model=List[schemas.CalfWeightOut])
def calf_weights(
    calf_id: int,
    db: Session = Depends(get_db),
    _: models.User = Depends(get_current_user),
):
    return (
        db.query(models.CalfWeightRecord)
        .filter(models.CalfWeightRecord.calf_id == calf_id)
        .order_by(models.CalfWeightRecord.record_date.asc())
        .all()
    )


@router.post("/{calf_id}/weights", response_model=schemas.CalfWeightOut, status_code=201)
def add_weight(
    calf_id: int,
    payload: schemas.CalfWeightCreate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(require_roles(*WRITE_ROLES)),
):
    calf = db.query(models.Calf).filter(models.Calf.id == calf_id).first()
    if not calf:
        raise HTTPException(404, "Calf not found")
    wr = models.CalfWeightRecord(
        calf_id=calf_id,
        record_date=payload.record_date,
        weight_kg=payload.weight_kg,
        recorded_by=current_user.id,
    )
    db.add(wr)
    db.commit()
    db.refresh(wr)
    return wr

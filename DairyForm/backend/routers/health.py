from datetime import date
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List, Optional

from database import get_db
from auth import get_current_user, require_roles
import models
import schemas

router = APIRouter(prefix="/api/health", tags=["health"])

WRITE_ROLES = (models.UserRole.admin, models.UserRole.manager, models.UserRole.vet)


@router.get("/", response_model=List[schemas.HealthRecordOut])
def list_health_records(
    animal_id: Optional[int] = None,
    severity: Optional[str] = None,
    unresolved_only: bool = False,
    db: Session = Depends(get_db),
    _=Depends(get_current_user)
):
    q = db.query(models.HealthRecord)
    if animal_id:
        q = q.filter(models.HealthRecord.animal_id == animal_id)
    if severity:
        q = q.filter(models.HealthRecord.severity == severity)
    if unresolved_only:
        q = q.filter(models.HealthRecord.resolved == False)
    return q.order_by(models.HealthRecord.record_date.desc()).all()


@router.post("/", response_model=schemas.HealthRecordOut)
def create_health_record(
    data: schemas.HealthRecordCreate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(require_roles(*WRITE_ROLES))
):
    record = models.HealthRecord(**data.model_dump(), recorded_by=current_user.id)
    db.add(record)
    # Update animal status if critical
    if data.severity in (models.HealthSeverity.treatment, models.HealthSeverity.critical):
        animal = db.query(models.Animal).filter(models.Animal.id == data.animal_id).first()
        if animal:
            animal.status = models.AnimalStatus.sick
    db.commit()
    db.refresh(record)
    return record


@router.put("/{record_id}", response_model=schemas.HealthRecordOut)
def update_health_record(
    record_id: int,
    data: schemas.HealthRecordUpdate,
    db: Session = Depends(get_db),
    _=Depends(require_roles(*WRITE_ROLES))
):
    r = db.query(models.HealthRecord).filter(models.HealthRecord.id == record_id).first()
    if not r:
        raise HTTPException(404, "Record not found")
    for k, v in data.model_dump(exclude_none=True).items():
        setattr(r, k, v)
    db.commit()
    db.refresh(r)
    return r


@router.get("/alerts")
def health_alerts(db: Session = Depends(get_db), _=Depends(get_current_user)):
    active = (
        db.query(models.HealthRecord)
        .filter(models.HealthRecord.resolved == False)
        .order_by(models.HealthRecord.record_date.desc())
        .all()
    )
    return active

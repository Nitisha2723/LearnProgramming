from datetime import date, timedelta
from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from sqlalchemy import func, and_
from typing import List, Optional

from database import get_db
from auth import get_current_user, require_roles
import models
import schemas

router = APIRouter(prefix="/api/animals", tags=["animals"])

WRITE_ROLES = (models.UserRole.admin, models.UserRole.manager, models.UserRole.data_entry)


@router.get("/", response_model=List[schemas.AnimalOut])
def list_animals(
    status: Optional[str] = None,
    active_only: bool = True,
    db: Session = Depends(get_db),
    _: models.User = Depends(get_current_user)
):
    q = db.query(models.Animal)
    if active_only:
        q = q.filter(models.Animal.is_active == True)
    if status:
        q = q.filter(models.Animal.status == status)
    return q.order_by(models.Animal.tag_number).all()


@router.get("/{animal_id}", response_model=schemas.AnimalOut)
def get_animal(animal_id: int, db: Session = Depends(get_db), _=Depends(get_current_user)):
    a = db.query(models.Animal).filter(models.Animal.id == animal_id).first()
    if not a:
        raise HTTPException(404, "Animal not found")
    return a


@router.post("/", response_model=schemas.AnimalOut)
def create_animal(
    data: schemas.AnimalCreate,
    db: Session = Depends(get_db),
    _=Depends(require_roles(*WRITE_ROLES))
):
    if db.query(models.Animal).filter(models.Animal.tag_number == data.tag_number).first():
        raise HTTPException(400, "Tag number already exists")
    a = models.Animal(**data.model_dump())
    db.add(a)
    db.commit()
    db.refresh(a)
    return a


@router.put("/{animal_id}", response_model=schemas.AnimalOut)
def update_animal(
    animal_id: int,
    data: schemas.AnimalUpdate,
    db: Session = Depends(get_db),
    _=Depends(require_roles(*WRITE_ROLES))
):
    a = db.query(models.Animal).filter(models.Animal.id == animal_id).first()
    if not a:
        raise HTTPException(404, "Animal not found")
    for k, v in data.model_dump(exclude_none=True).items():
        setattr(a, k, v)
    db.commit()
    db.refresh(a)
    return a


@router.get("/{animal_id}/milk-history")
def animal_milk_history(
    animal_id: int,
    days: int = 30,
    db: Session = Depends(get_db),
    _=Depends(get_current_user)
):
    since = date.today() - timedelta(days=days)
    records = (
        db.query(models.MilkRecord)
        .filter(models.MilkRecord.animal_id == animal_id, models.MilkRecord.record_date >= since)
        .order_by(models.MilkRecord.record_date)
        .all()
    )
    return records


@router.get("/{animal_id}/health-history")
def animal_health_history(animal_id: int, db: Session = Depends(get_db), _=Depends(get_current_user)):
    return (
        db.query(models.HealthRecord)
        .filter(models.HealthRecord.animal_id == animal_id)
        .order_by(models.HealthRecord.record_date.desc())
        .all()
    )

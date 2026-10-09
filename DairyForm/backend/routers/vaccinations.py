from datetime import date
from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session, joinedload
from typing import List, Optional

from database import get_db
from auth import get_current_user, require_roles
import models
import schemas

router = APIRouter(prefix="/api/vaccinations", tags=["vaccinations"])

WRITE_ROLES = (models.UserRole.admin, models.UserRole.manager, models.UserRole.vet)


@router.get("/", response_model=List[schemas.VaccinationOut])
def list_vaccinations(
    animal_id: Optional[int] = None,
    vaccine_name: Optional[str] = None,
    from_date: Optional[date] = None,
    to_date: Optional[date] = None,
    limit: int = Query(200, le=500),
    db: Session = Depends(get_db),
    _: models.User = Depends(get_current_user),
):
    q = db.query(models.Vaccination).order_by(models.Vaccination.vaccination_date.desc())
    if animal_id:
        q = q.filter(models.Vaccination.animal_id == animal_id)
    if vaccine_name:
        q = q.filter(models.Vaccination.vaccine_name.ilike(f"%{vaccine_name}%"))
    if from_date:
        q = q.filter(models.Vaccination.vaccination_date >= from_date)
    if to_date:
        q = q.filter(models.Vaccination.vaccination_date <= to_date)
    return q.limit(limit).all()


@router.get("/due", response_model=List[schemas.VaccinationDue])
def vaccinations_due(
    days_ahead: int = Query(30, le=365),
    db: Session = Depends(get_db),
    _: models.User = Depends(get_current_user),
):
    """Return vaccinations due within the next N days (or already overdue)."""
    today = date.today()
    cutoff = today
    from datetime import timedelta
    future = today + timedelta(days=days_ahead)

    rows = (
        db.query(models.Vaccination, models.Animal)
        .join(models.Animal, models.Vaccination.animal_id == models.Animal.id)
        .filter(models.Vaccination.next_due_date != None)
        .filter(models.Vaccination.next_due_date <= future)
        .filter(models.Animal.is_active == True)
        .order_by(models.Vaccination.next_due_date.asc())
        .all()
    )

    result = []
    for vax, animal in rows:
        result.append(schemas.VaccinationDue(
            animal_id=animal.id,
            tag_number=animal.tag_number,
            animal_name=animal.name,
            vaccine_name=vax.vaccine_name,
            next_due_date=vax.next_due_date,
            days_overdue=max(0, (today - vax.next_due_date).days),
        ))
    return result


@router.post("/", response_model=schemas.VaccinationOut, status_code=201)
def create_vaccination(
    payload: schemas.VaccinationCreate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(require_roles(*WRITE_ROLES)),
):
    animal = db.query(models.Animal).filter(models.Animal.id == payload.animal_id).first()
    if not animal:
        raise HTTPException(404, "Animal not found")
    vax = models.Vaccination(**payload.model_dump(), recorded_by=current_user.id)
    db.add(vax)
    db.commit()
    db.refresh(vax)
    return vax


@router.put("/{vax_id}", response_model=schemas.VaccinationOut)
def update_vaccination(
    vax_id: int,
    payload: schemas.VaccinationCreate,
    db: Session = Depends(get_db),
    _: models.User = Depends(require_roles(*WRITE_ROLES)),
):
    vax = db.query(models.Vaccination).filter(models.Vaccination.id == vax_id).first()
    if not vax:
        raise HTTPException(404, "Vaccination not found")
    for k, v in payload.model_dump(exclude_unset=True).items():
        setattr(vax, k, v)
    db.commit()
    db.refresh(vax)
    return vax


@router.delete("/{vax_id}", status_code=204)
def delete_vaccination(
    vax_id: int,
    db: Session = Depends(get_db),
    _: models.User = Depends(require_roles(models.UserRole.admin, models.UserRole.manager)),
):
    vax = db.query(models.Vaccination).filter(models.Vaccination.id == vax_id).first()
    if not vax:
        raise HTTPException(404, "Vaccination not found")
    db.delete(vax)
    db.commit()


@router.get("/animal/{animal_id}", response_model=List[schemas.VaccinationOut])
def animal_vaccinations(
    animal_id: int,
    db: Session = Depends(get_db),
    _: models.User = Depends(get_current_user),
):
    return (
        db.query(models.Vaccination)
        .filter(models.Vaccination.animal_id == animal_id)
        .order_by(models.Vaccination.vaccination_date.desc())
        .all()
    )

from datetime import date, timedelta
from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from typing import List, Optional

from database import get_db
from auth import get_current_user, require_roles
import models
import schemas

router = APIRouter(prefix="/api/reproductive", tags=["reproductive"])

WRITE_ROLES = (models.UserRole.admin, models.UserRole.manager, models.UserRole.vet, models.UserRole.data_entry)


@router.get("/", response_model=List[schemas.ReproEventOut])
def list_events(
    animal_id: Optional[int] = None,
    event_type: Optional[str] = None,
    from_date: Optional[date] = None,
    to_date: Optional[date] = None,
    limit: int = Query(200, le=500),
    db: Session = Depends(get_db),
    _: models.User = Depends(get_current_user),
):
    q = db.query(models.ReproductiveEvent).order_by(models.ReproductiveEvent.event_date.desc())
    if animal_id:
        q = q.filter(models.ReproductiveEvent.animal_id == animal_id)
    if event_type:
        q = q.filter(models.ReproductiveEvent.event_type == event_type)
    if from_date:
        q = q.filter(models.ReproductiveEvent.event_date >= from_date)
    if to_date:
        q = q.filter(models.ReproductiveEvent.event_date <= to_date)
    return q.limit(limit).all()


@router.post("/", response_model=schemas.ReproEventOut, status_code=201)
def create_event(
    payload: schemas.ReproEventCreate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(require_roles(*WRITE_ROLES)),
):
    animal = db.query(models.Animal).filter(models.Animal.id == payload.animal_id).first()
    if not animal:
        raise HTTPException(404, "Animal not found")

    ev = models.ReproductiveEvent(**payload.model_dump(), recorded_by=current_user.id)

    # Auto-set next heat date for failed AI (21-day cycle)
    if payload.event_type == models.ReproEventType.pregnancy_negative and not payload.next_heat_expected:
        ev.next_heat_expected = payload.event_date + timedelta(days=21)

    # Update animal scalar fields for calving events
    if payload.event_type == models.ReproEventType.calving:
        animal.last_calving_date = payload.event_date
        animal.parity = (animal.parity or 0) + 1
        animal.status = models.AnimalStatus.lactating
        animal.expected_calving_date = None

    # Auto-set calving/dry dates when AI is done (Murrah buffalo = 310-day gestation)
    if payload.event_type == models.ReproEventType.ai_done:
        animal.expected_calving_date = payload.event_date + timedelta(days=310)
        animal.expected_dry_date = payload.event_date + timedelta(days=250)
        animal.status = models.AnimalStatus.pregnant

    # Update status for confirmed pregnancy
    if payload.event_type == models.ReproEventType.pregnancy_confirmed:
        animal.status = models.AnimalStatus.pregnant

    db.add(ev)
    db.commit()
    db.refresh(ev)
    return ev


@router.put("/{event_id}", response_model=schemas.ReproEventOut)
def update_event(
    event_id: int,
    payload: schemas.ReproEventCreate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(require_roles(*WRITE_ROLES)),
):
    ev = db.query(models.ReproductiveEvent).filter(models.ReproductiveEvent.id == event_id).first()
    if not ev:
        raise HTTPException(404, "Event not found")
    for k, v in payload.model_dump(exclude_unset=True).items():
        setattr(ev, k, v)
    db.commit()
    db.refresh(ev)
    return ev


@router.delete("/{event_id}", status_code=204)
def delete_event(
    event_id: int,
    db: Session = Depends(get_db),
    _: models.User = Depends(require_roles(models.UserRole.admin, models.UserRole.manager)),
):
    ev = db.query(models.ReproductiveEvent).filter(models.ReproductiveEvent.id == event_id).first()
    if not ev:
        raise HTTPException(404, "Event not found")
    db.delete(ev)
    db.commit()


@router.get("/animal/{animal_id}", response_model=List[schemas.ReproEventOut])
def animal_history(
    animal_id: int,
    db: Session = Depends(get_db),
    _: models.User = Depends(get_current_user),
):
    return (
        db.query(models.ReproductiveEvent)
        .filter(models.ReproductiveEvent.animal_id == animal_id)
        .order_by(models.ReproductiveEvent.event_date.desc())
        .all()
    )

from datetime import date
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from sqlalchemy import func
from typing import List, Optional

from database import get_db
from auth import get_current_user, require_roles
import models
import schemas

router = APIRouter(prefix="/api/feed", tags=["feed"])

WRITE_ROLES = (models.UserRole.admin, models.UserRole.manager, models.UserRole.data_entry)


@router.get("/", response_model=List[schemas.FeedLogOut])
def list_feed_logs(
    start_date: Optional[date] = None,
    end_date: Optional[date] = None,
    db: Session = Depends(get_db),
    _=Depends(get_current_user)
):
    q = db.query(models.FeedLog)
    if start_date:
        q = q.filter(models.FeedLog.log_date >= start_date)
    if end_date:
        q = q.filter(models.FeedLog.log_date <= end_date)
    return q.order_by(models.FeedLog.log_date.desc()).all()


@router.post("/", response_model=schemas.FeedLogOut)
def create_feed_log(
    data: schemas.FeedLogCreate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(require_roles(*WRITE_ROLES))
):
    total = data.green_fodder_kg + data.dry_fodder_kg + data.concentrate_kg + data.mineral_mix_kg
    record = models.FeedLog(**data.model_dump(), total_kg=total, recorded_by=current_user.id)
    db.add(record)
    db.commit()
    db.refresh(record)
    return record


@router.get("/today")
def today_feed(db: Session = Depends(get_db), _=Depends(get_current_user)):
    today = date.today()
    rows = db.query(models.FeedLog).filter(models.FeedLog.log_date == today).all()
    totals = {"green_fodder_kg": 0.0, "dry_fodder_kg": 0.0, "concentrate_kg": 0.0, "total_kg": 0.0, "cost": 0.0}
    for r in rows:
        totals["green_fodder_kg"] += r.green_fodder_kg or 0
        totals["dry_fodder_kg"] += r.dry_fodder_kg or 0
        totals["concentrate_kg"] += r.concentrate_kg or 0
        totals["total_kg"] += r.total_kg or 0
        totals["cost"] += r.cost or 0
    return totals

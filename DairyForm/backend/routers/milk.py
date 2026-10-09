from datetime import date, timedelta
from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from sqlalchemy import func, and_
from typing import List, Optional

from database import get_db
from auth import get_current_user, require_roles
import models
import schemas

router = APIRouter(prefix="/api/milk", tags=["milk"])

WRITE_ROLES = (models.UserRole.admin, models.UserRole.manager, models.UserRole.data_entry)


@router.post("/", response_model=schemas.MilkRecordOut)
def create_milk_record(
    data: schemas.MilkRecordCreate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(require_roles(*WRITE_ROLES))
):
    record = models.MilkRecord(**data.model_dump(), recorded_by=current_user.id)
    db.add(record)
    db.commit()
    db.refresh(record)
    return record


@router.get("/", response_model=List[schemas.MilkRecordOut])
def list_milk_records(
    start_date: Optional[date] = None,
    end_date: Optional[date] = None,
    animal_id: Optional[int] = None,
    db: Session = Depends(get_db),
    _=Depends(get_current_user)
):
    q = db.query(models.MilkRecord)
    if animal_id:
        q = q.filter(models.MilkRecord.animal_id == animal_id)
    if start_date:
        q = q.filter(models.MilkRecord.record_date >= start_date)
    if end_date:
        q = q.filter(models.MilkRecord.record_date <= end_date)
    return q.order_by(models.MilkRecord.record_date.desc(), models.MilkRecord.session).all()


@router.get("/summary/daily")
def daily_summary(
    days: int = 30,
    db: Session = Depends(get_db),
    _=Depends(get_current_user)
):
    since = date.today() - timedelta(days=days)
    rows = (
        db.query(
            models.MilkRecord.record_date,
            models.MilkRecord.session,
            func.sum(models.MilkRecord.quantity_litres).label("total"),
            func.count(models.MilkRecord.animal_id.distinct()).label("animals"),
        )
        .filter(models.MilkRecord.record_date >= since)
        .group_by(models.MilkRecord.record_date, models.MilkRecord.session)
        .order_by(models.MilkRecord.record_date)
        .all()
    )
    # Pivot by date
    summary: dict = {}
    for r in rows:
        d = str(r.record_date)
        if d not in summary:
            summary[d] = {"date": d, "morning": 0, "evening": 0, "total": 0, "animals": 0}
        summary[d][r.session] = float(r.total or 0)
        summary[d]["total"] += float(r.total or 0)
        summary[d]["animals"] = max(summary[d]["animals"], r.animals)
    return list(summary.values())


@router.get("/today")
def today_milk(db: Session = Depends(get_db), _=Depends(get_current_user)):
    today = date.today()
    rows = (
        db.query(
            models.MilkRecord.session,
            func.sum(models.MilkRecord.quantity_litres).label("total"),
            func.count(models.MilkRecord.animal_id).label("count"),
        )
        .filter(models.MilkRecord.record_date == today)
        .group_by(models.MilkRecord.session)
        .all()
    )
    result = {"morning": 0.0, "evening": 0.0, "total": 0.0, "animals": 0}
    for r in rows:
        result[r.session] = float(r.total or 0)
        result["total"] += float(r.total or 0)
        result["animals"] = max(result["animals"], r.count)
    return result


@router.delete("/{record_id}")
def delete_milk_record(
    record_id: int,
    db: Session = Depends(get_db),
    _=Depends(require_roles(models.UserRole.admin, models.UserRole.manager))
):
    r = db.query(models.MilkRecord).filter(models.MilkRecord.id == record_id).first()
    if not r:
        raise HTTPException(404, "Record not found")
    db.delete(r)
    db.commit()
    return {"message": "Deleted"}

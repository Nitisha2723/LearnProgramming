from datetime import date
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List, Optional

from database import get_db
from auth import get_current_user, require_roles
import models
import schemas

router = APIRouter(prefix="/api/quality", tags=["quality"])

WRITE_ROLES = (models.UserRole.admin, models.UserRole.manager, models.UserRole.quality)


@router.get("/", response_model=List[schemas.QualityCheckOut])
def list_quality_checks(
    start_date: Optional[date] = None,
    end_date: Optional[date] = None,
    animal_id: Optional[int] = None,
    db: Session = Depends(get_db),
    _=Depends(get_current_user)
):
    q = db.query(models.QualityCheck)
    if animal_id:
        q = q.filter(models.QualityCheck.animal_id == animal_id)
    if start_date:
        q = q.filter(models.QualityCheck.check_date >= start_date)
    if end_date:
        q = q.filter(models.QualityCheck.check_date <= end_date)
    return q.order_by(models.QualityCheck.check_date.desc()).all()


@router.post("/", response_model=schemas.QualityCheckOut)
def create_quality_check(
    data: schemas.QualityCheckCreate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(require_roles(*WRITE_ROLES))
):
    # Auto-calculate grade
    grade = None
    if data.fat_percent and data.snf_percent:
        if data.fat_percent >= 7.0 and data.snf_percent >= 9.0:
            grade = "A"
        elif data.fat_percent >= 6.0 and data.snf_percent >= 8.5:
            grade = "B"
        else:
            grade = "C"

    record = models.QualityCheck(**data.model_dump(), grade=grade, recorded_by=current_user.id)
    db.add(record)
    db.commit()
    db.refresh(record)
    return record


@router.get("/latest")
def latest_quality(db: Session = Depends(get_db), _=Depends(get_current_user)):
    r = (
        db.query(models.QualityCheck)
        .filter(models.QualityCheck.animal_id == None)  # bulk tank
        .order_by(models.QualityCheck.check_date.desc())
        .first()
    )
    return r


@router.get("/averages")
def quality_averages(days: int = 30, db: Session = Depends(get_db), _=Depends(get_current_user)):
    from datetime import timedelta
    from sqlalchemy import func
    since = date.today() - timedelta(days=days)
    row = (
        db.query(
            func.avg(models.QualityCheck.fat_percent).label("avg_fat"),
            func.avg(models.QualityCheck.snf_percent).label("avg_snf"),
            func.avg(models.QualityCheck.protein_percent).label("avg_protein"),
            func.count(models.QualityCheck.id).label("checks"),
        )
        .filter(models.QualityCheck.check_date >= since)
        .first()
    )
    return {
        "avg_fat": round(float(row.avg_fat or 0), 2),
        "avg_snf": round(float(row.avg_snf or 0), 2),
        "avg_protein": round(float(row.avg_protein or 0), 2),
        "checks": row.checks,
    }

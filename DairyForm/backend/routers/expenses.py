from datetime import date, timedelta
from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from sqlalchemy import func, extract
from typing import List, Optional

from database import get_db
from auth import get_current_user, require_roles
import models
import schemas

router = APIRouter(prefix="/api/expenses", tags=["expenses"])

WRITE_ROLES = (models.UserRole.admin, models.UserRole.manager, models.UserRole.data_entry)


@router.get("/", response_model=List[schemas.ExpenseOut])
def list_expenses(
    category: Optional[str] = None,
    from_date: Optional[date] = None,
    to_date: Optional[date] = None,
    limit: int = Query(100, le=500),
    db: Session = Depends(get_db),
    _: models.User = Depends(get_current_user),
):
    q = db.query(models.Expense)
    if category:
        q = q.filter(models.Expense.category == category)
    if from_date:
        q = q.filter(models.Expense.expense_date >= from_date)
    if to_date:
        q = q.filter(models.Expense.expense_date <= to_date)
    return q.order_by(models.Expense.expense_date.desc()).limit(limit).all()


@router.get("/summary")
def expense_summary(
    months: int = 1,
    db: Session = Depends(get_db),
    _: models.User = Depends(get_current_user),
):
    since = date.today() - timedelta(days=30 * months)
    rows = (
        db.query(
            models.Expense.category,
            func.sum(models.Expense.amount).label("total"),
            func.count(models.Expense.id).label("count"),
        )
        .filter(models.Expense.expense_date >= since)
        .group_by(models.Expense.category)
        .all()
    )
    total = sum(r.total for r in rows)
    return {
        "period_months": months,
        "since": since.isoformat(),
        "grand_total": round(total, 2),
        "by_category": [
            {"category": r.category, "total": round(r.total, 2), "count": r.count}
            for r in sorted(rows, key=lambda x: x.total, reverse=True)
        ],
    }


@router.get("/monthly")
def monthly_totals(
    months: int = 6,
    db: Session = Depends(get_db),
    _: models.User = Depends(get_current_user),
):
    since = date.today() - timedelta(days=30 * months)
    rows = (
        db.query(
            extract("year", models.Expense.expense_date).label("year"),
            extract("month", models.Expense.expense_date).label("month"),
            func.sum(models.Expense.amount).label("total"),
        )
        .filter(models.Expense.expense_date >= since)
        .group_by("year", "month")
        .order_by("year", "month")
        .all()
    )
    return [
        {"month": f"{int(r.year):04d}-{int(r.month):02d}", "total": round(r.total, 2)}
        for r in rows
    ]


@router.post("/", response_model=schemas.ExpenseOut)
def create_expense(
    data: schemas.ExpenseCreate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(require_roles(*WRITE_ROLES)),
):
    expense = models.Expense(**data.model_dump(), recorded_by=current_user.id)
    db.add(expense)
    db.commit()
    db.refresh(expense)
    return expense


@router.put("/{expense_id}", response_model=schemas.ExpenseOut)
def update_expense(
    expense_id: int,
    data: schemas.ExpenseCreate,
    db: Session = Depends(get_db),
    _: models.User = Depends(require_roles(*WRITE_ROLES)),
):
    expense = db.query(models.Expense).filter(models.Expense.id == expense_id).first()
    if not expense:
        raise HTTPException(404, "Expense not found")
    for k, v in data.model_dump(exclude_none=True).items():
        setattr(expense, k, v)
    db.commit()
    db.refresh(expense)
    return expense


@router.delete("/{expense_id}")
def delete_expense(
    expense_id: int,
    db: Session = Depends(get_db),
    _: models.User = Depends(require_roles(models.UserRole.admin, models.UserRole.manager)),
):
    expense = db.query(models.Expense).filter(models.Expense.id == expense_id).first()
    if not expense:
        raise HTTPException(404, "Expense not found")
    db.delete(expense)
    db.commit()
    return {"ok": True}

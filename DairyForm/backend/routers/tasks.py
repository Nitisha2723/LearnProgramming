from datetime import date, datetime
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List, Optional

from database import get_db
from auth import get_current_user, require_roles
import models
import schemas

router = APIRouter(prefix="/api/tasks", tags=["tasks"])

WRITE_ROLES = (models.UserRole.admin, models.UserRole.manager)


@router.get("/", response_model=List[schemas.TaskOut])
def list_tasks(
    assigned_to: Optional[int] = None,
    status: Optional[str] = None,
    due_date: Optional[date] = None,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    q = db.query(models.Task)
    # Non-admin sees only their own tasks
    if current_user.role not in (models.UserRole.admin, models.UserRole.manager):
        q = q.filter(models.Task.assigned_to == current_user.id)
    elif assigned_to:
        q = q.filter(models.Task.assigned_to == assigned_to)

    if status:
        q = q.filter(models.Task.status == status)
    if due_date:
        q = q.filter(models.Task.due_date == due_date)

    return q.order_by(models.Task.due_date, models.Task.priority.desc()).all()


@router.get("/calendar")
def task_calendar(
    year: int,
    month: int,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    from calendar import monthrange
    first_day = date(year, month, 1)
    last_day = date(year, month, monthrange(year, month)[1])

    q = db.query(models.Task).filter(
        models.Task.due_date >= first_day,
        models.Task.due_date <= last_day
    )
    if current_user.role not in (models.UserRole.admin, models.UserRole.manager):
        q = q.filter(models.Task.assigned_to == current_user.id)

    tasks = q.all()
    # Group by date
    calendar: dict = {}
    for t in tasks:
        d = str(t.due_date)
        if d not in calendar:
            calendar[d] = []
        calendar[d].append({
            "id": t.id,
            "title": t.title,
            "category": t.category,
            "priority": t.priority,
            "status": t.status,
            "assigned_to": t.assigned_to,
        })
    return calendar


@router.post("/", response_model=schemas.TaskOut)
def create_task(
    data: schemas.TaskCreate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(require_roles(*WRITE_ROLES))
):
    task = models.Task(**data.model_dump(), created_by=current_user.id)
    db.add(task)
    db.commit()
    db.refresh(task)
    return task


@router.put("/{task_id}", response_model=schemas.TaskOut)
def update_task(
    task_id: int,
    data: schemas.TaskUpdate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    task = db.query(models.Task).filter(models.Task.id == task_id).first()
    if not task:
        raise HTTPException(404, "Task not found")

    # Workers can only update status of their own tasks
    if current_user.role not in (models.UserRole.admin, models.UserRole.manager):
        if task.assigned_to != current_user.id:
            raise HTTPException(403, "Not your task")
        if data.status:
            task.status = data.status
            if data.status == models.TaskStatus.done:
                task.completed_at = datetime.utcnow()
        db.commit()
        db.refresh(task)
        return task

    for k, v in data.model_dump(exclude_none=True).items():
        setattr(task, k, v)
    if data.status == models.TaskStatus.done and not task.completed_at:
        task.completed_at = datetime.utcnow()
    db.commit()
    db.refresh(task)
    return task


@router.delete("/{task_id}")
def delete_task(
    task_id: int,
    db: Session = Depends(get_db),
    _=Depends(require_roles(*WRITE_ROLES))
):
    task = db.query(models.Task).filter(models.Task.id == task_id).first()
    if not task:
        raise HTTPException(404, "Task not found")
    db.delete(task)
    db.commit()
    return {"message": "Deleted"}


@router.get("/my/today")
def my_tasks_today(
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    today = date.today()
    return (
        db.query(models.Task)
        .filter(
            models.Task.assigned_to == current_user.id,
            models.Task.due_date == today,
            models.Task.status != models.TaskStatus.done
        )
        .all()
    )

from datetime import date, timedelta
from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from sqlalchemy import func

from database import get_db
from auth import get_current_user
import models

router = APIRouter(prefix="/api/dashboard", tags=["dashboard"])


@router.get("/stats")
def dashboard_stats(db: Session = Depends(get_db), _=Depends(get_current_user)):
    today = date.today()
    yesterday = today - timedelta(days=1)

    # Animal counts
    animals = db.query(models.Animal).filter(models.Animal.is_active == True).all()
    total = len(animals)
    by_status = {}
    for a in animals:
        by_status[a.status.value] = by_status.get(a.status.value, 0) + 1

    # Milk today / yesterday
    def milk_on(d):
        r = db.query(func.sum(models.MilkRecord.quantity_litres)).filter(
            models.MilkRecord.record_date == d
        ).scalar()
        return float(r or 0)

    milk_today = milk_on(today)
    milk_yesterday = milk_on(yesterday)
    trend = round(((milk_today - milk_yesterday) / milk_yesterday * 100) if milk_yesterday else 0, 1)

    # Tasks
    tasks_today = db.query(models.Task).filter(
        models.Task.due_date == today,
        models.Task.status != models.TaskStatus.done
    ).count()
    tasks_overdue = db.query(models.Task).filter(
        models.Task.due_date < today,
        models.Task.status != models.TaskStatus.done
    ).count()

    # Quality today
    latest_qc = db.query(models.QualityCheck).filter(
        models.QualityCheck.check_date == today,
        models.QualityCheck.animal_id == None
    ).order_by(models.QualityCheck.created_at.desc()).first()

    # Alerts
    alerts = []
    sick = [a for a in animals if a.status == models.AnimalStatus.sick]
    for a in sick:
        alerts.append({"type": "health", "message": f"{a.tag_number} {a.name or ''} is sick", "severity": "high"})

    if tasks_overdue > 0:
        alerts.append({"type": "task", "message": f"{tasks_overdue} task(s) overdue", "severity": "medium"})

    if latest_qc and latest_qc.grade == "C":
        alerts.append({"type": "quality", "message": "Milk quality Grade C today", "severity": "high"})

    # Recent active health issues
    active_health = db.query(models.HealthRecord).filter(
        models.HealthRecord.resolved == False,
        models.HealthRecord.severity.in_([models.HealthSeverity.treatment, models.HealthSeverity.critical])
    ).count()
    if active_health:
        alerts.append({"type": "health", "message": f"{active_health} animal(s) under treatment", "severity": "medium"})

    # Vaccinations overdue
    vax_overdue = db.query(models.Vaccination).filter(
        models.Vaccination.next_due_date != None,
        models.Vaccination.next_due_date < today,
    ).count()
    if vax_overdue > 0:
        alerts.append({"type": "vaccination", "message": f"{vax_overdue} vaccination(s) overdue", "severity": "high"})

    # Vaccinations due in next 7 days
    vax_due_soon = db.query(models.Vaccination).filter(
        models.Vaccination.next_due_date != None,
        models.Vaccination.next_due_date >= today,
        models.Vaccination.next_due_date <= today + timedelta(days=7),
    ).count()
    if vax_due_soon > 0:
        alerts.append({"type": "vaccination", "message": f"{vax_due_soon} vaccination(s) due within 7 days", "severity": "medium"})

    # Calving due within 7 days (pregnant animals with expected_calving_date set)
    calving_due = db.query(models.Animal).filter(
        models.Animal.status == models.AnimalStatus.pregnant,
        models.Animal.expected_calving_date != None,
        models.Animal.expected_calving_date <= today + timedelta(days=7),
        models.Animal.is_active == True,
    ).all()
    for a in calving_due:
        days_left = (a.expected_calving_date - today).days
        msg = f"{a.tag_number} {a.name or ''} calving {'today' if days_left == 0 else f'in {days_left}d'}"
        alerts.append({"type": "calving", "message": msg, "severity": "high" if days_left <= 2 else "medium"})

    # Heat expected (from repro records — pregnancy_negative events with next_heat_expected within 3 days)
    heat_due = db.query(models.ReproductiveEvent).filter(
        models.ReproductiveEvent.event_type == models.ReproEventType.pregnancy_negative,
        models.ReproductiveEvent.next_heat_expected != None,
        models.ReproductiveEvent.next_heat_expected >= today,
        models.ReproductiveEvent.next_heat_expected <= today + timedelta(days=3),
    ).all()
    animal_ids_heat = {e.animal_id for e in heat_due}
    if animal_ids_heat:
        alerts.append({"type": "repro", "message": f"{len(animal_ids_heat)} animal(s) expected in heat within 3 days", "severity": "medium"})

    return {
        "total_animals": total,
        "lactating": by_status.get("lactating", 0),
        "dry": by_status.get("dry", 0),
        "pregnant": by_status.get("pregnant", 0),
        "sick": by_status.get("sick", 0),
        "milk_today_litres": milk_today,
        "milk_yesterday_litres": milk_yesterday,
        "milk_trend_pct": trend,
        "tasks_today": tasks_today,
        "tasks_overdue": tasks_overdue,
        "quality_grade_today": latest_qc.grade if latest_qc else None,
        "avg_fat_today": latest_qc.fat_percent if latest_qc else None,
        "avg_snf_today": latest_qc.snf_percent if latest_qc else None,
        "alerts": alerts,
    }


@router.get("/herd-health")
def herd_health(db: Session = Depends(get_db), _=Depends(get_current_user)):
    animals = db.query(models.Animal).filter(models.Animal.is_active == True).all()
    active_issues = {
        r.animal_id for r in
        db.query(models.HealthRecord).filter(models.HealthRecord.resolved == False).all()
    }
    healthy, attention, treatment = 0, 0, 0
    for a in animals:
        if a.id in active_issues:
            if a.status == models.AnimalStatus.sick:
                treatment += 1
            else:
                attention += 1
        else:
            healthy += 1
    return {"healthy": healthy, "attention": attention, "treatment": treatment, "total": len(animals)}

"""
Bhavani Dairy Farm — Smart Management System
FastAPI Backend
"""
from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
import os

from database import engine, SessionLocal
import models
from auth import hash_password

from routers import auth, users, animals, milk, health, quality, feed, tasks, devices, dashboard, expenses, reproductive, calves, vaccinations


@asynccontextmanager
async def lifespan(app: FastAPI):
    # Create all tables
    models.Base.metadata.create_all(bind=engine)
    # Seed admin user and demo data
    seed_database()
    yield


app = FastAPI(
    title="Bhavani Dairy Farm API",
    description="Smart Dairy Management System — Srikakulam, AP",
    version="1.0.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Register all routers
app.include_router(auth.router)
app.include_router(users.router)
app.include_router(animals.router)
app.include_router(milk.router)
app.include_router(health.router)
app.include_router(quality.router)
app.include_router(feed.router)
app.include_router(tasks.router)
app.include_router(devices.router)
app.include_router(dashboard.router)
app.include_router(expenses.router)
app.include_router(reproductive.router)
app.include_router(calves.router)
app.include_router(vaccinations.router)


@app.get("/api/health-check")
def health_check():
    return {"status": "ok", "app": "Bhavani Dairy Farm API"}


def seed_database():
    from datetime import date, timedelta
    import random

    db = SessionLocal()
    try:
        # Skip if admin already exists
        if db.query(models.User).first():
            return

        # ── Admin user ────────────────────────────────────────────────
        admin = models.User(
            name=os.getenv("ADMIN_NAME", "Smt. D. Bhavani"),
            email=os.getenv("ADMIN_EMAIL", "admin@bhavani.farm"),
            hashed_password=hash_password(os.getenv("ADMIN_PASSWORD", "Admin@2026")),
            role=models.UserRole.admin,
            avatar_initials="DB",
            is_active=True,
        )
        db.add(admin)
        db.flush()

        # ── Staff users ───────────────────────────────────────────────
        staff = [
            ("Ravi Kumar", "ravi@bhavani.farm", "Manager@123", models.UserRole.manager),
            ("Priya Devi", "priya@bhavani.farm", "Vet@2026", models.UserRole.vet),
            ("Suresh Babu", "suresh@bhavani.farm", "Entry@2026", models.UserRole.data_entry),
            ("Lakshmi", "lakshmi@bhavani.farm", "Quality@2026", models.UserRole.quality),
            ("Venkat Rao", "venkat@bhavani.farm", "Monitor@2026", models.UserRole.monitor),
        ]
        user_objs = []
        for name, email, pwd, role in staff:
            initials = "".join(w[0].upper() for w in name.split()[:2])
            u = models.User(
                name=name, email=email,
                hashed_password=hash_password(pwd),
                role=role, avatar_initials=initials,
                created_by=admin.id
            )
            db.add(u)
            user_objs.append(u)
        db.flush()

        data_entry_user = user_objs[2]  # Suresh
        vet_user = user_objs[1]         # Priya
        quality_user = user_objs[3]     # Lakshmi

        # ── Animals (40 Murrah buffaloes) ──────────────────────────────
        animal_names = [
            "Kamala","Meena","Durga","Saraswati","Lakshmi",
            "Bhavani","Ganga","Yamuna","Kaveri","Godavari",
            "Seetha","Radha","Parvati","Tulasi","Padma",
            "Rukmini","Draupadi","Savitri","Annapurna","Varalakshmi",
            "Suma","Uma","Rekha","Geeta","Nirmala",
            "Sunita","Pushpa","Shanthi","Vijaya","Revathi",
            "Archana","Sudha","Saroja","Vasantha","Hema",
            "Nalini","Malathi","Usha","Vimala","Jyothi",
        ]
        statuses = [models.AnimalStatus.lactating] * 32 + \
                   [models.AnimalStatus.pregnant] * 5 + \
                   [models.AnimalStatus.dry] * 2 + \
                   [models.AnimalStatus.heifer]

        today = date.today()
        animals = []
        for i, name in enumerate(animal_names):
            tag = f"B-{i+1:02d}"
            dob = today - timedelta(days=365 * random.randint(3, 7))
            dop = today - timedelta(days=random.randint(30, 365))
            last_calving = today - timedelta(days=random.randint(30, 180))
            a = models.Animal(
                tag_number=tag,
                name=name,
                breed="Murrah Buffalo",
                date_of_birth=dob,
                date_of_purchase=dop,
                purchase_price=random.choice([80000, 75000, 55000]),
                status=statuses[i],
                parity=random.randint(1, 4),
                last_calving_date=last_calving if statuses[i] == models.AnimalStatus.lactating else None,
                weight_kg=random.uniform(420, 560),
                iot_device_id=f"IOT-{tag}",
            )
            db.add(a)
            animals.append(a)
        db.flush()

        # ── Milk records — last 30 days ────────────────────────────────
        lactating = [a for a in animals if a.status == models.AnimalStatus.lactating]
        for day_offset in range(30, -1, -1):
            rec_date = today - timedelta(days=day_offset)
            for animal in lactating:
                for session in ["morning", "evening"]:
                    base = random.uniform(5.5, 9.5) if session == "morning" else random.uniform(4.5, 7.5)
                    mr = models.MilkRecord(
                        animal_id=animal.id,
                        record_date=rec_date,
                        session=session,
                        quantity_litres=round(base, 2),
                        recorded_by=data_entry_user.id,
                        source="manual",
                    )
                    db.add(mr)

        # ── Quality checks — last 14 days ──────────────────────────────
        for day_offset in range(14, -1, -1):
            rec_date = today - timedelta(days=day_offset)
            qc = models.QualityCheck(
                animal_id=None,  # bulk tank
                check_date=rec_date,
                session="morning",
                fat_percent=round(random.uniform(6.8, 7.8), 2),
                snf_percent=round(random.uniform(8.8, 9.5), 2),
                protein_percent=round(random.uniform(3.2, 3.8), 2),
                lactose_percent=round(random.uniform(4.6, 5.0), 2),
                scc=random.randint(150000, 350000),
                temperature_c=round(random.uniform(3.5, 5.0), 1),
                ph=round(random.uniform(6.5, 6.8), 2),
                adulteration=False,
                grade="A",
                source="manual",
                recorded_by=quality_user.id,
            )
            db.add(qc)

        # ── Health records ─────────────────────────────────────────────
        health_data = [
            (animals[6], models.HealthSeverity.watch, "Mild fever", 39.8, False),
            (animals[21], models.HealthSeverity.treatment, "Mastitis — right quarter", 40.2, False),
            (animals[14], models.HealthSeverity.normal, "Routine hoof trimming", None, True),
            (animals[3], models.HealthSeverity.normal, "Annual vaccination (FMD)", None, True),
            (animals[37], models.HealthSeverity.watch, "Low appetite — under observation", None, False),
        ]
        for animal, severity, condition, temp, resolved in health_data:
            hr = models.HealthRecord(
                animal_id=animal.id,
                record_date=today - timedelta(days=random.randint(0, 5)),
                condition=condition,
                severity=severity,
                temperature_c=temp,
                treatment="Antibiotics + anti-inflammatory" if severity == models.HealthSeverity.treatment else None,
                resolved=resolved,
                resolved_date=today - timedelta(days=1) if resolved else None,
                recorded_by=vet_user.id,
            )
            db.add(hr)
            if severity in (models.HealthSeverity.treatment, models.HealthSeverity.critical):
                animal.status = models.AnimalStatus.sick

        # ── Feed logs — last 7 days ────────────────────────────────────
        for day_offset in range(7, -1, -1):
            rec_date = today - timedelta(days=day_offset)
            for session in ["morning", "evening"]:
                fl = models.FeedLog(
                    log_date=rec_date,
                    session=session,
                    green_fodder_kg=random.uniform(380, 420),
                    dry_fodder_kg=random.uniform(95, 110),
                    concentrate_kg=random.uniform(75, 85),
                    mineral_mix_kg=1.0,
                    total_kg=0,
                    cost=random.uniform(2800, 3200),
                    recorded_by=data_entry_user.id,
                )
                fl.total_kg = fl.green_fodder_kg + fl.dry_fodder_kg + fl.concentrate_kg + fl.mineral_mix_kg
                db.add(fl)

        # ── Tasks ──────────────────────────────────────────────────────
        task_templates = [
            ("Morning milking — all animals", "milking", today, "06:00", models.TaskPriority.high, data_entry_user.id),
            ("Evening milking — all animals", "milking", today, "17:00", models.TaskPriority.high, data_entry_user.id),
            ("Morning feed distribution (TMR)", "feeding", today, "05:30", models.TaskPriority.high, data_entry_user.id),
            ("Milk quality test (bulk tank)", "quality", today, "07:00", models.TaskPriority.medium, quality_user.id),
            ("Health check — B-07 Meena (mastitis follow-up)", "health", today, "09:00", models.TaskPriority.high, vet_user.id),
            ("Shed cleaning — pressure wash", "maintenance", today, "11:00", models.TaskPriority.medium, data_entry_user.id),
            ("Biogas plant inspection", "maintenance", today + timedelta(days=1), "10:00", models.TaskPriority.low, data_entry_user.id),
            ("Vaccination — FMD booster (5 animals)", "health", today + timedelta(days=2), "09:00", models.TaskPriority.high, vet_user.id),
            ("Monthly stock audit (feed store)", "admin", today + timedelta(days=3), "14:00", models.TaskPriority.medium, user_objs[0].id),
            ("Solar panel cleaning", "maintenance", today + timedelta(days=5), "08:00", models.TaskPriority.low, data_entry_user.id),
        ]
        for title, category, due, due_time, priority, assigned_uid in task_templates:
            t = models.Task(
                title=title, category=category, due_date=due,
                due_time=due_time, priority=priority,
                assigned_to=assigned_uid, created_by=admin.id,
                status=models.TaskStatus.pending,
            )
            db.add(t)

        # ── Device ─────────────────────────────────────────────────────
        dev = models.Device(
            device_id="MM-001",
            name="Milking Machine 1",
            device_type=models.DeviceType.milk_meter,
            location="Milking Parlour",
            is_active=True,
            firmware_version="2.1.0",
            api_key="dev-milk-meter-api-key-001",
        )
        db.add(dev)

        qa_dev = models.Device(
            device_id="QA-001",
            name="Milk Quality Analyzer",
            device_type=models.DeviceType.quality_analyzer,
            location="Milk Room",
            is_active=True,
            firmware_version="1.5.2",
            api_key="dev-quality-analyzer-api-key-001",
        )
        db.add(qa_dev)

        db.commit()
        print("✅ Database seeded successfully")

    except Exception as e:
        db.rollback()
        print(f"⚠️  Seed error (may already be seeded): {e}")
    finally:
        db.close()

import enum
from datetime import datetime, date
from sqlalchemy import (
    Column, Integer, String, Float, Boolean, DateTime, Date,
    Text, ForeignKey, Enum as SAEnum, JSON
)
from sqlalchemy.orm import relationship
from database import Base


# ─── Enums ────────────────────────────────────────────────────────────────────

class UserRole(str, enum.Enum):
    admin = "admin"
    manager = "manager"
    data_entry = "data_entry"
    vet = "vet"
    quality = "quality"
    monitor = "monitor"


class AnimalStatus(str, enum.Enum):
    lactating = "lactating"
    dry = "dry"
    pregnant = "pregnant"
    heifer = "heifer"
    sick = "sick"
    culled = "culled"


class HealthSeverity(str, enum.Enum):
    normal = "normal"
    watch = "watch"
    treatment = "treatment"
    critical = "critical"


class TaskStatus(str, enum.Enum):
    pending = "pending"
    in_progress = "in_progress"
    done = "done"
    overdue = "overdue"


class TaskPriority(str, enum.Enum):
    low = "low"
    medium = "medium"
    high = "high"


class DeviceType(str, enum.Enum):
    milk_meter = "milk_meter"
    quality_analyzer = "quality_analyzer"
    ear_tag_iot = "ear_tag_iot"
    weight_scale = "weight_scale"
    env_sensor = "env_sensor"


# ─── User ─────────────────────────────────────────────────────────────────────

class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(100), nullable=False)
    email = Column(String(150), unique=True, index=True, nullable=False)
    phone = Column(String(20))
    hashed_password = Column(String(255), nullable=False)
    role = Column(SAEnum(UserRole), default=UserRole.monitor, nullable=False)
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime, default=datetime.utcnow)
    created_by = Column(Integer, ForeignKey("users.id"), nullable=True)
    last_login = Column(DateTime, nullable=True)
    avatar_initials = Column(String(4))

    tasks_assigned = relationship("Task", foreign_keys="Task.assigned_to", back_populates="assignee")
    tasks_created = relationship("Task", foreign_keys="Task.created_by", back_populates="creator")
    milk_records = relationship("MilkRecord", back_populates="recorded_by_user")
    health_records = relationship("HealthRecord", back_populates="recorded_by_user")
    quality_checks = relationship("QualityCheck", back_populates="recorded_by_user")


# ─── Animal ───────────────────────────────────────────────────────────────────

class Animal(Base):
    __tablename__ = "animals"

    id = Column(Integer, primary_key=True, index=True)
    tag_number = Column(String(20), unique=True, nullable=False, index=True)
    name = Column(String(60))
    breed = Column(String(60), default="Murrah Buffalo")
    date_of_birth = Column(Date)
    date_of_purchase = Column(Date)
    purchase_price = Column(Float)
    status = Column(SAEnum(AnimalStatus), default=AnimalStatus.lactating)
    parity = Column(Integer, default=1)  # lactation number
    last_calving_date = Column(Date)
    expected_dry_date = Column(Date)
    expected_calving_date = Column(Date)
    weight_kg = Column(Float)
    notes = Column(Text)
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime, default=datetime.utcnow)
    iot_device_id = Column(String(50), nullable=True)

    milk_records = relationship("MilkRecord", back_populates="animal")
    health_records = relationship("HealthRecord", back_populates="animal")
    quality_checks = relationship("QualityCheck", back_populates="animal")


# ─── Milk Record ──────────────────────────────────────────────────────────────

class MilkRecord(Base):
    __tablename__ = "milk_records"

    id = Column(Integer, primary_key=True, index=True)
    animal_id = Column(Integer, ForeignKey("animals.id"), nullable=False)
    record_date = Column(Date, nullable=False)
    session = Column(String(10), nullable=False)  # morning / evening
    quantity_litres = Column(Float, nullable=False)
    recorded_by = Column(Integer, ForeignKey("users.id"))
    source = Column(String(30), default="manual")  # manual / milk_meter / api
    device_id = Column(String(50), nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow)
    notes = Column(String(255))

    animal = relationship("Animal", back_populates="milk_records")
    recorded_by_user = relationship("User", back_populates="milk_records")


# ─── Health Record ────────────────────────────────────────────────────────────

class HealthRecord(Base):
    __tablename__ = "health_records"

    id = Column(Integer, primary_key=True, index=True)
    animal_id = Column(Integer, ForeignKey("animals.id"), nullable=False)
    record_date = Column(Date, nullable=False)
    condition = Column(String(120), nullable=False)
    severity = Column(SAEnum(HealthSeverity), default=HealthSeverity.normal)
    temperature_c = Column(Float)
    treatment = Column(Text)
    medication = Column(String(255))
    dosage = Column(String(100))
    vet_name = Column(String(100))
    follow_up_date = Column(Date)
    resolved = Column(Boolean, default=False)
    resolved_date = Column(Date)
    recorded_by = Column(Integer, ForeignKey("users.id"))
    created_at = Column(DateTime, default=datetime.utcnow)
    notes = Column(Text)

    animal = relationship("Animal", back_populates="health_records")
    recorded_by_user = relationship("User", back_populates="health_records")


# ─── Quality Check ────────────────────────────────────────────────────────────

class QualityCheck(Base):
    __tablename__ = "quality_checks"

    id = Column(Integer, primary_key=True, index=True)
    animal_id = Column(Integer, ForeignKey("animals.id"), nullable=True)  # null = bulk tank
    check_date = Column(Date, nullable=False)
    session = Column(String(10))
    fat_percent = Column(Float)
    snf_percent = Column(Float)          # solid-not-fat
    protein_percent = Column(Float)
    lactose_percent = Column(Float)
    scc = Column(Integer)                # somatic cell count (cells/ml)
    temperature_c = Column(Float)
    ph = Column(Float)
    adulteration = Column(Boolean, default=False)
    grade = Column(String(10))           # A / B / C
    source = Column(String(30), default="manual")  # manual / analyzer / api
    device_id = Column(String(50))
    recorded_by = Column(Integer, ForeignKey("users.id"))
    created_at = Column(DateTime, default=datetime.utcnow)
    notes = Column(String(255))

    animal = relationship("Animal", back_populates="quality_checks")
    recorded_by_user = relationship("User", back_populates="quality_checks")


# ─── Feed Log ─────────────────────────────────────────────────────────────────

class FeedLog(Base):
    __tablename__ = "feed_logs"

    id = Column(Integer, primary_key=True, index=True)
    log_date = Column(Date, nullable=False)
    session = Column(String(10))
    green_fodder_kg = Column(Float, default=0)
    dry_fodder_kg = Column(Float, default=0)
    concentrate_kg = Column(Float, default=0)
    mineral_mix_kg = Column(Float, default=0)
    total_kg = Column(Float)
    batch_number = Column(String(30))
    cost = Column(Float)
    recorded_by = Column(Integer, ForeignKey("users.id"))
    created_at = Column(DateTime, default=datetime.utcnow)
    notes = Column(String(255))


# ─── Task ─────────────────────────────────────────────────────────────────────

class Task(Base):
    __tablename__ = "tasks"

    id = Column(Integer, primary_key=True, index=True)
    title = Column(String(200), nullable=False)
    description = Column(Text)
    category = Column(String(50))  # feeding / health / milking / quality / maintenance / admin
    due_date = Column(Date, nullable=False)
    due_time = Column(String(10))
    priority = Column(SAEnum(TaskPriority), default=TaskPriority.medium)
    status = Column(SAEnum(TaskStatus), default=TaskStatus.pending)
    assigned_to = Column(Integer, ForeignKey("users.id"))
    created_by = Column(Integer, ForeignKey("users.id"))
    animal_id = Column(Integer, ForeignKey("animals.id"), nullable=True)
    completed_at = Column(DateTime, nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow)
    notes = Column(Text)

    assignee = relationship("User", foreign_keys=[assigned_to], back_populates="tasks_assigned")
    creator = relationship("User", foreign_keys=[created_by], back_populates="tasks_created")


# ─── Device ───────────────────────────────────────────────────────────────────

class Device(Base):
    __tablename__ = "devices"

    id = Column(Integer, primary_key=True, index=True)
    device_id = Column(String(50), unique=True, nullable=False, index=True)
    name = Column(String(100), nullable=False)
    device_type = Column(SAEnum(DeviceType), nullable=False)
    location = Column(String(100))
    is_active = Column(Boolean, default=True)
    last_seen = Column(DateTime)
    firmware_version = Column(String(30))
    api_key = Column(String(100), unique=True)
    created_at = Column(DateTime, default=datetime.utcnow)
    device_metadata = Column(JSON, default=dict)


# ─── Expense ──────────────────────────────────────────────────────────────────

class ExpenseCategory(str, enum.Enum):
    feed = "feed"
    veterinary = "veterinary"
    labour = "labour"
    medicine = "medicine"
    equipment = "equipment"
    utilities = "utilities"
    maintenance = "maintenance"
    insurance = "insurance"
    other = "other"


class Expense(Base):
    __tablename__ = "expenses"

    id = Column(Integer, primary_key=True, index=True)
    expense_date = Column(Date, nullable=False)
    category = Column(SAEnum(ExpenseCategory), nullable=False)
    description = Column(String(255), nullable=False)
    amount = Column(Float, nullable=False)
    vendor = Column(String(120))
    reference_no = Column(String(80))
    payment_mode = Column(String(40), default="cash")  # cash / upi / bank_transfer / cheque
    animal_id = Column(Integer, ForeignKey("animals.id"), nullable=True)
    recorded_by = Column(Integer, ForeignKey("users.id"))
    created_at = Column(DateTime, default=datetime.utcnow)
    notes = Column(Text)

    animal = relationship("Animal")
    recorded_by_user = relationship("User")


# ─── Reproductive Events ─────────────────────────────────────────────────────

class ReproEventType(str, enum.Enum):
    heat_observed = "heat_observed"
    ai_done = "ai_done"
    pregnancy_confirmed = "pregnancy_confirmed"
    pregnancy_negative = "pregnancy_negative"
    dry_off = "dry_off"
    calving = "calving"
    abortion = "abortion"


class ReproductiveEvent(Base):
    __tablename__ = "reproductive_events"

    id = Column(Integer, primary_key=True, index=True)
    animal_id = Column(Integer, ForeignKey("animals.id"), nullable=False)
    event_date = Column(Date, nullable=False)
    event_type = Column(SAEnum(ReproEventType), nullable=False)
    # AI fields
    bull_name = Column(String(100))           # semen bull name/tag
    semen_batch = Column(String(60))
    ai_technician = Column(String(80))
    ai_number = Column(Integer)               # 1st AI / 2nd AI / 3rd AI
    # Pregnancy diagnosis
    diagnosed_by = Column(String(80))
    # Calving fields
    calf_sex = Column(String(10))             # male / female
    calf_weight_kg = Column(Float)
    calving_ease = Column(String(20))         # normal / assisted / difficult
    # General
    next_heat_expected = Column(Date)         # auto-calculated for failed AI
    notes = Column(Text)
    recorded_by = Column(Integer, ForeignKey("users.id"))
    created_at = Column(DateTime, default=datetime.utcnow)

    animal = relationship("Animal")
    recorded_by_user = relationship("User")


# ─── Calves ───────────────────────────────────────────────────────────────────

class Calf(Base):
    __tablename__ = "calves"

    id = Column(Integer, primary_key=True, index=True)
    tag_number = Column(String(20), unique=True, nullable=False)
    name = Column(String(60))
    dam_id = Column(Integer, ForeignKey("animals.id"), nullable=False)   # mother
    sire_name = Column(String(80))                                         # bull name
    date_of_birth = Column(Date, nullable=False)
    sex = Column(String(10), nullable=False)              # male / female
    birth_weight_kg = Column(Float)
    colostrum_given = Column(Boolean, default=False)
    colostrum_time_hrs = Column(Float)                    # hours after birth first colostrum given
    status = Column(String(20), default="alive")          # alive / sold / dead / weaned
    weaning_date = Column(Date)
    weaning_weight_kg = Column(Float)
    sold_date = Column(Date)
    sold_price = Column(Float)
    notes = Column(Text)
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime, default=datetime.utcnow)

    dam = relationship("Animal")


# ─── Calf Weight Records ──────────────────────────────────────────────────────

class CalfWeightRecord(Base):
    __tablename__ = "calf_weight_records"

    id = Column(Integer, primary_key=True, index=True)
    calf_id = Column(Integer, ForeignKey("calves.id"), nullable=False)
    record_date = Column(Date, nullable=False)
    weight_kg = Column(Float, nullable=False)
    recorded_by = Column(Integer, ForeignKey("users.id"))
    created_at = Column(DateTime, default=datetime.utcnow)

    calf = relationship("Calf")


# ─── Vaccinations ─────────────────────────────────────────────────────────────

class Vaccination(Base):
    __tablename__ = "vaccinations"

    id = Column(Integer, primary_key=True, index=True)
    animal_id = Column(Integer, ForeignKey("animals.id"), nullable=False)
    vaccine_name = Column(String(100), nullable=False)   # e.g. FMD Polyvalent, HS, BQ
    disease = Column(String(100))                          # e.g. Foot and Mouth Disease
    vaccination_date = Column(Date, nullable=False)
    dose_ml = Column(Float)
    route = Column(String(20))                             # SC / IM / Oral
    batch_no = Column(String(60))
    manufacturer = Column(String(80))
    next_due_date = Column(Date)
    given_by = Column(String(80))                          # vet/technician name
    cost = Column(Float)
    is_govt_free = Column(Boolean, default=False)
    recorded_by = Column(Integer, ForeignKey("users.id"))
    created_at = Column(DateTime, default=datetime.utcnow)
    notes = Column(Text)

    animal = relationship("Animal")
    recorded_by_user = relationship("User")


# ─── Device Readings (raw IoT data) ──────────────────────────────────────────

class DeviceReading(Base):
    __tablename__ = "device_readings"

    id = Column(Integer, primary_key=True, index=True)
    device_id = Column(String(50), ForeignKey("devices.device_id"), nullable=False)
    animal_tag = Column(String(20), nullable=True)
    reading_type = Column(String(50))  # milk_quantity / fat / snf / temperature / weight
    value = Column(Float)
    unit = Column(String(20))
    raw_payload = Column(JSON)
    processed = Column(Boolean, default=False)
    created_at = Column(DateTime, default=datetime.utcnow)

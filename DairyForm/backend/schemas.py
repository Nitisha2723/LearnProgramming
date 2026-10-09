from __future__ import annotations
from datetime import datetime, date
from typing import Optional, List, Any
from pydantic import BaseModel, EmailStr, field_validator
from models import UserRole, AnimalStatus, HealthSeverity, TaskStatus, TaskPriority, DeviceType, ExpenseCategory, ReproEventType


# ─── Token ────────────────────────────────────────────────────────────────────

class Token(BaseModel):
    access_token: str
    token_type: str
    user: UserOut


class TokenData(BaseModel):
    user_id: Optional[int] = None


# ─── User ─────────────────────────────────────────────────────────────────────

class UserCreate(BaseModel):
    name: str
    email: EmailStr
    phone: Optional[str] = None
    password: str
    role: UserRole = UserRole.monitor


class UserUpdate(BaseModel):
    name: Optional[str] = None
    phone: Optional[str] = None
    role: Optional[UserRole] = None
    is_active: Optional[bool] = None
    password: Optional[str] = None


class UserOut(BaseModel):
    id: int
    name: str
    email: str
    phone: Optional[str]
    role: UserRole
    is_active: bool
    created_at: datetime
    last_login: Optional[datetime]
    avatar_initials: Optional[str]

    model_config = {"from_attributes": True}


# ─── Animal ───────────────────────────────────────────────────────────────────

class AnimalCreate(BaseModel):
    tag_number: str
    name: Optional[str] = None
    breed: str = "Murrah Buffalo"
    date_of_birth: Optional[date] = None
    date_of_purchase: Optional[date] = None
    purchase_price: Optional[float] = None
    status: AnimalStatus = AnimalStatus.lactating
    parity: int = 1
    last_calving_date: Optional[date] = None
    expected_dry_date: Optional[date] = None
    expected_calving_date: Optional[date] = None
    weight_kg: Optional[float] = None
    notes: Optional[str] = None
    iot_device_id: Optional[str] = None


class AnimalUpdate(BaseModel):
    name: Optional[str] = None
    status: Optional[AnimalStatus] = None
    parity: Optional[int] = None
    last_calving_date: Optional[date] = None
    expected_dry_date: Optional[date] = None
    expected_calving_date: Optional[date] = None
    weight_kg: Optional[float] = None
    notes: Optional[str] = None
    iot_device_id: Optional[str] = None
    is_active: Optional[bool] = None


class AnimalOut(BaseModel):
    id: int
    tag_number: str
    name: Optional[str]
    breed: str
    date_of_birth: Optional[date]
    date_of_purchase: Optional[date]
    purchase_price: Optional[float]
    status: AnimalStatus
    parity: int
    last_calving_date: Optional[date]
    expected_dry_date: Optional[date]
    expected_calving_date: Optional[date]
    weight_kg: Optional[float]
    notes: Optional[str]
    is_active: bool
    iot_device_id: Optional[str]
    created_at: datetime

    model_config = {"from_attributes": True}


# ─── Milk Record ──────────────────────────────────────────────────────────────

class MilkRecordCreate(BaseModel):
    animal_id: int
    record_date: date
    session: str  # morning / evening
    quantity_litres: float
    source: str = "manual"
    device_id: Optional[str] = None
    notes: Optional[str] = None


class MilkRecordOut(BaseModel):
    id: int
    animal_id: int
    record_date: date
    session: str
    quantity_litres: float
    recorded_by: Optional[int]
    source: str
    device_id: Optional[str]
    created_at: datetime
    notes: Optional[str]

    model_config = {"from_attributes": True}


class DailyMilkSummary(BaseModel):
    record_date: date
    total_litres: float
    morning_litres: float
    evening_litres: float
    animal_count: int


# ─── Health Record ────────────────────────────────────────────────────────────

class HealthRecordCreate(BaseModel):
    animal_id: int
    record_date: date
    condition: str
    severity: HealthSeverity = HealthSeverity.normal
    temperature_c: Optional[float] = None
    treatment: Optional[str] = None
    medication: Optional[str] = None
    dosage: Optional[str] = None
    vet_name: Optional[str] = None
    follow_up_date: Optional[date] = None
    notes: Optional[str] = None


class HealthRecordUpdate(BaseModel):
    severity: Optional[HealthSeverity] = None
    treatment: Optional[str] = None
    medication: Optional[str] = None
    follow_up_date: Optional[date] = None
    resolved: Optional[bool] = None
    resolved_date: Optional[date] = None
    notes: Optional[str] = None


class HealthRecordOut(BaseModel):
    id: int
    animal_id: int
    record_date: date
    condition: str
    severity: HealthSeverity
    temperature_c: Optional[float]
    treatment: Optional[str]
    medication: Optional[str]
    dosage: Optional[str]
    vet_name: Optional[str]
    follow_up_date: Optional[date]
    resolved: bool
    resolved_date: Optional[date]
    recorded_by: Optional[int]
    created_at: datetime
    notes: Optional[str]

    model_config = {"from_attributes": True}


# ─── Quality Check ────────────────────────────────────────────────────────────

class QualityCheckCreate(BaseModel):
    animal_id: Optional[int] = None
    check_date: date
    session: Optional[str] = None
    fat_percent: Optional[float] = None
    snf_percent: Optional[float] = None
    protein_percent: Optional[float] = None
    lactose_percent: Optional[float] = None
    scc: Optional[int] = None
    temperature_c: Optional[float] = None
    ph: Optional[float] = None
    adulteration: bool = False
    grade: Optional[str] = None
    source: str = "manual"
    device_id: Optional[str] = None
    notes: Optional[str] = None


class QualityCheckOut(BaseModel):
    id: int
    animal_id: Optional[int]
    check_date: date
    session: Optional[str]
    fat_percent: Optional[float]
    snf_percent: Optional[float]
    protein_percent: Optional[float]
    lactose_percent: Optional[float]
    scc: Optional[int]
    temperature_c: Optional[float]
    ph: Optional[float]
    adulteration: bool
    grade: Optional[str]
    source: str
    device_id: Optional[str]
    recorded_by: Optional[int]
    created_at: datetime
    notes: Optional[str]

    model_config = {"from_attributes": True}


# ─── Feed Log ─────────────────────────────────────────────────────────────────

class FeedLogCreate(BaseModel):
    log_date: date
    session: Optional[str] = None
    green_fodder_kg: float = 0
    dry_fodder_kg: float = 0
    concentrate_kg: float = 0
    mineral_mix_kg: float = 0
    batch_number: Optional[str] = None
    cost: Optional[float] = None
    notes: Optional[str] = None


class FeedLogOut(BaseModel):
    id: int
    log_date: date
    session: Optional[str]
    green_fodder_kg: float
    dry_fodder_kg: float
    concentrate_kg: float
    mineral_mix_kg: float
    total_kg: Optional[float]
    batch_number: Optional[str]
    cost: Optional[float]
    recorded_by: Optional[int]
    created_at: datetime

    model_config = {"from_attributes": True}


# ─── Task ─────────────────────────────────────────────────────────────────────

class TaskCreate(BaseModel):
    title: str
    description: Optional[str] = None
    category: Optional[str] = None
    due_date: date
    due_time: Optional[str] = None
    priority: TaskPriority = TaskPriority.medium
    assigned_to: Optional[int] = None
    animal_id: Optional[int] = None
    notes: Optional[str] = None


class TaskUpdate(BaseModel):
    title: Optional[str] = None
    description: Optional[str] = None
    category: Optional[str] = None
    due_date: Optional[date] = None
    due_time: Optional[str] = None
    priority: Optional[TaskPriority] = None
    status: Optional[TaskStatus] = None
    assigned_to: Optional[int] = None
    notes: Optional[str] = None


class TaskOut(BaseModel):
    id: int
    title: str
    description: Optional[str]
    category: Optional[str]
    due_date: date
    due_time: Optional[str]
    priority: TaskPriority
    status: TaskStatus
    assigned_to: Optional[int]
    created_by: Optional[int]
    animal_id: Optional[int]
    completed_at: Optional[datetime]
    created_at: datetime
    assignee: Optional[UserOut] = None

    model_config = {"from_attributes": True}


# ─── Device ───────────────────────────────────────────────────────────────────

class DeviceCreate(BaseModel):
    device_id: str
    name: str
    device_type: DeviceType
    location: Optional[str] = None
    firmware_version: Optional[str] = None


class DeviceOut(BaseModel):
    id: int
    device_id: str
    name: str
    device_type: DeviceType
    location: Optional[str]
    is_active: bool
    last_seen: Optional[datetime]
    firmware_version: Optional[str]
    created_at: datetime

    model_config = {"from_attributes": True}


# ─── Device Ingestion ─────────────────────────────────────────────────────────

class DeviceReading(BaseModel):
    device_id: str
    api_key: str
    animal_tag: Optional[str] = None
    readings: List[dict]  # [{type, value, unit, timestamp}]



# ─── Expense ──────────────────────────────────────────────────────────────────

class ExpenseCreate(BaseModel):
    expense_date: date
    category: ExpenseCategory
    description: str
    amount: float
    vendor: Optional[str] = None
    reference_no: Optional[str] = None
    payment_mode: str = "cash"
    animal_id: Optional[int] = None
    notes: Optional[str] = None


class ExpenseOut(BaseModel):
    id: int
    expense_date: date
    category: ExpenseCategory
    description: str
    amount: float
    vendor: Optional[str]
    reference_no: Optional[str]
    payment_mode: str
    animal_id: Optional[int]
    recorded_by: Optional[int]
    created_at: datetime
    notes: Optional[str]

    model_config = {"from_attributes": True}


class ExpenseSummary(BaseModel):
    category: str
    total: float
    count: int


# ─── Reproductive Events ──────────────────────────────────────────────────────

class ReproEventCreate(BaseModel):
    animal_id: int
    event_date: date
    event_type: ReproEventType
    bull_name: Optional[str] = None
    semen_batch: Optional[str] = None
    ai_technician: Optional[str] = None
    ai_number: Optional[int] = None
    diagnosed_by: Optional[str] = None
    calf_sex: Optional[str] = None
    calf_weight_kg: Optional[float] = None
    calving_ease: Optional[str] = None
    next_heat_expected: Optional[date] = None
    notes: Optional[str] = None


class ReproEventOut(BaseModel):
    id: int
    animal_id: int
    event_date: date
    event_type: ReproEventType
    bull_name: Optional[str]
    semen_batch: Optional[str]
    ai_technician: Optional[str]
    ai_number: Optional[int]
    diagnosed_by: Optional[str]
    calf_sex: Optional[str]
    calf_weight_kg: Optional[float]
    calving_ease: Optional[str]
    next_heat_expected: Optional[date]
    notes: Optional[str]
    recorded_by: Optional[int]
    created_at: datetime

    model_config = {"from_attributes": True}


# ─── Calves ───────────────────────────────────────────────────────────────────

class CalfCreate(BaseModel):
    tag_number: str
    name: Optional[str] = None
    dam_id: int
    sire_name: Optional[str] = None
    date_of_birth: date
    sex: str
    birth_weight_kg: Optional[float] = None
    colostrum_given: bool = False
    colostrum_time_hrs: Optional[float] = None
    notes: Optional[str] = None


class CalfUpdate(BaseModel):
    name: Optional[str] = None
    status: Optional[str] = None
    weaning_date: Optional[date] = None
    weaning_weight_kg: Optional[float] = None
    sold_date: Optional[date] = None
    sold_price: Optional[float] = None
    notes: Optional[str] = None
    is_active: Optional[bool] = None


class CalfOut(BaseModel):
    id: int
    tag_number: str
    name: Optional[str]
    dam_id: int
    sire_name: Optional[str]
    date_of_birth: date
    sex: str
    birth_weight_kg: Optional[float]
    colostrum_given: bool
    colostrum_time_hrs: Optional[float]
    status: str
    weaning_date: Optional[date]
    weaning_weight_kg: Optional[float]
    sold_date: Optional[date]
    sold_price: Optional[float]
    notes: Optional[str]
    is_active: bool
    created_at: datetime

    model_config = {"from_attributes": True}


class CalfWeightCreate(BaseModel):
    calf_id: int
    record_date: date
    weight_kg: float


class CalfWeightOut(BaseModel):
    id: int
    calf_id: int
    record_date: date
    weight_kg: float
    recorded_by: Optional[int]
    created_at: datetime

    model_config = {"from_attributes": True}


# ─── Vaccinations ─────────────────────────────────────────────────────────────

class VaccinationCreate(BaseModel):
    animal_id: int
    vaccine_name: str
    disease: Optional[str] = None
    vaccination_date: date
    dose_ml: Optional[float] = None
    route: Optional[str] = None
    batch_no: Optional[str] = None
    manufacturer: Optional[str] = None
    next_due_date: Optional[date] = None
    given_by: Optional[str] = None
    cost: Optional[float] = None
    is_govt_free: bool = False
    notes: Optional[str] = None


class VaccinationOut(BaseModel):
    id: int
    animal_id: int
    vaccine_name: str
    disease: Optional[str]
    vaccination_date: date
    dose_ml: Optional[float]
    route: Optional[str]
    batch_no: Optional[str]
    manufacturer: Optional[str]
    next_due_date: Optional[date]
    given_by: Optional[str]
    cost: Optional[float]
    is_govt_free: bool
    recorded_by: Optional[int]
    created_at: datetime
    notes: Optional[str]

    model_config = {"from_attributes": True}


class VaccinationDue(BaseModel):
    animal_id: int
    tag_number: str
    animal_name: Optional[str]
    vaccine_name: str
    next_due_date: date
    days_overdue: int


class DashboardStats(BaseModel):
    total_animals: int
    lactating: int
    dry: int
    sick: int
    milk_today_litres: float
    milk_yesterday_litres: float
    milk_trend_pct: float
    tasks_today: int
    tasks_overdue: int
    quality_grade_today: Optional[str]
    avg_fat_today: Optional[float]
    avg_snf_today: Optional[float]
    alerts: List[dict]

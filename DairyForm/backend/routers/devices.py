"""
Device ingestion API — smart devices push readings here using their device_id + api_key.
Supports: milk meters, quality analyzers, IoT ear tags, weight scales, env sensors.
"""
from datetime import datetime, date
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List

from database import get_db
from auth import require_admin
import models
import schemas

router = APIRouter(prefix="/api/devices", tags=["devices"])


# ── Device management (admin) ────────────────────────────────────────────────

@router.get("/", response_model=List[schemas.DeviceOut])
def list_devices(db: Session = Depends(get_db), _=Depends(require_admin)):
    return db.query(models.Device).order_by(models.Device.name).all()


@router.post("/", response_model=schemas.DeviceOut)
def register_device(
    data: schemas.DeviceCreate,
    db: Session = Depends(get_db),
    _=Depends(require_admin)
):
    import secrets
    if db.query(models.Device).filter(models.Device.device_id == data.device_id).first():
        raise HTTPException(400, "Device ID already registered")
    api_key = secrets.token_urlsafe(32)
    device = models.Device(**data.model_dump(), api_key=api_key)
    db.add(device)
    db.commit()
    db.refresh(device)
    return device


@router.delete("/{device_id}")
def deactivate_device(device_id: str, db: Session = Depends(get_db), _=Depends(require_admin)):
    d = db.query(models.Device).filter(models.Device.device_id == device_id).first()
    if not d:
        raise HTTPException(404, "Device not found")
    d.is_active = False
    db.commit()
    return {"message": "Deactivated"}


# ── Data ingestion (called by device, no user auth — uses api_key) ───────────

@router.post("/ingest")
def ingest_readings(data: schemas.DeviceReading, db: Session = Depends(get_db)):
    device = db.query(models.Device).filter(
        models.Device.device_id == data.device_id,
        models.Device.api_key == data.api_key,
        models.Device.is_active == True
    ).first()
    if not device:
        raise HTTPException(401, "Invalid device credentials")

    device.last_seen = datetime.utcnow()

    results = []
    for r in data.readings:
        reading_type = r.get("type")
        value = r.get("value")
        unit = r.get("unit", "")
        ts_str = r.get("timestamp")
        ts = datetime.fromisoformat(ts_str) if ts_str else datetime.utcnow()
        rec_date = ts.date()

        # Store raw reading
        raw = models.DeviceReading(
            device_id=data.device_id,
            animal_tag=data.animal_tag,
            reading_type=reading_type,
            value=value,
            unit=unit,
            raw_payload=r,
        )
        db.add(raw)

        # Auto-create records from device readings
        if reading_type == "milk_quantity" and data.animal_tag:
            animal = db.query(models.Animal).filter(models.Animal.tag_number == data.animal_tag).first()
            if animal:
                session = "morning" if ts.hour < 12 else "evening"
                # Avoid duplicate for same animal/date/session from device
                exists = db.query(models.MilkRecord).filter(
                    models.MilkRecord.animal_id == animal.id,
                    models.MilkRecord.record_date == rec_date,
                    models.MilkRecord.session == session,
                    models.MilkRecord.source == "milk_meter",
                ).first()
                if not exists:
                    milk = models.MilkRecord(
                        animal_id=animal.id,
                        record_date=rec_date,
                        session=session,
                        quantity_litres=float(value),
                        source="milk_meter",
                        device_id=data.device_id,
                    )
                    db.add(milk)
                    results.append({"type": "milk_record", "animal": data.animal_tag})

        elif reading_type in ("fat", "snf", "protein"):
            # Quality analyzer — batch multiple readings into one QC record
            # For simplicity, store raw and let a scheduled job aggregate
            results.append({"type": "raw_quality", "parameter": reading_type})

        raw.processed = True

    db.commit()
    return {"accepted": len(data.readings), "created": results}


@router.get("/readings")
def list_raw_readings(
    device_id: str = None,
    limit: int = 100,
    db: Session = Depends(get_db),
    _=Depends(require_admin)
):
    q = db.query(models.DeviceReading)
    if device_id:
        q = q.filter(models.DeviceReading.device_id == device_id)
    return q.order_by(models.DeviceReading.created_at.desc()).limit(limit).all()

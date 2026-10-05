"""
Solution to Exercise 02: Vehicle Hierarchy
"""

from abc import ABC, abstractmethod
from typing import List, Optional


class Vehicle(ABC):
    """Abstract base class for all vehicles."""

    def __init__(self, make: str, model: str, year: int) -> None:
        if not make.strip():
            raise ValueError("Make cannot be empty")
        if not model.strip():
            raise ValueError("Model cannot be empty")
        if year < 1885:  # First automobile invented ~1885
            raise ValueError(f"Year {year} is too early for a vehicle")

        self._make = make
        self._model = model
        self._year = year
        self._mileage = 0.0

    @property
    def make(self) -> str:
        return self._make

    @property
    def model(self) -> str:
        return self._model

    @property
    def year(self) -> int:
        return self._year

    @property
    def mileage(self) -> float:
        return self._mileage

    @property
    @abstractmethod
    def fuel_type(self) -> str:
        ...

    @property
    @abstractmethod
    def max_speed(self) -> float:
        ...

    @abstractmethod
    def move(self, distance: float) -> str:
        ...

    def vehicle_info(self) -> str:
        return (
            f"{self._year} {self._make} {self._model}\n"
            f"  Fuel: {self.fuel_type} | Max speed: {self.max_speed} km/h\n"
            f"  Mileage: {self._mileage:,.1f} km"
        )

    def __str__(self) -> str:
        return f"{self._year} {self._make} {self._model}"

    def __repr__(self) -> str:
        return (
            f"{type(self).__name__}(make={self._make!r}, "
            f"model={self._model!r}, year={self._year})"
        )

    def __lt__(self, other: "Vehicle") -> bool:
        if not isinstance(other, Vehicle):
            return NotImplemented
        return self._year < other._year


class MotorVehicle(Vehicle):
    """Abstract class for vehicles with engines."""

    def __init__(
        self,
        make: str,
        model: str,
        year: int,
        engine_size: float,
        fuel_capacity: float,
    ) -> None:
        super().__init__(make, model, year)
        if engine_size <= 0:
            raise ValueError(f"Engine size must be positive: {engine_size}")
        if fuel_capacity <= 0:
            raise ValueError(f"Fuel capacity must be positive: {fuel_capacity}")

        self._engine_size = engine_size
        self._fuel_capacity = fuel_capacity
        self._fuel_level = 100.0   # Starts full
        self._is_running = False

    @property
    def engine_size(self) -> float:
        return self._engine_size

    @property
    def fuel_level(self) -> float:
        return self._fuel_level

    @property
    def is_running(self) -> bool:
        return self._is_running

    def start_engine(self) -> str:
        if self._is_running:
            raise RuntimeError(f"{self} engine is already running")
        if self._fuel_level <= 0:
            raise RuntimeError(f"{self} has no fuel!")
        self._is_running = True
        return f"{self} engine started"

    def stop_engine(self) -> str:
        if not self._is_running:
            raise RuntimeError(f"{self} engine is not running")
        self._is_running = False
        return f"{self} engine stopped"

    def refuel(self, amount: float) -> str:
        if amount <= 0:
            raise ValueError(f"Fuel amount must be positive: {amount}")
        if self._is_running:
            raise RuntimeError("Cannot refuel while engine is running")

        added_percent = (amount / self._fuel_capacity) * 100
        self._fuel_level = min(100.0, self._fuel_level + added_percent)
        return f"Refueled {amount}L. Fuel level: {self._fuel_level:.1f}%"

    def vehicle_info(self) -> str:
        parent_info = super().vehicle_info()
        return (
            f"{parent_info}\n"
            f"  Engine: {self._engine_size}L | "
            f"Fuel: {self._fuel_level:.1f}% | "
            f"{'Running' if self._is_running else 'Off'}"
        )


class Car(MotorVehicle):
    """A standard car."""

    DEFAULT_FUEL_CONSUMPTION = 7.0   # L/100km

    def __init__(
        self,
        make: str,
        model: str,
        year: int,
        engine_size: float,
        fuel_capacity: float = 50.0,
        num_doors: int = 4,
        is_diesel: bool = False,
    ) -> None:
        super().__init__(make, model, year, engine_size, fuel_capacity)
        self.num_doors = num_doors
        self._is_diesel = is_diesel

    @property
    def fuel_type(self) -> str:
        return "diesel" if self._is_diesel else "gasoline"

    @property
    def max_speed(self) -> float:
        return 180.0

    def move(self, distance: float) -> str:
        if not self._is_running:
            raise RuntimeError(f"{self} engine is not running")
        if distance <= 0:
            raise ValueError(f"Distance must be positive: {distance}")

        fuel_needed_percent = (self.DEFAULT_FUEL_CONSUMPTION * distance / 100) / self._fuel_capacity * 100
        if fuel_needed_percent > self._fuel_level:
            raise RuntimeError(f"Insufficient fuel for {distance}km trip")

        self._mileage += distance
        self._fuel_level -= fuel_needed_percent
        return f"{self} drives {distance} km (fuel: {self._fuel_level:.1f}%)"


class Truck(MotorVehicle):
    """A cargo truck."""

    BASE_FUEL_CONSUMPTION = 25.0  # L/100km unloaded

    def __init__(
        self,
        make: str,
        model: str,
        year: int,
        engine_size: float,
        payload_capacity: float,
        fuel_capacity: float = 200.0,
    ) -> None:
        super().__init__(make, model, year, engine_size, fuel_capacity)
        if payload_capacity <= 0:
            raise ValueError(f"Payload capacity must be positive: {payload_capacity}")
        self._payload_capacity = payload_capacity
        self._current_load = 0.0

    @property
    def fuel_type(self) -> str:
        return "diesel"

    @property
    def max_speed(self) -> float:
        return 120.0

    @property
    def current_load(self) -> float:
        return self._current_load

    def load_cargo(self, weight: float) -> str:
        if weight <= 0:
            raise ValueError(f"Weight must be positive: {weight}")
        if self._current_load + weight > self._payload_capacity:
            raise RuntimeError(
                f"Cannot load {weight}kg: would exceed capacity "
                f"({self._current_load + weight} > {self._payload_capacity})"
            )
        self._current_load += weight
        return f"Loaded {weight}kg. Current load: {self._current_load}kg"

    def unload_cargo(self, weight: float) -> str:
        if weight <= 0:
            raise ValueError(f"Weight must be positive: {weight}")
        if weight > self._current_load:
            raise RuntimeError(
                f"Cannot unload {weight}kg: only {self._current_load}kg loaded"
            )
        self._current_load -= weight
        return f"Unloaded {weight}kg. Current load: {self._current_load}kg"

    def move(self, distance: float) -> str:
        if not self._is_running:
            raise RuntimeError(f"{self} engine is not running")
        if distance <= 0:
            raise ValueError(f"Distance must be positive: {distance}")

        fuel_per_100km = self.BASE_FUEL_CONSUMPTION + 2.0 * self._current_load / 1000
        fuel_needed_percent = (fuel_per_100km * distance / 100) / self._fuel_capacity * 100

        if fuel_needed_percent > self._fuel_level:
            raise RuntimeError(f"Insufficient fuel for {distance}km trip")

        self._mileage += distance
        self._fuel_level -= fuel_needed_percent
        return (
            f"{self} hauls {self._current_load}kg for {distance}km "
            f"(fuel: {self._fuel_level:.1f}%)"
        )


class ElectricCar(Car):
    """An electric car."""

    KWH_PER_KM = 0.2

    def __init__(
        self,
        make: str,
        model: str,
        year: int,
        battery_capacity_kwh: float,
        range_km: float = 500.0,
        num_doors: int = 4,
    ) -> None:
        # Use battery_capacity_kwh as the "fuel capacity" — it represents energy storage
        super().__init__(make, model, year, 0.0, battery_capacity_kwh, num_doors)
        self._battery_capacity_kwh = battery_capacity_kwh
        self._range_km = range_km

    @property
    def fuel_type(self) -> str:
        return "electric"

    @property
    def max_speed(self) -> float:
        return 250.0

    def start_engine(self) -> str:
        if self._is_running:
            raise RuntimeError(f"{self} electric motor is already active")
        if self._fuel_level <= 0:
            raise RuntimeError(f"{self} battery is dead!")
        self._is_running = True
        return f"{self} electric motor activated"

    def stop_engine(self) -> str:
        if not self._is_running:
            raise RuntimeError(f"{self} electric motor is not active")
        self._is_running = False
        return f"{self} electric motor deactivated"

    def charge(self, amount_kwh: float) -> str:
        """Charge the battery."""
        if amount_kwh <= 0:
            raise ValueError(f"Charge amount must be positive: {amount_kwh}")
        if self._is_running:
            raise RuntimeError("Cannot charge while motor is running")
        added_percent = (amount_kwh / self._battery_capacity_kwh) * 100
        self._fuel_level = min(100.0, self._fuel_level + added_percent)
        return f"Charged {amount_kwh}kWh. Battery: {self._fuel_level:.1f}%"

    def refuel(self, amount: float) -> str:
        """Override refuel to use charge instead."""
        return self.charge(amount)

    def move(self, distance: float) -> str:
        if not self._is_running:
            raise RuntimeError(f"{self} motor is not running")
        if distance <= 0:
            raise ValueError(f"Distance must be positive: {distance}")

        energy_needed_kwh = self.KWH_PER_KM * distance
        energy_needed_percent = (energy_needed_kwh / self._battery_capacity_kwh) * 100

        if energy_needed_percent > self._fuel_level:
            raise RuntimeError(f"Insufficient charge for {distance}km trip")

        self._mileage += distance
        self._fuel_level -= energy_needed_percent
        return f"{self} silently drives {distance}km (battery: {self._fuel_level:.1f}%)"


class Bicycle(Vehicle):
    """A bicycle."""

    def __init__(self, make: str, model: str, year: int, gear_count: int = 21) -> None:
        super().__init__(make, model, year)
        if gear_count < 1:
            raise ValueError(f"Gear count must be at least 1: {gear_count}")
        self.gear_count = gear_count

    @property
    def fuel_type(self) -> str:
        return "human power"

    @property
    def max_speed(self) -> float:
        return 30.0

    def move(self, distance: float) -> str:
        if distance <= 0:
            raise ValueError(f"Distance must be positive: {distance}")
        self._mileage += distance
        return f"{self} rider pedals {distance}km (total: {self._mileage:.1f}km)"

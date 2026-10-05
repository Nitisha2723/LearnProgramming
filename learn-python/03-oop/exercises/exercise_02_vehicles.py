"""
Exercise 02: Vehicle Hierarchy

TOPIC: Inheritance, super(), ABC, polymorphism, @classmethod

SCENARIO:
You're modeling a fleet management system. Different types of vehicles
(car, truck, electric car, bicycle) share some behavior but differ in others.

WHAT TO IMPLEMENT:
1. Vehicle — abstract base class
2. MotorVehicle — abstract intermediate class (has engine)
3. Car — concrete car class
4. Truck — concrete truck class
5. ElectricCar — inherits from Car, overrides fuel-related methods
6. Bicycle — inherits directly from Vehicle (no engine)

LEARNING GOALS:
- Multi-level inheritance (Car → MotorVehicle → Vehicle)
- super().__init__() chains
- @abstractmethod — what must be implemented vs what's optional
- Method overriding with and without calling super()
- isinstance() in polymorphic functions

RUN TO TEST:
    python exercise_02_vehicles.py
"""

from abc import ABC, abstractmethod
from typing import List, Optional


# ============================================================
# TODO 1: Implement Vehicle (abstract base class)
# ============================================================

class Vehicle(ABC):
    """Abstract base class for all vehicles.

    All vehicles have:
    - make: Manufacturer (e.g., "Toyota")
    - model: Model name (e.g., "Corolla")
    - year: Manufacturing year
    - mileage: Current mileage in km (starts at 0)

    Abstract methods that ALL vehicles must implement:
    - move(distance: float) -> str
    - fuel_type() -> str (property)
    - max_speed() -> float (property)
    """

    def __init__(self, make: str, model: str, year: int) -> None:
        # TODO: Store make, model, year, initialize _mileage = 0.0
        pass

    @property
    def make(self) -> str:
        # TODO
        pass

    @property
    def model(self) -> str:
        # TODO
        pass

    @property
    def year(self) -> int:
        # TODO
        pass

    @property
    def mileage(self) -> float:
        # TODO
        pass

    @property
    @abstractmethod
    def fuel_type(self) -> str:
        """Return the type of fuel this vehicle uses (e.g., 'gasoline', 'electric', 'human power')."""
        ...

    @property
    @abstractmethod
    def max_speed(self) -> float:
        """Return the maximum speed in km/h."""
        ...

    @abstractmethod
    def move(self, distance: float) -> str:
        """Move the vehicle the given distance.

        Should update mileage and return a description of the movement.
        """
        ...

    def vehicle_info(self) -> str:
        """Return a summary of this vehicle's information.

        Concrete method using abstract properties — Template Method pattern.
        """
        return (
            f"{self.year} {self.make} {self.model}\n"
            f"  Fuel: {self.fuel_type} | Max speed: {self.max_speed} km/h\n"
            f"  Mileage: {self.mileage:,.1f} km"
        )

    def __str__(self) -> str:
        return f"{self.year} {self.make} {self.model}"

    def __repr__(self) -> str:
        return f"{type(self).__name__}(make={self.make!r}, model={self.model!r}, year={self.year})"

    def __lt__(self, other: "Vehicle") -> bool:
        """Compare vehicles by year (for sorting by age)."""
        if not isinstance(other, Vehicle):
            return NotImplemented
        return self.year < other.year


# ============================================================
# TODO 2: Implement MotorVehicle (abstract intermediate class)
# ============================================================

class MotorVehicle(Vehicle):
    """Abstract class for vehicles with engines.

    Extends Vehicle with:
    - engine_size: Engine size in liters (e.g., 2.0)
    - fuel_level: Current fuel level (0.0 to 100.0 percent)
    - is_running: Whether the engine is on

    Implements:
    - start_engine() / stop_engine()
    - refuel(amount)

    Still abstract (doesn't implement move() or the abstract properties).
    """

    def __init__(
        self,
        make: str,
        model: str,
        year: int,
        engine_size: float,
        fuel_capacity: float
    ) -> None:
        """Initialize MotorVehicle.

        IMPORTANT: Call super().__init__(make, model, year) first!
        Then add engine-specific attributes.
        """
        # TODO: Call super().__init__(make, model, year)
        # TODO: Store engine_size, fuel_capacity
        # TODO: Initialize _fuel_level = 100.0 (starts full)
        # TODO: Initialize _is_running = False
        pass

    @property
    def engine_size(self) -> float:
        # TODO
        pass

    @property
    def fuel_level(self) -> float:
        # TODO
        pass

    @property
    def is_running(self) -> bool:
        # TODO
        pass

    def start_engine(self) -> str:
        """Start the engine.

        Raises RuntimeError if already running or no fuel.
        """
        # TODO: Check if already running → RuntimeError
        # TODO: Check fuel_level == 0 → RuntimeError("No fuel!")
        # TODO: Set _is_running = True, return message
        pass

    def stop_engine(self) -> str:
        """Stop the engine."""
        # TODO: Check if not running → RuntimeError
        # TODO: Set _is_running = False, return message
        pass

    def refuel(self, amount: float) -> str:
        """Add fuel to the tank.

        Args:
            amount: Amount to add in liters

        Cannot exceed fuel_capacity. Cannot refuel while running.
        """
        # TODO: Validate amount > 0
        # TODO: Raise RuntimeError if engine is running
        # TODO: Add fuel (capped at 100%)
        pass

    def vehicle_info(self) -> str:
        """Extend parent's info with engine details."""
        parent_info = super().vehicle_info()
        return (
            f"{parent_info}\n"
            f"  Engine: {self.engine_size}L | "
            f"Fuel: {self.fuel_level:.1f}% | "
            f"{'Running' if self.is_running else 'Off'}"
        )


# ============================================================
# TODO 3: Implement Car
# ============================================================

class Car(MotorVehicle):
    """A standard car.

    Properties:
    - fuel_type: "gasoline" (or "diesel" for diesel cars)
    - max_speed: 180 km/h (typical family car)
    - num_doors: Number of doors (usually 2 or 4)

    move(): Car drives. Burns 7L/100km of fuel.
            Raises RuntimeError if engine isn't running.
    """

    DEFAULT_FUEL_CONSUMPTION = 7.0   # liters per 100 km

    def __init__(
        self,
        make: str,
        model: str,
        year: int,
        engine_size: float,
        fuel_capacity: float = 50.0,
        num_doors: int = 4,
        is_diesel: bool = False
    ) -> None:
        # TODO: Call super().__init__(make, model, year, engine_size, fuel_capacity)
        # TODO: Store num_doors, is_diesel
        pass

    @property
    def fuel_type(self) -> str:
        # TODO: Return "diesel" if is_diesel else "gasoline"
        pass

    @property
    def max_speed(self) -> float:
        # TODO: Return 180.0
        pass

    def move(self, distance: float) -> str:
        """Drive the car.

        Raises RuntimeError if not running.
        Burns fuel: DEFAULT_FUEL_CONSUMPTION * distance / 100 liters.
        """
        # TODO: Check engine is running
        # TODO: Calculate fuel needed = DEFAULT_FUEL_CONSUMPTION * distance / 100
        # TODO: Check enough fuel available
        # TODO: Update _mileage += distance
        # TODO: Update _fuel_level (subtract proportional amount)
        # TODO: Return description like "Toyota Corolla drives 50 km (fuel: 85.0%)"
        pass


# ============================================================
# TODO 4: Implement Truck
# ============================================================

class Truck(MotorVehicle):
    """A cargo truck.

    Properties:
    - fuel_type: "diesel"
    - max_speed: 120 km/h
    - payload_capacity: How much cargo (in kg) it can carry
    - current_load: Current cargo weight

    move(): Truck drives. Burns 25L/100km + 2L per 1000kg of load.
    """

    BASE_FUEL_CONSUMPTION = 25.0  # liters per 100 km (unloaded)

    def __init__(
        self,
        make: str,
        model: str,
        year: int,
        engine_size: float,
        payload_capacity: float,
        fuel_capacity: float = 200.0,
    ) -> None:
        # TODO: Call super().__init__(...)
        # TODO: Store payload_capacity
        # TODO: Initialize _current_load = 0.0
        pass

    @property
    def fuel_type(self) -> str:
        # TODO
        pass

    @property
    def max_speed(self) -> float:
        # TODO
        pass

    @property
    def current_load(self) -> float:
        # TODO
        pass

    def load_cargo(self, weight: float) -> str:
        """Load cargo onto the truck."""
        # TODO: Check weight > 0
        # TODO: Check doesn't exceed payload_capacity
        # TODO: Add to _current_load
        pass

    def unload_cargo(self, weight: float) -> str:
        """Unload cargo from the truck."""
        # TODO: Check not more than current_load
        # TODO: Subtract from _current_load
        pass

    def move(self, distance: float) -> str:
        """Drive the truck."""
        # TODO: Similar to Car but with load-dependent fuel consumption
        # Fuel = (BASE_FUEL_CONSUMPTION + 2.0 * current_load / 1000) * distance / 100
        pass


# ============================================================
# TODO 5: Implement ElectricCar (inherits from Car)
# ============================================================

class ElectricCar(Car):
    """An electric car.

    Extends Car but changes:
    - fuel_type: "electric"
    - Uses battery_level (0-100%) instead of fuel_level
    - move(): uses electricity (0.2 kWh/km)
    - max_speed: 250 km/h
    - refuel() should be renamed/replaced by charge()
    - start_engine() / stop_engine() → activate() / deactivate()

    Hint: Override start_engine to say "Electric motor activated" instead.
    Override fuel_type to return "electric".
    Override move() to use electricity instead of fuel.
    """

    KWH_PER_KM = 0.2   # kWh per km

    def __init__(
        self,
        make: str,
        model: str,
        year: int,
        battery_capacity_kwh: float,
        range_km: float = 500.0,
        num_doors: int = 4,
    ) -> None:
        # TODO: Call Car's __init__ with fake engine_size=0 and fuel_capacity=battery_capacity_kwh
        # This is a design compromise — in a real system you'd redesign the hierarchy
        # TODO: Store range_km, battery_capacity_kwh
        pass

    @property
    def fuel_type(self) -> str:
        # TODO: Return "electric"
        pass

    @property
    def max_speed(self) -> float:
        # TODO: Return 250.0
        pass

    def start_engine(self) -> str:
        """Override to say 'Electric motor activated'."""
        # TODO: Call super().start_engine() but change the message
        # Or implement directly
        pass

    def charge(self, amount_kwh: float) -> str:
        """Charge the battery.

        Similar to refuel() but for electricity.
        """
        # TODO: Implement charging logic
        pass

    def move(self, distance: float) -> str:
        """Drive the electric car."""
        # TODO: Similar to Car.move() but uses electricity (KWH_PER_KM)
        pass


# ============================================================
# TODO 6: Implement Bicycle
# ============================================================

class Bicycle(Vehicle):
    """A bicycle — no engine, human-powered.

    Properties:
    - fuel_type: "human power"
    - max_speed: 30 km/h
    - gear_count: Number of gears

    move(): Rider pedals. Always works (no fuel needed).
    """

    def __init__(self, make: str, model: str, year: int, gear_count: int = 21) -> None:
        # TODO: Call super().__init__(make, model, year)
        # TODO: Store gear_count
        pass

    @property
    def fuel_type(self) -> str:
        # TODO
        pass

    @property
    def max_speed(self) -> float:
        # TODO
        pass

    def move(self, distance: float) -> str:
        # TODO: Update mileage, return "... rides X km"
        pass


# ============================================================
# Tests
# ============================================================

def run_tests() -> None:
    """Run tests. These should all pass when you're done."""

    print("Testing Vehicle hierarchy...")

    # Cannot instantiate abstract classes
    try:
        v = Vehicle("Test", "Test", 2020)   # type: ignore
        print("  ✗ Should not create abstract Vehicle")
    except TypeError:
        print("  ✓ Cannot instantiate abstract Vehicle")

    # Car tests
    car = Car("Toyota", "Corolla", 2022, 1.8)
    assert car.make == "Toyota"
    assert car.fuel_type == "gasoline"
    assert car.max_speed == 180.0
    assert car.mileage == 0.0
    print("  ✓ Car creation")

    car.start_engine()
    result = car.move(100.0)
    assert car.mileage == 100.0
    assert "100" in result
    print("  ✓ Car move")

    # Cannot move without engine
    car.stop_engine()
    try:
        car.move(50.0)
        print("  ✗ Should raise RuntimeError when engine is off")
    except RuntimeError:
        print("  ✓ Cannot move without running engine")

    # Bicycle tests (no engine needed)
    bike = Bicycle("Trek", "Domane", 2021, gear_count=22)
    assert bike.fuel_type == "human power"
    result = bike.move(30.0)
    assert bike.mileage == 30.0
    print("  ✓ Bicycle move")

    # isinstance checks
    assert isinstance(car, Car)
    assert isinstance(car, MotorVehicle)
    assert isinstance(car, Vehicle)
    assert isinstance(bike, Vehicle)
    assert not isinstance(bike, MotorVehicle)
    print("  ✓ isinstance checks")

    # Polymorphism
    vehicles: List[Vehicle] = [car, bike, Truck("Volvo", "FH16", 2020, 12.0, 20000.0)]
    for v in vehicles:
        info = v.vehicle_info()
        assert str(v) in info or v.make in info
    print("  ✓ Polymorphic vehicle_info")

    # Sorting
    v1 = Car("A", "A", 2018, 1.0)
    v2 = Car("B", "B", 2022, 2.0)
    v3 = Bicycle("C", "C", 2015)
    sorted_vehicles = sorted([v2, v3, v1])
    assert sorted_vehicles[0].year == 2015
    assert sorted_vehicles[-1].year == 2022
    print("  ✓ Sorting by year")

    print("\n✓ All tests passed!")


if __name__ == "__main__":
    run_tests()

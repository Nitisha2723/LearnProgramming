/**
 * Exercise 02 Solution: Vehicle Hierarchy
 */
public class Exercise02_Solution {

    public static void main(String[] args) {
        System.out.println("Exercise 02 Solution: Vehicle Hierarchy");
        System.out.println("=".repeat(50));

        Car car = new Car("Toyota", "Camry", 2022, "Gasoline", 4, 12.0);
        ElectricCar tesla = new ElectricCar("Tesla", "Model 3", 2023, 82.0, 6.0);
        Motorcycle bike = new Motorcycle("Harley-Davidson", "Sportster", 2021, false, 883);

        System.out.println("\n--- Vehicle Information ---");
        car.printInfo();
        System.out.println();
        tesla.printInfo();
        System.out.println();
        bike.printInfo();

        System.out.println("\n--- Acceleration and Braking ---");
        car.accelerate(60);
        System.out.println("Car speed: " + car.getCurrentSpeed() + " km/h");
        car.brake(20);
        System.out.println("After braking: " + car.getCurrentSpeed() + " km/h");
        car.brake(100);
        System.out.println("After hard brake: " + car.getCurrentSpeed() + " km/h");

        System.out.println("\n--- Fuel Costs (100 km trip) ---");
        double distance = 100.0;
        System.out.printf("Car (Gasoline):   $%.2f%n", car.calculateFuelCost(distance));
        System.out.printf("Tesla (Electric): $%.2f%n", tesla.calculateFuelCost(distance));
        System.out.printf("Motorcycle:       $%.2f%n", bike.calculateFuelCost(distance));

        System.out.println("\n--- Polymorphism ---");
        Vehicle[] fleet = {car, tesla, bike};
        for (Vehicle v : fleet) {
            System.out.printf("%-15s: $%.2f for 100km%n",
                             v.getVehicleType(), v.calculateFuelCost(100));
        }

        System.out.println("\n--- ElectricCar Specific ---");
        System.out.println("Tesla remaining range: " + tesla.getRemainingRange() + " km");
        tesla.setCharge(0.5);
        System.out.println("At 50% charge: " + tesla.getRemainingRange() + " km");
        tesla.charge();
        System.out.println("After full charge: " + tesla.getRemainingRange() + " km");
    }

    // ====================================================================
    // Abstract Vehicle class
    // ====================================================================

    static abstract class Vehicle {
        protected final String make;
        protected final String model;
        protected final int year;
        protected final String fuelType;
        protected double currentSpeed;

        public Vehicle(String make, String model, int year, String fuelType) {
            if (make == null || make.trim().isEmpty()) {
                throw new IllegalArgumentException("Make cannot be empty");
            }
            if (year < 1886) {
                throw new IllegalArgumentException("Year must be >= 1886 (first car invented)");
            }
            this.make = make;
            this.model = model;
            this.year = year;
            this.fuelType = fuelType;
            this.currentSpeed = 0;
        }

        // Abstract methods — subclasses must implement
        public abstract double calculateFuelCost(double distanceKm);
        public abstract String getVehicleType();

        // Concrete shared methods
        public void accelerate(double amount) {
            if (amount <= 0) throw new IllegalArgumentException("Acceleration must be positive");
            currentSpeed += amount;
            System.out.printf("%s %s accelerates to %.1f km/h%n", make, model, currentSpeed);
        }

        public void brake(double amount) {
            if (amount <= 0) throw new IllegalArgumentException("Braking must be positive");
            currentSpeed = Math.max(0, currentSpeed - amount);
            System.out.printf("%s %s slows to %.1f km/h%n", make, model, currentSpeed);
        }

        public void printInfo() {
            System.out.println(getVehicleType() + ": " + year + " " + make + " " + model);
            System.out.println("  Fuel type: " + fuelType);
            System.out.printf("  Cost per 100 km: $%.2f%n", calculateFuelCost(100));
        }

        public double getCurrentSpeed() { return currentSpeed; }

        @Override
        public String toString() {
            return year + " " + make + " " + model + " (" + getVehicleType() + ")";
        }
    }

    // ====================================================================
    // Car class
    // ====================================================================

    static class Car extends Vehicle {
        private final int numDoors;
        private final double fuelEfficiencyKmPerLiter;
        private static final double GAS_PRICE_PER_LITER = 1.60;

        public Car(String make, String model, int year, String fuelType,
                   int numDoors, double fuelEfficiencyKmPerLiter) {
            super(make, model, year, fuelType);
            this.numDoors = numDoors;
            this.fuelEfficiencyKmPerLiter = fuelEfficiencyKmPerLiter;
        }

        @Override
        public double calculateFuelCost(double distanceKm) {
            double litersNeeded = distanceKm / fuelEfficiencyKmPerLiter;
            return litersNeeded * GAS_PRICE_PER_LITER;
        }

        @Override
        public String getVehicleType() { return "Car"; }

        @Override
        public void printInfo() {
            super.printInfo();
            System.out.println("  Doors: " + numDoors);
            System.out.printf("  Fuel efficiency: %.1f km/L%n", fuelEfficiencyKmPerLiter);
        }
    }

    // ====================================================================
    // ElectricCar class
    // ====================================================================

    static class ElectricCar extends Vehicle {
        private final double batteryCapacityKwh;
        private final double kmPerKwh;
        private double currentCharge;  // 0.0 to 1.0
        private static final double ELECTRICITY_PRICE_PER_KWH = 0.15;

        public ElectricCar(String make, String model, int year,
                           double batteryCapacityKwh, double kmPerKwh) {
            super(make, model, year, "Electric");
            this.batteryCapacityKwh = batteryCapacityKwh;
            this.kmPerKwh = kmPerKwh;
            this.currentCharge = 1.0;  // Starts fully charged
        }

        @Override
        public double calculateFuelCost(double distanceKm) {
            double kwhNeeded = distanceKm / kmPerKwh;
            return kwhNeeded * ELECTRICITY_PRICE_PER_KWH;
        }

        @Override
        public String getVehicleType() { return "Electric Car"; }

        public void charge() {
            currentCharge = 1.0;
            System.out.println(make + " " + model + " fully charged!");
        }

        public double getRemainingRange() {
            return batteryCapacityKwh * currentCharge * kmPerKwh;
        }

        public void setCharge(double charge) {
            if (charge < 0 || charge > 1) throw new IllegalArgumentException("Charge 0.0-1.0");
            this.currentCharge = charge;
        }

        @Override
        public void printInfo() {
            super.printInfo();
            System.out.printf("  Battery: %.0f kWh (%.0f%% charged)%n",
                             batteryCapacityKwh, currentCharge * 100);
            System.out.printf("  Remaining range: %.0f km%n", getRemainingRange());
        }
    }

    // ====================================================================
    // Motorcycle class
    // ====================================================================

    static class Motorcycle extends Vehicle {
        private final boolean hasSidecar;
        private final int engineCC;
        private static final double GAS_PRICE_PER_LITER = 1.60;

        public Motorcycle(String make, String model, int year,
                          boolean hasSidecar, int engineCC) {
            super(make, model, year, "Gasoline");
            this.hasSidecar = hasSidecar;
            this.engineCC = engineCC;
        }

        @Override
        public double calculateFuelCost(double distanceKm) {
            double efficiencyKmPerLiter = engineCC / 100.0;
            double litersNeeded = distanceKm / efficiencyKmPerLiter;
            return litersNeeded * GAS_PRICE_PER_LITER;
        }

        @Override
        public String getVehicleType() {
            return hasSidecar ? "Motorcycle with Sidecar" : "Motorcycle";
        }

        @Override
        public void printInfo() {
            super.printInfo();
            System.out.println("  Engine: " + engineCC + " cc");
            System.out.println("  Sidecar: " + (hasSidecar ? "Yes" : "No"));
        }
    }
}

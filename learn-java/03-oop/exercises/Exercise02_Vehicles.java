/**
 * Exercise 02: Vehicle Hierarchy
 * ============================================================
 *
 * BUILD A VEHICLE HIERARCHY using inheritance and abstract classes.
 *
 * LEARNING GOALS:
 * - Design an inheritance hierarchy with abstract classes
 * - Use abstract methods to enforce contracts on subclasses
 * - Override inherited methods appropriately
 * - Use super() to call parent constructors
 * - Apply the "Is-A" test correctly
 *
 * ============================================================
 * PART A: Implement the Vehicle abstract class
 * ============================================================
 *
 * An abstract Vehicle has:
 *   - make (String): manufacturer (e.g., "Toyota")
 *   - model (String): model name (e.g., "Camry")
 *   - year (int): manufacturing year (must be 1886 or later — first car)
 *   - fuelType (String): e.g., "Gasoline", "Diesel", "Electric", "Hybrid"
 *   - currentSpeed (double): starts at 0
 *
 * Abstract methods (each subclass must implement):
 *   - abstract double calculateFuelCost(double distanceKm): cost in dollars
 *   - abstract String getVehicleType(): returns "Car", "Electric Car", "Motorcycle"
 *
 * Concrete methods (shared by all vehicles):
 *   - accelerate(double amount): increases speed
 *   - brake(double amount): decreases speed (min 0)
 *   - printInfo(): prints make, model, year, type, fuel
 *   - toString(): readable representation
 *
 * ============================================================
 * PART B: Implement Car extends Vehicle
 * ============================================================
 *
 * A Car has:
 *   - numDoors (int): 2 or 4
 *   - fuelEfficiency (double): km per liter (e.g., 12.0)
 *
 * calculateFuelCost(distanceKm):
 *   - Gasoline costs $1.60/liter in our simulation
 *   - Cost = (distanceKm / fuelEfficiency) * 1.60
 *
 * ============================================================
 * PART C: Implement ElectricCar extends Vehicle
 * ============================================================
 *
 * An ElectricCar has:
 *   - batteryCapacityKwh (double): total battery capacity
 *   - rangePerKwh (double): km per kilowatt-hour
 *   - currentCharge (double): 0.0 to 1.0 (100%)
 *
 * calculateFuelCost(distanceKm):
 *   - Electricity costs $0.15/kWh in our simulation
 *   - Cost = (distanceKm / rangePerKwh) * 0.15
 *
 * Additional ElectricCar methods:
 *   - charge(): resets currentCharge to 1.0
 *   - getRemainingRange(): calculates km left on current charge
 *
 * ============================================================
 * PART D: Implement Motorcycle extends Vehicle
 * ============================================================
 *
 * A Motorcycle has:
 *   - hasSidecar (boolean)
 *   - engineCC (int): engine displacement in cubic centimeters
 *
 * calculateFuelCost(distanceKm):
 *   - Motorcycle fuel efficiency = engineCC / 100.0 km per liter
 *   - Cost = (distanceKm / efficiency) * 1.60
 *
 * ============================================================
 * STRETCH GOALS:
 * ============================================================
 *
 * 1. Add a HybridCar that extends Car and has both fuel and electric costs
 * 2. Create a Fleet class that holds multiple vehicles and can:
 *    - Calculate total fleet cost for a trip
 *    - Find the most fuel-efficient vehicle for a given trip
 * 3. Add an interface Registrable with getRegistrationInfo() and calculateTax()
 */
public class Exercise02_Vehicles {

    public static void main(String[] args) {
        System.out.println("Exercise 02: Vehicle Hierarchy");
        System.out.println("=".repeat(50));

        // ====================================================================
        // TEST YOUR IMPLEMENTATION BY UNCOMMENTING THESE TESTS
        // ====================================================================

        // --- Test basic vehicle creation ---
        // Car car = new Car("Toyota", "Camry", 2022, "Gasoline", 4, 12.0);
        // ElectricCar tesla = new ElectricCar("Tesla", "Model 3", 2023, 82.0, 6.0);
        // Motorcycle bike = new Motorcycle("Harley-Davidson", "Sportster", 2021, false, 883);

        // --- Test printInfo() ---
        // car.printInfo();
        // System.out.println();
        // tesla.printInfo();
        // System.out.println();
        // bike.printInfo();
        // System.out.println();

        // --- Test acceleration and braking ---
        // car.accelerate(60);
        // System.out.println("Car speed: " + car.currentSpeed + " km/h");
        // car.brake(20);
        // System.out.println("After braking: " + car.currentSpeed + " km/h");
        // car.brake(100);  // Should stop at 0, not go negative
        // System.out.println("After hard brake: " + car.currentSpeed + " km/h");

        // --- Test fuel cost calculation ---
        // double distance = 100.0;  // 100 km trip
        // System.out.printf("%nFuel costs for %.0f km trip:%n", distance);
        // System.out.printf("  Car (Gasoline):    $%.2f%n", car.calculateFuelCost(distance));
        // System.out.printf("  Tesla (Electric):  $%.2f%n", tesla.calculateFuelCost(distance));
        // System.out.printf("  Motorcycle:        $%.2f%n", bike.calculateFuelCost(distance));

        // --- Test polymorphism ---
        // Vehicle[] fleet = {car, tesla, bike};
        // System.out.println("\nAll vehicles in fleet:");
        // for (Vehicle v : fleet) {
        //     System.out.printf("  %s: $%.2f for 100km%n",
        //                       v.getVehicleType(), v.calculateFuelCost(100));
        // }

        // --- Test ElectricCar-specific ---
        // System.out.println("\nElectric car specifics:");
        // System.out.println("Remaining range: " + tesla.getRemainingRange() + " km");
        // tesla.charge();
        // System.out.println("After charge: " + tesla.getRemainingRange() + " km");

        System.out.println("\nImplement Vehicle, Car, ElectricCar, and Motorcycle above main(), then uncomment tests.");
    }

    // ====================================================================
    // IMPLEMENT YOUR CLASSES BELOW THIS LINE
    // ====================================================================

    // TODO: Implement abstract Vehicle class
    // Remember:
    // - abstract keyword before class
    // - calculateFuelCost and getVehicleType are abstract
    // - year validation: >= 1886
    // - currentSpeed cannot go below 0

    // TODO: Implement Car extends Vehicle

    // TODO: Implement ElectricCar extends Vehicle

    // TODO: Implement Motorcycle extends Vehicle
}

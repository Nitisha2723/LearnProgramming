package patterns.decorator;

/**
 * DESIGN PATTERN: Decorator
 * ==========================
 * Intent: Attach additional responsibilities to an object dynamically.
 * Decorators provide a flexible alternative to subclassing for extending
 * functionality.
 *
 * REAL-WORLD USE CASE: Coffee Shop Ordering System
 * -------------------------------------------------
 * A coffee shop has base drinks (Espresso, Americano, Latte) and add-ons
 * (Milk, Caramel, Whip, Vanilla). Customers can combine any add-ons with
 * any base drink. Each add-on changes the description and cost.
 *
 * ============================================================
 * THE PROBLEM: COMBINATORIAL EXPLOSION WITH SUBCLASSING
 * ============================================================
 *
 * WITHOUT Decorator (using inheritance only):
 *
 *   Espresso
 *   EspressoWithMilk
 *   EspressoWithCaramel
 *   EspressoWithWhip
 *   EspressoWithMilkAndCaramel
 *   EspressoWithMilkAndWhip
 *   EspressoWithCaramelAndWhip
 *   EspressoWithMilkAndCaramelAndWhip
 *   Americano
 *   AmericanoWithMilk
 *   ... (3 coffees × 4 add-ons = up to 3 × 2^4 = 48 subclasses!)
 *
 * Adding a 5th add-on doubles the number of classes.
 * This is called the "class explosion" anti-pattern.
 *
 * WITH Decorator:
 *   new WhipDecorator(new CaramelDecorator(new MilkDecorator(new Espresso())))
 *   Only 3 + 4 = 7 classes total, regardless of how many combinations exist.
 *   Any combination of any base with any add-ons works automatically.
 *
 * ============================================================
 * DECORATOR vs INHERITANCE:
 * ============================================================
 *
 *   Inheritance:         Composition (Decorator):
 *   - Compile-time       - Runtime
 *   - Static             - Dynamic (can wrap/unwrap)
 *   - Type-fixed         - Flexible combinations
 *   - N×M problem        - N+M solution
 *
 * ============================================================
 * THE FAMOUS REAL-WORLD EXAMPLE: Java I/O STREAMS
 * ============================================================
 * Java's I/O library is built entirely on the Decorator pattern:
 *
 *   InputStream                           ← Component interface
 *   ├── FileInputStream                   ← Concrete Component
 *   ├── ByteArrayInputStream              ← Concrete Component
 *   └── FilterInputStream                 ← Abstract Decorator
 *       ├── BufferedInputStream(fis)      ← Adds buffering
 *       ├── DataInputStream(bis)          ← Adds typed reads
 *       └── GZIPInputStream(dis)          ← Adds decompression
 *
 * Example:
 *   new GZIPInputStream(
 *     new BufferedInputStream(
 *       new DataInputStream(
 *         new FileInputStream("data.gz")
 *       )))
 *
 * Each wrapper adds one behavior, just like our coffee add-ons!
 * You can wrap in any order, combine any way, swap components freely.
 *
 * ============================================================
 * KEY STRUCTURAL RULES:
 * ============================================================
 *   1. Decorator implements the SAME interface as the component it wraps
 *   2. Decorator holds a REFERENCE to the component (composition, not inheritance)
 *   3. Decorator delegates to the wrapped component, then adds its behavior
 *   4. Decorators are TRANSPARENT: code that accepts Coffee works with
 *      decorated Coffee too (because they share the same interface)
 */

// =============================================================================
// FILE STRUCTURE:
//   1. Coffee interface                — Component (the contract)
//   2. Espresso, Americano, Latte      — Concrete Components (base objects)
//   3. CoffeeDecorator abstract class  — Abstract Decorator (optional, for DRY)
//   4. MilkDecorator                   — Concrete Decorator
//   5. CaramelDecorator                — Concrete Decorator
//   6. WhipDecorator                   — Concrete Decorator
//   7. VanillaDecorator                — Concrete Decorator
//   8. CoffeeDecorator (public class)  — Demo runner with main()
// =============================================================================

// -----------------------------------------------------------------------------
// COMPONENT INTERFACE: the shared contract for base coffees AND decorators
// -----------------------------------------------------------------------------
/**
 * Coffee — the Component interface.
 *
 * CRITICAL INSIGHT: Both the base coffees (Espresso, Latte) AND the add-on
 * decorators (MilkDecorator, WhipDecorator) implement this interface.
 *
 * This is what makes decorators TRANSPARENT: code written to accept Coffee
 * automatically works with "Coffee with Milk with Caramel" — they're all Coffee.
 *
 * A client calling order.getCost() doesn't know or care whether they have
 * a plain Espresso or an Espresso wrapped in 3 decorators.
 */
interface Coffee {
    /**
     * Returns a human-readable description of this coffee and all its add-ons.
     * Each decorator adds its name to the base coffee's description.
     */
    String getDescription();

    /**
     * Returns the total cost of this coffee including all add-ons.
     * Each decorator adds its price to the base coffee's price.
     */
    double getCost();
}

// -----------------------------------------------------------------------------
// CONCRETE COMPONENTS: base coffees (no add-ons)
// -----------------------------------------------------------------------------
/**
 * Espresso — a single shot of concentrated coffee.
 * This is the INNERMOST object in a decorator stack.
 */
class Espresso implements Coffee {
    @Override
    public String getDescription() {
        return "Espresso";
    }

    @Override
    public double getCost() {
        return 2.50;
    }
}

/**
 * Americano — espresso diluted with hot water.
 */
class Americano implements Coffee {
    @Override
    public String getDescription() {
        return "Americano";
    }

    @Override
    public double getCost() {
        return 2.00;
    }
}

/**
 * Latte — espresso with steamed milk.
 */
class Latte implements Coffee {
    @Override
    public String getDescription() {
        return "Latte";
    }

    @Override
    public double getCost() {
        return 3.50;
    }
}

// -----------------------------------------------------------------------------
// ABSTRACT DECORATOR: optional base class to reduce code duplication
// -----------------------------------------------------------------------------
/**
 * CoffeeDecorator — Abstract Decorator base class.
 *
 * This is optional but useful. It holds the reference to the wrapped Coffee
 * and delegates both methods to it by default. Concrete decorators then
 * only need to override what they change (usually BOTH methods, but still).
 *
 * WHY ABSTRACT DECORATOR?
 * Without it, each concrete decorator would duplicate:
 *   private final Coffee wrappedCoffee;
 *   CoffeeDecorator(Coffee coffee) { this.wrappedCoffee = coffee; }
 *
 * The abstract base class extracts this common code (DRY principle).
 *
 * NOTE: CoffeeDecorator extends nothing special — it just implements Coffee
 * (same interface as the things it wraps). This is the key to transparency.
 */
abstract class AbstractCoffeeDecorator implements Coffee {
    // The wrapped object — could be a base coffee or another decorator
    // (this is what enables stacking!)
    protected final Coffee wrappedCoffee;

    /**
     * @param coffee The Coffee to wrap. Could be Espresso, Latte, or another decorator.
     */
    protected AbstractCoffeeDecorator(Coffee coffee) {
        if (coffee == null) throw new IllegalArgumentException("Cannot decorate null Coffee");
        this.wrappedCoffee = coffee;
    }

    /**
     * Default delegation — pass through to the wrapped coffee.
     * Concrete decorators override this to add their contribution.
     */
    @Override
    public String getDescription() {
        return wrappedCoffee.getDescription(); // delegate to wrapped object
    }

    @Override
    public double getCost() {
        return wrappedCoffee.getCost(); // delegate to wrapped object
    }
}

// -----------------------------------------------------------------------------
// CONCRETE DECORATORS: each adds one specific add-on
// -----------------------------------------------------------------------------
/**
 * MilkDecorator — adds steamed milk to any coffee.
 *
 * The delegation chain when this is called:
 *   MilkDecorator.getCost()
 *     → calls wrappedCoffee.getCost() (which might be Espresso or another decorator)
 *     → adds 0.50
 *     → returns total
 *
 * This "unwrapping" continues until we hit the base Espresso/Latte/Americano.
 */
class MilkDecorator extends AbstractCoffeeDecorator {

    public MilkDecorator(Coffee coffee) {
        super(coffee);
    }

    @Override
    public String getDescription() {
        // Extend the description by appending our add-on
        return wrappedCoffee.getDescription() + ", Milk";
    }

    @Override
    public double getCost() {
        // Add our cost to the wrapped coffee's cost
        return wrappedCoffee.getCost() + 0.50;
    }
}

/**
 * CaramelDecorator — adds caramel sauce.
 */
class CaramelDecorator extends AbstractCoffeeDecorator {

    public CaramelDecorator(Coffee coffee) {
        super(coffee);
    }

    @Override
    public String getDescription() {
        return wrappedCoffee.getDescription() + ", Caramel Sauce";
    }

    @Override
    public double getCost() {
        return wrappedCoffee.getCost() + 0.75;
    }
}

/**
 * WhipDecorator — adds whipped cream.
 */
class WhipDecorator extends AbstractCoffeeDecorator {

    public WhipDecorator(Coffee coffee) {
        super(coffee);
    }

    @Override
    public String getDescription() {
        return wrappedCoffee.getDescription() + ", Whipped Cream";
    }

    @Override
    public double getCost() {
        return wrappedCoffee.getCost() + 0.60;
    }
}

/**
 * VanillaDecorator — adds vanilla syrup.
 */
class VanillaDecorator extends AbstractCoffeeDecorator {

    public VanillaDecorator(Coffee coffee) {
        super(coffee);
    }

    @Override
    public String getDescription() {
        return wrappedCoffee.getDescription() + ", Vanilla Syrup";
    }

    @Override
    public double getCost() {
        return wrappedCoffee.getCost() + 0.65;
    }
}

// =============================================================================
// DEMO RUNNER
// =============================================================================
/**
 * Demonstrates the Decorator pattern with various coffee combinations.
 *
 * Key observation: The order function (printOrder) accepts just 'Coffee'.
 * It works equally well with a plain Espresso or an Espresso with 4 decorators.
 * This is the TRANSPARENCY property of the Decorator pattern.
 */
public class CoffeeDecorator {

    /** Simulates a barista printing the order ticket */
    private static void printOrder(Coffee coffee, int orderNum) {
        System.out.printf("  Order #%d: %-50s $%.2f%n",
                orderNum, coffee.getDescription(), coffee.getCost());
    }

    public static void main(String[] args) {
        System.out.println("=".repeat(65));
        System.out.println("DESIGN PATTERN: Decorator");
        System.out.println("USE CASE: Coffee Shop Ordering System");
        System.out.println("=".repeat(65) + "\n");

        // -------------------------------------------------------
        // DEMO 1: Plain coffees (no decorators)
        // -------------------------------------------------------
        System.out.println("--- DEMO 1: Plain Coffees (no add-ons) ---");
        printOrder(new Espresso(), 1);
        printOrder(new Americano(), 2);
        printOrder(new Latte(), 3);

        // -------------------------------------------------------
        // DEMO 2: Single decorator
        // -------------------------------------------------------
        System.out.println("\n--- DEMO 2: Single Add-On ---");
        // Wrap Espresso in MilkDecorator — still a Coffee!
        Coffee espressoWithMilk = new MilkDecorator(new Espresso());
        printOrder(espressoWithMilk, 4);

        Coffee latteWithCaramel = new CaramelDecorator(new Latte());
        printOrder(latteWithCaramel, 5);

        // -------------------------------------------------------
        // DEMO 3: Stacking multiple decorators
        // -------------------------------------------------------
        System.out.println("\n--- DEMO 3: Stacked Add-Ons ---");

        // Read from inside out: Espresso, wrapped in Milk, wrapped in Caramel, wrapped in Whip
        Coffee fancyEspresso = new WhipDecorator(
                                    new CaramelDecorator(
                                        new MilkDecorator(
                                            new Espresso())));
        printOrder(fancyEspresso, 6);

        // Americano with vanilla and milk
        Coffee vanillaAmericano = new MilkDecorator(
                                      new VanillaDecorator(
                                          new Americano()));
        printOrder(vanillaAmericano, 7);

        // Latte with ALL the add-ons
        Coffee overTheTopLatte = new VanillaDecorator(
                                     new WhipDecorator(
                                         new CaramelDecorator(
                                             new MilkDecorator(
                                                 new Latte()))));
        printOrder(overTheTopLatte, 8);

        // -------------------------------------------------------
        // DEMO 4: Double add-ons (same decorator twice — allowed!)
        // -------------------------------------------------------
        System.out.println("\n--- DEMO 4: Double Add-On (same decorator twice) ---");
        // Double shot of milk? Double caramel? Valid with Decorator!
        Coffee doubleCaramel = new CaramelDecorator(
                                   new CaramelDecorator(
                                       new Latte()));
        printOrder(doubleCaramel, 9);

        // -------------------------------------------------------
        // DEMO 5: Transparency — function works for any Coffee
        // -------------------------------------------------------
        System.out.println("\n--- DEMO 5: Transparency (all accepted as Coffee) ---");
        System.out.println("The same printOrder() function handles ALL of these:");

        Coffee[] orders = {
            new Espresso(),                                // no decoration
            new MilkDecorator(new Espresso()),             // one decorator
            new WhipDecorator(new CaramelDecorator(        // two decorators
                new MilkDecorator(new Americano()))),
        };

        for (int i = 0; i < orders.length; i++) {
            // printOrder accepts Coffee — it doesn't know or care about wrapping
            printOrder(orders[i], 10 + i);
        }

        // -------------------------------------------------------
        // DEMO 6: Show the I/O streams parallel
        // -------------------------------------------------------
        System.out.println("\n--- DEMO 6: Java I/O Streams Analogy ---");
        System.out.println("Java I/O uses the exact same pattern:");
        System.out.println("  InputStream base = new FileInputStream(\"file.gz\");");
        System.out.println("  InputStream buffered = new BufferedInputStream(base);");
        System.out.println("  InputStream decompressed = new GZIPInputStream(buffered);");
        System.out.println("  DataInputStream data = new DataInputStream(decompressed);");
        System.out.println();
        System.out.println("  CoffeeShop equivalent:");
        System.out.println("  Coffee base = new Espresso();             // FileInputStream");
        System.out.println("  Coffee c2   = new MilkDecorator(base);    // BufferedInputStream");
        System.out.println("  Coffee c3   = new CaramelDecorator(c2);   // DataInputStream");
        System.out.println("  Coffee c4   = new WhipDecorator(c3);      // GZIPInputStream");
        System.out.println("  → Same pattern, same transparency, same power!");

        // -------------------------------------------------------
        // Show the combinatorial explosion we avoided
        // -------------------------------------------------------
        System.out.println("\n--- What Decorator Avoided ---");
        System.out.println("3 base coffees × 4 add-ons = up to 3 × 2^4 = 48 subclasses");
        System.out.println("With Decorator: 3 + 4 = 7 classes, infinite combinations");
        System.out.println("\nDone!");
    }
}

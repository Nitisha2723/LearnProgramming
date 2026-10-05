/**
 * AnimalDemo — demonstrates polymorphism with the Animal hierarchy.
 *
 * KEY CONCEPTS SHOWN:
 * 1. Polymorphic variables (Animal reference → Dog/Cat object)
 * 2. Dynamic dispatch (the right makeSound() is called based on actual type)
 * 3. Polymorphic arrays and collections
 * 4. Downcasting with instanceof
 * 5. The power of writing code to the parent type
 */
public class AnimalDemo {

    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("Animal Hierarchy Demonstration");
        System.out.println("=".repeat(60));

        demo1_PolymorphicVariable();
        demo2_PolymorphicArray();
        demo3_Downcasting();
        demo4_NoNewCodeNeeded();
    }

    // =========================================================================
    // Demo 1: The Polymorphic Variable
    // =========================================================================
    static void demo1_PolymorphicVariable() {
        System.out.println("\n--- Demo 1: Polymorphic Variable ---");

        // Animal reference CAN point to a Dog object
        // (Dog IS an Animal — upcasting, always safe)
        Animal animal = new Dog("Rex", 3, "German Shepherd");

        System.out.println("animal variable type: Animal");
        System.out.println("actual object type: " + animal.getClass().getSimpleName());
        System.out.println();

        // Calling makeSound() — which version runs?
        System.out.print("animal.makeSound() returns: ");
        System.out.println(animal.makeSound());   // "Woof!" — DOG's version!
        // Even though the variable is Animal, it's actually a Dog object,
        // so Dog's makeSound() is called. This is DYNAMIC DISPATCH.

        // Inherited methods work too
        animal.eat();
        animal.describe();

        System.out.println("\n--- Same variable, now points to a Cat ---");
        animal = new Cat("Luna", 2, true);  // Same variable!
        System.out.print("animal.makeSound() now returns: ");
        System.out.println(animal.makeSound());  // "Meow!" — CAT's version!
        animal.describe();
    }

    // =========================================================================
    // Demo 2: Polymorphic Array
    // =========================================================================
    static void demo2_PolymorphicArray() {
        System.out.println("\n--- Demo 2: Polymorphic Array ---");

        // An array of Animal references can hold any Animal subtype
        Animal[] farm = {
            new Dog("Rex", 3, "Labrador"),
            new Cat("Whiskers", 5, false),
            new Dog("Buddy", 2, "Poodle"),
            new Cat("Luna", 1, true),
            new Dog("Max", 7, "German Shepherd", true),
        };

        System.out.println("All animals speak:");
        for (Animal animal : farm) {
            // This single line works for ALL animal types
            // No if-else needed — polymorphism handles it
            System.out.printf("  %-15s says: %s%n", animal.getName(), animal.makeSound());
        }

        System.out.println("\nAll animals introducing themselves:");
        for (Animal animal : farm) {
            animal.describe();
        }
    }

    // =========================================================================
    // Demo 3: Downcasting — Accessing Subtype-Specific Methods
    // =========================================================================
    static void demo3_Downcasting() {
        System.out.println("\n--- Demo 3: Downcasting ---");

        Animal[] animals = {
            new Dog("Rex", 3, "Labrador"),
            new Cat("Luna", 2, true),
            new Dog("Buddy", 1, "Beagle"),
        };

        for (Animal animal : animals) {
            // Can always call Animal methods
            System.out.println("\n" + animal.getName() + ":");
            animal.eat();

            // To call Dog/Cat-specific methods, must downcast
            // First check with instanceof to avoid ClassCastException
            if (animal instanceof Dog) {
                Dog dog = (Dog) animal;  // Downcast — safe because we checked
                dog.fetch("tennis ball");
                dog.bark(3);
            } else if (animal instanceof Cat) {
                Cat cat = (Cat) animal;
                cat.purr();
                cat.knockThingsOff("coffee mug");
            }
        }

        System.out.println("\n--- Java 16+ Pattern Matching instanceof ---");
        Animal a = new Dog("Rex", 3, "Lab");

        // Cleaner syntax — cast happens in the instanceof check
        if (a instanceof Dog d) {   // 'd' is automatically a Dog reference
            System.out.println(d.getName() + " fetch!");
            d.fetch("frisbee");
        }
    }

    // =========================================================================
    // Demo 4: New Animal Types Need Zero Changes to This Method
    // =========================================================================
    static void demo4_NoNewCodeNeeded() {
        System.out.println("\n--- Demo 4: The Power of Polymorphism ---");
        System.out.println("makeAllSpeak() works for ANY Animal subtype.");
        System.out.println("Adding a new animal type never requires changing it.");
        System.out.println();

        Animal[] animals = {
            new Dog("Rex", 3, "Lab"),
            new Cat("Luna", 2, true),
        };

        makeAllSpeak(animals);

        System.out.println("\n(If we added: Bird extends Animal with makeSound() = 'Tweet!')");
        System.out.println("makeAllSpeak() would still work with no changes.");
    }

    /**
     * Works for EVERY Animal subclass — past, present, and future.
     * This is the power of polymorphism: write once, works for all.
     */
    static void makeAllSpeak(Animal[] animals) {
        for (Animal animal : animals) {
            System.out.printf("%-10s (%s): %s%n",
                             animal.getName(),
                             animal.getAnimalType(),
                             animal.makeSound());
        }
    }
}

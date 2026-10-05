/**
 * Dog — a concrete subclass of Animal demonstrating:
 * - Extending an abstract class
 * - Calling super() constructor
 * - Implementing abstract methods
 * - Adding new fields and methods specific to Dog
 * - Method overriding with @Override
 * - Calling super.method() to extend (not just replace) parent behavior
 */
public class Dog extends Animal {

    // Dog-specific fields
    private String breed;
    private boolean isTrained;

    /**
     * Constructor — calls super() to initialize inherited fields.
     *
     * The first statement MUST be super(name, age) because Animal
     * has no default (no-arg) constructor.
     *
     * @param name    the dog's name
     * @param age     the dog's age in years
     * @param breed   the dog's breed (e.g., "Labrador", "Poodle")
     */
    public Dog(String name, int age, String breed) {
        super(name, age);   // Initialize Animal fields
        this.breed = breed;
        this.isTrained = false;  // Default: untrained
    }

    /**
     * Overloaded constructor — adds isTrained parameter.
     * Chains to the main Dog constructor.
     */
    public Dog(String name, int age, String breed, boolean isTrained) {
        this(name, age, breed);  // Chain to Dog(String, int, String)
        this.isTrained = isTrained;
    }

    // =========================================================================
    // REQUIRED: Implement abstract methods from Animal
    // =========================================================================

    /**
     * @Override tells the compiler this is an intentional override.
     * If we typo'd "makeSound" as "makesound", compiler catches it.
     */
    @Override
    public String makeSound() {
        return "Woof!";
    }

    @Override
    public String getAnimalType() {
        return "Dog (" + breed + ")";
    }

    // =========================================================================
    // OVERRIDE: Change inherited behavior
    // =========================================================================

    /**
     * Overrides Animal.eat() to add dog-specific behavior.
     * Uses super.eat() to also run the parent behavior — extending, not replacing.
     */
    @Override
    public void eat() {
        super.eat();  // "Rex is happily eating." from Animal
        System.out.println(name + " wags its tail enthusiastically!");  // Dog extra
    }

    // =========================================================================
    // NEW: Dog-specific methods not in Animal
    // =========================================================================

    /**
     * Dog-specific behavior: fetch.
     * This method doesn't exist in Animal — it's unique to Dog.
     * You can only call this when you have a Dog reference (not Animal reference).
     */
    public void fetch(String item) {
        System.out.printf("%s dashes to retrieve the %s and brings it back!%n",
                         name, item);
    }

    /**
     * Dog-specific behavior: bark.
     * Distinct from makeSound() — this is an action, not just the sound.
     */
    public void bark(int times) {
        StringBuilder barks = new StringBuilder(name + " barks: ");
        for (int i = 0; i < times; i++) {
            barks.append("Woof! ");
        }
        System.out.println(barks.toString().trim());
    }

    /**
     * Training method.
     */
    public void train() {
        if (isTrained) {
            System.out.println(name + " already knows all the tricks!");
        } else {
            isTrained = true;
            System.out.println(name + " learned: sit, stay, shake! Good dog!");
        }
    }

    // Getters
    public String getBreed() { return breed; }
    public boolean isTrained() { return isTrained; }

    /**
     * Override toString() to include Dog-specific information.
     */
    @Override
    public String toString() {
        return String.format("Dog{name='%s', age=%d, breed='%s', trained=%s}",
                            name, age, breed, isTrained);
    }
}

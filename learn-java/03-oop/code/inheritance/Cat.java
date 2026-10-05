/**
 * Cat — another concrete subclass of Animal.
 *
 * Shows how two sibling classes (Dog and Cat) can share a parent (Animal)
 * while having completely different implementations of the abstract methods
 * and their own unique behaviors.
 */
public class Cat extends Animal {

    private boolean isIndoor;
    private int livesRemaining;  // Cats have 9 lives (allegedly)

    /**
     * Constructor.
     *
     * @param name     the cat's name
     * @param age      the cat's age
     * @param isIndoor whether this is an indoor or outdoor cat
     */
    public Cat(String name, int age, boolean isIndoor) {
        super(name, age);      // Call Animal constructor
        this.isIndoor = isIndoor;
        this.livesRemaining = 9;  // Cats start with 9 lives
    }

    // =========================================================================
    // REQUIRED: Implement abstract methods from Animal
    // =========================================================================

    @Override
    public String makeSound() {
        return "Meow!";
    }

    @Override
    public String getAnimalType() {
        return isIndoor ? "Indoor Cat" : "Outdoor Cat";
    }

    // =========================================================================
    // NEW: Cat-specific methods
    // =========================================================================

    /**
     * Cats purr. Dogs don't. This is Cat-only behavior.
     */
    public void purr() {
        System.out.println(name + " purrs contentedly: Purrrrr...");
    }

    /**
     * Cats knock things off tables.
     */
    public void knockThingsOff(String item) {
        System.out.printf("%s stares you in the eye and slowly pushes the %s off the table.%n",
                         name, item);
    }

    /**
     * Classic cat behavior: ignoring you.
     */
    public void ignore(String person) {
        System.out.println(name + " hears " + person + " calling and decides not to respond.");
    }

    /**
     * Cats use one of their lives when in danger.
     */
    public boolean useLive() {
        if (livesRemaining > 0) {
            livesRemaining--;
            System.out.println(name + " used a life! " + livesRemaining + " lives remaining.");
            return true;
        }
        System.out.println(name + " has no lives left!");
        return false;
    }

    // Getters
    public boolean isIndoor() { return isIndoor; }
    public int getLivesRemaining() { return livesRemaining; }

    @Override
    public String toString() {
        return String.format("Cat{name='%s', age=%d, indoor=%s, lives=%d}",
                            name, age, isIndoor, livesRemaining);
    }
}

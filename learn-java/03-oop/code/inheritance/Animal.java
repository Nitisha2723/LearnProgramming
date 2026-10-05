/**
 * Animal — abstract base class for the animal hierarchy.
 *
 * WHY ABSTRACT?
 * An "Animal" in isolation doesn't make sense in our domain.
 * There's no animal that's just an "Animal" — every real animal
 * is a specific type: a dog, cat, bird, fish, etc.
 * Making Animal abstract enforces that you must create a specific type.
 *
 * WHAT THIS CLASS PROVIDES:
 * - Shared fields: name, age, sound
 * - Concrete shared behavior: sleep(), eat(), describe()
 * - Abstract contract: makeSound() — every animal makes a sound, but each differently
 */
public abstract class Animal {

    // Protected fields — accessible by subclasses but not external code
    protected final String name;  // final: an animal's name doesn't change
    protected int age;            // Not final: age can change (birthdays!)

    /**
     * Constructor for Animal.
     *
     * Subclasses MUST call super(name, age) to initialize these fields.
     * This ensures every Animal has a name and age from the start.
     */
    public Animal(String name, int age) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Animal must have a name");
        }
        if (age < 0) {
            throw new IllegalArgumentException("Age cannot be negative");
        }
        this.name = name;
        this.age = age;
    }

    // =========================================================================
    // ABSTRACT METHODS — subclasses MUST implement these
    // =========================================================================

    /**
     * Returns the sound this animal makes.
     *
     * Abstract because every animal makes a different sound.
     * There is no sensible default for "generic animal sound."
     *
     * @return the sound (e.g., "Woof!", "Meow!", "Moo!")
     */
    public abstract String makeSound();

    /**
     * Returns the type/species of this animal.
     *
     * @return species name (e.g., "Dog", "Cat", "Cow")
     */
    public abstract String getAnimalType();

    // =========================================================================
    // CONCRETE METHODS — shared behavior for ALL animals
    // =========================================================================

    /**
     * Makes the animal eat.
     * All animals eat — this behavior is shared.
     */
    public void eat() {
        System.out.println(name + " is happily eating.");
    }

    /**
     * Makes the animal sleep.
     * All animals sleep — this behavior is shared.
     */
    public void sleep() {
        System.out.println(name + " is sleeping. Zzz...");
    }

    /**
     * Describes this animal.
     *
     * This method uses makeSound() which is abstract.
     * When called on a Dog, it will use Dog's makeSound().
     * When called on a Cat, it will use Cat's makeSound().
     *
     * This is polymorphism at work — the same describe() method
     * produces different output based on the actual animal type.
     */
    public void describe() {
        System.out.printf("%s is a %d-year-old %s who says '%s'%n",
                         name, age, getAnimalType(), makeSound());
    }

    /**
     * Simulates the animal's birthday.
     */
    public void haveBirthday() {
        age++;
        System.out.println("Happy birthday, " + name + "! Now " + age + " years old.");
    }

    // Getters
    public String getName() { return name; }
    public int getAge() { return age; }

    @Override
    public String toString() {
        return getAnimalType() + " named " + name + " (age " + age + ")";
    }
}

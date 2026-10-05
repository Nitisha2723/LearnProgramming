import java.util.Objects;

/**
 * Person — abstract base class for all people in the school system.
 *
 * OOP CONCEPTS:
 * - Abstract class: cannot instantiate Person directly (there's no generic "person")
 * - Encapsulation: all fields private, accessed via getters
 * - Template method: displayInfo() calls getRole() which is abstract
 */
public abstract class Person {

    private final String personId;   // Unique identifier, immutable
    private String name;
    private int age;
    private String email;

    public Person(String personId, String name, int age, String email) {
        if (personId == null || personId.trim().isEmpty()) {
            throw new IllegalArgumentException("Person ID cannot be empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (age < 0 || age > 120) {
            throw new IllegalArgumentException("Age must be 0-120");
        }

        this.personId = personId;
        this.name = name.trim();
        this.age = age;
        this.email = email;
    }

    // Abstract method — subclasses define their role
    public abstract String getRole();

    // Concrete shared method that uses the abstract method (template method pattern)
    public void displayInfo() {
        System.out.printf("[%s] %s %s (age %d) - %s%n",
                         getRole(), personId, name, age,
                         email != null ? email : "no email");
    }

    // Getters
    public String getPersonId() { return personId; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getEmail() { return email; }

    /**
     * Simulates a birthday — increments age by 1.
     */
    public void haveBirthday() {
        age++;
        System.out.println("Happy birthday, " + name + "! Now " + age + " years old.");
    }

    // Setters (only for mutable properties)
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        this.name = name.trim();
    }

    public void setAge(int age) {
        if (age < 0 || age > 120) throw new IllegalArgumentException("Invalid age");
        this.age = age;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Person)) return false;
        Person other = (Person) obj;
        return Objects.equals(personId, other.personId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(personId);
    }

    @Override
    public String toString() {
        return getRole() + "{id=" + personId + ", name=" + name + "}";
    }
}

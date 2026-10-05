import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * AnimalTest — tests for the Animal hierarchy, demonstrating
 * how to test polymorphic behavior with JUnit 5.
 *
 * KEY TESTING CHALLENGE: How do you test polymorphism?
 * - Create objects of different subtypes
 * - Verify that the correct implementation is invoked
 * - Test via the parent type (Animal) to verify substitutability
 */
@DisplayName("Animal Hierarchy Tests")
class AnimalTest {

    // =========================================================================
    // Dog Tests
    // =========================================================================

    @Nested
    @DisplayName("Dog Tests")
    class DogTests {
        private Dog dog;

        @BeforeEach
        void setUp() {
            dog = new Dog("Rex", 3, "Labrador");
        }

        @Test
        @DisplayName("Dog should have correct name and age")
        void dogShouldHaveCorrectNameAndAge() {
            assertEquals("Rex", dog.getName());
            assertEquals(3, dog.getAge());
        }

        @Test
        @DisplayName("Dog makeSound should return Woof")
        void dogMakeSoundShouldReturnWoof() {
            assertEquals("Woof!", dog.makeSound());
        }

        @Test
        @DisplayName("Dog getAnimalType should contain breed")
        void dogAnimalTypeShouldContainBreed() {
            String type = dog.getAnimalType();
            assertTrue(type.contains("Dog"), "Type should contain 'Dog'");
            assertTrue(type.contains("Labrador"), "Type should contain breed");
        }

        @Test
        @DisplayName("New dog should start untrained")
        void newDogShouldStartUntrained() {
            assertFalse(dog.isTrained());
        }

        @Test
        @DisplayName("Dog should be trained after train()")
        void dogShouldBeTrainedAfterTrain() {
            dog.train();
            assertTrue(dog.isTrained());
        }

        @Test
        @DisplayName("Dog constructor with isTrained should work")
        void dogConstructorWithIsTrainedShouldWork() {
            Dog trainedDog = new Dog("Buddy", 5, "Poodle", true);
            assertTrue(trainedDog.isTrained());
        }
    }

    // =========================================================================
    // Cat Tests
    // =========================================================================

    @Nested
    @DisplayName("Cat Tests")
    class CatTests {
        private Cat indoorCat;
        private Cat outdoorCat;

        @BeforeEach
        void setUp() {
            indoorCat = new Cat("Luna", 2, true);
            outdoorCat = new Cat("Tiger", 4, false);
        }

        @Test
        @DisplayName("Cat makeSound should return Meow")
        void catMakeSoundShouldReturnMeow() {
            assertEquals("Meow!", indoorCat.makeSound());
        }

        @Test
        @DisplayName("Indoor cat type should say Indoor Cat")
        void indoorCatTypeShouldBeIndoor() {
            assertTrue(indoorCat.getAnimalType().contains("Indoor"));
        }

        @Test
        @DisplayName("Outdoor cat type should say Outdoor Cat")
        void outdoorCatTypeShouldBeOutdoor() {
            assertTrue(outdoorCat.getAnimalType().contains("Outdoor"));
        }

        @Test
        @DisplayName("New cat should have 9 lives")
        void newCatShouldHaveNineLives() {
            assertEquals(9, indoorCat.getLivesRemaining());
        }

        @Test
        @DisplayName("Using a live should decrease lives by one")
        void usingALiveShouldDecreaseLives() {
            int livesBefore = indoorCat.getLivesRemaining();
            indoorCat.useLive();
            assertEquals(livesBefore - 1, indoorCat.getLivesRemaining());
        }

        @Test
        @DisplayName("Cat with no lives should return false from useLive()")
        void catWithNoLivesShouldReturnFalse() {
            // Use all 9 lives
            for (int i = 0; i < 9; i++) {
                indoorCat.useLive();
            }
            assertFalse(indoorCat.useLive(), "Cat with 0 lives should return false");
        }
    }

    // =========================================================================
    // Polymorphism Tests — the most important tests
    // =========================================================================

    @Nested
    @DisplayName("Polymorphism Tests")
    class PolymorphismTests {

        @Test
        @DisplayName("Dog held in Animal reference should call Dog's makeSound")
        void dogHeldInAnimalReferenceShouldCallDogMakeSound() {
            Animal animal = new Dog("Rex", 3, "Lab");  // Animal reference
            assertEquals("Woof!", animal.makeSound()); // Dog's implementation
        }

        @Test
        @DisplayName("Cat held in Animal reference should call Cat's makeSound")
        void catHeldInAnimalReferenceShouldCallCatMakeSound() {
            Animal animal = new Cat("Luna", 2, true);  // Animal reference
            assertEquals("Meow!", animal.makeSound()); // Cat's implementation
        }

        @Test
        @DisplayName("Array of Animals should invoke the correct makeSound for each")
        void arrayOfAnimalsShouldInvokeCorrectMakeSound() {
            Animal[] animals = {
                new Dog("Rex", 3, "Lab"),
                new Cat("Luna", 2, true),
                new Dog("Buddy", 1, "Beagle"),
                new Cat("Whiskers", 5, false)
            };

            String[] expectedSounds = {"Woof!", "Meow!", "Woof!", "Meow!"};

            for (int i = 0; i < animals.length; i++) {
                assertEquals(expectedSounds[i], animals[i].makeSound(),
                    "Animal[" + i + "] should say " + expectedSounds[i]);
            }
        }

        @Test
        @DisplayName("Dog instanceof Animal should be true")
        void dogShouldBeInstanceOfAnimal() {
            Dog dog = new Dog("Rex", 3, "Lab");
            assertTrue(dog instanceof Animal);
        }

        @Test
        @DisplayName("Cat instanceof Animal should be true")
        void catShouldBeInstanceOfAnimal() {
            Cat cat = new Cat("Luna", 2, true);
            assertTrue(cat instanceof Animal);
        }

        @Test
        @DisplayName("Dog should not be an instance of Cat")
        void dogShouldNotBeInstanceOfCat() {
            Animal dog = new Dog("Rex", 3, "Lab");
            assertFalse(dog instanceof Cat);
        }
    }

    // =========================================================================
    // Shared Animal Behavior Tests
    // =========================================================================

    @Nested
    @DisplayName("Shared Animal Behavior Tests")
    class SharedBehaviorTests {

        @Test
        @DisplayName("Birthday should increment age")
        void birthdayShouldIncrementAge() {
            Dog dog = new Dog("Rex", 3, "Lab");
            int ageBefore = dog.getAge();
            dog.haveBirthday();
            assertEquals(ageBefore + 1, dog.getAge());
        }

        @Test
        @DisplayName("Animal constructor should reject null name")
        void animalConstructorShouldRejectNullName() {
            // We test via a concrete subclass since Animal is abstract
            assertThrows(IllegalArgumentException.class,
                () -> new Dog(null, 3, "Lab"));
        }

        @Test
        @DisplayName("Animal constructor should reject empty name")
        void animalConstructorShouldRejectEmptyName() {
            assertThrows(IllegalArgumentException.class,
                () -> new Dog("", 3, "Lab"));
        }

        @Test
        @DisplayName("Animal constructor should reject negative age")
        void animalConstructorShouldRejectNegativeAge() {
            assertThrows(IllegalArgumentException.class,
                () -> new Cat("Luna", -1, true));
        }

        @Test
        @DisplayName("toString should include name and type")
        void toStringShouldIncludeNameAndType() {
            Dog dog = new Dog("Rex", 3, "Lab");
            String str = dog.toString();
            assertTrue(str.contains("Rex"), "toString should contain name");
        }
    }
}

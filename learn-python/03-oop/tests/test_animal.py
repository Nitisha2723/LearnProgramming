"""
Tests for the Animal hierarchy.

Run with:
    cd 03-oop && pytest tests/test_animal.py -v
"""

import pytest
import sys
import os

sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..', 'code', 'inheritance'))

from animal import Animal, Dog, Cat, Bird, GuideDog


class TestAnimalAbstract:
    """Test that Animal ABC cannot be instantiated."""

    def test_cannot_instantiate_animal(self) -> None:
        with pytest.raises(TypeError, match="abstract"):
            Animal("Rex", 3, "wild")   # type: ignore

    def test_animal_subclasses_can_be_instantiated(self) -> None:
        # Verify all concrete subclasses work
        dog = Dog("Rex", 3, "Lab")
        cat = Cat("Whiskers", 5)
        bird = Bird("Tweety", 2, "Canary")
        guide = GuideDog("Buddy", 4, "Golden", "John")
        # If we get here without exception, the test passes
        assert all([dog, cat, bird, guide])


class TestDogCreation:
    """Test Dog initialization."""

    def test_dog_basic_creation(self) -> None:
        dog = Dog("Rex", 3, "Labrador")
        assert dog.name == "Rex"
        assert dog.age == 3
        assert dog.breed == "Labrador"
        assert dog.habitat == "domestic"

    def test_dog_is_trained_default_false(self) -> None:
        dog = Dog("Rex", 3, "Lab")
        assert dog.is_trained is False

    def test_dog_can_be_trained(self) -> None:
        dog = Dog("Rex", 3, "Lab", is_trained=True)
        assert dog.is_trained is True

    def test_dog_empty_name_raises(self) -> None:
        with pytest.raises(ValueError):
            Dog("", 3, "Lab")

    def test_dog_negative_age_raises(self) -> None:
        with pytest.raises(ValueError):
            Dog("Rex", -1, "Lab")

    def test_dog_initial_energy(self) -> None:
        dog = Dog("Rex", 3, "Lab")
        assert dog._energy == 100.0


class TestDogMethods:
    """Test Dog methods."""

    @pytest.fixture
    def rex(self) -> Dog:
        return Dog("Rex", 3, "Labrador")

    def test_speak_returns_woof(self, rex: Dog) -> None:
        assert rex.speak() == "Woof!"

    def test_move_describes_running(self, rex: Dog) -> None:
        assert "four" in rex.move() or "running" in rex.move()

    def test_eat_increases_energy(self, rex: Dog) -> None:
        rex._energy = 50.0
        rex.eat("bones")
        assert rex._energy > 50.0

    def test_eat_returns_message_with_food(self, rex: Dog) -> None:
        result = rex.eat("kibble")
        assert "kibble" in result
        assert rex.name in result

    def test_eat_includes_tail_wagging(self, rex: Dog) -> None:
        result = rex.eat("bones")
        assert "tail" in result.lower() or "wag" in result.lower()

    def test_fetch_returns_message(self, rex: Dog) -> None:
        result = rex.fetch()
        assert rex.name in result

    def test_fetch_custom_item(self, rex: Dog) -> None:
        result = rex.fetch("stick")
        assert "stick" in result

    def test_fetch_decreases_energy(self, rex: Dog) -> None:
        initial_energy = rex._energy
        rex.fetch()
        assert rex._energy < initial_energy

    def test_learn_trick(self, rex: Dog) -> None:
        rex.learn_trick("sit")
        assert rex.is_trained is True
        result = rex.perform_tricks()
        assert "sit" in result

    def test_learn_multiple_tricks(self, rex: Dog) -> None:
        rex.learn_trick("sit")
        rex.learn_trick("shake")
        rex.learn_trick("roll over")
        result = rex.perform_tricks()
        assert "sit" in result
        assert "shake" in result

    def test_no_tricks_message(self, rex: Dog) -> None:
        result = rex.perform_tricks()
        assert "doesn't know" in result or "no tricks" in result.lower()

    def test_sleep_increases_energy(self, rex: Dog) -> None:
        rex._energy = 30.0
        rex.sleep()
        assert rex._energy > 30.0

    def test_is_tired_when_low_energy(self, rex: Dog) -> None:
        rex._energy = 20.0
        assert rex.is_tired() is True

    def test_not_tired_when_full_energy(self, rex: Dog) -> None:
        assert rex.is_tired() is False

    def test_describe_contains_name(self, rex: Dog) -> None:
        description = rex.describe()
        assert rex.name in description

    def test_describe_uses_abstract_methods(self, rex: Dog) -> None:
        description = rex.describe()
        # describe() calls speak() and move() — both should appear in output
        assert rex.speak() in description
        assert rex.move() in description


class TestDogDunderMethods:
    """Test Dog dunder methods."""

    def test_str_contains_name(self) -> None:
        dog = Dog("Rex", 3, "Lab")
        assert "Rex" in str(dog)

    def test_repr_shows_class_name(self) -> None:
        dog = Dog("Rex", 3, "Lab")
        assert "Dog" in repr(dog)

    def test_eq_same_name_same_type(self) -> None:
        d1 = Dog("Rex", 3, "Lab")
        d2 = Dog("Rex", 5, "Poodle")
        assert d1 == d2

    def test_eq_different_name(self) -> None:
        d1 = Dog("Rex", 3, "Lab")
        d2 = Dog("Max", 3, "Lab")
        assert d1 != d2

    def test_eq_different_type(self) -> None:
        dog = Dog("Rex", 3, "Lab")
        cat = Cat("Rex", 3)
        # Same name but different types — depends on implementation
        result = dog.__eq__(cat)
        # Either False or NotImplemented is acceptable
        assert result is False or result is NotImplemented

    def test_lt_compares_by_age(self) -> None:
        young = Dog("Young", 1, "Lab")
        old = Dog("Old", 10, "Lab")
        assert young < old

    def test_sort_animals_by_age(self) -> None:
        animals = [
            Dog("Rex", 5, "Lab"),
            Cat("Whiskers", 2),
            Bird("Tweety", 8, "Canary"),
        ]
        sorted_animals = sorted(animals)
        assert sorted_animals[0].age == 2
        assert sorted_animals[-1].age == 8


class TestCat:
    """Tests for Cat class."""

    def test_cat_creation(self) -> None:
        cat = Cat("Whiskers", 5)
        assert cat.name == "Whiskers"
        assert cat.age == 5
        assert cat.is_indoor is True

    def test_outdoor_cat_has_wild_habitat(self) -> None:
        cat = Cat("Felix", 3, is_indoor=False)
        assert cat.habitat == "wild"

    def test_indoor_cat_has_domestic_habitat(self) -> None:
        cat = Cat("Felix", 3, is_indoor=True)
        assert cat.habitat == "domestic"

    def test_cat_speaks_meow(self) -> None:
        cat = Cat("Whiskers", 5)
        assert cat.speak() == "Meow!"

    def test_cat_purr(self) -> None:
        cat = Cat("Whiskers", 5)
        result = cat.purr()
        assert cat.name in result

    def test_cat_climb(self) -> None:
        cat = Cat("Whiskers", 5)
        result = cat.climb("bookshelf")
        assert "bookshelf" in result


class TestBird:
    """Tests for Bird class."""

    def test_bird_creation(self) -> None:
        bird = Bird("Tweety", 2, "Canary")
        assert bird.name == "Tweety"
        assert bird.species == "Canary"
        assert bird.can_fly is True

    def test_flightless_bird(self) -> None:
        penguin = Bird("Pingu", 3, "Penguin", can_fly=False)
        assert penguin.can_fly is False

    def test_flying_bird_can_fly(self) -> None:
        eagle = Bird("Eagle", 5, "Bald Eagle")
        result = eagle.fly()
        assert "fly" in result.lower() or "soar" in result.lower()

    def test_flightless_bird_cannot_fly(self) -> None:
        penguin = Bird("Pingu", 3, "Penguin", can_fly=False)
        result = penguin.fly()
        assert "cannot" in result or "can't" in result.lower()

    def test_bird_move_reflects_flying_ability(self) -> None:
        eagle = Bird("Eagle", 5, "Eagle")
        penguin = Bird("Pingu", 3, "Penguin", can_fly=False)
        assert eagle.move() != penguin.move()


class TestGuideDog:
    """Tests for GuideDog multilevel inheritance."""

    @pytest.fixture
    def buddy(self) -> GuideDog:
        return GuideDog("Buddy", 4, "Golden Retriever", "John")

    def test_guide_dog_creation(self, buddy: GuideDog) -> None:
        assert buddy.name == "Buddy"
        assert buddy.breed == "Golden Retriever"
        assert buddy.handler_name == "John"
        assert buddy.is_trained is True   # Guide dogs are always trained

    def test_guide_dog_is_trained(self, buddy: GuideDog) -> None:
        assert buddy.is_trained is True

    def test_guide_dog_speaks_quietly(self, buddy: GuideDog) -> None:
        # GuideDog overrides speak() to be quiet
        sound = buddy.speak()
        assert sound != "Woof!"   # Should not bark like regular dogs

    def test_guide_dog_can_fetch(self, buddy: GuideDog) -> None:
        # Inherited from Dog
        result = buddy.fetch()
        assert buddy.name in result

    def test_guide_dog_can_guide(self, buddy: GuideDog) -> None:
        result = buddy.guide()
        assert "John" in result
        assert buddy.name in result

    def test_guide_dog_isinstance_checks(self, buddy: GuideDog) -> None:
        from animal import Animal
        assert isinstance(buddy, GuideDog)
        assert isinstance(buddy, Dog)
        assert isinstance(buddy, Animal)

    def test_mro_for_guide_dog(self) -> None:
        mro_names = [cls.__name__ for cls in GuideDog.__mro__]
        assert "GuideDog" in mro_names
        assert "Dog" in mro_names
        assert "Animal" in mro_names
        # GuideDog before Dog before Animal in MRO
        assert mro_names.index("GuideDog") < mro_names.index("Dog")
        assert mro_names.index("Dog") < mro_names.index("Animal")


class TestPolymorphism:
    """Tests for polymorphic behavior."""

    @pytest.fixture
    def animals(self) -> list:
        return [
            Dog("Rex", 3, "Lab"),
            Cat("Whiskers", 5),
            Bird("Tweety", 2, "Canary"),
            GuideDog("Buddy", 4, "Golden", "John"),
        ]

    def test_all_animals_can_speak(self, animals: list) -> None:
        for animal in animals:
            result = animal.speak()
            assert isinstance(result, str)
            assert len(result) > 0

    def test_all_animals_can_move(self, animals: list) -> None:
        for animal in animals:
            result = animal.move()
            assert isinstance(result, str)

    def test_all_animals_can_eat(self, animals: list) -> None:
        for animal in animals:
            result = animal.eat("food")
            assert isinstance(result, str)

    def test_all_animals_can_describe(self, animals: list) -> None:
        for animal in animals:
            result = animal.describe()
            assert animal.name in result

    def test_different_speak_for_different_animals(self) -> None:
        sounds = {
            Dog("a", 1, "x").speak(),
            Cat("b", 2).speak(),
            Bird("c", 3, "y").speak(),
        }
        # All three should produce different sounds
        assert len(sounds) == 3

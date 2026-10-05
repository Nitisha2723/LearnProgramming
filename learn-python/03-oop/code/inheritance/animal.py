"""
Animal hierarchy — demonstrating inheritance in Python.

This module shows:
- Abstract Base Classes (ABC) with @abstractmethod
- super().__init__() to call parent constructors
- Method overriding
- __str__ at different levels of the hierarchy
- Polymorphism: treating objects through their base class
- isinstance() and type() usage

Compare with Java:
    Java abstract class → Python ABC
    Java @Override → Python just defines method with same name
    Java super.method() → Python super().method()
"""

from abc import ABC, abstractmethod
from typing import Optional


class Animal(ABC):
    """Abstract base class for all animals.

    In Java, you'd write: public abstract class Animal { ... }
    In Python, inherit from ABC and mark abstract methods with @abstractmethod.

    You cannot create an Animal() directly — you must create a specific subclass.
    """

    def __init__(self, name: str, age: int, habitat: str) -> None:
        """Initialize an animal.

        This runs when a subclass calls super().__init__().
        Just like Java's super() constructor call.

        Args:
            name: The animal's name
            age: Age in years
            habitat: Where the animal lives ("domestic", "wild", "zoo")
        """
        if not name.strip():
            raise ValueError("Animal name cannot be empty")
        if age < 0:
            raise ValueError(f"Age cannot be negative: {age}")

        # These are instance attributes — each animal has its own
        self.name: str = name
        self.age: int = age
        self.habitat: str = habitat
        self._energy: float = 100.0   # Protected — managed by methods below

    # -----------------------------------------------------------------------
    # Abstract methods — subclasses MUST implement these
    # -----------------------------------------------------------------------

    @abstractmethod
    def speak(self) -> str:
        """Produce the animal's characteristic sound.

        Every animal speaks differently — there's no default.
        Subclasses MUST provide an implementation.

        Compare Java: public abstract String speak();
        """
        ...

    @abstractmethod
    def move(self) -> str:
        """Describe how the animal moves.

        Different animals move differently — abstract.
        """
        ...

    # -----------------------------------------------------------------------
    # Concrete methods — shared by all animals
    # -----------------------------------------------------------------------

    def eat(self, food: str) -> str:
        """All animals eat, but they eat different things.

        This is a concrete method — a default implementation all subclasses
        inherit. They can override it if needed.
        """
        self._energy = min(100.0, self._energy + 10.0)
        return f"{self.name} eats {food} (energy: {self._energy:.0f}%)"

    def sleep(self) -> str:
        """All animals sleep similarly."""
        self._energy = min(100.0, self._energy + 30.0)
        return f"{self.name} sleeps peacefully (energy: {self._energy:.0f}%)"

    def is_tired(self) -> bool:
        """Check if the animal is tired (energy below 30%)."""
        return self._energy < 30.0

    def describe(self) -> str:
        """Describe this animal.

        This concrete method USES the abstract methods.
        It works for all subclasses because each implements speak() and move().
        This is the Template Method pattern!
        """
        return (
            f"{type(self).__name__} named {self.name}\n"
            f"  Age: {self.age} years | Habitat: {self.habitat}\n"
            f"  Sound: {self.speak()}\n"
            f"  Movement: {self.move()}"
        )

    # -----------------------------------------------------------------------
    # Dunder methods
    # -----------------------------------------------------------------------

    def __str__(self) -> str:
        """Human-readable representation."""
        return f"{type(self).__name__}({self.name}, {self.age}yr)"

    def __repr__(self) -> str:
        """Developer representation — shows class and key attributes."""
        return (
            f"{type(self).__name__}("
            f"name={self.name!r}, "
            f"age={self.age}, "
            f"habitat={self.habitat!r})"
        )

    def __eq__(self, other: object) -> bool:
        """Two animals are equal if they have the same name and are the same type."""
        if type(self) is not type(other):
            return NotImplemented
        return self.name == other.name  # type: ignore[union-attr]

    def __lt__(self, other: "Animal") -> bool:
        """Compare animals by age (allows sorting)."""
        if not isinstance(other, Animal):
            return NotImplemented
        return self.age < other.age


class Dog(Animal):
    """A domesticated dog.

    Inherits from Animal. Must implement all abstract methods.

    In Java: public class Dog extends Animal { ... }
    In Python: class Dog(Animal): ...
    """

    def __init__(
        self,
        name: str,
        age: int,
        breed: str,
        is_trained: bool = False
    ) -> None:
        """Initialize a Dog.

        MUST call super().__init__() to run Animal's initialization.
        Just like Java: super(name, age, "domestic");

        Args:
            name: Dog's name
            age: Age in years
            breed: Breed of dog
            is_trained: Whether the dog has basic obedience training
        """
        # Call parent __init__ — sets name, age, habitat, _energy
        super().__init__(name, age, habitat="domestic")

        # Dog-specific attributes
        self.breed: str = breed
        self.is_trained: bool = is_trained
        self._tricks: list = []

    # -----------------------------------------------------------------------
    # Implementing abstract methods — REQUIRED
    # -----------------------------------------------------------------------

    def speak(self) -> str:
        """Dog's sound — required by Animal.

        No @override decorator in Python — just define the method with the same name.
        Python knows it overrides the parent's speak() automatically.
        """
        return "Woof!"

    def move(self) -> str:
        """Dogs run on four legs."""
        return "running on four legs"

    # -----------------------------------------------------------------------
    # Overriding concrete methods
    # -----------------------------------------------------------------------

    def eat(self, food: str) -> str:
        """Dogs eat dog food — override parent with more specific behavior."""
        result = super().eat(food)   # Call parent's eat() first
        return f"{result} (tail wagging!)"

    # -----------------------------------------------------------------------
    # Dog-specific methods
    # -----------------------------------------------------------------------

    def fetch(self, item: str = "ball") -> str:
        """Dogs can fetch — this method doesn't exist on Animal."""
        self._energy -= 10.0
        return f"{self.name} fetches the {item}!"

    def learn_trick(self, trick: str) -> str:
        """Teach the dog a new trick."""
        self._tricks.append(trick)
        self.is_trained = True
        return f"{self.name} learned: {trick}!"

    def perform_tricks(self) -> str:
        """Show all tricks the dog knows."""
        if not self._tricks:
            return f"{self.name} doesn't know any tricks yet"
        tricks_str = ", ".join(self._tricks)
        return f"{self.name} performs: {tricks_str}"

    def __str__(self) -> str:
        """Override parent's __str__ with dog-specific info."""
        trained = " (trained)" if self.is_trained else ""
        return f"Dog({self.name}, {self.breed}{trained})"


class Cat(Animal):
    """A cat — could be domestic or wild."""

    def __init__(
        self,
        name: str,
        age: int,
        is_indoor: bool = True,
        favorite_spot: Optional[str] = None
    ) -> None:
        """Initialize a Cat.

        Args:
            name: Cat's name
            age: Age in years
            is_indoor: Whether the cat lives indoors
            favorite_spot: The cat's preferred resting place
        """
        habitat = "domestic" if is_indoor else "wild"
        super().__init__(name, age, habitat=habitat)

        self.is_indoor: bool = is_indoor
        self.favorite_spot: Optional[str] = favorite_spot or "sunny windowsill"

    def speak(self) -> str:
        return "Meow!"

    def move(self) -> str:
        return "padding silently on four paws"

    def purr(self) -> str:
        """Cats purr — dog-specific behavior doesn't exist here."""
        self._energy += 5.0
        return f"{self.name} purrs contentedly..."

    def climb(self, target: str) -> str:
        """Cats love to climb."""
        self._energy -= 5.0
        return f"{self.name} climbs onto the {target}"

    def __str__(self) -> str:
        location = "indoor" if self.is_indoor else "outdoor"
        return f"Cat({self.name}, {location})"


class Bird(Animal):
    """A bird — shows more specific type of movement."""

    def __init__(
        self,
        name: str,
        age: int,
        species: str,
        can_fly: bool = True
    ) -> None:
        """Initialize a Bird.

        Args:
            name: Bird's name
            age: Age in years
            species: Bird species (e.g., "Parrot", "Penguin", "Eagle")
            can_fly: Whether this bird can fly (penguins can't!)
        """
        super().__init__(name, age, habitat="wild")
        self.species: str = species
        self.can_fly: bool = can_fly

    def speak(self) -> str:
        return "Tweet!"

    def move(self) -> str:
        if self.can_fly:
            return "flying with wings"
        else:
            return "waddling on two legs"

    def fly(self) -> str:
        """Attempt to fly."""
        if not self.can_fly:
            return f"{self.name} cannot fly (it's a {self.species})"
        self._energy -= 15.0
        return f"{self.name} soars through the sky!"

    def __str__(self) -> str:
        can_fly_str = "flying" if self.can_fly else "flightless"
        return f"Bird({self.name}, {self.species}, {can_fly_str})"


class GuideDog(Dog):
    """A trained guide dog — demonstrates multilevel inheritance.

    GuideDog → Dog → Animal → ABC → object

    Three levels of inheritance. Each level adds specific behavior.
    """

    def __init__(self, name: str, age: int, breed: str, handler_name: str) -> None:
        """Initialize a GuideDog.

        Calls Dog.__init__, which calls Animal.__init__.
        The super() chain handles this automatically.
        """
        # super() calls Dog.__init__ (not Animal.__init__ directly)
        super().__init__(name, age, breed, is_trained=True)
        self.handler_name: str = handler_name

    def guide(self) -> str:
        """Guide dog-specific behavior."""
        return f"{self.name} guides {self.handler_name} safely across the street"

    def speak(self) -> str:
        """Guide dogs are quiet — override Dog's speak()."""
        return "..."   # Guide dogs don't bark often

    def __str__(self) -> str:
        return f"GuideDog({self.name}, handler={self.handler_name})"

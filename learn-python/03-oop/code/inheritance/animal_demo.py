"""
Animal demo — showing inheritance and polymorphism in action.

Run this file:
    python code/inheritance/animal_demo.py
"""

from animal import Animal, Dog, Cat, Bird, GuideDog


def separator(title: str = "") -> None:
    if title:
        print(f"\n{'─' * 20} {title} {'─' * 20}")
    else:
        print(f"\n{'─' * 60}")


def demo_creating_animals() -> None:
    """Show that you can't instantiate Animal but CAN instantiate subclasses."""
    separator("Creating Animals")

    # This would fail — Animal is abstract
    try:
        generic = Animal("Generic", 5, "wild")  # type: ignore
    except TypeError as e:
        print(f"Cannot create Animal directly: {e}")

    # These work — they implement all abstract methods
    rex = Dog("Rex", 3, "Labrador")
    whiskers = Cat("Whiskers", 5)
    tweety = Bird("Tweety", 2, "Canary")
    guide = GuideDog("Buddy", 4, "Golden Retriever", "John")

    print(f"\nCreated animals:")
    print(f"  {rex}")
    print(f"  {whiskers}")
    print(f"  {tweety}")
    print(f"  {guide}")


def demo_polymorphism() -> None:
    """Demonstrate polymorphism — same code works with different types."""
    separator("Polymorphism")

    # A list of different animals — all are Animal subclasses
    animals: list[Animal] = [
        Dog("Rex", 3, "Labrador"),
        Cat("Whiskers", 5),
        Bird("Tweety", 2, "Canary"),
        Bird("Penny", 3, "Penguin", can_fly=False),
        GuideDog("Buddy", 4, "Golden Retriever", "John"),
    ]

    print("Making all animals speak (polymorphism):")
    for animal in animals:
        # speak() is called on different types — each responds differently
        # This is polymorphism!
        print(f"  {animal.name}: {animal.speak()}")

    print("\nDescribing all animals:")
    for animal in animals:
        print(animal.describe())
        print()


def demo_isinstance() -> None:
    """Show isinstance() with inheritance hierarchy."""
    separator("isinstance() Checks")

    rex = Dog("Rex", 3, "Labrador")
    guide = GuideDog("Buddy", 4, "Golden Retriever", "John")
    whiskers = Cat("Whiskers", 5)

    # isinstance() respects the full hierarchy
    print(f"isinstance(rex, Dog):     {isinstance(rex, Dog)}")       # True
    print(f"isinstance(rex, Animal):  {isinstance(rex, Animal)}")    # True — Dog IS-A Animal
    print(f"isinstance(rex, Cat):     {isinstance(rex, Cat)}")       # False

    print()
    # GuideDog IS-A Dog IS-A Animal
    print(f"isinstance(guide, GuideDog): {isinstance(guide, GuideDog)}")  # True
    print(f"isinstance(guide, Dog):      {isinstance(guide, Dog)}")       # True
    print(f"isinstance(guide, Animal):   {isinstance(guide, Animal)}")    # True
    print(f"isinstance(guide, Cat):      {isinstance(guide, Cat)}")       # False

    print()
    # type() gives the EXACT type, ignoring inheritance
    print(f"type(rex) == Dog:    {type(rex) == Dog}")       # True
    print(f"type(rex) == Animal: {type(rex) == Animal}")    # False! — rex is a Dog, not Animal
    print(f"type(guide) == Dog:  {type(guide) == Dog}")     # False! — guide is a GuideDog


def demo_duck_typing() -> None:
    """Show duck typing — works without any inheritance from Animal."""
    separator("Duck Typing")

    class AlienCreature:
        """An alien — NOT inheriting from Animal at all."""

        def __init__(self, name: str):
            self.name = name

        def speak(self) -> str:
            return "*telepathic signals*"

        def move(self) -> str:
            return "floating through space"

        def eat(self, food: str) -> str:
            return f"{self.name} absorbs {food}"

    # Duck typing: if it has speak() and eat(), we can call those methods
    alien = AlienCreature("Zorg")

    def make_any_creature_speak(creature) -> None:
        """Works with ANYTHING that has a speak() method — no type declaration needed."""
        print(f"{creature.name} says: {creature.speak()}")

    rex = Dog("Rex", 3, "Labrador")
    make_any_creature_speak(rex)        # Works — Dog has speak()
    make_any_creature_speak(alien)      # Works — AlienCreature has speak()

    print()
    # But AlienCreature is not an Animal:
    print(f"isinstance(alien, Animal): {isinstance(alien, Animal)}")   # False
    print(f"Has speak(): {hasattr(alien, 'speak')}")                   # True


def demo_super_chain() -> None:
    """Show how super() chains through the MRO."""
    separator("super() Chain")

    guide = GuideDog("Buddy", 4, "Golden Retriever", "John")

    # guide is a GuideDog
    # GuideDog.__init__ calls super().__init__ → Dog.__init__
    # Dog.__init__ calls super().__init__ → Animal.__init__
    # All initializations happen in the right order
    print(f"Name (from Animal.__init__): {guide.name}")
    print(f"Age (from Animal.__init__): {guide.age}")
    print(f"Breed (from Dog.__init__): {guide.breed}")
    print(f"Is trained (from Dog.__init__): {guide.is_trained}")
    print(f"Handler (from GuideDog.__init__): {guide.handler_name}")

    print()
    # GuideDog overrides speak() from Dog
    print(f"Guide dog speaks: '{guide.speak()}'")  # "..." — GuideDog's speak()
    print(f"Can still fetch: {guide.fetch()}")     # Inherited from Dog


def demo_sorting() -> None:
    """Show sorting with __lt__."""
    separator("Sorting Animals by Age")

    animals = [
        Dog("Rex", 5, "Labrador"),
        Cat("Whiskers", 2),
        Bird("Tweety", 8, "Canary"),
        Dog("Max", 1, "Poodle"),
        Cat("Luna", 3),
    ]

    # __lt__ is defined on Animal — compares by age
    sorted_animals = sorted(animals)
    print("Animals sorted by age:")
    for animal in sorted_animals:
        print(f"  {animal} — {animal.age} years")


def demo_describe_template_method() -> None:
    """Show the Template Method pattern — describe() uses abstract methods."""
    separator("Template Method Pattern")

    animals = [
        Dog("Rex", 3, "Labrador"),
        Bird("Eagle", 5, "Bald Eagle"),
    ]

    # describe() is concrete in Animal, but uses speak() and move()
    # which are abstract and implemented differently per subclass
    # This is the Template Method design pattern!
    for animal in animals:
        print(animal.describe())
        print()


if __name__ == "__main__":
    print("Animal Hierarchy Demo — Python Inheritance")
    print("=" * 60)

    demo_creating_animals()
    demo_polymorphism()
    demo_isinstance()
    demo_duck_typing()
    demo_super_chain()
    demo_sorting()
    demo_describe_template_method()

    print("\n" + "=" * 60)
    print("Demo complete!")

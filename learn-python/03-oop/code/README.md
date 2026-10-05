# Code Examples

This directory contains runnable Python code demonstrating OOP concepts.

## Structure

```
code/
├── basics/
│   ├── bank_account.py      — BankAccount class demonstrating encapsulation
│   └── bank_account_demo.py — Demo script showing all BankAccount features
├── inheritance/
│   ├── animal.py            — Animal/Dog/Cat/Bird hierarchy
│   └── animal_demo.py       — Demo script for the animal hierarchy
└── protocols/
    ├── shapes.py            — Shape Protocol and shape classes
    └── shapes_demo.py       — Demo script for Protocol-based polymorphism
```

## Running the Examples

From the `03-oop/` directory:

```bash
# Run from the appropriate directory to keep imports working
cd code/basics && python bank_account_demo.py
cd code/inheritance && python animal_demo.py
cd code/protocols && python shapes_demo.py
```

## Key Concepts Per Example

### basics/bank_account.py
- `_underscore` convention for "protected" attributes
- `@property` for read-only access (no setter)
- `@property` + `@setter` for validated read-write access
- `__str__` vs `__repr__`
- `__eq__` and `__hash__` for equality and hashing
- `__bool__` and `__lt__` for boolean value and sorting
- Class attributes vs instance attributes
- `@classmethod` for class-level operations

### inheritance/animal.py
- `ABC` and `@abstractmethod` for abstract classes
- `super().__init__()` for calling parent constructor
- Method overriding (no `@override` needed in Python)
- Multi-level inheritance (`GuideDog → Dog → Animal`)
- Template Method pattern (concrete method using abstract methods)
- `isinstance()` respecting the full hierarchy

### protocols/shapes.py
- `typing.Protocol` for structural typing
- `@runtime_checkable` for `isinstance()` at runtime
- Classes satisfying Protocol without inheriting from it
- Multiple Protocols on one class
- Functions accepting Protocol-typed parameters
- Duck typing with formal type hints

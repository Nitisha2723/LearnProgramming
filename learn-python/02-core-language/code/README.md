# Code Demos

This directory contains runnable Python files that demonstrate the concepts from the theory files.
Each file is heavily commented — read along as you run them.

## Files

| File | Concepts Demonstrated |
|------|----------------------|
| `control_flow_demo.py` | if/elif/else, truthy/falsy, ternary, match, while, for, range, comprehensions, break/continue/pass |
| `functions_demo.py` | def, return, positional/keyword args, defaults, *args/**kwargs, docstrings, first-class functions, lambda, pure functions, closures, type hints |
| `lists_demo.py` | List creation, indexing, slicing, all list methods, comprehensions, nested lists, tuples, named tuples, filter/map/reduce patterns |
| `strings_demo.py` | String immutability, all string methods, f-strings with format specs, slicing, raw strings, encoding, formatting systems |

## How to Run

```bash
# From the 02-core-language directory:
python code/control_flow_demo.py
python code/functions_demo.py
python code/lists_demo.py
python code/strings_demo.py
```

## Learning Strategy

1. Open the demo file in your editor
2. Read a section (marked with `# SECTION N:`)
3. Run the file and match the output to the code
4. Experiment: change values, break things, observe what happens
5. Try to predict output before running — that's the best test of understanding

## Tip: Use the REPL

Many of these examples are great for exploring interactively:

```bash
python3
>>> fruits = ["apple", "banana", "cherry"]
>>> fruits[::-1]
['cherry', 'banana', 'apple']
>>> fruits.sort(key=lambda x: x[-1])
>>> fruits
['banana', 'apple', 'cherry']
```

The REPL (Read-Eval-Print Loop) gives immediate feedback — use it constantly.

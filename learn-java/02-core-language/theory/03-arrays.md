# Arrays

An array is a **fixed-size container that holds multiple values of the same type**, stored side by side in memory.

Think of an array like an egg carton: it holds a fixed number of eggs (elements), each in a numbered slot (index). You can put an egg in slot 3 or take the egg from slot 7, but the carton always has the same number of slots.

---

## Why Arrays?

Without arrays, storing 100 student scores would require 100 variables:

```java
int score1 = 85;
int score2 = 92;
int score3 = 78;
// ... 97 more lines
```

With an array:

```java
int[] scores = new int[100];
```

---

## Declaring and Initializing Arrays

There are three ways to create an array in Java.

### Method 1: Declare, then allocate

```java
int[] scores;               // declare (no memory yet)
scores = new int[5];        // allocate 5 slots, all initialized to 0
```

### Method 2: Declare and allocate together

```java
int[] scores = new int[5];  // allocate 5 slots, all initialized to 0
```

Default values after allocation:
- `int[]`, `double[]`, etc.: `0`
- `boolean[]`: `false`
- `String[]`, any object `[]`: `null`

### Method 3: Array literal (declare, allocate, and initialize)

```java
int[] scores = {85, 92, 78, 90, 88};  // 5 elements, all specified
```

Or with `new`:
```java
int[] scores = new int[]{85, 92, 78, 90, 88};
```

---

## Accessing Elements by Index

Arrays use **zero-based indexing** — the first element is at index 0, not 1.

```java
int[] scores = {85, 92, 78, 90, 88};
//               [0] [1] [2] [3] [4]

System.out.println(scores[0]); // 85 — first element
System.out.println(scores[4]); // 88 — last element
System.out.println(scores[2]); // 78 — middle element
```

Why zero-based? It's rooted in how memory addressing works — the index is actually an *offset* from the start of the array. The first element is 0 positions from the start.

### Modifying elements

```java
scores[2] = 95;  // change the third element from 78 to 95
```

---

## Array Length

Every array has a `.length` property (not a method — no parentheses):

```java
int[] scores = {85, 92, 78, 90, 88};
System.out.println(scores.length); // 5
```

The last valid index is always `length - 1`. This is critical for loops.

---

## Common Array Operations

### Iterating with a for loop

```java
int[] scores = {85, 92, 78, 90, 88};

for (int i = 0; i < scores.length; i++) {
    System.out.println("Score " + i + ": " + scores[i]);
}
```

### Iterating with for-each

```java
for (int score : scores) {
    System.out.println(score);
}
```

Use for-each when you don't need the index.

### Sum and Average

```java
int[] scores = {85, 92, 78, 90, 88};
int sum = 0;

for (int score : scores) {
    sum += score;
}

double average = (double) sum / scores.length;
System.out.println("Sum: " + sum);          // 433
System.out.println("Average: " + average);  // 86.6
```

Note the cast to `double` before division. Without it, `433 / 5` would give `86` (integer division).

### Finding Min and Max

```java
int[] scores = {85, 92, 78, 90, 88};
int max = scores[0]; // start with the first element
int min = scores[0];

for (int score : scores) {
    if (score > max) max = score;
    if (score < min) min = score;
}

System.out.println("Max: " + max); // 92
System.out.println("Min: " + min); // 78
```

### Searching (Linear Search)

```java
public static int findIndex(int[] array, int target) {
    for (int i = 0; i < array.length; i++) {
        if (array[i] == target) {
            return i; // found it
        }
    }
    return -1; // convention: -1 means "not found"
}
```

---

## Multi-Dimensional Arrays

A 2D array is an array of arrays — like a grid or matrix.

### Declaration and initialization

```java
// 3 rows, 4 columns — a 3×4 grid
int[][] matrix = new int[3][4];

// Or with values:
int[][] grid = {
    {1, 2, 3},
    {4, 5, 6},
    {7, 8, 9}
};
```

### Accessing elements

```java
System.out.println(grid[0][0]); // 1  (row 0, column 0)
System.out.println(grid[1][2]); // 6  (row 1, column 2)
System.out.println(grid[2][1]); // 8  (row 2, column 1)
```

### Iterating a 2D array

```java
for (int row = 0; row < grid.length; row++) {
    for (int col = 0; col < grid[row].length; col++) {
        System.out.print(grid[row][col] + " ");
    }
    System.out.println(); // new line after each row
}
// Output:
// 1 2 3
// 4 5 6
// 7 8 9
```

---

## Common Pitfall: ArrayIndexOutOfBoundsException

This is one of the most common runtime errors in Java. It happens when you access an index that doesn't exist.

```java
int[] numbers = {10, 20, 30};  // valid indices: 0, 1, 2

System.out.println(numbers[3]); // CRASH! Index 3 doesn't exist.
// java.lang.ArrayIndexOutOfBoundsException: Index 3 out of bounds for length 3
```

Common causes:
- Off-by-one in loop: `i <= array.length` should be `i < array.length`
- Forgetting zero-based indexing: accessing `array[length]` instead of `array[length - 1]`
- Hardcoding an index without checking the array size

---

## The Arrays Utility Class

`java.util.Arrays` provides helpful methods for working with arrays.

```java
import java.util.Arrays;

int[] numbers = {5, 2, 8, 1, 9, 3};

// Sort in ascending order
Arrays.sort(numbers);
System.out.println(Arrays.toString(numbers)); // [1, 2, 3, 5, 8, 9]

// Binary search (array must be sorted first)
int index = Arrays.binarySearch(numbers, 5);
System.out.println(index); // 3

// Fill array with a value
int[] zeros = new int[5];
Arrays.fill(zeros, 7);
System.out.println(Arrays.toString(zeros)); // [7, 7, 7, 7, 7]

// Copy array
int[] copy = Arrays.copyOf(numbers, numbers.length);
int[] partial = Arrays.copyOfRange(numbers, 1, 4); // indices 1, 2, 3

// Check equality
System.out.println(Arrays.equals(numbers, copy)); // true

// Pretty-print (without this, you'd get something like [I@1a2b3c)
System.out.println(Arrays.toString(numbers)); // [1, 2, 3, 5, 8, 9]
```

**Important:** Never use `==` to compare two arrays. Use `Arrays.equals()`.

```java
int[] a = {1, 2, 3};
int[] b = {1, 2, 3};

System.out.println(a == b);             // false — compares memory addresses
System.out.println(Arrays.equals(a, b)); // true  — compares contents
```

---

## Limitations of Arrays (and What Comes Next)

Arrays have a fundamental limitation: **their size is fixed at creation time**. You can't add or remove elements. If you need a resizable container, you'll use `ArrayList` from the Collections framework — covered in Module 04.

```java
int[] scores = new int[5];
// Need to store a 6th score? You can't add it directly.
// You'd have to create a new, larger array and copy everything.
```

This is exactly the problem `ArrayList` solves.

---

## Summary

| Operation | Code |
|-----------|------|
| Declare + allocate | `int[] arr = new int[10];` |
| Declare + initialize | `int[] arr = {1, 2, 3};` |
| Access element | `arr[i]` |
| Length | `arr.length` |
| Sort | `Arrays.sort(arr)` |
| Print | `Arrays.toString(arr)` |
| 2D element | `matrix[row][col]` |

→ Continue to `04-strings-in-depth.md`

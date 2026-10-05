# Module 04 Mini-Project: Inventory Management System

## Overview

A command-line inventory management system demonstrating real-world usage of Java's core data structures.

## Data Structures Used

| Structure | Purpose | Why This Structure? |
|-----------|---------|---------------------|
| `HashMap<String, Product>` | Product catalog by ID | O(1) lookup by product ID |
| `HashMap<String, List<Product>>` | Products grouped by category | Fast category filtering |
| `PriorityQueue<Product>` | Low-stock alerts | Auto-sorted by stock level; most critical first |
| `LinkedList<Transaction>` | Transaction history | O(1) append; efficient descending iteration |
| `TreeMap<String, Product>` | Products sorted by name | Always-sorted view for catalog display |

## Features

- Add products to the catalog
- Process sales with automatic stock deduction
- Restock products with automatic alert resolution
- Process customer returns
- Update product prices
- Low-stock alert system (threshold = 10 units)
- Revenue reporting by product
- Transaction history (most recent first)

## How to Run

```bash
cd 04-data-structures/mini-project
javac InventorySystem.java
java InventorySystem
```

## Key Design Decisions

1. **Why HashMap for the catalog?** Products are accessed most frequently by ID — O(1) lookup is critical for a system that may have thousands of products.

2. **Why PriorityQueue for low-stock alerts?** When the warehouse manager checks for low-stock items, they want the most critical ones first. PriorityQueue automatically maintains the min-stock ordering.

3. **Why LinkedList for transaction history?** Transactions are always appended to the end (addLast) and read from the most recent (descendingIterator). LinkedList provides O(1) for both. We never need random access by index.

4. **Why TreeMap for the sorted catalog view?** The product catalog display is always alphabetical. TreeMap maintains this order automatically — no explicit sorting step needed.

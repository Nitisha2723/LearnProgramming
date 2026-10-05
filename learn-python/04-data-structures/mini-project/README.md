# Mini-Project: Inventory Management System

## Overview

Build a complete inventory management system for a small retail business.
This project uses every major data structure from this module.

## What You'll Build

A command-line inventory system that can:

1. **Product catalog** — Add, update, and look up products by SKU
2. **Stock tracking** — Track quantities, flag low stock
3. **Category management** — Organize products into categories using sets
4. **Sales recording** — Record sales and update stock automatically
5. **Analytics** — Best sellers, category statistics, low stock alerts

## Data Structures Used

| Feature             | Structure       | Why                                   |
|---------------------|-----------------|---------------------------------------|
| Product lookup      | `dict`          | O(1) lookup by SKU                    |
| Categories          | `dict[str, set]`| No duplicate SKUs per category        |
| Sales history       | `list`          | Ordered, append-only                  |
| Sales counts        | `Counter`       | Built-in frequency counting           |
| Low stock items     | `set`           | Fast membership test                  |
| Product records     | `namedtuple`    | Lightweight, immutable product data   |
| Category grouping   | `defaultdict`   | Auto-create category lists            |

## Running

```bash
python mini-project/inventory_system.py
```

## Extending This Project

Ideas for extending once the core is working:

- Add persistence with `json` file storage
- Add CSV import/export
- Add a reorder level system that auto-generates purchase orders
- Add expiry date tracking for perishables

# Project 04: Web Scraper Pipeline

A data pipeline demonstrating Python's data-processing strengths:
generators for memory efficiency, HTML parsing without external deps,
asyncio for concurrent scraping, and export to CSV/JSON.

## Architecture

```
src/
├── scraper/         # HTTP fetching (sync + async)
├── parser/          # HTML parsing
├── pipeline/        # Generator-based pipeline transforms
├── exporter/        # CSV and JSON export
└── main.py          # Demo entry point
```

## Features

- Sync scraper using urllib (zero external deps)
- Async scraper using asyncio + urllib
- HTML parsing with html.parser
- Generator pipeline for memory-efficient transforms
- Filter, map, aggregate steps
- Export results to CSV and JSON

## Running

```bash
python src/main.py
```

## Running Tests

```bash
pytest tests/ -v
```

## Concepts Demonstrated

- `urllib.request` for HTTP (no external deps)
- `html.parser.HTMLParser` for parsing
- Generator functions and `yield`
- `asyncio` and `asyncio.gather`
- `csv` and `json` modules
- `unittest.mock` for HTTP mocking in tests

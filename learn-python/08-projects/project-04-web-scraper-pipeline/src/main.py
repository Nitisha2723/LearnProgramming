"""Demo entry point for the Web Scraper Pipeline."""

import sys
import os
sys.path.insert(0, os.path.dirname(__file__))

from scraper.sync_scraper import SyncScraper
from parser.html_parser import ArticleParser
from pipeline.pipeline import Pipeline
from exporter.csv_exporter import CsvExporter
from exporter.json_exporter import JsonExporter

# Demo with static HTML instead of live network calls
SAMPLE_HTML = """
<!DOCTYPE html>
<html>
<head><title>Python News</title></head>
<body>
  <h1>Latest Python Updates</h1>
  <h2>Python 3.13 Released</h2>
  <p>Python 3.13 brings significant performance improvements and new features
  for developers working on modern applications.</p>
  <h2>PEP 750 Accepted</h2>
  <p>Template strings (t-strings) have been accepted and will appear in a
  future Python release, enabling safe string interpolation.</p>
  <a href="https://python.org">Python.org</a>
  <a href="https://peps.python.org">PEPs</a>
</body>
</html>
"""


def main() -> None:
    parser = ArticleParser()
    article = parser.parse(SAMPLE_HTML, url="https://example.com/python-news")

    # Build a record dict for the pipeline
    record = {
        "url": article.url,
        "title": article.title,
        "word_count": article.word_count,
        "headings": " | ".join(article.headings),
        "links": len(article.links),
    }

    # Pipeline demo
    records = [record, {**record, "word_count": 5, "title": "Short"}]
    result = (
        Pipeline(records)
        .filter(lambda r: r["word_count"] > 10)
        .map(lambda r: {**r, "category": "article"})
        .to_list()
    )

    print("Pipeline result:")
    for r in result:
        print(f"  {r}")

    # Export
    csv_exporter = CsvExporter()
    csv_str = csv_exporter.to_string(result, fields=["url", "title", "word_count", "category"])
    print("\n--- CSV Export ---")
    print(csv_str)

    json_exporter = JsonExporter(indent=2)
    print("--- JSON Export ---")
    print(json_exporter.to_string(result))


if __name__ == "__main__":
    main()

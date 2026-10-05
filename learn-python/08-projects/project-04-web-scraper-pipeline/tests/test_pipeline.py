"""Tests for the generator pipeline."""

import pytest
from src.pipeline.pipeline import Pipeline


class TestPipelineFilter:
    def test_filter_keeps_matching(self):
        result = Pipeline([1, 2, 3, 4, 5]).filter(lambda x: x % 2 == 0).to_list()
        assert result == [2, 4]

    def test_filter_all_removed(self):
        result = Pipeline([1, 3, 5]).filter(lambda x: x % 2 == 0).to_list()
        assert result == []


class TestPipelineMap:
    def test_map_doubles(self):
        result = Pipeline([1, 2, 3]).map(lambda x: x * 2).to_list()
        assert result == [2, 4, 6]


class TestPipelineLimit:
    def test_limit_caps_items(self):
        result = Pipeline(range(100)).limit(5).to_list()
        assert result == [0, 1, 2, 3, 4]


class TestPipelineChaining:
    def test_filter_then_map(self):
        result = (
            Pipeline(range(10))
            .filter(lambda x: x % 2 == 0)
            .map(lambda x: x ** 2)
            .to_list()
        )
        assert result == [0, 4, 16, 36, 64]

    def test_batch(self):
        result = Pipeline(range(5)).batch(2).to_list()
        assert result == [[0, 1], [2, 3], [4]]

    def test_count(self):
        count = Pipeline(range(10)).filter(lambda x: x > 4).count()
        assert count == 5


class TestHTMLParser:
    SAMPLE_HTML = """
    <html>
    <head><title>Test Page</title></head>
    <body>
        <h1>Main Heading</h1>
        <p>This is a paragraph with enough words to pass the threshold check.</p>
        <a href="https://example.com">Link</a>
    </body>
    </html>
    """

    def test_parses_title(self):
        from src.parser.html_parser import ArticleParser
        article = ArticleParser().parse(self.SAMPLE_HTML, url="https://test.com")
        assert article.title == "Test Page"

    def test_parses_headings(self):
        from src.parser.html_parser import ArticleParser
        article = ArticleParser().parse(self.SAMPLE_HTML)
        assert "Main Heading" in article.headings

    def test_parses_links(self):
        from src.parser.html_parser import ArticleParser
        article = ArticleParser().parse(self.SAMPLE_HTML)
        assert "https://example.com" in article.links


class TestExporters:
    RECORDS = [{"url": "https://a.com", "title": "A", "word_count": 100}]

    def test_csv_export_contains_header(self):
        from src.exporter.csv_exporter import CsvExporter
        csv_str = CsvExporter().to_string(self.RECORDS)
        assert "url" in csv_str
        assert "https://a.com" in csv_str

    def test_json_export_is_valid_json(self):
        import json
        from src.exporter.json_exporter import JsonExporter
        json_str = JsonExporter().to_string(self.RECORDS)
        data = json.loads(json_str)
        assert data[0]["title"] == "A"

    def test_csv_empty_returns_empty_string(self):
        from src.exporter.csv_exporter import CsvExporter
        assert CsvExporter().to_string([]) == ""

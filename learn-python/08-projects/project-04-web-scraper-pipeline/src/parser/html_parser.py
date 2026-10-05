"""HTML parser using the stdlib html.parser module."""

from dataclasses import dataclass, field
from html.parser import HTMLParser
from typing import List, Optional
from urllib.parse import urljoin


@dataclass
class Article:
    """Extracted article data."""
    url: str
    title: str = ""
    links: List[str] = field(default_factory=list)
    headings: List[str] = field(default_factory=list)
    text_snippets: List[str] = field(default_factory=list)
    word_count: int = 0


class _InternalParser(HTMLParser):
    """Internal stateful parser."""

    def __init__(self, base_url: str = "") -> None:
        super().__init__()
        self.base_url = base_url
        self.title = ""
        self.links: List[str] = []
        self.headings: List[str] = []
        self.text_snippets: List[str] = []

        self._in_title = False
        self._in_heading = False
        self._in_body_text = False
        self._skip_tags = {"script", "style", "nav", "footer"}
        self._skip_depth = 0
        self._current_text: List[str] = []

    def handle_starttag(self, tag: str, attrs):
        attr_dict = dict(attrs)

        if tag in self._skip_tags:
            self._skip_depth += 1
            return

        if tag == "title":
            self._in_title = True

        elif tag in ("h1", "h2", "h3"):
            self._in_heading = True
            self._current_text = []

        elif tag == "a" and "href" in attr_dict:
            href = attr_dict["href"]
            if href.startswith("http"):
                self.links.append(href)
            elif href.startswith("/") and self.base_url:
                self.links.append(urljoin(self.base_url, href))

        elif tag == "p":
            self._in_body_text = True
            self._current_text = []

    def handle_endtag(self, tag: str):
        if tag in self._skip_tags:
            self._skip_depth = max(0, self._skip_depth - 1)
            return

        if tag == "title":
            self._in_title = False

        elif tag in ("h1", "h2", "h3"):
            text = " ".join(self._current_text).strip()
            if text:
                self.headings.append(text)
            self._in_heading = False
            self._current_text = []

        elif tag == "p":
            text = " ".join(self._current_text).strip()
            if len(text) > 20:  # skip very short fragments
                self.text_snippets.append(text)
            self._in_body_text = False
            self._current_text = []

    def handle_data(self, data: str):
        if self._skip_depth > 0:
            return
        text = data.strip()
        if not text:
            return
        if self._in_title:
            self.title += text
        elif self._in_heading or self._in_body_text:
            self._current_text.append(text)


class ArticleParser:
    """
    Parse an HTML page into an Article.

    Usage:
        parser = ArticleParser()
        article = parser.parse(html_content, url="https://example.com")
    """

    def parse(self, html: str, url: str = "") -> Article:
        """Parse HTML content and return an Article."""
        internal = _InternalParser(base_url=url)
        internal.feed(html)

        all_text = " ".join(internal.text_snippets)
        word_count = len(all_text.split())

        return Article(
            url=url,
            title=internal.title,
            links=internal.links[:50],          # cap at 50
            headings=internal.headings[:20],     # cap at 20
            text_snippets=internal.text_snippets[:10],  # cap at 10
            word_count=word_count,
        )

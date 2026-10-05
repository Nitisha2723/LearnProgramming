"""Synchronous HTTP scraper using only urllib (no external dependencies)."""

import urllib.request
import urllib.error
from dataclasses import dataclass
from typing import Optional


@dataclass
class ScrapeResult:
    """Holds the result of an HTTP fetch."""
    url: str
    status_code: int
    content: str
    error: Optional[str] = None

    @property
    def success(self) -> bool:
        return self.error is None and self.status_code == 200


class SyncScraper:
    """
    Synchronous web scraper with configurable timeout and user-agent.

    Usage:
        scraper = SyncScraper(timeout=10)
        result = scraper.fetch("https://example.com")
        if result.success:
            print(result.content[:500])
    """

    DEFAULT_USER_AGENT = (
        "Mozilla/5.0 (compatible; PythonScraper/1.0)"
    )

    def __init__(self, timeout: int = 10, user_agent: Optional[str] = None) -> None:
        self.timeout = timeout
        self.user_agent = user_agent or self.DEFAULT_USER_AGENT

    def fetch(self, url: str) -> ScrapeResult:
        """Fetch a URL and return a ScrapeResult."""
        request = urllib.request.Request(url, headers={"User-Agent": self.user_agent})
        try:
            with urllib.request.urlopen(request, timeout=self.timeout) as response:
                content = response.read().decode("utf-8", errors="replace")
                return ScrapeResult(url=url, status_code=response.status, content=content)
        except urllib.error.HTTPError as e:
            return ScrapeResult(url=url, status_code=e.code, content="", error=str(e))
        except urllib.error.URLError as e:
            return ScrapeResult(url=url, status_code=0, content="", error=str(e.reason))
        except Exception as e:
            return ScrapeResult(url=url, status_code=0, content="", error=str(e))

    def fetch_many(self, urls: list[str]) -> list[ScrapeResult]:
        """Fetch multiple URLs sequentially."""
        return [self.fetch(url) for url in urls]

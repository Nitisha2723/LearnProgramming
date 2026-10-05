"""Asynchronous scraper using asyncio + urllib (compatible with stdlib)."""

import asyncio
import urllib.request
import urllib.error
from typing import List

from .sync_scraper import ScrapeResult


class AsyncScraper:
    """
    Async scraper that runs multiple fetches concurrently using asyncio.
    Wraps the sync urllib call in a thread pool for true non-blocking IO.

    Usage:
        scraper = AsyncScraper(max_concurrent=5)
        results = asyncio.run(scraper.fetch_many(urls))
    """

    def __init__(self, max_concurrent: int = 10, timeout: int = 10) -> None:
        self.max_concurrent = max_concurrent
        self.timeout = timeout
        self._semaphore: asyncio.Semaphore = None  # type: ignore

    async def fetch(self, url: str) -> ScrapeResult:
        """Fetch a single URL asynchronously."""
        loop = asyncio.get_event_loop()
        async with self._semaphore:
            try:
                result = await loop.run_in_executor(
                    None, self._blocking_fetch, url
                )
                return result
            except Exception as e:
                return ScrapeResult(url=url, status_code=0, content="", error=str(e))

    def _blocking_fetch(self, url: str) -> ScrapeResult:
        req = urllib.request.Request(
            url,
            headers={"User-Agent": "PythonAsyncScraper/1.0"}
        )
        try:
            with urllib.request.urlopen(req, timeout=self.timeout) as resp:
                content = resp.read().decode("utf-8", errors="replace")
                return ScrapeResult(url=url, status_code=resp.status, content=content)
        except urllib.error.HTTPError as e:
            return ScrapeResult(url=url, status_code=e.code, content="", error=str(e))
        except Exception as e:
            return ScrapeResult(url=url, status_code=0, content="", error=str(e))

    async def fetch_many(self, urls: List[str]) -> List[ScrapeResult]:
        """Fetch multiple URLs concurrently."""
        self._semaphore = asyncio.Semaphore(self.max_concurrent)
        tasks = [self.fetch(url) for url in urls]
        return await asyncio.gather(*tasks)

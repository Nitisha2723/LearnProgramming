"""
Exercise 03: Browser History — SOLUTION
========================================
"""

from collections import deque
from typing import Optional


class BrowserHistory:
    def __init__(self, start_url: str = "about:blank", max_history: int = 10):
        self._current = start_url
        self._back_stack = deque(maxlen=max_history)
        self._forward_stack: deque[str] = deque()

    def visit(self, url: str) -> None:
        self._back_stack.append(self._current)
        self._current = url
        self._forward_stack.clear()

    def back(self) -> Optional[str]:
        if not self._back_stack:
            return None
        self._forward_stack.append(self._current)
        self._current = self._back_stack.pop()
        return self._current

    def forward(self) -> Optional[str]:
        if not self._forward_stack:
            return None
        self._back_stack.append(self._current)
        self._current = self._forward_stack.pop()
        return self._current

    def current_url(self) -> str:
        return self._current

    def back_history(self) -> list[str]:
        return list(self._back_stack)

    def forward_history(self) -> list[str]:
        return list(reversed(self._forward_stack))

    def can_go_back(self) -> bool:
        return len(self._back_stack) > 0

    def can_go_forward(self) -> bool:
        return len(self._forward_stack) > 0


class TabManager:
    def __init__(self):
        self._tabs: dict[str, BrowserHistory] = {}

    def open_tab(self, name: str, url: str = "about:blank") -> None:
        self._tabs[name] = BrowserHistory(url)

    def close_tab(self, name: str) -> None:
        if name not in self._tabs:
            raise ValueError(f"Tab '{name}' not found")
        del self._tabs[name]

    def get_tab(self, name: str) -> BrowserHistory:
        if name not in self._tabs:
            raise ValueError(f"Tab '{name}' not found")
        return self._tabs[name]

    def list_tabs(self) -> list[str]:
        return list(self._tabs.keys())


# =============================================================================
# Demonstration
# =============================================================================

if __name__ == "__main__":
    print("=== Browser History Demo ===\n")
    browser = BrowserHistory("google.com")

    print(f"Start: {browser.current_url()}")
    for url in ["github.com", "stackoverflow.com", "python.org", "docs.python.org"]:
        browser.visit(url)
        print(f"Visit: {url}")

    print(f"\nCurrent: {browser.current_url()}")
    print(f"Back history: {browser.back_history()}")

    print("\nGoing back twice:")
    print(f"  -> {browser.back()}")
    print(f"  -> {browser.back()}")
    print(f"Current: {browser.current_url()}")
    print(f"Forward history: {browser.forward_history()}")

    print("\nGoing forward once:")
    print(f"  -> {browser.forward()}")

    print("\n=== Tab Manager Demo ===\n")
    tabs = TabManager()
    tabs.open_tab("work", "notion.so")
    tabs.open_tab("docs", "docs.python.org")
    tabs.open_tab("social", "twitter.com")

    print(f"Open tabs: {tabs.list_tabs()}")

    work = tabs.get_tab("work")
    work.visit("github.com")
    work.visit("jira.company.com")
    print(f"\nWork tab: {work.current_url()}")
    print(f"Work history: {work.back_history()}")

    tabs.close_tab("social")
    print(f"\nAfter closing social: {tabs.list_tabs()}")

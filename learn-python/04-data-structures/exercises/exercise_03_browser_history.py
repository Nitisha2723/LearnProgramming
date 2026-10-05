"""
Exercise 03: Browser History
==============================

Implement a browser history system using deque.

Learning goals:
  - collections.deque with maxlen
  - Stack and queue patterns
  - Double-ended queue operations

Complete the BrowserHistory class below.
"""

from collections import deque
from typing import Optional


class BrowserHistory:
    """
    Simulate browser forward/back navigation.

    The browser maintains:
    - A back stack of previously visited URLs
    - A forward stack for forward navigation
    - The current URL being viewed

    Behavior:
    - visit(url): Navigate to a new URL. Clears the forward history.
                  Old "current" goes to the back stack.
    - back(): Go to previous URL. Current goes to forward stack.
    - forward(): Go to next URL (after going back). Current goes to back stack.
    - current_url(): Return the currently viewed URL.
    - back_history(): Return list of URLs in back stack (oldest first).
    - forward_history(): Return list of URLs in forward stack (next first).

    The back_stack should have a maximum length of max_history.
    When max_history is exceeded, the oldest history entry is discarded.
    """

    def __init__(self, start_url: str = "about:blank", max_history: int = 10):
        """
        Initialize with a starting URL and maximum history size.

        Args:
            start_url: The initial page (default "about:blank")
            max_history: Maximum entries to keep in the back stack
        """
        # YOUR CODE HERE
        pass

    def visit(self, url: str) -> None:
        """
        Navigate to a new URL.
        - Saves current URL to back stack
        - Clears forward stack
        - Sets current to new url

        Args:
            url: The URL to visit
        """
        # YOUR CODE HERE
        pass

    def back(self) -> Optional[str]:
        """
        Go back to the previous URL.
        - Saves current to forward stack
        - Sets current to back stack top
        - Returns the new current URL, or None if already at start

        Returns:
            The URL navigated to, or None if can't go back
        """
        # YOUR CODE HERE
        pass

    def forward(self) -> Optional[str]:
        """
        Go forward to the next URL (only works after going back).
        - Saves current to back stack
        - Sets current to forward stack top
        - Returns the new current URL, or None if no forward history

        Returns:
            The URL navigated to, or None if can't go forward
        """
        # YOUR CODE HERE
        pass

    def current_url(self) -> str:
        """Return the currently viewed URL."""
        # YOUR CODE HERE
        pass

    def back_history(self) -> list[str]:
        """
        Return the back history as a list (oldest entry first).
        The most recent entry (poppable) is at the end.
        """
        # YOUR CODE HERE
        pass

    def forward_history(self) -> list[str]:
        """
        Return the forward history as a list (next to navigate to first).
        """
        # YOUR CODE HERE
        pass

    def can_go_back(self) -> bool:
        """Return True if there is back history."""
        # YOUR CODE HERE
        pass

    def can_go_forward(self) -> bool:
        """Return True if there is forward history."""
        # YOUR CODE HERE
        pass


class TabManager:
    """
    Manage multiple browser tabs, each with their own BrowserHistory.

    Each tab is identified by a name (string).
    """

    def __init__(self):
        # YOUR CODE HERE
        pass

    def open_tab(self, name: str, url: str = "about:blank") -> None:
        """Open a new tab with the given name and initial URL."""
        # YOUR CODE HERE
        pass

    def close_tab(self, name: str) -> None:
        """Close a tab. Raise ValueError if tab doesn't exist."""
        # YOUR CODE HERE
        pass

    def get_tab(self, name: str) -> BrowserHistory:
        """Return the BrowserHistory for a given tab. Raise ValueError if not found."""
        # YOUR CODE HERE
        pass

    def list_tabs(self) -> list[str]:
        """Return list of open tab names."""
        # YOUR CODE HERE
        pass


# =============================================================================
# TESTS
# =============================================================================

def test_browser_history():
    browser = BrowserHistory("google.com")
    assert browser.current_url() == "google.com"
    assert not browser.can_go_back()

    browser.visit("github.com")
    browser.visit("stackoverflow.com")
    browser.visit("python.org")

    assert browser.current_url() == "python.org"
    assert browser.can_go_back()
    assert not browser.can_go_forward()

    # Go back
    result = browser.back()
    assert result == "stackoverflow.com"
    assert browser.current_url() == "stackoverflow.com"
    assert browser.can_go_forward()

    result = browser.back()
    assert result == "github.com"

    # Go forward
    result = browser.forward()
    assert result == "stackoverflow.com"
    assert browser.current_url() == "stackoverflow.com"

    # Visit clears forward
    browser.visit("docs.python.org")
    assert not browser.can_go_forward()
    assert browser.current_url() == "docs.python.org"

    print("  BrowserHistory navigation: PASSED")


def test_max_history():
    browser = BrowserHistory("start.com", max_history=3)
    urls = ["a.com", "b.com", "c.com", "d.com", "e.com"]
    for url in urls:
        browser.visit(url)

    # Back stack should only have 3 items max
    assert len(browser.back_history()) <= 3, \
        f"Expected max 3 history items, got {len(browser.back_history())}"

    print("  max_history limit: PASSED")


def test_tab_manager():
    tabs = TabManager()
    tabs.open_tab("work", "notion.so")
    tabs.open_tab("personal", "gmail.com")

    assert "work" in tabs.list_tabs()
    assert "personal" in tabs.list_tabs()

    work_tab = tabs.get_tab("work")
    work_tab.visit("github.com")
    assert work_tab.current_url() == "github.com"

    # Closing a tab removes it
    tabs.close_tab("personal")
    assert "personal" not in tabs.list_tabs()

    # Getting a non-existent tab raises ValueError
    try:
        tabs.get_tab("personal")
        assert False, "Should raise ValueError"
    except ValueError:
        pass

    print("  TabManager: PASSED")


if __name__ == "__main__":
    print("Running tests...\n")
    try:
        test_browser_history()
        test_max_history()
        test_tab_manager()
        print("\nAll tests passed!")
    except AssertionError as e:
        print(f"\nTest FAILED: {e}")
    except TypeError:
        print("\nTest FAILED: returned None — did you implement all methods?")

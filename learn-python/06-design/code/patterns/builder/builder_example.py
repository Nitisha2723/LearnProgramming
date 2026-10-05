"""
Builder Pattern — Python Examples

Fluent builder for constructing complex HttpRequest objects.
"""

from dataclasses import dataclass, field
from typing import Any
import json


@dataclass
class HttpRequest:
    """The object we are building — immutable once created."""
    url: str
    method: str
    headers: dict[str, str]
    params: dict[str, str]
    body: str | None
    timeout: int
    retry_count: int
    verify_ssl: bool

    def __str__(self) -> str:
        parts = [f"{self.method} {self.url}"]
        if self.params:
            query = "&".join(f"{k}={v}" for k, v in self.params.items())
            parts[0] += f"?{query}"
        for key, value in self.headers.items():
            if key.lower() == "authorization":
                parts.append(f"  {key}: ***")
            else:
                parts.append(f"  {key}: {value}")
        if self.body:
            parts.append(f"  Body: {self.body[:100]}{'...' if len(self.body) > 100 else ''}")
        return "\n".join(parts)


class HttpRequestBuilder:
    """
    Fluent builder for HttpRequest.

    Each method returns self (enabling method chaining).
    Call build() to create the immutable HttpRequest.

    Example:
        request = (
            HttpRequestBuilder("https://api.example.com/users")
            .post()
            .bearer_auth("token123")
            .json_body({"name": "Alice"})
            .timeout(10)
            .build()
        )
    """

    def __init__(self, url: str):
        if not url:
            raise ValueError("URL cannot be empty")
        self._url = url
        self._method = "GET"
        self._headers: dict[str, str] = {}
        self._params: dict[str, str] = {}
        self._body: str | None = None
        self._timeout = 30
        self._retry_count = 0
        self._verify_ssl = True

    # ─── HTTP Method ──────────────────────────────────────────────────────────

    def get(self) -> "HttpRequestBuilder":
        self._method = "GET"
        return self

    def post(self) -> "HttpRequestBuilder":
        self._method = "POST"
        return self

    def put(self) -> "HttpRequestBuilder":
        self._method = "PUT"
        return self

    def patch(self) -> "HttpRequestBuilder":
        self._method = "PATCH"
        return self

    def delete(self) -> "HttpRequestBuilder":
        self._method = "DELETE"
        return self

    def method(self, http_method: str) -> "HttpRequestBuilder":
        self._method = http_method.upper()
        return self

    # ─── Headers ──────────────────────────────────────────────────────────────

    def header(self, key: str, value: str) -> "HttpRequestBuilder":
        self._headers[key] = value
        return self

    def headers(self, headers: dict[str, str]) -> "HttpRequestBuilder":
        self._headers.update(headers)
        return self

    def content_type(self, content_type: str) -> "HttpRequestBuilder":
        self._headers["Content-Type"] = content_type
        return self

    def accept(self, media_type: str) -> "HttpRequestBuilder":
        self._headers["Accept"] = media_type
        return self

    # ─── Authentication ───────────────────────────────────────────────────────

    def bearer_auth(self, token: str) -> "HttpRequestBuilder":
        self._headers["Authorization"] = f"Bearer {token}"
        return self

    def basic_auth(self, username: str, password: str) -> "HttpRequestBuilder":
        import base64
        credentials = base64.b64encode(f"{username}:{password}".encode()).decode()
        self._headers["Authorization"] = f"Basic {credentials}"
        return self

    def api_key(self, key: str, header_name: str = "X-API-Key") -> "HttpRequestBuilder":
        self._headers[header_name] = key
        return self

    # ─── Query Parameters ─────────────────────────────────────────────────────

    def param(self, key: str, value: Any) -> "HttpRequestBuilder":
        self._params[key] = str(value)
        return self

    def params(self, params: dict[str, Any]) -> "HttpRequestBuilder":
        self._params.update({k: str(v) for k, v in params.items()})
        return self

    # ─── Body ─────────────────────────────────────────────────────────────────

    def body(self, content: str) -> "HttpRequestBuilder":
        self._body = content
        return self

    def json_body(self, data: Any) -> "HttpRequestBuilder":
        """Set body as JSON and automatically set Content-Type."""
        self._body = json.dumps(data)
        self._headers["Content-Type"] = "application/json"
        return self

    def form_body(self, fields: dict[str, str]) -> "HttpRequestBuilder":
        """Set body as URL-encoded form data."""
        from urllib.parse import urlencode
        self._body = urlencode(fields)
        self._headers["Content-Type"] = "application/x-www-form-urlencoded"
        return self

    # ─── Options ──────────────────────────────────────────────────────────────

    def timeout(self, seconds: int) -> "HttpRequestBuilder":
        if seconds <= 0:
            raise ValueError("Timeout must be a positive number of seconds")
        self._timeout = seconds
        return self

    def retry(self, count: int) -> "HttpRequestBuilder":
        if count < 0:
            raise ValueError("Retry count cannot be negative")
        self._retry_count = count
        return self

    def disable_ssl_verification(self) -> "HttpRequestBuilder":
        """WARNING: Only use in development environments."""
        self._verify_ssl = False
        return self

    # ─── Build ────────────────────────────────────────────────────────────────

    def build(self) -> HttpRequest:
        """Validate and create the HttpRequest. Raises ValueError for invalid config."""
        if not self._url.startswith(("http://", "https://")):
            raise ValueError(
                f"URL must start with http:// or https://, got: {self._url}"
            )

        if self._method in ("POST", "PUT", "PATCH") and self._body is None:
            # Not an error, but worth noting — some POST requests have no body
            pass

        return HttpRequest(
            url=self._url,
            method=self._method,
            headers=dict(self._headers),
            params=dict(self._params),
            body=self._body,
            timeout=self._timeout,
            retry_count=self._retry_count,
            verify_ssl=self._verify_ssl,
        )


# ─── Demo ─────────────────────────────────────────────────────────────────────

if __name__ == "__main__":
    # Create a user
    create_user = (
        HttpRequestBuilder("https://api.example.com/users")
        .post()
        .bearer_auth("my-secret-token")
        .json_body({"name": "Alice", "email": "alice@example.com"})
        .timeout(10)
        .retry(3)
        .build()
    )
    print("=== Create User Request ===")
    print(create_user)

    # Search with query params
    search = (
        HttpRequestBuilder("https://api.example.com/products")
        .get()
        .api_key("api_key_123")
        .params({"category": "electronics", "min_price": 50, "max_price": 500})
        .accept("application/json")
        .timeout(5)
        .build()
    )
    print("\n=== Search Request ===")
    print(search)

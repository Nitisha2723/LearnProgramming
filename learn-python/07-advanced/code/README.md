# Module 07 Code Examples

Working code demonstrating all advanced Python topics.

## Files

| File | Demonstrates |
|------|-------------|
| `decorators_advanced.py` | `@retry`, `@timer`, `@cache_result`, `@validate_types`, `@rate_limit` |
| `generators_advanced.py` | Infinite generators, data pipelines, `send()`, `yield from`, itertools |
| `async_demo.py` | Concurrent requests, producer-consumer queue, background tasks, timeouts |

## Running the Examples

From this directory:

```bash
# Decorators demo
python decorators_advanced.py

# Generators demo
python generators_advanced.py

# Async demo (install aiohttp for real HTTP requests)
pip install aiohttp
python async_demo.py
```

## Dependencies

Standard library only, except:
- `aiohttp` for the HTTP examples in `async_demo.py` (falls back to simulation if not installed)

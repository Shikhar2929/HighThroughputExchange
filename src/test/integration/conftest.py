from __future__ import annotations

import os
import sys
import time
from pathlib import Path
import uuid

import pytest


def _repo_root() -> Path:
    # conftest.py -> integration -> test -> src -> repo
    return Path(__file__).resolve().parents[3]


# Make existing helpers available (src/test/env_loader.py)
_REPO = _repo_root()
_SYS_TEST_DIR = _REPO / "src" / "test"
if str(_SYS_TEST_DIR) not in sys.path:
    sys.path.insert(0, str(_SYS_TEST_DIR))

# Make integration helpers importable from nested test folders
_INTEGRATION_DIR = _SYS_TEST_DIR / "integration"
if str(_INTEGRATION_DIR) not in sys.path:
    sys.path.insert(0, str(_INTEGRATION_DIR))


@pytest.fixture(scope="session", autouse=True)
def _load_env_once() -> None:
    # Reuse existing env loading logic, but allow CI to override via env vars.
    try:
        from env_loader import load_env  # type: ignore

        # Prefer a local `.env` (often gitignored, may contain real creds),
        # then fall back to the committed `public.env` template.
        load_env(".env")
        load_env("public.env")
    except Exception:
        # Don't hard-fail if dotenv isn't installed; env_loader already has fallback.
        pass


@pytest.fixture(scope="session")
def base_url() -> str:
    return os.getenv("HTTP_URL", "http://localhost:8080").rstrip("/")


@pytest.fixture(scope="session")
def client(base_url: str):
    from exchange_client import (
        ExchangeClient,
    )  # local import to avoid path ordering issues

    c = ExchangeClient(base_url)
    try:
        yield c
    finally:
        c.close()


@pytest.fixture(autouse=True)
def _reset_orderbook_per_test(client) -> None:
    # Tests share a long-lived server; keep them isolated by clearing order books.
    # Without this, earlier resting orders (especially bids) can unintentionally fill
    # a later test's liquidity-seeding ask, causing market orders to see NO_LIQUIDITY.
    client.admin_set_state(1)
    client.admin_set_price({"A": 100})


@pytest.fixture()
def unique_username():
    def _unique(prefix: str = "ci") -> str:
        return f"{prefix}_{uuid.uuid4().hex[:12]}"

    return _unique


def _wait_for_http_up(url: str, timeout_s: float = 45.0) -> None:
    import requests

    deadline = time.time() + timeout_s
    last_err: Exception | None = None
    while time.time() < deadline:
        try:
            r = requests.get(f"{url}/get_state", timeout=2)
            if r.status_code == 200:
                return
        except Exception as e:  # pragma: no cover
            last_err = e
        time.sleep(0.5)

    if last_err:
        raise RuntimeError(f"Server not reachable at {url}: {last_err}")
    raise RuntimeError(f"Server not reachable at {url}")


@pytest.fixture(scope="session", autouse=True)
def _ensure_server_is_running(base_url: str) -> None:
    # CI starts the server as a separate step, but local runs benefit too.
    _wait_for_http_up(base_url)

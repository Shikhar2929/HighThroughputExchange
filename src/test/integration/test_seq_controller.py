from __future__ import annotations

import time
import uuid
from typing import Any

import pytest
import requests

from exchange_client import ExchangeClient


@pytest.fixture(scope="session")
def client(base_url: str) -> ExchangeClient:
    return ExchangeClient(base_url)


def _unique_username(prefix: str) -> str:
    return f"{prefix}_{uuid.uuid4().hex[:12]}"


def test_latest_seq_returns_number(client: ExchangeClient) -> None:
    latest = client.latest_seq()
    assert isinstance(latest, int)
    assert latest >= 0


def test_snapshot_returns_json_and_latest_seq(client: ExchangeClient) -> None:
    data = client.snapshot()

    assert "latestSeq" in data
    assert isinstance(int(data["latestSeq"]), int)

    # SnapshotResponse uses @JsonRawValue, so snapshot comes through as JSON.
    assert "snapshot" in data
    snapshot = data["snapshot"]
    assert isinstance(snapshot, dict)


def test_updates_requires_from_exclusive_param(base_url: str) -> None:
    r = requests.get(f"{base_url}/updates", timeout=5.0)
    assert r.status_code == 400


def test_updates_returns_updates_after_trade(client: ExchangeClient) -> None:
    # Create a trade to populate RecentTrades, then wait for SocketController's
    # scheduled task to append it into OrderbookSeqLog.
    client.admin_set_state(1)

    seller = _unique_username("seller")
    buyer = _unique_username("buyer")

    seller_key = client.admin_add_user(username=seller, name="CI Seller", email=f"{seller}@example.com")
    buyer_key = client.admin_add_user(username=buyer, name="CI Buyer", email=f"{buyer}@example.com")

    seller_session = client.buildup(seller, seller_key)
    buyer_session = client.buildup(buyer, buyer_key)

    try:
        client.limit_order(seller_session, ticker="A", volume=1, price=120, is_bid=False)
        time.sleep(0.05)
        client.market_order(buyer_session, ticker="A", volume=1, is_bid=True)

        deadline = time.time() + 8.0

        # Try a permissive fromExclusive first; if the log has rotated, the
        # server will tell us the minimal allowed fromExclusive.
        from_exclusive = -1

        last_status: int | None = None
        last_data: dict[str, Any] | None = None

        while time.time() < deadline:
            status, data = client.updates_allow_gone(from_exclusive)
            last_status, last_data = status, data

            if status == 200:
                assert isinstance(data.get("updates"), list)
                updates: list[dict[str, Any]] = data["updates"]
                assert len(updates) > 0

                # Ensure at least one PriceChange mentions ticker A.
                saw_ticker_a = False
                for update in updates:
                    for pc in update.get("priceChanges", []) or []:
                        if pc.get("ticker") == "A":
                            saw_ticker_a = True
                            break
                    if saw_ticker_a:
                        break

                assert saw_ticker_a, f"Expected at least one update for ticker A, got: {updates!r}"
                return

            if status == 410:
                err = data.get("error")

                # Log empty: scheduled task hasn't appended yet.
                if err == "min-seq-unavailable":
                    time.sleep(0.2)
                    continue

                # Too old: update from_exclusive to the minimum allowed.
                if err == "from-too-old":
                    min_from_exclusive = data.get("minFromExclusive")
                    if min_from_exclusive is not None:
                        from_exclusive = int(min_from_exclusive)
                        time.sleep(0.05)
                        continue

            # Any other status: brief pause and retry (can be transient).
            time.sleep(0.2)

        raise AssertionError(f"/updates never returned 200 (last={last_status} {last_data})")

    finally:
        client.teardown(seller_session)
        client.teardown(buyer_session)

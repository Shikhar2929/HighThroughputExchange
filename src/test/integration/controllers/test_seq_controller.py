from __future__ import annotations

import time
from typing import Any

import requests

from exchange_client import ExchangeClient


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


def test_updates_returns_updates_after_trade(client: ExchangeClient, unique_username) -> None:
    # Create a trade to populate RecentTrades, then wait for SocketController's
    # scheduled task to append it into OrderbookSeqLog.
    client.admin_set_state(1)

    seller = unique_username("seller")
    buyer = unique_username("buyer")

    seller_key = client.admin_add_user(username=seller, name="CI Seller", email=f"{seller}@example.com")
    buyer_key = client.admin_add_user(username=buyer, name="CI Buyer", email=f"{buyer}@example.com")

    seller_session = client.buildup(seller, seller_key)
    buyer_session = client.buildup(buyer, buyer_key)

    try:
        client.limit_order(seller_session, ticker="A", volume=1, price=120, is_bid=False)
        time.sleep(0.05)
        client.market_order(buyer_session, ticker="A", volume=1, is_bid=True)

        deadline = time.time() + 8.0

        # Query updates by exact seq. Missing seq returns HTTP 400 with
        # message=INVALID_SEQ_NUM, so we just keep polling forward.
        seq = 0

        last_status: int | None = None
        last_data: dict[str, Any] | None = None

        while time.time() < deadline:
            status, data = client.update_allow_missing(seq)
            last_status, last_data = status, data

            if status == 200:
                update = data.get("update")
                assert isinstance(update, dict)

                # Advance to the next seq.
                got_seq = int(update.get("seq"))
                seq = got_seq + 1

                for pc in update.get("priceChanges", []) or []:
                    if pc.get("ticker") == "A":
                        return

                time.sleep(0.05)
                continue

            if status == 400:
                msg = data.get("message")
                assert isinstance(msg, dict)
                assert int(msg.get("errorCode")) == 8
                time.sleep(0.1)
                continue

            # Any other status: brief pause and retry (can be transient).
            time.sleep(0.2)

        raise AssertionError(f"/updates never returned 200 (last={last_status} {last_data})")

    finally:
        client.teardown(seller_session)
        client.teardown(buyer_session)

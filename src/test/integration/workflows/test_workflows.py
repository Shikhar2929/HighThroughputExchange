from __future__ import annotations

import time

from exchange_client import ExchangeClient


def test_user_trade_then_snapshot_and_updates(
    client: ExchangeClient, unique_username
) -> None:
    # Cross-controller workflow:
    # AdminController -> SessionController -> OrderController -> SeqController
    client.admin_set_state(1)

    seller = unique_username("wf_seller")
    buyer = unique_username("wf_buyer")

    seller_key = client.admin_add_user(
        username=seller, name="WF Seller", email=f"{seller}@example.com"
    )
    buyer_key = client.admin_add_user(
        username=buyer, name="WF Buyer", email=f"{buyer}@example.com"
    )

    seller_session = client.buildup(seller, seller_key)
    buyer_session = client.buildup(buyer, buyer_key)

    try:
        client.limit_order(
            seller_session, ticker="A", volume=1, price=120, is_bid=False
        )
        time.sleep(0.05)
        client.market_order(buyer_session, ticker="A", volume=1, is_bid=True)

        # Snapshot should always be available and JSON.
        snap = client.snapshot()
        assert isinstance(snap.get("snapshot"), dict)

        # Updates should eventually include the trade.
        deadline = time.time() + 8.0
        seq = 0
        while time.time() < deadline:
            status, data = client.update_allow_missing(seq)
            if status == 200:
                update = data.get("update")
                assert isinstance(update, dict)
                seq = int(update.get("seq")) + 1
                return
            if status == 400:
                msg = data.get("message")
                assert isinstance(msg, dict)
                assert int(msg.get("errorCode")) == 8
                time.sleep(0.1)
                continue
            time.sleep(0.2)

        raise AssertionError("Expected /updates to return non-empty updates")

    finally:
        client.teardown(seller_session)
        client.teardown(buyer_session)


def test_bot_batch_then_cleanup(client: ExchangeClient, unique_username) -> None:
    # Cross-controller workflow:
    # AdminController -> SessionController -> BatchController -> OrderController
    client.admin_set_state(1)

    bot_name = unique_username("wf_batch")
    api_key = client.admin_add_bot(username=bot_name, name="WF Bot")
    bot_session = client.bot_buildup(bot_name, api_key)

    ops = [
        {"type": "limit_order", "ticker": "A", "price": 100, "volume": 1, "bid": True},
        {"type": "remove_all"},
    ]
    data = client.batch(bot_session, ops)
    assert data.get("status") == "SUCCESS"

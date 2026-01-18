from __future__ import annotations

import time

from exchange_client import ExchangeClient


def _message_error_code(resp: dict) -> int | None:
    msg = resp.get("message")
    if isinstance(msg, dict):
        try:
            return int(msg.get("errorCode"))
        except Exception:
            return None
    return None


def test_trading_endpoints_locked_when_state_stop(client: ExchangeClient, unique_username) -> None:
    # AdminController -> SessionController -> AdminController -> OrderController
    # Ensure we can create a session first.
    client.admin_set_state(1)

    username = unique_username("wf_lock")
    api_key = client.admin_add_user(username=username, name="WF Lock", email=f"{username}@example.com")
    session = client.buildup(username, api_key)

    try:
        # Lock trading.
        client.admin_set_state(0)

        status, data = client.post_allow_http_error(
            "/limit_order",
            {
                "username": session.username,
                "sessionToken": session.session_token,
                "ticker": "A",
                "volume": 1,
                "price": 123,
                "isBid": True,
            },
        )
        assert status == 423
        assert _message_error_code(data) == 3  # TRADE_LOCKED

        status, data = client.post_allow_http_error(
            "/market_order",
            {
                "username": session.username,
                "sessionToken": session.session_token,
                "ticker": "A",
                "volume": 1,
                "isBid": True,
            },
        )
        assert status == 423
        assert _message_error_code(data) == 3  # TRADE_LOCKED

        # Unlock trading.
        client.admin_set_state(1)

        # Now trading should work.
        resp = client.limit_order(session, ticker="A", volume=1, price=123, is_bid=True)
        assert resp["message"]["errorCode"] == 0

    finally:
        # Best-effort restore state and cleanup.
        try:
            client.admin_set_state(1)
        except Exception:
            pass
        time.sleep(0.05)
        client.teardown(session)


def test_auction_locked_in_trade_state_then_allows_bids_in_auction_state(client: ExchangeClient, unique_username) -> None:
    # AdminController -> SessionController -> AuctionController
    client.admin_set_state(1)

    username = unique_username("wf_auc_lock")
    api_key = client.admin_add_user(username=username, name="WF Auction", email=f"{username}@example.com")
    session = client.buildup(username, api_key)

    try:
        # In TRADE state, auction is locked.
        status, data = client.post_allow_http_error(
            "/bid_auction",
            {"username": session.username, "sessionToken": session.session_token, "bid": 1},
        )
        assert status == 423
        assert _message_error_code(data) == 5  # AUCTION_LOCKED

        # Enable auction and try again.
        client.admin_set_state(2)
        resp = client.bid_auction(session, bid=1)
        assert resp["message"]["errorCode"] == 0

    finally:
        # Restore trade mode for other tests.
        try:
            client.admin_set_state(1)
        except Exception:
            pass
        client.teardown(session)

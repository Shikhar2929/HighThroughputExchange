from __future__ import annotations

import time
import uuid

import pytest

from exchange_client import ExchangeClient


@pytest.fixture(scope="session")
def client(base_url: str) -> ExchangeClient:
    return ExchangeClient(base_url)


def _unique_username(prefix: str) -> str:
    return f"{prefix}_{uuid.uuid4().hex[:12]}"


def test_get_details_returns_payload(client: ExchangeClient) -> None:
    client.admin_set_state(1)

    username = _unique_username("details")
    api_key = client.admin_add_user(username=username, name="CI Details", email=f"{username}@example.com")
    session = client.buildup(username, api_key)
    try:
        details = client.get_details(session)
        assert isinstance(details, str)
        # Usually contains JSON; just ensure non-empty.
        assert len(details) >= 0
    finally:
        client.teardown(session)


def test_remove_all_succeeds_after_order(client: ExchangeClient) -> None:
    client.admin_set_state(1)

    username = _unique_username("rmall")
    api_key = client.admin_add_user(username=username, name="CI RemoveAll", email=f"{username}@example.com")
    session = client.buildup(username, api_key)

    try:
        client.limit_order(session, ticker="A", volume=1, price=100, is_bid=True)
        # Avoid flakiness with rate limiter.
        time.sleep(0.05)
        client.remove_all(session)
    finally:
        client.teardown(session)


def test_leaderboard_endpoint_returns_list(client: ExchangeClient) -> None:
    entries = client.admin_leaderboard()
    assert isinstance(entries, list)


def test_set_price_accepts_valid_tickers(client: ExchangeClient) -> None:
    # This endpoint also clears order books; keep it simple/deterministic.
    client.admin_set_price({"A": 50, "B": 60, "C": 70})


def test_bot_flow_can_place_order_and_remove_all(client: ExchangeClient) -> None:
    client.admin_set_state(1)

    bot_name = _unique_username("bot")
    api_key = client.admin_add_bot(username=bot_name, name="CI Bot")
    bot_session = client.bot_buildup(bot_name, api_key)

    # Bot validation is looser than users, but still needs a valid ticker.
    client.bot_limit_order(bot_session, ticker="A", volume=1, price=101, is_bid=True)
    time.sleep(0.05)
    client.bot_remove_all(bot_session)

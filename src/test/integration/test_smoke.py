from __future__ import annotations

import time
import uuid

import pytest

from exchange_client import ExchangeClient


@pytest.fixture(scope="session")
def client(base_url: str) -> ExchangeClient:
    return ExchangeClient(base_url)


def _unique_username(prefix: str = "ci") -> str:
    return f"{prefix}_{uuid.uuid4().hex[:12]}"


def test_get_state_responds(client: ExchangeClient) -> None:
    state = client.get_state()
    assert state in (0, 1, 2)


def test_admin_can_create_user_buildup_and_teardown(client: ExchangeClient) -> None:
    username = _unique_username("user")
    api_key = client.admin_add_user(username=username, name="CI User", email=f"{username}@example.com")
    session = client.buildup(username, api_key)

    # Small delay to reduce flakiness around immediate teardown/rate limiting.
    time.sleep(0.05)
    client.teardown(session)


@pytest.mark.parametrize("target_state", [1])
def test_trading_flow_limit_order_success(client: ExchangeClient, target_state: int) -> None:
    # Ensure trading is allowed.
    new_state = client.admin_set_state(target_state)
    assert new_state == target_state

    username = _unique_username("trader")
    api_key = client.admin_add_user(username=username, name="CI Trader", email=f"{username}@example.com")
    session = client.buildup(username, api_key)

    try:
        resp = client.limit_order(session, ticker="A", volume=1, price=123, is_bid=True)
        assert resp["message"]["errorCode"] == 0
    finally:
        client.teardown(session)

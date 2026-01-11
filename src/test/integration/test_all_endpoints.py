from __future__ import annotations

import os
import time
import uuid

import pytest
import requests

from exchange_client import ExchangeClient


@pytest.fixture(scope="session")
def client(base_url: str) -> ExchangeClient:
    return ExchangeClient(base_url)


def _unique_username(prefix: str) -> str:
    return f"{prefix}_{uuid.uuid4().hex[:12]}"


def test_admin_page_success(client: ExchangeClient) -> None:
    client.admin_page()


def test_private_page_success(client: ExchangeClient) -> None:
    client.admin_set_state(1)

    username = _unique_username("priv")
    api_key = client.admin_add_user(username=username, name="CI Private", email=f"{username}@example.com")
    session = client.buildup(username, api_key)
    try:
        client.private_page(session)
    finally:
        client.teardown(session)


def test_remove_by_order_id(client: ExchangeClient) -> None:
    client.admin_set_state(1)

    username = _unique_username("rm")
    api_key = client.admin_add_user(username=username, name="CI Remove", email=f"{username}@example.com")
    session = client.buildup(username, api_key)

    try:
        resp = client.limit_order(session, ticker="A", volume=1, price=111, is_bid=True)
        order_id = int(resp["message"]["orderId"])
        time.sleep(0.05)
        client.remove(session, order_id)
    finally:
        client.teardown(session)


def test_market_order_matches_against_limit(client: ExchangeClient) -> None:
    client.admin_set_state(1)

    seller = _unique_username("seller")
    buyer = _unique_username("buyer")

    seller_key = client.admin_add_user(username=seller, name="CI Seller", email=f"{seller}@example.com")
    buyer_key = client.admin_add_user(username=buyer, name="CI Buyer", email=f"{buyer}@example.com")

    seller_session = client.buildup(seller, seller_key)
    buyer_session = client.buildup(buyer, buyer_key)

    try:
        # Create liquidity: seller places an ask.
        client.limit_order(seller_session, ticker="A", volume=1, price=120, is_bid=False)
        time.sleep(0.05)

        resp = client.market_order(buyer_session, ticker="A", volume=1, is_bid=True)
        assert resp["message"]["errorCode"] == 0
    finally:
        client.teardown(seller_session)
        client.teardown(buyer_session)


def test_bot_remove_by_order_id(client: ExchangeClient) -> None:
    client.admin_set_state(1)

    bot_name = _unique_username("botrm")
    api_key = client.admin_add_bot(username=bot_name, name="CI Bot")
    bot_session = client.bot_buildup(bot_name, api_key)

    resp = client.bot_limit_order(bot_session, ticker="A", volume=1, price=130, is_bid=True)
    order_id = int(resp["message"]["orderId"])
    time.sleep(0.05)
    client.bot_remove(bot_session, order_id)


def test_bot_market_order_success(client: ExchangeClient) -> None:
    client.admin_set_state(1)

    bot_name = _unique_username("botmkt")
    api_key = client.admin_add_bot(username=bot_name, name="CI Bot")
    bot_session = client.bot_buildup(bot_name, api_key)

    # Market order may fill 0 if no liquidity; still should be a valid request.
    resp = client.bot_market_order(bot_session, ticker="A", volume=1, is_bid=True)
    assert resp["message"]["errorCode"] == 0


def test_batch_endpoint_processes_operations(client: ExchangeClient) -> None:
    client.admin_set_state(1)

    bot_name = _unique_username("batch")
    api_key = client.admin_add_bot(username=bot_name, name="CI Batch")
    bot_session = client.bot_buildup(bot_name, api_key)

    operations = [
        {"type": "limit_order", "ticker": "A", "price": 100, "volume": 1, "bid": True},
        {"type": "remove_all"},
    ]
    data = client.batch(bot_session, operations)
    assert data["status"] == "SUCCESS"
    assert isinstance(data.get("results"), list)
    assert len(data["results"]) == len(operations)


def test_auction_endpoints_flow(client: ExchangeClient) -> None:
    # Enable auction state.
    client.admin_set_state(2)

    username = _unique_username("auc")
    api_key = client.admin_add_user(username=username, name="CI Auction", email=f"{username}@example.com")
    session = client.buildup(username, api_key)

    try:
        client.bid_auction(session, bid=1)
        lead = client.get_leading_auction_bid()
        assert "user" in lead and "bid" in lead

        term = client.terminate_auction()
        assert "user" in term and "bid" in term
    finally:
        client.teardown(session)
        # Put server back into trade mode for any other tests.
        client.admin_set_state(1)


def test_shutdown_requires_auth(base_url: str) -> None:
    # Do NOT stop the server in CI; just ensure endpoint is protected.
    r = requests.post(
        f"{base_url}/shutdown",
        json={"adminUsername": "wrong", "adminPassword": "wrong"},
        timeout=5,
    )
    assert r.status_code in (401, 403)


@pytest.mark.skipif(os.getenv("ENABLE_SHUTDOWN_TEST") != "true", reason="Set ENABLE_SHUTDOWN_TEST=true to run")
def test_shutdown_success_when_enabled(base_url: str) -> None:
    # This test is intentionally opt-in because it will stop the server.
    r = requests.post(
        f"{base_url}/shutdown",
        json={
            "adminUsername": os.environ["ADMIN_USERNAME"],
            "adminPassword": os.environ["ADMIN_PASSWORD"],
        },
        timeout=5,
    )
    assert r.status_code == 200

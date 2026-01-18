from __future__ import annotations

import time

from exchange_client import ExchangeClient


def test_limit_order_success(client: ExchangeClient, unique_username) -> None:
    client.admin_set_state(1)

    username = unique_username("trader")
    api_key = client.admin_add_user(username=username, name="CI Trader", email=f"{username}@example.com")
    session = client.buildup(username, api_key)

    try:
        resp = client.limit_order(session, ticker="A", volume=1, price=123, is_bid=True)
        assert resp["message"]["errorCode"] == 0
    finally:
        client.teardown(session)


def test_remove_all_succeeds_after_order(client: ExchangeClient, unique_username) -> None:
    client.admin_set_state(1)

    username = unique_username("rmall")
    api_key = client.admin_add_user(username=username, name="CI RemoveAll", email=f"{username}@example.com")
    session = client.buildup(username, api_key)

    try:
        client.limit_order(session, ticker="A", volume=1, price=100, is_bid=True)
        time.sleep(0.05)
        client.remove_all(session)
    finally:
        client.teardown(session)


def test_remove_by_order_id(client: ExchangeClient, unique_username) -> None:
    client.admin_set_state(1)

    username = unique_username("rm")
    api_key = client.admin_add_user(username=username, name="CI Remove", email=f"{username}@example.com")
    session = client.buildup(username, api_key)

    try:
        resp = client.limit_order(session, ticker="A", volume=1, price=111, is_bid=True)
        order_id = int(resp["message"]["orderId"])
        time.sleep(0.05)
        client.remove(session, order_id)
    finally:
        client.teardown(session)


def test_market_order_matches_against_limit(client: ExchangeClient, unique_username) -> None:
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

        resp = client.market_order(buyer_session, ticker="A", volume=1, is_bid=True)
        assert resp["message"]["errorCode"] == 0
    finally:
        client.teardown(seller_session)
        client.teardown(buyer_session)


def test_bot_flow_can_place_order_and_remove_all(client: ExchangeClient, unique_username) -> None:
    client.admin_set_state(1)

    bot_name = unique_username("bot")
    api_key = client.admin_add_bot(username=bot_name, name="CI Bot")
    bot_session = client.bot_buildup(bot_name, api_key)

    client.bot_limit_order(bot_session, ticker="A", volume=1, price=101, is_bid=True)
    time.sleep(0.05)
    client.bot_remove_all(bot_session)


def test_bot_remove_by_order_id(client: ExchangeClient, unique_username) -> None:
    client.admin_set_state(1)

    bot_name = unique_username("botrm")
    api_key = client.admin_add_bot(username=bot_name, name="CI Bot")
    bot_session = client.bot_buildup(bot_name, api_key)

    resp = client.bot_limit_order(bot_session, ticker="A", volume=1, price=130, is_bid=True)
    order_id = int(resp["message"]["orderId"])
    time.sleep(0.05)
    client.bot_remove(bot_session, order_id)


def test_bot_market_order_success(client: ExchangeClient, unique_username) -> None:
    client.admin_set_state(1)

    bot_name = unique_username("botmkt")
    api_key = client.admin_add_bot(username=bot_name, name="CI Bot")
    bot_session = client.bot_buildup(bot_name, api_key)

    # Market order may fill 0 if no liquidity; still should be a valid request.
    resp = client.bot_market_order(bot_session, ticker="A", volume=1, is_bid=True)
    assert resp["message"]["errorCode"] == 0

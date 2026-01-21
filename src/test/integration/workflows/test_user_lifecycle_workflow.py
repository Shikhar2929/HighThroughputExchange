from __future__ import annotations

import time

from exchange_client import ExchangeClient


def test_user_lifecycle_create_trade_remove_details_teardown(
    client: ExchangeClient, unique_username
) -> None:
    # AdminController -> SessionController -> OrderController -> SystemController
    client.admin_set_state(1)

    username = unique_username("wf_user")
    api_key = client.admin_add_user(
        username=username, name="WF User", email=f"{username}@example.com"
    )
    session = client.buildup(username, api_key)

    try:
        # Place an order
        resp = client.limit_order(session, ticker="A", volume=1, price=111, is_bid=True)
        assert resp["message"]["errorCode"] == 0

        # Remove it by id
        order_id = int(resp["message"]["orderId"])
        time.sleep(0.05)
        client.remove(session, order_id)

        # Check details after actions
        details = client.get_details(session)
        assert isinstance(details, str)

    finally:
        time.sleep(0.05)
        client.teardown(session)


def test_two_users_trade_then_remove_all(
    client: ExchangeClient, unique_username
) -> None:
    # AdminController -> SessionController -> OrderController
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
        resp = client.market_order(buyer_session, ticker="A", volume=1, is_bid=True)
        assert resp["message"]["errorCode"] == 0

        # Cleanup on both sides
        time.sleep(0.05)
        client.remove_all(seller_session)
        client.remove_all(buyer_session)

    finally:
        client.teardown(seller_session)
        client.teardown(buyer_session)

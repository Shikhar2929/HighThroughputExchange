from __future__ import annotations

import time

from exchange_client import ExchangeClient


def test_bot_lifecycle_place_remove_market_remove_all(client: ExchangeClient, unique_username) -> None:
    # AdminController -> SessionController -> OrderController
    client.admin_set_state(1)

    bot_name = unique_username("wf_bot")
    api_key = client.admin_add_bot(username=bot_name, name="WF Bot")
    bot_session = client.bot_buildup(bot_name, api_key)

    # Place an order, remove by id, place market, remove all.
    resp = client.bot_limit_order(bot_session, ticker="A", volume=1, price=130, is_bid=True)
    assert resp["message"]["errorCode"] == 0

    order_id = int(resp["message"]["orderId"])
    time.sleep(0.05)
    client.bot_remove(bot_session, order_id)

    time.sleep(0.05)
    mkt = client.bot_market_order(bot_session, ticker="A", volume=1, is_bid=True)
    assert mkt["message"]["errorCode"] == 0

    time.sleep(0.05)
    client.bot_remove_all(bot_session)

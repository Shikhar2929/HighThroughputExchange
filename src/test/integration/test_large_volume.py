"""Integration tests for large volume trading and market making.

This module tests bot trading with large volumes and market maker functionality.

Converted from: src/test/large_volume_test.py
"""

from __future__ import annotations

import time

from exchange_client import ExchangeClient


def test_bot_can_place_large_volume_order(
    client: ExchangeClient, unique_username
) -> None:
    """Test that a bot can place orders with large volumes."""
    client.admin_set_state(1)

    bot_name = unique_username("large_vol_bot")
    api_key = client.admin_add_bot(username=bot_name, name="Large Volume Bot")
    bot_session = client.bot_buildup(bot_name, api_key)

    time.sleep(0.05)

    # Place a large volume order (10,000,000 like the original test)
    result = client.bot_limit_order(
        bot_session, ticker="A", volume=10000000, price=200, is_bid=True
    )

    assert result is not None
    assert "message" in result


def test_market_maker_bot_bid_ask_spread(
    client: ExchangeClient, unique_username
) -> None:
    """Test that a bot can act as a market maker with bid/ask spread."""
    client.admin_set_state(1)

    bot_name = unique_username("market_maker")
    api_key = client.admin_add_bot(username=bot_name, name="Market Maker Bot")
    bot_session = client.bot_buildup(bot_name, api_key)

    time.sleep(0.05)

    # Market making: place bid and ask around a mid price
    mid_price = 200
    bid_price = mid_price - 1
    ask_price = mid_price + 1
    volume = 10000000

    # Place bid
    bid_result = client.bot_limit_order(
        bot_session, ticker="A", volume=volume, price=bid_price, is_bid=True
    )
    assert bid_result is not None

    time.sleep(0.05)

    # Place ask
    ask_result = client.bot_limit_order(
        bot_session, ticker="A", volume=volume, price=ask_price, is_bid=False
    )
    assert ask_result is not None


def test_multiple_large_volume_orders(client: ExchangeClient, unique_username) -> None:
    """Test placing multiple large volume orders sequentially."""
    client.admin_set_state(1)

    bot_name = unique_username("multi_large_bot")
    api_key = client.admin_add_bot(username=bot_name, name="Multi Large Bot")
    bot_session = client.bot_buildup(bot_name, api_key)

    time.sleep(0.05)

    # Place multiple large volume orders
    num_orders = 5
    orders_placed = 0

    for i in range(num_orders):
        result = client.bot_limit_order(
            bot_session,
            ticker="A",
            volume=5000000,
            price=200 + i * 10,
            is_bid=(i % 2 == 0),
        )
        if result is not None:
            orders_placed += 1
        time.sleep(0.05)

    assert orders_placed == num_orders


def test_market_maker_multiple_tickers(client: ExchangeClient, unique_username) -> None:
    """Test market making across multiple tickers."""
    client.admin_set_state(1)

    bot_name = unique_username("multi_ticker_mm")
    api_key = client.admin_add_bot(username=bot_name, name="Multi Ticker MM")
    bot_session = client.bot_buildup(bot_name, api_key)

    time.sleep(0.05)

    # Market make on multiple tickers
    tickers = ["A", "B", "C"]
    mid_price = 200
    volume = 10000000

    for ticker in tickers:
        # Place bid
        bid_result = client.bot_limit_order(
            bot_session, ticker=ticker, volume=volume, price=mid_price - 1, is_bid=True
        )
        assert bid_result is not None

        time.sleep(0.05)

        # Place ask
        ask_result = client.bot_limit_order(
            bot_session, ticker=ticker, volume=volume, price=mid_price + 1, is_bid=False
        )
        assert ask_result is not None

        time.sleep(0.05)


def test_large_volume_market_order(client: ExchangeClient, unique_username) -> None:
    """Test placing large volume market orders."""
    client.admin_set_state(1)

    # First, create liquidity with a limit order
    maker_name = unique_username("large_maker")
    maker_key = client.admin_add_bot(username=maker_name, name="Large Maker")
    maker_session = client.bot_buildup(maker_name, maker_key)

    time.sleep(0.05)

    # Place a large limit ask
    client.bot_limit_order(
        maker_session, ticker="A", volume=10000000, price=200, is_bid=False
    )

    time.sleep(0.1)

    # Now place a large market order
    taker_name = unique_username("large_taker")
    taker_key = client.admin_add_bot(username=taker_name, name="Large Taker")
    taker_session = client.bot_buildup(taker_name, taker_key)

    time.sleep(0.05)

    result = client.bot_market_order(
        taker_session, ticker="A", volume=5000000, is_bid=True
    )

    assert result is not None


def test_market_maker_dynamic_spread(client: ExchangeClient, unique_username) -> None:
    """Test market maker with dynamically changing spread."""
    client.admin_set_state(1)

    bot_name = unique_username("dynamic_mm")
    api_key = client.admin_add_bot(username=bot_name, name="Dynamic MM Bot")
    bot_session = client.bot_buildup(bot_name, api_key)

    time.sleep(0.05)

    # Place orders with different spreads
    mid_price = 200
    # Use integer spreads since prices are integers
    spreads = [1, 2, 3, 4]
    volume = 10000000

    for spread in spreads:
        # Remove previous orders
        client.bot_remove_all(bot_session)
        time.sleep(0.05)

        # Place new orders with current spread
        bid_result = client.bot_limit_order(
            bot_session,
            ticker="A",
            volume=volume,
            price=mid_price - spread,
            is_bid=True,
        )
        assert bid_result is not None

        time.sleep(0.05)

        ask_result = client.bot_limit_order(
            bot_session,
            ticker="A",
            volume=volume,
            price=mid_price + spread,
            is_bid=False,
        )
        assert ask_result is not None

        time.sleep(0.05)


def test_large_volume_both_sides(client: ExchangeClient, unique_username) -> None:
    """Test placing large volume orders on both bid and ask sides."""
    client.admin_set_state(1)

    bot_name = unique_username("both_sides_bot")
    api_key = client.admin_add_bot(username=bot_name, name="Both Sides Bot")
    bot_session = client.bot_buildup(bot_name, api_key)

    time.sleep(0.05)

    volume = 10000000

    # Place large bid
    bid_result = client.bot_limit_order(
        bot_session, ticker="A", volume=volume, price=195, is_bid=True
    )
    assert bid_result is not None

    time.sleep(0.05)

    # Place large ask
    ask_result = client.bot_limit_order(
        bot_session, ticker="A", volume=volume, price=205, is_bid=False
    )
    assert ask_result is not None


def test_market_maker_continuous_quoting(
    client: ExchangeClient, unique_username
) -> None:
    """Test market maker continuously updating quotes (simulated)."""
    client.admin_set_state(1)

    bot_name = unique_username("continuous_mm")
    api_key = client.admin_add_bot(username=bot_name, name="Continuous MM")
    bot_session = client.bot_buildup(bot_name, api_key)

    time.sleep(0.05)

    # Simulate continuous quoting by updating quotes multiple times
    base_price = 200
    volume = 10000000
    num_updates = 3

    for i in range(num_updates):
        # Remove old quotes
        if i > 0:
            client.bot_remove_all(bot_session)
            time.sleep(0.05)

        # Adjust price slightly
        mid_price = base_price + i

        # Place new bid
        bid_result = client.bot_limit_order(
            bot_session, ticker="A", volume=volume, price=mid_price - 1, is_bid=True
        )
        assert bid_result is not None

        time.sleep(0.05)

        # Place new ask
        ask_result = client.bot_limit_order(
            bot_session, ticker="A", volume=volume, price=mid_price + 1, is_bid=False
        )
        assert ask_result is not None

        time.sleep(0.1)


def test_large_volume_edge_cases(client: ExchangeClient, unique_username) -> None:
    """Test edge cases with very large volumes."""
    client.admin_set_state(1)

    bot_name = unique_username("edge_bot")
    api_key = client.admin_add_bot(username=bot_name, name="Edge Case Bot")
    bot_session = client.bot_buildup(bot_name, api_key)

    time.sleep(0.05)

    # Test with very large volume
    result = client.bot_limit_order(
        bot_session,
        ticker="A",
        volume=100000000,
        price=200,
        is_bid=True,  # 100 million
    )

    assert result is not None

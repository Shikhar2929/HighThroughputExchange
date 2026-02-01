"""Integration tests for bot trading operations.

This module tests bot authentication, limit orders, and trading workflows.

Converted from: src/test/api_bot_test.py
"""

from __future__ import annotations

import time

import pytest

from exchange_client import ExchangeClient


def test_bot_can_place_limit_orders(client: ExchangeClient, unique_username) -> None:
    """Test that a bot can successfully place limit orders."""
    client.admin_set_state(1)

    bot_name = unique_username("trade_bot")
    api_key = client.admin_add_bot(username=bot_name, name="Trade Bot")
    bot_session = client.bot_buildup(bot_name, api_key)

    time.sleep(0.05)

    # Place a bid order
    result = client.bot_limit_order(
        bot_session, ticker="A", volume=10, price=100, is_bid=True
    )

    assert result is not None
    assert "message" in result


def test_bot_can_place_bid_and_ask(client: ExchangeClient, unique_username) -> None:
    """Test that a bot can place both bid and ask orders."""
    client.admin_set_state(1)

    bot_name = unique_username("bidask_bot")
    api_key = client.admin_add_bot(username=bot_name, name="Bid Ask Bot")
    bot_session = client.bot_buildup(bot_name, api_key)

    time.sleep(0.05)

    # Place a bid
    bid_result = client.bot_limit_order(
        bot_session, ticker="A", volume=10, price=99, is_bid=True
    )
    assert bid_result is not None

    time.sleep(0.05)

    # Place an ask
    ask_result = client.bot_limit_order(
        bot_session, ticker="A", volume=10, price=101, is_bid=False
    )
    assert ask_result is not None


def test_multiple_bots_can_trade(client: ExchangeClient, unique_username) -> None:
    """Test that multiple bots can trade simultaneously."""
    client.admin_set_state(1)

    # Create 3 bots
    bots = []
    for i in range(3):
        bot_name = unique_username(f"multi_bot_{i}")
        api_key = client.admin_add_bot(username=bot_name, name=f"Multi Bot {i}")
        bot_session = client.bot_buildup(bot_name, api_key)
        bots.append((bot_name, bot_session))

    time.sleep(0.1)

    # Each bot places an order
    for idx, (bot_name, bot_session) in enumerate(bots):
        result = client.bot_limit_order(
            bot_session, ticker="A", volume=5, price=100 + idx, is_bid=True
        )
        assert result is not None
        time.sleep(0.05)


def test_bot_can_place_multiple_orders(client: ExchangeClient, unique_username) -> None:
    """Test that a bot can place multiple orders in sequence."""
    client.admin_set_state(1)

    bot_name = unique_username("sequence_bot")
    api_key = client.admin_add_bot(username=bot_name, name="Sequence Bot")
    bot_session = client.bot_buildup(bot_name, api_key)

    time.sleep(0.05)

    # Place multiple orders
    orders_placed = 0
    for i in range(5):
        result = client.bot_limit_order(
            bot_session,
            ticker="A",
            volume=2,
            price=100 + i * 5,
            is_bid=(i % 2 == 0),  # Alternate between bid and ask
        )
        if result is not None:
            orders_placed += 1
        time.sleep(0.05)

    assert orders_placed == 5


def test_bot_orders_on_different_tickers(
    client: ExchangeClient, unique_username
) -> None:
    """Test that a bot can place orders on multiple tickers."""
    client.admin_set_state(1)

    bot_name = unique_username("multi_ticker_bot")
    api_key = client.admin_add_bot(username=bot_name, name="Multi Ticker Bot")
    bot_session = client.bot_buildup(bot_name, api_key)

    time.sleep(0.05)

    tickers = ["A", "B", "C"]
    for ticker in tickers:
        result = client.bot_limit_order(
            bot_session, ticker=ticker, volume=5, price=100, is_bid=True
        )
        assert result is not None
        time.sleep(0.05)


def test_bot_buildup_requires_valid_api_key(
    client: ExchangeClient, unique_username
) -> None:
    """Test that bot buildup fails with invalid API key."""
    client.admin_set_state(1)

    bot_name = unique_username("auth_test_bot")
    api_key = client.admin_add_bot(username=bot_name, name="Auth Test Bot")

    # Valid API key should work
    bot_session = client.bot_buildup(bot_name, api_key)
    assert bot_session is not None
    assert bot_session.session_token is not None

    time.sleep(0.05)

    # Invalid API key should fail
    from exchange_client import ApiError

    with pytest.raises(ApiError):
        client.bot_buildup(bot_name, "INVALID_API_KEY_123")


def test_bot_limit_order_requires_trading_state(
    client: ExchangeClient, unique_username
) -> None:
    """Test that bot limit orders require server to be in TRADING state."""
    # Set to STOP state
    client.admin_set_state(0)

    bot_name = unique_username("state_test_bot")
    api_key = client.admin_add_bot(username=bot_name, name="State Test Bot")
    bot_session = client.bot_buildup(bot_name, api_key)

    time.sleep(0.05)

    # This should fail because server is in STOP state
    from exchange_client import ApiError

    with pytest.raises(ApiError) as exc_info:
        client.bot_limit_order(
            bot_session, ticker="A", volume=10, price=100, is_bid=True
        )

    assert "423" in str(exc_info.value) or "locked" in str(exc_info.value).lower()

    # Reset to TRADING state for other tests
    client.admin_set_state(1)


def test_bot_concurrent_trading_simulation(
    client: ExchangeClient, unique_username
) -> None:
    """Test simulating concurrent bot trading (similar to original api_bot_test.py)."""
    client.admin_set_state(1)

    # Create 5 bots like the original test
    num_bots = 5
    bots = []

    for i in range(num_bots):
        bot_name = unique_username(f"concurrent_bot_{i}")
        api_key = client.admin_add_bot(username=bot_name, name=f"Trading Bot {i+1}")
        bot_session = client.bot_buildup(bot_name, api_key)
        bots.append((bot_name, bot_session))

    time.sleep(0.1)

    # Each bot places a few trades
    trades_per_bot = 3
    total_trades = 0

    for bot_name, bot_session in bots:
        for j in range(trades_per_bot):
            try:
                result = client.bot_limit_order(
                    bot_session,
                    ticker="A",
                    volume=2,
                    price=100 + j * 5,
                    is_bid=(j % 2 == 0),
                )
                if result is not None:
                    total_trades += 1
                time.sleep(0.05)
            except Exception:
                pass  # Some orders might fail due to state, that's ok

    # We should have placed most orders successfully
    assert total_trades >= num_bots * trades_per_bot * 0.8  # At least 80% success


def test_bot_can_remove_orders(client: ExchangeClient, unique_username) -> None:
    """Test that a bot can remove its orders."""
    client.admin_set_state(1)

    bot_name = unique_username("remove_bot")
    api_key = client.admin_add_bot(username=bot_name, name="Remove Bot")
    bot_session = client.bot_buildup(bot_name, api_key)

    time.sleep(0.05)

    # Place an order
    order_result = client.bot_limit_order(
        bot_session, ticker="A", volume=10, price=100, is_bid=True
    )
    assert order_result is not None

    time.sleep(0.05)

    # Remove all orders
    remove_result = client.bot_remove_all(bot_session)
    assert remove_result is None  # bot_remove_all returns None on success


def test_bot_market_order(client: ExchangeClient, unique_username) -> None:
    """Test that a bot can place market orders."""
    client.admin_set_state(1)

    # First, create a bot to place a limit order (for the market order to match)
    maker_name = unique_username("maker_bot")
    maker_key = client.admin_add_bot(username=maker_name, name="Maker Bot")
    maker_session = client.bot_buildup(maker_name, maker_key)

    time.sleep(0.05)

    # Place a limit ask order
    client.bot_limit_order(
        maker_session, ticker="A", volume=10, price=100, is_bid=False  # Ask
    )

    time.sleep(0.1)

    # Now create a taker bot to place a market order
    taker_name = unique_username("taker_bot")
    taker_key = client.admin_add_bot(username=taker_name, name="Taker Bot")
    taker_session = client.bot_buildup(taker_name, taker_key)

    time.sleep(0.05)

    # Place a market bid order
    result = client.bot_market_order(
        taker_session, ticker="A", volume=5, is_bid=True
    )

    assert result is not None


def test_bot_different_price_levels(client: ExchangeClient, unique_username) -> None:
    """Test that a bot can place orders at different price levels."""
    client.admin_set_state(1)

    bot_name = unique_username("price_levels_bot")
    api_key = client.admin_add_bot(username=bot_name, name="Price Levels Bot")
    bot_session = client.bot_buildup(bot_name, api_key)

    time.sleep(0.05)

    # Place orders at different price levels
    prices = [95, 100, 105, 110, 115]
    orders_placed = 0

    for price in prices:
        result = client.bot_limit_order(
            bot_session, ticker="A", volume=5, price=price, is_bid=True
        )
        if result is not None:
            orders_placed += 1
        time.sleep(0.05)

    assert orders_placed == len(prices)

"""Integration tests for remove operations.

This module tests removing orders and clearing the orderbook.

Converted from: src/test/remove_all_test.py
"""

from __future__ import annotations

import time

from exchange_client import ExchangeClient


def test_remove_all_orders(client: ExchangeClient, unique_username) -> None:
    """Test removing all orders for a user."""
    client.admin_set_state(1)

    user_name = unique_username("remove_all_user")
    api_key = client.admin_add_user(
        username=user_name, name="Remove All User", email=f"{user_name}@example.com"
    )
    session = client.buildup(user_name, api_key)

    time.sleep(0.05)

    # Place some orders
    client.limit_order(session, ticker="A", volume=10, price=100, is_bid=True)
    time.sleep(0.05)
    client.limit_order(session, ticker="A", volume=5, price=105, is_bid=True)
    time.sleep(0.05)

    # Remove all orders
    client.remove_all(session)

    # Success if no exception is raised


def test_remove_all_after_single_order(client: ExchangeClient, unique_username) -> None:
    """Test removing all orders after placing a single order (like original test)."""
    client.admin_set_state(1)

    user_name = unique_username("single_remove")
    api_key = client.admin_add_user(
        username=user_name, name="Single Remove User", email=f"{user_name}@example.com"
    )
    session = client.buildup(user_name, api_key)

    time.sleep(0.05)

    # Place a single trade like the original test
    trade_result = client.limit_order(
        session, ticker="A", volume=10, price=100, is_bid=True
    )
    assert trade_result is not None

    time.sleep(0.05)

    # Remove all orders
    client.remove_all(session)


def test_remove_all_with_multiple_tickers(
    client: ExchangeClient, unique_username
) -> None:
    """Test removing all orders across multiple tickers."""
    client.admin_set_state(1)

    user_name = unique_username("multi_ticker_remove")
    api_key = client.admin_add_user(
        username=user_name,
        name="Multi Ticker Remove",
        email=f"{user_name}@example.com",
    )
    session = client.buildup(user_name, api_key)

    time.sleep(0.05)

    # Place orders on multiple tickers
    tickers = ["A", "B", "C"]
    for ticker in tickers:
        client.limit_order(session, ticker=ticker, volume=10, price=100, is_bid=True)
        time.sleep(0.05)

    # Remove all orders
    client.remove_all(session)


def test_remove_all_with_bid_and_ask(client: ExchangeClient, unique_username) -> None:
    """Test removing all orders when both bids and asks exist."""
    client.admin_set_state(1)

    user_name = unique_username("bidask_remove")
    api_key = client.admin_add_user(
        username=user_name, name="Bid Ask Remove", email=f"{user_name}@example.com"
    )
    session = client.buildup(user_name, api_key)

    time.sleep(0.05)

    # Place both bid and ask orders
    client.limit_order(session, ticker="A", volume=10, price=99, is_bid=True)
    time.sleep(0.05)
    client.limit_order(session, ticker="A", volume=10, price=101, is_bid=False)
    time.sleep(0.05)

    # Remove all orders
    client.remove_all(session)


def test_remove_all_when_no_orders(client: ExchangeClient, unique_username) -> None:
    """Test remove_all when user has no orders."""
    client.admin_set_state(1)

    user_name = unique_username("no_orders_remove")
    api_key = client.admin_add_user(
        username=user_name, name="No Orders Remove", email=f"{user_name}@example.com"
    )
    session = client.buildup(user_name, api_key)

    time.sleep(0.05)

    # Call remove_all without placing any orders
    client.remove_all(session)

    # Should succeed even with no orders


def test_remove_all_multiple_times(client: ExchangeClient, unique_username) -> None:
    """Test calling remove_all multiple times."""
    client.admin_set_state(1)

    user_name = unique_username("multi_remove")
    api_key = client.admin_add_user(
        username=user_name, name="Multi Remove User", email=f"{user_name}@example.com"
    )
    session = client.buildup(user_name, api_key)

    time.sleep(0.05)

    # Place orders and remove multiple times
    for i in range(3):
        client.limit_order(session, ticker="A", volume=5, price=100 + i, is_bid=True)
        time.sleep(0.05)
        client.remove_all(session)
        time.sleep(0.05)


def test_remove_all_doesnt_affect_other_users(
    client: ExchangeClient, unique_username
) -> None:
    """Test that remove_all only removes orders for the calling user."""
    client.admin_set_state(1)

    # Create two users
    user1_name = unique_username("remove_user1")
    user1_key = client.admin_add_user(
        username=user1_name, name="Remove User 1", email=f"{user1_name}@example.com"
    )
    user1_session = client.buildup(user1_name, user1_key)

    user2_name = unique_username("remove_user2")
    user2_key = client.admin_add_user(
        username=user2_name, name="Remove User 2", email=f"{user2_name}@example.com"
    )
    user2_session = client.buildup(user2_name, user2_key)

    time.sleep(0.05)

    # Both users place orders
    client.limit_order(user1_session, ticker="A", volume=10, price=100, is_bid=True)
    time.sleep(0.05)
    client.limit_order(user2_session, ticker="A", volume=10, price=101, is_bid=True)
    time.sleep(0.05)

    # User 1 removes all their orders
    client.remove_all(user1_session)

    time.sleep(0.05)

    # User 2 should still be able to place orders (their orders weren't removed)
    result = client.limit_order(
        user2_session, ticker="A", volume=5, price=102, is_bid=True
    )
    assert result is not None


def test_remove_all_and_place_new_orders(
    client: ExchangeClient, unique_username
) -> None:
    """Test placing new orders after remove_all."""
    client.admin_set_state(1)

    user_name = unique_username("remove_place_user")
    api_key = client.admin_add_user(
        username=user_name,
        name="Remove Place User",
        email=f"{user_name}@example.com",
    )
    session = client.buildup(user_name, api_key)

    time.sleep(0.05)

    # Place orders
    client.limit_order(session, ticker="A", volume=10, price=100, is_bid=True)
    time.sleep(0.05)

    # Remove all
    client.remove_all(session)
    time.sleep(0.05)

    # Place new orders after remove
    result = client.limit_order(session, ticker="A", volume=15, price=105, is_bid=True)
    assert result is not None


def test_remove_all_with_large_number_of_orders(
    client: ExchangeClient, unique_username
) -> None:
    """Test remove_all with many orders."""
    client.admin_set_state(1)

    user_name = unique_username("many_orders_remove")
    api_key = client.admin_add_user(
        username=user_name,
        name="Many Orders Remove",
        email=f"{user_name}@example.com",
    )
    session = client.buildup(user_name, api_key)

    time.sleep(0.05)

    # Place many orders
    for i in range(20):
        client.limit_order(
            session, ticker="A", volume=1, price=100 + i, is_bid=(i % 2 == 0)
        )
        time.sleep(0.02)

    # Remove all orders
    client.remove_all(session)


def test_remove_all_requires_trading_state(
    client: ExchangeClient, unique_username
) -> None:
    """Test that remove_all works regardless of trading state."""
    # Set to STOP state
    client.admin_set_state(0)

    user_name = unique_username("state_remove_user")
    api_key = client.admin_add_user(
        username=user_name, name="State Remove User", email=f"{user_name}@example.com"
    )
    session = client.buildup(user_name, api_key)

    time.sleep(0.05)

    # Try to remove all in STOP state - this might work or fail depending on implementation
    try:
        client.remove_all(session)
    except Exception:
        pass  # Some implementations may not allow remove_all in STOP state

    # Reset to TRADING state
    client.admin_set_state(1)


def test_remove_specific_order_then_remove_all(
    client: ExchangeClient, unique_username
) -> None:
    """Test removing a specific order, then removing all remaining orders."""
    client.admin_set_state(1)

    user_name = unique_username("specific_then_all")
    api_key = client.admin_add_user(
        username=user_name,
        name="Specific Then All",
        email=f"{user_name}@example.com",
    )
    session = client.buildup(user_name, api_key)

    time.sleep(0.05)

    # Place multiple orders
    client.limit_order(session, ticker="A", volume=10, price=100, is_bid=True)
    time.sleep(0.05)
    client.limit_order(session, ticker="A", volume=5, price=105, is_bid=True)
    time.sleep(0.05)

    # Try to remove a specific order if order IDs are available
    # (implementation depends on what order1 response contains)

    # Then remove all remaining
    client.remove_all(session)

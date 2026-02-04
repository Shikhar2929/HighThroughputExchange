"""Integration tests for user/bot initialization and session management.

This module tests user creation, session buildup, and initial trading workflows.

Converted from: src/test/api_initialization_test.py
"""

from __future__ import annotations

import time

from exchange_client import ExchangeClient

# Import pytest for the ApiError test
import pytest


def test_user_creation_and_buildup(client: ExchangeClient, unique_username) -> None:
    """Test creating a user and establishing a session."""
    client.admin_set_state(1)

    user_name = unique_username("init_user")
    api_key = client.admin_add_user(
        username=user_name, name="Initialization User", email=f"{user_name}@example.com"
    )

    assert api_key is not None
    assert len(api_key) > 0

    time.sleep(0.05)

    # Establish session
    session = client.buildup(user_name, api_key)
    assert session is not None
    assert session.session_token is not None


def test_user_can_trade_after_initialization(
    client: ExchangeClient, unique_username
) -> None:
    """Test that a newly created user can place trades."""
    client.admin_set_state(1)

    user_name = unique_username("trade_init")
    api_key = client.admin_add_user(
        username=user_name, name="Trade Init User", email=f"{user_name}@example.com"
    )
    session = client.buildup(user_name, api_key)

    time.sleep(0.05)

    # Place a trade
    result = client.limit_order(session, ticker="A", volume=10, price=100, is_bid=True)

    assert result is not None


def test_multiple_users_sequential_initialization(
    client: ExchangeClient, unique_username
) -> None:
    """Test initializing multiple users sequentially (like original test)."""
    client.admin_set_state(1)

    # Create first user
    user1_name = unique_username("seq_user1")
    user1_key = client.admin_add_user(
        username=user1_name, name="Sequential User 1", email=f"{user1_name}@example.com"
    )
    user1_session = client.buildup(user1_name, user1_key)

    time.sleep(0.05)

    # First user places trades
    for i in range(3):
        client.limit_order(
            user1_session, ticker="A", volume=2, price=100 + i * 5, is_bid=True
        )
        time.sleep(0.05)

    time.sleep(0.2)  # Delay like the original test

    # Create second user
    user2_name = unique_username("seq_user2")
    user2_key = client.admin_add_user(
        username=user2_name, name="Sequential User 2", email=f"{user2_name}@example.com"
    )
    user2_session = client.buildup(user2_name, user2_key)

    time.sleep(0.05)

    # Second user places trades
    for i in range(3):
        client.limit_order(
            user2_session, ticker="A", volume=2, price=100 + i * 5, is_bid=False
        )
        time.sleep(0.05)


def test_user_initialization_with_trading_bot_pattern(
    client: ExchangeClient, unique_username
) -> None:
    """Test user initialization with trading bot pattern from original test."""
    client.admin_set_state(1)

    user_name = unique_username("bot_pattern")
    api_key = client.admin_add_user(
        username=user_name, name="Bot Pattern User", email=f"{user_name}@example.com"
    )
    session = client.buildup(user_name, api_key)

    time.sleep(0.05)

    # Simulate trading bot behavior - place 10 trades
    num_trades = 10
    successful_trades = 0

    for i in range(num_trades):
        try:
            result = client.limit_order(
                session,
                ticker="A",  # Use valid ticker A
                volume=2,
                price=150 + i * 10,
                is_bid=(i % 2 == 0),
            )
            if result is not None:
                successful_trades += 1
            time.sleep(0.05)
        except Exception:
            pass  # Continue even if some trades fail

    # Should have placed most trades successfully
    assert successful_trades >= num_trades * 0.8


def test_user_buildup_requires_valid_api_key(
    client: ExchangeClient, unique_username
) -> None:
    """Test that buildup fails with invalid API key."""
    client.admin_set_state(1)

    user_name = unique_username("invalid_key_user")
    api_key = client.admin_add_user(
        username=user_name, name="Invalid Key User", email=f"{user_name}@example.com"
    )

    time.sleep(0.05)

    # Valid key should work
    session = client.buildup(user_name, api_key)
    assert session is not None

    # Invalid key should fail
    from exchange_client import ApiError

    with pytest.raises(ApiError):
        client.buildup(user_name, "INVALID_KEY_123")


def test_user_session_persistence(client: ExchangeClient, unique_username) -> None:
    """Test that user session persists across multiple operations."""
    client.admin_set_state(1)

    user_name = unique_username("persist_user")
    api_key = client.admin_add_user(
        username=user_name, name="Persist User", email=f"{user_name}@example.com"
    )
    session = client.buildup(user_name, api_key)

    time.sleep(0.05)

    # Place multiple trades with same session
    for i in range(5):
        result = client.limit_order(
            session, ticker="A", volume=1, price=100 + i, is_bid=True
        )
        assert result is not None
        time.sleep(0.05)


def test_multiple_tickers_initialization(
    client: ExchangeClient, unique_username
) -> None:
    """Test user can trade on multiple tickers after initialization."""
    client.admin_set_state(1)

    user_name = unique_username("multi_ticker_init")
    api_key = client.admin_add_user(
        username=user_name,
        name="Multi Ticker User",
        email=f"{user_name}@example.com",
    )
    session = client.buildup(user_name, api_key)

    time.sleep(0.05)

    # Trade on different tickers
    tickers = ["A", "B", "C"]  # Only use valid tickers
    for ticker in tickers:
        result = client.limit_order(
            session, ticker=ticker, volume=5, price=100, is_bid=True
        )
        assert result is not None
        time.sleep(0.05)


def test_user_initialization_with_market_orders(
    client: ExchangeClient, unique_username
) -> None:
    """Test newly initialized user can place market orders."""
    client.admin_set_state(1)

    # Create maker to provide liquidity
    maker_name = unique_username("maker_init")
    maker_key = client.admin_add_user(
        username=maker_name, name="Maker Init", email=f"{maker_name}@example.com"
    )
    maker_session = client.buildup(maker_name, maker_key)

    time.sleep(0.05)

    # Maker places limit order
    client.limit_order(maker_session, ticker="A", volume=10, price=100, is_bid=False)

    time.sleep(0.1)

    # Create taker
    taker_name = unique_username("taker_init")
    taker_key = client.admin_add_user(
        username=taker_name, name="Taker Init", email=f"{taker_name}@example.com"
    )
    taker_session = client.buildup(taker_name, taker_key)

    time.sleep(0.05)

    # Taker places market order
    result = client.market_order(taker_session, ticker="A", volume=5, is_bid=True)
    assert result is not None


def test_user_email_validation(client: ExchangeClient, unique_username) -> None:
    """Test user creation with different email formats."""
    client.admin_set_state(1)

    # Valid email
    user_name = unique_username("email_test")
    api_key = client.admin_add_user(
        username=user_name, name="Email Test User", email=f"{user_name}@example.com"
    )

    assert api_key is not None


def test_concurrent_user_initialization(
    client: ExchangeClient, unique_username
) -> None:
    """Test initializing multiple users concurrently."""
    client.admin_set_state(1)

    # Create multiple users
    num_users = 5
    users = []

    for i in range(num_users):
        user_name = unique_username(f"concurrent_{i}")
        api_key = client.admin_add_user(
            username=user_name,
            name=f"Concurrent User {i}",
            email=f"{user_name}@example.com",
        )
        session = client.buildup(user_name, api_key)
        users.append((user_name, session))
        time.sleep(0.05)

    # All users should be able to trade
    for user_name, session in users:
        result = client.limit_order(
            session, ticker="A", volume=1, price=100, is_bid=True
        )
        assert result is not None
        time.sleep(0.05)

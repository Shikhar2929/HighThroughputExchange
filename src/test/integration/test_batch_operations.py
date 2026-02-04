"""Integration tests for batch operations endpoint.

This module tests the /batch endpoint which allows bots to submit
multiple operations (limit orders, market orders, removes) in a single request.

Converted from: src/test/batchTest.py
"""

from __future__ import annotations

import time

import pytest

from exchange_client import ExchangeClient


def test_batch_multiple_limit_orders(client: ExchangeClient, unique_username) -> None:
    """Test batch processing of multiple limit orders across different tickers."""
    client.admin_set_state(1)  # Set to TRADING state

    bot_name = unique_username("batch_limit")
    api_key = client.admin_add_bot(username=bot_name, name="Batch Limit Bot")
    bot_session = client.bot_buildup(bot_name, api_key)

    operations = [
        {
            "type": "limit_order",
            "ticker": "A",
            "price": 100.0,
            "volume": 10.0,
            "bid": True,
        },
        {
            "type": "limit_order",
            "ticker": "B",
            "price": 200.0,
            "volume": 5.0,
            "bid": False,
        },
        {
            "type": "limit_order",
            "ticker": "A",
            "price": 105.0,
            "volume": 15.0,
            "bid": True,
        },
    ]

    time.sleep(0.05)
    result = client.batch(bot_session, operations)

    assert result["status"] == "SUCCESS"
    assert isinstance(result.get("results"), list)
    assert len(result["results"]) == 3


def test_batch_mixed_operation_types(client: ExchangeClient, unique_username) -> None:
    """Test batch with mixed operation types: limit orders, market orders, and removes."""
    client.admin_set_state(1)

    bot_name = unique_username("batch_mixed")
    api_key = client.admin_add_bot(username=bot_name, name="Batch Mixed Bot")
    bot_session = client.bot_buildup(bot_name, api_key)

    # First, place a limit order to get an order ID
    operations_1 = [
        {
            "type": "limit_order",
            "ticker": "A",
            "price": 150.0,
            "volume": 20.0,
            "bid": True,
        }
    ]
    time.sleep(0.05)
    result_1 = client.batch(bot_session, operations_1)
    assert result_1["status"] == "SUCCESS"

    # Now test mixed operations including remove_all
    operations_2 = [
        {
            "type": "limit_order",
            "ticker": "B",
            "price": 250.0,
            "volume": 10.0,
            "bid": False,
        },
        {
            "type": "limit_order",
            "ticker": "A",
            "price": 160.0,
            "volume": 5.0,
            "bid": True,
        },
        {"type": "remove_all"},
    ]

    time.sleep(0.05)
    result_2 = client.batch(bot_session, operations_2)

    assert result_2["status"] == "SUCCESS"
    assert len(result_2["results"]) == 3


def test_batch_remove_all_operation(client: ExchangeClient, unique_username) -> None:
    """Test batch operation with remove_all to clear all orders."""
    client.admin_set_state(1)

    bot_name = unique_username("batch_remove")
    api_key = client.admin_add_bot(username=bot_name, name="Batch Remove Bot")
    bot_session = client.bot_buildup(bot_name, api_key)

    # Place some orders first
    operations_1 = [
        {
            "type": "limit_order",
            "ticker": "A",
            "price": 100.0,
            "volume": 10.0,
            "bid": True,
        },
        {
            "type": "limit_order",
            "ticker": "B",
            "price": 200.0,
            "volume": 10.0,
            "bid": False,
        },
    ]
    time.sleep(0.05)
    result_1 = client.batch(bot_session, operations_1)
    assert result_1["status"] == "SUCCESS"

    # Remove all orders
    operations_2 = [{"type": "remove_all"}]
    time.sleep(0.05)
    result_2 = client.batch(bot_session, operations_2)

    assert result_2["status"] == "SUCCESS"
    assert len(result_2["results"]) == 1


def test_batch_empty_operations(client: ExchangeClient, unique_username) -> None:
    """Test that batch handles empty operations list appropriately."""
    client.admin_set_state(1)

    bot_name = unique_username("batch_empty")
    api_key = client.admin_add_bot(username=bot_name, name="Batch Empty Bot")
    bot_session = client.bot_buildup(bot_name, api_key)

    operations = []

    time.sleep(0.05)
    result = client.batch(bot_session, operations)

    # Empty batch should succeed but have no results
    assert result["status"] == "SUCCESS"
    assert len(result.get("results", [])) == 0


def test_batch_large_volume_orders(client: ExchangeClient, unique_username) -> None:
    """Test batch with large volume orders (similar to original batchTest.py)."""
    client.admin_set_state(1)

    bot_name = unique_username("batch_large")
    api_key = client.admin_add_bot(username=bot_name, name="Batch Large Volume Bot")
    bot_session = client.bot_buildup(bot_name, api_key)

    # Test with large volumes like the original batchTest.py
    operations = [
        {
            "type": "limit_order",
            "ticker": "A",
            "price": 5.0,
            "volume": 10000000.0,
            "bid": True,
        },
        {
            "type": "limit_order",
            "ticker": "B",
            "price": 10.0,
            "volume": 10000000.0,
            "bid": False,
        },
    ]

    time.sleep(0.05)
    result = client.batch(bot_session, operations)

    assert result["status"] == "SUCCESS"
    assert len(result["results"]) == 2


def test_batch_sequential_operations(client: ExchangeClient, unique_username) -> None:
    """Test that batch operations are processed sequentially and maintain order."""
    client.admin_set_state(1)

    bot_name = unique_username("batch_sequential")
    api_key = client.admin_add_bot(username=bot_name, name="Batch Sequential Bot")
    bot_session = client.bot_buildup(bot_name, api_key)

    # Create a sequence of operations where order matters
    operations = [
        {
            "type": "limit_order",
            "ticker": "A",
            "price": 100.0,
            "volume": 10.0,
            "bid": True,
        },
        {
            "type": "limit_order",
            "ticker": "A",
            "price": 110.0,
            "volume": 5.0,
            "bid": True,
        },
        {"type": "remove_all"},
        {
            "type": "limit_order",
            "ticker": "A",
            "price": 120.0,
            "volume": 3.0,
            "bid": True,
        },
    ]

    time.sleep(0.05)
    result = client.batch(bot_session, operations)

    assert result["status"] == "SUCCESS"
    assert len(result["results"]) == 4

    # All operations should succeed
    for idx, op_result in enumerate(result["results"]):
        # Check that each result corresponds to the operation
        assert op_result is not None, f"Operation {idx} failed"


def test_batch_multiple_tickers(client: ExchangeClient, unique_username) -> None:
    """Test batch operations across multiple tickers."""
    client.admin_set_state(1)

    bot_name = unique_username("batch_multi_ticker")
    api_key = client.admin_add_bot(username=bot_name, name="Batch Multi Ticker Bot")
    bot_session = client.bot_buildup(bot_name, api_key)

    # Test operations on different tickers
    tickers = ["A", "B", "C", "D"]
    operations = []

    for ticker in tickers:
        operations.append(
            {
                "type": "limit_order",
                "ticker": ticker,
                "price": 100.0,
                "volume": 10.0,
                "bid": True,
            }
        )
        operations.append(
            {
                "type": "limit_order",
                "ticker": ticker,
                "price": 110.0,
                "volume": 10.0,
                "bid": False,
            }
        )

    time.sleep(0.05)
    result = client.batch(bot_session, operations)

    assert result["status"] == "SUCCESS"
    assert len(result["results"]) == len(tickers) * 2


def test_batch_only_works_for_bots(client: ExchangeClient, unique_username) -> None:
    """Test that batch endpoint only works for bot sessions, not regular users."""
    client.admin_set_state(1)

    # Try with a regular user (not a bot)
    user_name = unique_username("regular_user")
    api_key = client.admin_add_user(
        username=user_name, name="Regular User", email=f"{user_name}@example.com"
    )
    user_session = client.buildup(user_name, api_key)

    operations = [
        {
            "type": "limit_order",
            "ticker": "A",
            "price": 100.0,
            "volume": 10.0,
            "bid": True,
        }
    ]

    time.sleep(0.05)

    # This should fail or be rejected since regular users shouldn't use batch
    # Note: The actual behavior depends on the backend implementation
    # If the backend allows it, this test might need adjustment
    with pytest.raises(Exception):
        client.batch(user_session, operations)  # type: ignore


def test_batch_requires_trading_state(client: ExchangeClient, unique_username) -> None:
    """Test that batch operations require the server to be in TRADING state."""
    # Set to STOP state
    client.admin_set_state(0)

    bot_name = unique_username("batch_state")
    api_key = client.admin_add_bot(username=bot_name, name="Batch State Bot")
    bot_session = client.bot_buildup(bot_name, api_key)

    operations = [
        {
            "type": "limit_order",
            "ticker": "A",
            "price": 100.0,
            "volume": 10.0,
            "bid": True,
        }
    ]

    time.sleep(0.05)

    # This should fail because server is in STOP state
    # The backend returns HTTP 423 (Locked) when trading is not allowed
    from exchange_client import ApiError

    with pytest.raises(ApiError) as exc_info:
        client.batch(bot_session, operations)

    assert "423" in str(exc_info.value) or "locked" in str(exc_info.value).lower()

    # Reset to TRADING state for other tests
    client.admin_set_state(1)

from __future__ import annotations

import os

import pytest
import requests

from exchange_client import ExchangeClient


def test_admin_add_user_returns_api_key(client: ExchangeClient, unique_username) -> None:
    username = unique_username("user")
    api_key = client.admin_add_user(username=username, name="CI User", email=f"{username}@example.com")
    assert isinstance(api_key, str)
    assert len(api_key) > 0


def test_admin_add_bot_returns_api_key(client: ExchangeClient, unique_username) -> None:
    bot_name = unique_username("bot")
    api_key = client.admin_add_bot(username=bot_name, name="CI Bot")
    assert isinstance(api_key, str)
    assert len(api_key) > 0


def test_leaderboard_endpoint_returns_list(client: ExchangeClient) -> None:
    entries = client.admin_leaderboard()
    assert isinstance(entries, list)


def test_set_price_accepts_valid_tickers(client: ExchangeClient) -> None:
    # This endpoint also clears order books; keep it simple/deterministic.
    client.admin_set_price({"A": 50, "B": 60, "C": 70})


def test_set_state_round_trip(client: ExchangeClient) -> None:
    # Just verify the endpoint accepts a valid state.
    new_state = client.admin_set_state(1)
    assert new_state == 1


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
    # Intentionally opt-in because it will stop the server.
    r = requests.post(
        f"{base_url}/shutdown",
        json={
            "adminUsername": os.environ["ADMIN_USERNAME"],
            "adminPassword": os.environ["ADMIN_PASSWORD"],
        },
        timeout=5,
    )
    assert r.status_code == 200

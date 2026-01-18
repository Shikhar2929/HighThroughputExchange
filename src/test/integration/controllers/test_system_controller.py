from __future__ import annotations

import time

from exchange_client import ExchangeClient


def test_get_state_responds(client: ExchangeClient) -> None:
    state = client.get_state()
    assert state in (0, 1, 2)


def test_get_details_returns_payload(client: ExchangeClient, unique_username) -> None:
    client.admin_set_state(1)

    username = unique_username("details")
    api_key = client.admin_add_user(username=username, name="CI Details", email=f"{username}@example.com")
    session = client.buildup(username, api_key)
    try:
        details = client.get_details(session)
        assert isinstance(details, str)
        assert len(details) >= 0
    finally:
        time.sleep(0.05)
        client.teardown(session)

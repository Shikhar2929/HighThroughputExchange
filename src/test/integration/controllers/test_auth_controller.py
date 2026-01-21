from __future__ import annotations

import time

from exchange_client import ExchangeClient


def test_admin_page_success(client: ExchangeClient) -> None:
    client.admin_page()


def test_private_page_success(client: ExchangeClient, unique_username) -> None:
    client.admin_set_state(1)

    username = unique_username("priv")
    api_key = client.admin_add_user(
        username=username, name="CI Private", email=f"{username}@example.com"
    )
    session = client.buildup(username, api_key)
    try:
        client.private_page(session)
    finally:
        # Reduce flakiness around immediate teardown/rate limiting.
        time.sleep(0.05)
        client.teardown(session)

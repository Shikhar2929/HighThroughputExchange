from __future__ import annotations

import time

from exchange_client import ExchangeClient


def test_buildup_and_teardown_success(client: ExchangeClient, unique_username) -> None:
    username = unique_username("sess")
    api_key = client.admin_add_user(
        username=username, name="CI Session", email=f"{username}@example.com"
    )
    session = client.buildup(username, api_key)

    # Small delay to reduce flakiness around immediate teardown/rate limiting.
    time.sleep(0.05)
    client.teardown(session)


def test_bot_buildup_success(client: ExchangeClient, unique_username) -> None:
    client.admin_set_state(1)

    bot_name = unique_username("botsess")
    api_key = client.admin_add_bot(username=bot_name, name="CI Bot")
    bot_session = client.bot_buildup(bot_name, api_key)

    assert bot_session.username == bot_name
    assert isinstance(bot_session.session_token, str)
    assert len(bot_session.session_token) > 0

from __future__ import annotations

import time

from exchange_client import ExchangeClient


def test_batch_endpoint_processes_operations(client: ExchangeClient, unique_username) -> None:
    client.admin_set_state(1)

    bot_name = unique_username("batch")
    api_key = client.admin_add_bot(username=bot_name, name="CI Batch")
    bot_session = client.bot_buildup(bot_name, api_key)

    operations = [
        {"type": "limit_order", "ticker": "A", "price": 100, "volume": 1, "bid": True},
        {"type": "remove_all"},
    ]

    # Small delay can help avoid rate limiter in tight CI runs.
    time.sleep(0.05)
    data = client.batch(bot_session, operations)
    assert data["status"] == "SUCCESS"
    assert isinstance(data.get("results"), list)
    assert len(data["results"]) == len(operations)

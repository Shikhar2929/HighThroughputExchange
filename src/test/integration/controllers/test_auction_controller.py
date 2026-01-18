from __future__ import annotations

from exchange_client import ExchangeClient


def test_auction_endpoints_flow(client: ExchangeClient, unique_username) -> None:
    # Enable auction state.
    client.admin_set_state(2)

    username = unique_username("auc")
    api_key = client.admin_add_user(username=username, name="CI Auction", email=f"{username}@example.com")
    session = client.buildup(username, api_key)

    try:
        client.bid_auction(session, bid=1)
        lead = client.get_leading_auction_bid()
        assert "user" in lead and "bid" in lead

        term = client.terminate_auction()
        assert "user" in term and "bid" in term
    finally:
        client.teardown(session)
        # Put server back into trade mode for any other tests.
        client.admin_set_state(1)

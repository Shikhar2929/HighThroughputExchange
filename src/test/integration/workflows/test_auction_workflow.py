from __future__ import annotations

from exchange_client import ExchangeClient


def test_auction_full_flow_bid_lead_terminate(
    client: ExchangeClient, unique_username
) -> None:
    # AdminController -> SessionController -> AuctionController
    client.admin_set_state(2)

    username = unique_username("wf_auc")
    api_key = client.admin_add_user(
        username=username, name="WF Auction", email=f"{username}@example.com"
    )
    session = client.buildup(username, api_key)

    try:
        client.bid_auction(session, bid=1)
        lead = client.get_leading_auction_bid()
        assert "user" in lead and "bid" in lead

        term = client.terminate_auction()
        assert "user" in term and "bid" in term
    finally:
        client.teardown(session)
        client.admin_set_state(1)

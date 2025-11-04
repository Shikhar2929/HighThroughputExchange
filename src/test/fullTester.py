import admin
import json
import urllib.request
import OrderBook
from env_loader import load_env, getenv

load_env()
URL = getenv("HTTP_URL", "http://localhost:8080")


def user_buildup(username, api_key):
    form_data = {"username": username, "apiKey": api_key}
    req = urllib.request.Request(
        URL + "/buildup", data=json.dumps(form_data).encode("utf-8"), method="POST"
    )
    req.add_header("Content-Type", "application/json")
    return json.loads(urllib.request.urlopen(req).read().decode("utf-8"))


def place_trade(username, session_token, ticker, volume, price, is_bid):
    form_data = {
        "username": username,
        "sessionToken": session_token,
        "ticker": ticker,
        "volume": volume,
        "price": price,
        "isBid": is_bid,
    }
    req = urllib.request.Request(
        URL + "/limit_order", data=json.dumps(form_data).encode("utf-8"), method="POST"
    )
    req.add_header("Content-Type", "application/json")
    return json.loads(urllib.request.urlopen(req).read().decode("utf-8"))


if __name__ == "__main__":
    bot_sessions = []
    usernames = admin.create_random_users(2)
    order_book = None
    for username, user_data in usernames:
        session_data = user_buildup(username, user_data["apiKey"])
        print("Session data:", session_data)  # Debugging
        order_book_data = session_data.get("orderBookData")
        if not order_book:
            try:
                order_book = OrderBook.OrderBook(
                    json.loads(session_data["orderBookData"])
                )
            except json.JSONDecodeError:
                print("Error decoding orderbook data")
                order_book = None
        bot_sessions.append(
            {"username": username, "session_token": session_data["sessionToken"]}
        )
    # websocketConnection.run_socket(order_book)

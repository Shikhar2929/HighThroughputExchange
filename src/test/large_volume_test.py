import json
import urllib.request
import random
import time
from env_loader import load_env, getenv

load_env()
URL = getenv('HTTP_URL', 'http://localhost:8080')

# Create bot
def create_bot(username):
    form_data = {
        'adminUsername': getenv('ADMIN_USERNAME'),
        'adminPassword': getenv('ADMIN_PASSWORD'),
        'username': username
    }
    req = urllib.request.Request(URL + '/add_bot', data=json.dumps(form_data).encode('utf-8'), method='POST')
    req.add_header('Content-Type', 'application/json')
    return json.loads(urllib.request.urlopen(req).read().decode('utf-8'))

# Get session
def bot_buildup(username, api_key):
    req = urllib.request.Request(
        URL + "/bot_buildup",
        data=json.dumps({"username": username, "apiKey": api_key}).encode("utf-8"),
        method="POST",
    )
    req.add_header("Content-Type", "application/json")
    return json.loads(urllib.request.urlopen(req).read().decode("utf-8"))


# Place trade
def place_trade(username, session_token, ticker, volume, price, is_bid):
    trade_details = {
        "username": username,
        "sessionToken": session_token,
        "ticker": ticker,
        "volume": volume,
        "price": price,
        "isBid": is_bid,
    }
    print(f"Placing trade: {trade_details}")
    req = urllib.request.Request(
        URL + "/bot_limit_order",
        data=json.dumps(trade_details).encode("utf-8"),
        method="POST",
    )
    req.add_header("Content-Type", "application/json")
    response = json.loads(urllib.request.urlopen(req).read().decode("utf-8"))
    print(f"Trade response: {response}")
    return response


# Market making bot
def market_make(username, session_token, ticker, price, volume=10000000):
    while True:
        bid_price = round(price - random.uniform(0.5, 1.5), 2)
        ask_price = round(price + random.uniform(0.5, 1.5), 2)
        print(f"Market making at bid: {bid_price}, ask: {ask_price}")
        place_trade(username, session_token, ticker, volume, bid_price, True)
        place_trade(username, session_token, ticker, volume, ask_price, False)
        time.sleep(1)


# Main
if __name__ == "__main__":
    username = "marketmaker"
    user_data = create_bot(username)
    session_data = bot_buildup(username, user_data["apiKey"])
    market_make(username, session_data["sessionToken"], "A", 200)

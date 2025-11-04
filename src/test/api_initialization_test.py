import json
import urllib.request
import random
import time
from env_loader import load_env, getenv

load_env()
URL = getenv('HTTP_URL', 'http://localhost:8080')

# Function to create a user
def create_user(username, name, email):
    form_data = {
    'adminUsername': getenv('ADMIN_USERNAME'),
    'adminPassword': getenv('ADMIN_PASSWORD'),
        'username': username,
        'name': name,
        'email': email
    }
    req = urllib.request.Request(
        URL + "/add_user", data=json.dumps(form_data).encode("utf-8"), method="POST"
    )
    req.add_header("Content-Type", "application/json")
    return json.loads(urllib.request.urlopen(req).read().decode("utf-8"))


# Function to initiate user session
def user_buildup(username, api_key):
    form_data = {"username": username, "apiKey": api_key}
    req = urllib.request.Request(
        URL + "/buildup", data=json.dumps(form_data).encode("utf-8"), method="POST"
    )
    req.add_header("Content-Type", "application/json")
    return json.loads(urllib.request.urlopen(req).read().decode("utf-8"))


# Function to place a trade
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


# Function to terminate a session
def teardown(username, session_token):
    form_data = {"username": username, "sessionToken": session_token}
    req = urllib.request.Request(
        URL + "/teardown", data=json.dumps(form_data).encode("utf-8"), method="POST"
    )
    req.add_header("Content-Type", "application/json")
    return json.loads(urllib.request.urlopen(req).read().decode("utf-8"))


# Bot trading logic for a defined number of trades
def trading_bot(username, session_token, ticker, num_trades=10):
    balance = 100000  # Initial balance
    position = 100  # Maximum position

    for _ in range(num_trades):
        if balance <= 0 or position <= 0:
            break

        volume = random.randint(1, min(5, position))  # Trade up to 5 shares
        price = round(random.uniform(100, 300), 2)  # Price range between 100 and 300
        is_bid = random.choice([True, False])
        cost = volume * price if is_bid else 0

        if balance >= cost:
            resp = place_trade(username, session_token, ticker, volume, price, is_bid)
            balance -= cost if is_bid else 0
            position -= volume if not is_bid else 0
            print(
                f"Bot {username} placed a {'bid' if is_bid else 'ask'} for {volume} shares at {price}: {resp}"
            )
        else:
            print(
                f"Bot {username} cannot place trade due to insufficient balance or position."
            )

        time.sleep(random.uniform(0.5, 1.5))  # Simulate active trading


# Main script
if __name__ == "__main__":
    ticker = "AAPL"
    num_trades_per_bot = 10  # Number of trades each bot will attempt

    # Create bot1
    bot1_data = create_user("bot1", "Trading Bot 1", "bot1@example.com")
    bot1_session = user_buildup("bot1", bot1_data["apiKey"])
    print(f"Bot1 session established: {bot1_session}")

    # Start trading for bot1 immediately
    trading_bot("bot1", bot1_session["sessionToken"], ticker, num_trades_per_bot)

    # Delay for the second bot's start
    time.sleep(5)

    # Create bot2
    bot2_data = create_user("bot2", "Trading Bot 2", "bot2@example.com")
    bot2_session = user_buildup("bot2", bot2_data["apiKey"])
    print(f"Bot2 session established: {bot2_session}")

    # Start trading for bot2 after the delay
    trading_bot("bot2", bot2_session["sessionToken"], ticker, num_trades_per_bot)

import json
import urllib.request
import random
import time
#URL='http://ec2-13-59-143-196.us-east-2.compute.amazonaws.com:8080'
URL = 'http://localhost:8080'
#URL = 'http://ec2-3-16-107-184.us-east-2.compute.amazonaws.com:8080'
def create_bot(username, name, email):
    form_data = {
        'adminUsername': 'trading_club_admin',
        'adminPassword': 'abcxyz',
        'username': username,
        'name': name,
    }
    req = urllib.request.Request(URL + '/add_bot', data=json.dumps(form_data).encode('utf-8'), method='POST')
    req.add_header('Content-Type', 'application/json')
    return json.loads(urllib.request.urlopen(req).read().decode('utf-8'))

# User buildup
def bot_buildup(username, api_key):
    form_data = {
        'username': username,
        'apiKey': api_key
    }
    req = urllib.request.Request(URL + '/bot_buildup', data=json.dumps(form_data).encode('utf-8'), method='POST')
    req.add_header('Content-Type', 'application/json')
    return json.loads(urllib.request.urlopen(req).read().decode('utf-8'))

# Place a trade
def place_trade(username, session_token, ticker, volume, price, is_bid):
    form_data = {
        'username': username,
        'sessionToken': session_token,
        'ticker': ticker,
        'volume': volume,
        'price': price,
        'isBid': is_bid
    }
    req = urllib.request.Request(URL + '/bot_limit_order', data=json.dumps(form_data).encode('utf-8'), method='POST')
    req.add_header('Content-Type', 'application/json')
    return json.loads(urllib.request.urlopen(req).read().decode('utf-8'))

# Teardown a session
def teardown(username, session_token):
    form_data = {
        'username': username,
        'sessionToken': session_token
    }
    req = urllib.request.Request(URL + '/teardown', data=json.dumps(form_data).encode('utf-8'), method='POST')
    req.add_header('Content-Type', 'application/json')
    return json.loads(urllib.request.urlopen(req).read().decode('utf-8'))

# Bot logic
def trading_bot(username, session_token, ticker, initial_balance=100000, max_position=100):
    balance = initial_balance
    position = max_position

    while balance > 0 and position > 0:
        max_volume = min(5, position)  # Trade a max of 5 shares per trade
        volume = random.randint(1, max_volume)
        price = round(random.uniform(100, 300), 2)  # Keep price range modest
        cost = volume * price

        # Ensure the trade does not exceed the remaining balance
        if balance >= cost:
            is_bid = random.choice([True, False])
            resp = place_trade(username, session_token, ticker, volume, price, is_bid)

            # Update balance and position
            balance -= cost if is_bid else 0
            position -= volume if not is_bid else 0

            print(f"Bot {username} placed a {'bid' if is_bid else 'ask'}: {resp}")
            #print(f"Updated balance: {balance}, Updated position: {position}")
        else:
            print(f"Bot {username} cannot place trade due to insufficient balance.")

        time.sleep(random.uniform(0.5, 1.5))  # Shorter delay to simulate activity

    #print(f"Bot {username} finished trading. Final balance: {balance}, Final position: {position}")
def test_batch(username, session_token, ticker):
    print(username, session_token)
    form_data = {
        'username': username,
        'sessionToken': session_token,
        'operations': [
            {"type": "remove_all"},
            #{"type": "limit_order", "ticker": "A", "price": 150.0, "volume": 10000000, "bid": True},
            #{"type": "limit_order", "ticker": "A", "price": 160.0, "volume": 10000000, "bid": False},
            #{"type": "limit_order", "ticker": "A", "price": 210.0, "volume": 10, "bid": False},
            #{"type": "limit_order", "ticker": "A", "price": 220.0, "volume": 10, "bid": False},

            {"type": "market_order", "ticker": "A", "volume": 1000, "bid": False },
            #{"type": "remove", "orderId": 1},
        ]
    }

    req = urllib.request.Request(URL + '/batch', data=json.dumps(form_data).encode('utf-8'), method='POST')
    req.add_header('Content-Type', 'application/json')

    print(json.loads(urllib.request.urlopen(req).read().decode('utf-8')))

# Main script
if __name__ == "__main__":
    bot_count = 1  # Number of bots
    ticker = 'A'  # Stock ticker to trade
    bot_sessions = []

    username = "tradingbot1"
    apiKey = "PRDXTXSRQQZHNSFH"

    session_data = bot_buildup(username, apiKey)
    print(f"Bot {username} session established: {session_data}")
    bot_sessions.append({
        "username": username,
        "session_token": session_data['sessionToken'],
        "balance": 100000,  # Initial balance
        "position": 100     # Maximum position
    })
    test_batch(username, session_data['sessionToken'], ticker)

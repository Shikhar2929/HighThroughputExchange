import json
import urllib.request
import random
import time

#URL = 'http://ec2-18-119-248-10.us-east-2.compute.amazonaws.com:8080'
URL='http://ec2-13-59-143-196.us-east-2.compute.amazonaws.com:8080'
#URL = 'http://localhost:8080'
# Admin creates multiple users (bots)
def create_user(username, name, email):
    form_data = {
        'adminUsername': 'trading_club_admin',
        'adminPassword': 'abcxyz',
        'username': username,
        'name': name,
        'email': email
    }
    req = urllib.request.Request(URL + '/add_user', data=json.dumps(form_data).encode('utf-8'), method='POST')
    req.add_header('Content-Type', 'application/json')
    return json.loads(urllib.request.urlopen(req).read().decode('utf-8'))

# User buildup
def user_buildup(username, api_key):
    form_data = {
        'username': username,
        'apiKey': api_key
    }
    req = urllib.request.Request(URL + '/buildup', data=json.dumps(form_data).encode('utf-8'), method='POST')
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
    req = urllib.request.Request(URL + '/limit_order', data=json.dumps(form_data).encode('utf-8'), method='POST')
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

# Main script
if __name__ == "__main__":
    bot_count = 5  # Number of bots
    ticker = 'AAPL'  # Stock ticker to trade
    bot_sessions = []

    try:
        # Create bots
        for i in range(bot_count):
            username = f"bot{i + 1}"
            name = f"Trading Bot {i + 1}"
            email = f"bot{i + 1}@example.com"

            # Step 1: Admin creates a bot user
            user_data = create_user(username, name, email)
            print(f"Created bot user: {user_data}")

            # Step 2: User gets session token
            session_data = user_buildup(username, user_data['apiKey'])
            print(f"Bot {username} session established: {session_data}")
            bot_sessions.append({
                "username": username,
                "session_token": session_data['sessionToken'],
                "balance": 100000,  # Initial balance
                "position": 100     # Maximum position
            })

        # Step 3: Interleave trading for all bots
        active_bots = len(bot_sessions)
        while active_bots > 0:
            for bot in bot_sessions:
                if bot["balance"] > 0 and bot["position"] > 0:
                    # Execute one trade per bot in each round
                    username = bot["username"]
                    session_token = bot["session_token"]
                    max_volume = min(5, bot["position"])  # Trade a max of 5 shares per trade
                    volume = random.randint(1, max_volume)
                    price = round(random.uniform(100, 300), 2)  # Random price
                    cost = volume * price

                    if bot["balance"] >= cost:
                        is_bid = random.choice([True, False])
                        try:
                            resp = place_trade(username, session_token, ticker, volume, price, is_bid)

                            # Update balance and position
                            if is_bid:
                                bot["balance"] -= cost
                            else:
                                bot["position"] -= volume

                            print(f"Bot {username} placed a {'bid' if is_bid else 'ask'}: {resp}")
                        except Exception as e:
                            print(f"Error in trading for {username}: {e}")
                    else:
                        print(f"Bot {username} cannot place trade due to insufficient balance or position.")
                else:
                    print(f"Bot {bot['username']} finished trading. Final balance: {bot['balance']}, Final position: {bot['position']}")
                    active_bots -= 1

                time.sleep(random.uniform(0.5, 1.5))  # Delay to simulate activity

    finally:
        # Step 4: Teardown all sessions
        for bot in bot_sessions:
            try:
                resp = teardown(bot["username"], bot["session_token"])
                print(f"Tore down session for {bot['username']}: {resp}")
            except Exception as e:
                print(f"Error tearing down session for {bot['username']}: {e}")

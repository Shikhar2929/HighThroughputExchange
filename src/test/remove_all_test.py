import json
import urllib.request
from env_loader import load_env, getenv

load_env()
URL = getenv('HTTP_URL', 'http://localhost:8080')

# Create a user (bot)
def create_user(username, name, email):
    form_data = {
    'adminUsername': getenv('ADMIN_USERNAME'),
    'adminPassword': getenv('ADMIN_PASSWORD'),
        'username': username,
        'name': name,
        'email': email
    }
    req = urllib.request.Request(URL + '/add_user', data=json.dumps(form_data).encode('utf-8'), method='POST')
    req.add_header('Content-Type', 'application/json')
    response = urllib.request.urlopen(req).read().decode('utf-8')
    print("Create user response:", response)
    return json.loads(response)

# Buildup to get an active session
def buildup(username, api_key):
    form_data = {
        'username': username,
        'apiKey': api_key
    }
    req = urllib.request.Request(URL + '/buildup', data=json.dumps(form_data).encode('utf-8'), method='POST')
    req.add_header('Content-Type', 'application/json')
    response = urllib.request.urlopen(req).read().decode('utf-8')
    print("Buildup response:", response)
    return json.loads(response)

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
    response = urllib.request.urlopen(req).read().decode('utf-8')
    print("Trade placed response:", response)
    return json.loads(response)

# Remove all trades or sessions
def remove_all(session_token):
    form_data = {
        'username': username,
        'sessionToken': session_token,
    }
    req = urllib.request.Request(URL + '/remove_all', data=json.dumps(form_data).encode('utf-8'), method='POST')
    req.add_header('Content-Type', 'application/json')
    response = urllib.request.urlopen(req).read().decode('utf-8')
    print("Remove all response:", response)
    return json.loads(response)

# Teardown the session
def teardown(username, session_token):
    form_data = {
        'username': username,
        'sessionToken': session_token
    }
    req = urllib.request.Request(URL + '/teardown', data=json.dumps(form_data).encode('utf-8'), method='POST')
    req.add_header('Content-Type', 'application/json')
    response = urllib.request.urlopen(req).read().decode('utf-8')
    print("Teardown response:", response)
    return json.loads(response)

# Main script execution
if __name__ == "__main__":
    username = 'botUser'
    name = 'Bot User'
    email = 'botuser@example.com'
    user_data = create_user(username, name, email)
    api_key = user_data['apiKey'] if 'apiKey' in user_data else exit("Failed to create user.")

    session_data = buildup(username, api_key)
    session_token = session_data['sessionToken'] if 'sessionToken' in session_data else exit("Failed to buildup.")

    # Place a single trade for demonstration
    trade_response = place_trade(username, session_token, 'A', 10, 100.0, True)

    # Remove all trades/sessions
    remove_all_response = remove_all(session_token)

    # Teardown the session
    teardown_response = teardown(username, session_token)

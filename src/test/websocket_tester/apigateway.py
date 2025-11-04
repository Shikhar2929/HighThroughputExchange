import websocket
import json
from env_loader import load_env, getenv

load_env()

def on_message(ws, message):
    print("Received:", message)

def on_error(ws, error):
    print("Error:", error)

def on_close(ws, close_status_code, close_msg):
    print("Connection closed")

def on_open(ws):
    # Send a message that will be routed to $default
    message = {
        "action": "customRoute",
        "data": "This will trigger the $default route"
    }
    ws.send(json.dumps(message))
    print("Message sent to $default route")

ws_url = getenv('AWS_WS_URL', 'wss://b2sylfzefg.execute-api.us-east-2.amazonaws.com/production')
ws = websocket.WebSocketApp(ws_url, on_open=on_open, on_message=on_message, on_error=on_error, on_close=on_close)
ws.run_forever()
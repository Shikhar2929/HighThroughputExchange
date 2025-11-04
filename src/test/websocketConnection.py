import json
import websocket
import threading
from websocket import WebSocketApp
import time
import OrderBook
from env_loader import load_env, getenv

load_env()


class WebSocketClient:
    def __init__(self, order_book: OrderBook):
        self.ws_url = getenv('WS_URL', 'ws://localhost:8080/exchange-socket')
        self.connected = False
        self.ws = None
        self.order_book = order_book

    def on_message(self, ws, message):
        """Handle incoming WebSocket messages."""
        try:
            if isinstance(message, bytes):
                message = message.decode("utf-8")

            if "\n\n" in message:
                headers, body = message.split("\n\n", 1)
                body = body.replace("\x00", "").strip()
                json_body = json.loads(body)

                if "content" in json_body:
                    content = json.loads(json_body["content"])
                    print(content)
                    if isinstance(content, list):
                        self.order_book.update_volumes(content)
        except Exception:
            pass

    def on_error(self, ws, error):
        print(f"Error: {error}")

    def on_open(self, ws):
        print("WebSocket connection established")
        # Send STOMP CONNECT frame
        connect_frame = "CONNECT\naccept-version:1.1,1.0\nhost:localhost\n\n\x00"
        ws.send(connect_frame)

        # Subscribe to orderbook topic
        subscribe_frame = (
            "SUBSCRIBE\nid:sub-0\ndestination:/topic/orderbook\nack:auto\n\n\x00"
        )
        ws.send(subscribe_frame)

        self.connected = True
        print("STOMP connection and subscription established")

    def on_close(self, ws, close_status_code, close_msg):
        print(f"Disconnected: {close_msg if close_msg else 'No message'}")
        self.connected = False

    def connect(self):
        """Connect to the WebSocket STOMP broker"""
        websocket.enableTrace(False)
        self.ws = WebSocketApp(
            self.ws_url,
            on_open=self.on_open,
            on_message=self.on_message,
            on_error=self.on_error,
            on_close=self.on_close,
        )

        # Start WebSocket connection in a separate thread
        wst = threading.Thread(target=self.ws.run_forever)
        wst.daemon = True
        wst.start()

    def disconnect(self):
        """Disconnect from the STOMP broker"""
        if self.connected and self.ws:
            # Send STOMP DISCONNECT frame
            disconnect_frame = "DISCONNECT\nreceipt:77\n\n\x00"
            self.ws.send(disconnect_frame)
            self.ws.close()
            print("Disconnected from broker")

    def send_start_signal(self):
        """Send the start signal with admin credentials"""
        if self.connected and self.ws:
            message = {
                "adminUsername": getenv('ADMIN_USERNAME'),
                "adminPassword": getenv('ADMIN_PASSWORD')
            }

            # Construct STOMP SEND frame
            send_frame = (
                "SEND\n"
                "destination:/app/start\n"
                "content-type:application/json\n"
                f"content-length:{len(json.dumps(message))}\n"
                "\n"
                f"{json.dumps(message)}\x00"
            )

            self.ws.send(send_frame)
            print("Start signal sent")
        else:
            print("Not connected to broker")


def run_socket(order_book: OrderBook):
    if (
        not hasattr(order_book, "__class__")
        or order_book.__class__.__name__ != "OrderBook"
    ):
        print(order_book)
        raise Exception("Bad input - must be an instance of the OrderBook class")
    client = WebSocketClient(order_book)
    client.connect()
    while True:
        time.sleep(0.3)

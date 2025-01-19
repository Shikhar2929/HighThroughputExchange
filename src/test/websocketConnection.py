URL = 'http://localhost:8080'
import stomp
import json
import time
class MyListener(stomp.ConnectionListener):
    def __init__(self, connection):
        self.connection = connection

    def on_connected(self, headers, body):
        print("Connected to the broker.")
        # Subscribe to the topic after connection
        self.connection.subscribe(destination='/topic/orderbook', id=1, ack='auto')

    def on_message(self, headers, message):
        print("Received message: ", json.loads(message).get('content', 'No content'))

    def on_error(self, headers, message):
        print("Broker reported error:", message)

    def on_disconnected(self):
        print("Disconnected from the broker.")

    def on_heartbeat_timeout(self):
        print("Heartbeat timeout, disconnecting...")
        self.connection.disconnect()

def connect_to_websocket(broker_url, username=None, password=None):
    host, port = broker_url.replace("ws://", "").split(':')
    port = int(port)

    # Establish the connection
    conn = stomp.Connection([(host, port)])
    listener = MyListener(conn)
    conn.set_listener('', listener)

    conn.connect(username=username, passcode=password, wait=True)
    return conn

def send_start_signal(conn):
    # Send a message to the app destination
    conn.send(destination='/app/start', body=json.dumps({
        "adminUsername": "trading_club_admin",
        "adminPassword": "abc"
    }))
    print("Start signal sent.")

def main():
    broker_url = "ws://ec2-13-59-143-196.us-east-2.compute.amazonaws.com:8080/exchange-socket"

    try:
        connection = connect_to_websocket(broker_url)

        # Simulate sending the start signal after connecting
        send_start_signal(connection)

        print("Waiting for messages...")
        while True:
            time.sleep(1)  # Keep the program running to receive messages
    except KeyboardInterrupt:
        print("Disconnecting...")
        connection.disconnect()

if __name__ == "__main__":
    main()

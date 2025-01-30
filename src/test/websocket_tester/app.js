const sessionId = "IGKJUWLLEEOIKFUO"; // Replace with the actual Session ID variable
const username = "bot1"; // Replace with the actual Username variable

// Construct the WebSocket URL with query parameters
const brokerURL = `ws://localhost:8080/exchange-socket?Session-ID=${encodeURIComponent(sessionId)}&Username=${encodeURIComponent(username)}`;

const stompClient = new StompJs.Client({
    brokerURL: brokerURL,
    debug: (str) => {
        console.log(str); // Logs all debug messages for troubleshooting
    },
    reconnectDelay: 5000, // Auto-reconnect after 5 seconds
    heartbeatIncoming: 4000, // Keep the connection alive
    heartbeatOutgoing: 4000
});

stompClient.onConnect = (frame) => {
    setConnected(true);
    console.log("Connected: " + frame);

    // Subscribe to public messages (orderbook updates)
    stompClient.subscribe("/topic/orderbook", (message) => {
        console.log("Orderbook message received: " + message.body);
        showMessage(JSON.parse(message.body).content); // Handle orderbook messages
    });

    // Subscribe to private messages (user-specific updates)
    stompClient.subscribe("/user/queue/private", (message) => {
        console.log("Private message received: " + message.body);
        showMessage(`Private: ${message.body}`); // Handle private messages
    });
};

stompClient.onWebSocketError = (error) => {
    console.error("WebSocket Error: ", error);
};

stompClient.onStompError = (frame) => {
    console.error("Broker reported error: ", frame.headers['message']);
    console.error("Additional details: ", frame.body);
};

// Utility function to update UI connection status
function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    } else {
        $("#conversation").hide();
    }
    $("#greetings").html("");
}

// Connect to WebSocket server
function connect() {
    console.log("Connecting to WebSocket...");
    stompClient.activate();
}

// Disconnect from WebSocket server
function disconnect() {
    console.log("Disconnecting from WebSocket...");
    stompClient.deactivate();
    setConnected(false);
}

// Send start signal to initiate the stream
function sendStartSignal() {
    stompClient.publish({
        destination: "/app/start",
        body: JSON.stringify({
            adminUsername: "trading_club_admin",
            adminPassword: "abcxyz"
        })
    });
    console.log("Start signal sent.");
}

// Display a message in the UI
function showMessage(message) {
    $("#messages").append("<tr><td>" + message + "</td></tr>");
}

// Event listeners for buttons
$(function() {
    $("#connect").click(() => connect());
    $("#disconnect").click(() => disconnect());
    $("#start").click(() => sendStartSignal());
});
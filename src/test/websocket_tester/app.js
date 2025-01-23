const stompClient = new StompJs.Client({
    "brokerURL": "ws://localhost:8080/exchange-socket"
    //"brokerURL": "ws://ec2-18-119-248-10.us-east-2.compute.amazonaws.com:8080/exchange-socket"
    //"brokerURL": "ws://ec2-13-59-143-196.us-east-2.compute.amazonaws.com:8080/exchange-socket"
});

stompClient.onConnect = (frame) => {
    setConnected(true);
    console.log("Connected: " + frame);
    stompClient.subscribe("/topic/orderbook", onMessage);
}

stompClient.onWebSocketError = (error) => {
    console.log("Web Socket Error: " + error);
}

stompClient.onStompError = (frame) => {
    console.log("Broker reported error: " + frame.headers['message']);
    console.log("Additional details: " + frame.body);
}

function onMessage(message) {
    showMessage(JSON.parse(message.body).content)
}

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    } else {
        $("#conversation").show();
    }
    $("#greetings").html("");
}

function connect() {
    stompClient.activate();
}

function disconnect() {
    stompClient.deactivate();
    setConnected(false);
    console.log("disconnected");
}

function sendStartSignal() {
    stompClient.publish({
        destination: "/app/start",
        body: JSON.stringify({ "adminUsername": "trading_club_admin", "adminPassword": "abcxyz" })
    });
}

function showMessage(message) {
    $("#messages").append("<tr><td>" + message + "</td></tr>");
}

$(function() {
    $("#connect").click(() => connect());
    $("#disconnect").click(() => disconnect());
    $("#start").click(() => sendStartSignal());
});
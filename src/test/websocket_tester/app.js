let stompClient = null;

function getConfigValue(key, fallback) {
    return (window.PUBLIC_CONFIG && window.PUBLIC_CONFIG[key]) || fallback;
}

function setStatus(text, type) {
    const color = type === 'error' ? '#b94a48' : (type === 'ok' ? '#3c763d' : '#333');
    $('#status').html(`<span style="color:${color}">${escapeHtml(text)}</span>`);
}

function escapeHtml(s) {
    return String(s)
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}

function safeFormatMessage(body) {
    try {
        const parsed = JSON.parse(body);
        return JSON.stringify(parsed);
    } catch (e) {
        // ignore
    }
    return body;
}

function readInputs() {
    return {
        restBaseUrl: $('#restBaseUrl').val().trim(),
        wsBaseUrl: $('#wsBaseUrl').val().trim(),
        username: $('#username').val().trim(),
        apiKey: $('#apiKey').val().trim(),
        sessionId: $('#sessionId').val().trim(),
        adminUsername: $('#adminUsername').val().trim(),
        adminPassword: $('#adminPassword').val().trim(),
    };
}

function persistInputs(values) {
    try {
        localStorage.setItem('ws_tester_state', JSON.stringify(values));
    } catch (e) {
        // ignore
    }
}

function restoreInputs() {
    const defaults = {
        restBaseUrl: getConfigValue('REST_BASE_URL', 'http://localhost:8080'),
        wsBaseUrl: getConfigValue('WS_URL', 'ws://localhost:8080/exchange-socket'),
        username: 'team49',
        apiKey: '',
        sessionId: '',
        adminUsername: getConfigValue('ADMIN_USERNAME', 'trading_club_admin'),
        adminPassword: getConfigValue('ADMIN_PASSWORD', 'abcxyz'),
    };

    let saved = null;
    try {
        saved = JSON.parse(localStorage.getItem('ws_tester_state') || 'null');
    } catch (e) {
        saved = null;
    }

    const merged = Object.assign({}, defaults, saved || {});
    $('#restBaseUrl').val(merged.restBaseUrl);
    $('#wsBaseUrl').val(merged.wsBaseUrl);
    $('#username').val(merged.username);
    $('#apiKey').val(merged.apiKey);
    $('#sessionId').val(merged.sessionId);
    $('#adminUsername').val(merged.adminUsername);
    $('#adminPassword').val(merged.adminPassword);

    return merged;
}

function buildBrokerUrl(wsBaseUrl, sessionId, username) {
    return `${wsBaseUrl}?Session-ID=${encodeURIComponent(sessionId)}&Username=${encodeURIComponent(username)}`;
}

function ensureClientConnectedStateHandlers(client) {
    client.onConnect = (frame) => {
        setConnected(true);
        setStatus('Connected', 'ok');
        console.log('Connected:', frame);

        client.subscribe('/topic/orderbook', (message) => {
            console.log('Orderbook message received:', message.body);
            showMessage(`Orderbook: ${safeFormatMessage(message.body)}`);
        });

        client.subscribe('/user/queue/private', (message) => {
            console.log('Private message received:', message.body);
            showMessage(`Private: ${safeFormatMessage(message.body)}`);
        });
    };

    client.onWebSocketError = (error) => {
        console.error('WebSocket Error:', error);
        setStatus('WebSocket error (see console)', 'error');
    };

    client.onStompError = (frame) => {
        console.error('Broker reported error:', frame.headers['message']);
        console.error('Additional details:', frame.body);
        setStatus(`STOMP error: ${frame.headers['message'] || 'unknown'}`, 'error');
    };
}

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
    const values = readInputs();
    persistInputs(values);

    if (!values.wsBaseUrl) {
        setStatus('WS endpoint is required', 'error');
        return;
    }
    if (!values.username) {
        setStatus('Username is required', 'error');
        return;
    }
    if (!values.sessionId) {
        setStatus('Session-ID is required (click Buildup first)', 'error');
        return;
    }

    const brokerURL = buildBrokerUrl(values.wsBaseUrl, values.sessionId, values.username);
    console.log('Connecting to WebSocket...', brokerURL);
    setStatus('Connecting...', 'info');

    stompClient = new StompJs.Client({
        brokerURL: brokerURL,
        debug: (str) => console.log(str),
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000
    });
    ensureClientConnectedStateHandlers(stompClient);
    stompClient.activate();
}

// Disconnect from WebSocket server
function disconnect() {
    console.log("Disconnecting from WebSocket...");
    if (stompClient) {
        stompClient.deactivate();
        stompClient = null;
    }
    setConnected(false);
    setStatus('Disconnected', 'info');
}

async function buildup() {
    const values = readInputs();
    persistInputs(values);

    if (!values.restBaseUrl) {
        setStatus('REST base is required', 'error');
        return;
    }
    if (!values.username) {
        setStatus('Username is required', 'error');
        return;
    }
    if (!values.apiKey) {
        setStatus('API key is required', 'error');
        return;
    }

    setStatus('Calling /buildup ...', 'info');
    try {
        const resp = await fetch(`${values.restBaseUrl.replace(/\/$/, '')}/buildup`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: values.username, apiKey: values.apiKey })
        });

        const json = await resp.json().catch(() => ({}));
        if (!resp.ok) {
            setStatus(`Buildup failed (${resp.status}): ${json.message || 'unknown'}`, 'error');
            console.warn('buildup error response:', json);
            return;
        }

        if (!json.sessionToken) {
            setStatus('Buildup succeeded but no sessionToken returned', 'error');
            console.warn('buildup response:', json);
            return;
        }

        $('#sessionId').val(json.sessionToken);
        persistInputs(readInputs());
        setStatus('Buildup OK (Session-ID filled)', 'ok');
    } catch (e) {
        console.error('buildup error:', e);
        setStatus('Buildup error (see console)', 'error');
    }
}

// Send start signal to initiate the stream
function sendStartSignal() {
    const values = readInputs();
    persistInputs(values);
    if (!stompClient) {
        setStatus('Connect first', 'error');
        return;
    }

    stompClient.publish({
        destination: "/app/start",
        body: JSON.stringify({
            adminUsername: values.adminUsername,
            adminPassword: values.adminPassword,
        })
    });
    console.log('Start signal sent.');
}

// Display a message in the UI
function showMessage(message) {
    $("#messages").append("<tr><td>" + escapeHtml(message) + "</td></tr>");
}

// Event listeners for buttons
$(function() {
    restoreInputs();
    setConnected(false);
    setStatus('Ready', 'info');

    $("#connect").click(() => connect());
    $("#disconnect").click(() => disconnect());
    $("#buildup").click(() => buildup());
    $("#start").click(() => sendStartSignal());

    $('#restBaseUrl, #wsBaseUrl, #username, #apiKey, #sessionId, #adminUsername, #adminPassword').on('change', () => {
        persistInputs(readInputs());
    });
});
# HighThroughputExchange

---

### Run tests
```sh
mvn test
```

### Run format and checkstyle
```sh
mvn spotless:apply
mvn checkstyle:check
```

<h3>API Design</h3>

[//]: # (Todo: stopping mechanism; action endpoint; show auction results; frontend orderbook; individual order cancelation)

<table>
    <tr>
        <th>Name</th>
        <th>Route</th>
        <th>Visibility</th>
        <th>Description</th>
        <th>Input</th>
        <th>Output</th>
    </tr>
    <tr>
        <td>Home</td>
        <td>/</td>
        <td>public</td>
        <td>General landing homepage for competition.</td>
        <td>N/A - just a simple HTTP GET</td>
        <td>N/A - just a raw, static frontend</td>
    </tr>
    <tr>
        <td>Leaderboard</td>
        <td>/leaderboard</td>
        <td>admin</td>
        <td>Displays team ranking by PnL.</td>
        <td>N/A - just a simple HTTP GET</td>
        <td>N/A - just a raw, static frontend</td>
    </tr>
    <tr>
        <td>Add User</td>
        <td>/add_user</td>
        <td>admin</td>
        <td>Allows the admin to submit a new team's registration information.</td>
        <td>{"adminUsername": string, "adminPassword": string, "username": string, "name": string, "email": string}</td>
        <td>{"auth": boolean, "success": boolean, "message": string}</td>
    </tr>
    <tr>
        <td>Set State</td>
        <td>/set_state</td>
        <td>admin</td>
        <td>Sets the state of the server (STOP, TRADING, or AUCTION). State is represented as an integer (0 is STOP, 1 is TRADE, 2 is AUCTION).</td>
        <td>{"adminUsername": string, "adminPassword": string, "state": int}</td>
        <td>{"newState": int}</td>
    </tr>
    <tr>
        <td>Clear Round</td>
        <td>/clear_round</td>
        <td>admin</td>
        <td>Clears everything except for PNL.</td>
        <td>{"adminUsername": string, "adminPassword": string, "username": string, "name": string, "email": string}</td>
        <td>{"auth": boolean, "success": boolean, "message": string}</td>
    </tr>
    <tr>
        <td>Shutdown</td>
        <td>/shutdown</td>
        <td>admin</td>
        <td>Saves all the database states in a JSON file.</td>
        <td>{"adminUsername": string, "adminPassword": string}</td>
        <td>Success/Failure</td>
    </tr>
    <tr>
        <td>Buildup</td>
        <td>/buildup</td>
        <td>private</td>
        <td>Authenticates a team, and then issues a sessionToken for the team to use to make trades and subscribe to the websocket.</td>
        <td>{"username": string, "apiKey": string}</td>
        <td>{"auth": boolean, "success": boolean, "message": string}</td>
    </tr>
    <tr>
        <td>Teardown</td>
        <td>/teardown</td>
        <td>private</td>
        <td>Closes the sessionToken of the team, terminating their ability to subscribe to the websocket.</td>
        <td>{"username": string, "sessionToken": string}</td>
        <td>{"auth": string, "success": string}</td>
    </tr>
    <tr>
        <td>Limit Order</td>
        <td>/limit_order</td>
        <td>private</td>
        <td>Places a limit order for team.</td>
        <td>{"username": string, "sessionToken": string, "ticker": string, "volume": float, "price": float, "isBid": boolean}</td>
        <td>Success/Fail</td>
    </tr>
    <tr>
        <td>Market Order</td>
        <td>/market_order</td>
        <td>private</td>
        <td>Places a market order for team.</td>
        <td>{"username": string, "sessionToken": string, "ticker": string, "volume": float, "isBid": boolean}</td>
        <td>Success/Fail</td>
    </tr>
    <tr>
        <td>Remove All</td>
        <td>/remove_all</td>
        <td>private</td>
        <td>Removes all active orders of team.</td>
        <td>{"username": string, "sessionToken": string}</td>
        <td>Success/Fail</td>
    </tr>
    <tr>
        <td>Auction</td>
        <td>/auction</td>
        <td>private</td>
        <td>Facilitates the auction.</td>
        <td>{"username": string, "sessionToken": string, "price": float}</td>
        <td>Success/Fail</td>
    </tr>
    <tr>
        <td>Seq Version</td>
        <td>/version</td>
        <td>public</td>
        <td>Current monotonic sequence/version.</td>
        <td>N/A - simple HTTP GET</td>
        <td>{"version": long}</td>
    </tr>
    <tr>
        <td>Seq Updates Replay</td>
        <td>/updates?from=&lt;seq&gt;</td>
        <td>public</td>
        <td>Replay updates with seq &gt; from (from is exclusive).</td>
        <td>Query: from=long</td>
        <td>200: {"fromExclusive": long, "latestSeq": long, "updates": [{"seq": long, "priceChanges": [...]}, ...]}<br/>410: {"error": "from-too-old", "fromExclusive": long, "minAvailableSeq": long, "minFromExclusive": long, "latestSeq": long}</td>
    </tr>
    <tr>
        <td>Snapshot</td>
        <td>/snapshot</td>
        <td>public</td>
        <td>Full orderbook snapshot for recovery.</td>
        <td>N/A - HTTP POST</td>
        <td>{"snapshot": object, "version": long}</td>
    </tr>
</table>

### WebSocket (STOMP)

- Topic: `/topic/orderbook`
- Payload: `SocketResponse` JSON: `{"content": string, "seq": long}`
- Semantics:
    - When there are trades/orderbook changes, `seq` increases monotonically and `content` contains the JSON-encoded update payload.
    - When there are no recent trades, the server sends a heartbeat message where `seq` is the latest known value (may repeat) and `content` is a human-readable string (currently `"No recent trades"`).

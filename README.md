# HighThroughputExchange

---

## Usage

### Run the server (Spring Boot)
Runs the API locally (defaults to port 8080 unless overridden).
```sh
mvn spring-boot:run
```

### Run Java tests (JUnit/Surefire)
```sh
mvn test
```

### Run Python integration tests (pytest)
Uses `pytest.ini` (default suite is under `src/test/integration`). If you run `pytest` from the repo root, start the server first (`mvn spring-boot:run`) since these are integration tests.
```sh
pytest
```

### Format Java code (Spotless)
```sh
mvn spotless:apply
```

### Verify formatting (Spotless)
```sh
mvn spotless:check
```

### Lint Java code (Checkstyle)
```sh
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
        <td>/latestSeq</td>
        <td>public</td>
        <td>Current monotonic sequence counter (next seq to be allocated).</td>
        <td>N/A - simple HTTP GET</td>
        <td>{"message": {"errorCode": int, "errorMessage": string}, "latestSeq": long}</td>
    </tr>
    <tr>
        <td>Seq Updates Replay</td>
        <td>/updates?seq=&lt;seq&gt;</td>
        <td>public</td>
        <td>Fetch exactly one orderbook update by sequence number.</td>
        <td>Query: seq=long</td>
        <td>200: {"message": {"errorCode": int, "errorMessage": string}, "seq": long, "update": {"seq": long, "priceChanges": [...]}}<br/>400: {"message": {"errorCode": 8, "errorMessage": string}}</td>
    </tr>
    <tr>
        <td>Snapshot</td>
        <td>/snapshot</td>
        <td>public</td>
        <td>Full orderbook snapshot for recovery.</td>
        <td>N/A - HTTP POST</td>
        <td>{"message": {"errorCode": int, "errorMessage": string}, "snapshot": object, "latestSeq": long}</td>
    </tr>
</table>

### WebSocket (STOMP)

- Topic: `/topic/orderbook`
- Payload: `SocketResponse` JSON: `{"content": string, "seq": long}`
- Semantics:
    - When there are trades/orderbook changes, `seq` increases monotonically and `content` contains the JSON-encoded update payload.
    - When there are no recent trades, the server sends a heartbeat message where `seq` is the latest known value (may repeat) and `content` is a human-readable string (currently `"No recent trades"`).

#### Recovery note

`/updates` is intentionally **single-update**: clients should request the exact missing sequence number with `/updates?seq=<n>` and apply it once. This avoids corrupting the frontend orderbook when the same update is fetched multiple times.

---

## Order placement rules (finite vs infinite, users vs bots)

This section documents the *effective* rules enforced by the Java API + matching engine.

### Terminology

- **Bid** = buy order (`bid: true` / `Side.BID`)
- **Ask** = sell order (`bid: false` / `Side.ASK`)
- **Finite mode**: cash balance constrains buying power; users cannot short or oversell.
- **Infinite mode**: cash is not the limiter; a per-ticker **position limit** constrains exposure.

Engine mode comes from `config.json`:

- `mode: "finite"` or `mode: "infinite"`
- If infinite: `defaults.positionLimit` is enforced.

### API-level requirements (before the engine)

**Users (human teams)**

- Must have a session token from `/buildup`.
- Requests to `/limit_order` and `/market_order` are rate-limited.
- Trading must not be locked (`state != STOP`).

**Bots**

- Must be created via `/add_bot` and have a session token from `/bot_buildup`.
- `/bot_limit_order` and `/bot_market_order` are *not* rate-limited (bot remove endpoints are).
- Trading must not be locked (`state != STOP`).

### Input normalization (DTO preprocessing)

These are API DTO clamps applied before reaching the matching engine:

- **Price** is clamped to `[0, 3000]`.
- **User volume** is clamped to `[0, 1000]`.
- **Bot volume** is clamped to `>= 0` (no max clamp).

The matching engine additionally requires:

- `price > 0` and `volume > 0` for limit orders.
- `volume > 0` for market orders.

### Engine-level rules (what is accepted / rejected)

These rules apply to the core `MatchingEngine` placement handlers.

**Shared sanity checks (users and bots)**

- Ticker must exist (order book initialized).
- The placing principal must be initialized in the engine (user/bot exists).
- Limit orders must be `Status.ACTIVE` and have `price > 0` and `volume > 0`.

#### Finite mode rules

**Users (finite)**

- **Bid (limit)**: must have sufficient cash for the full order notional: `cash >= price * volume`.
- **Bid (market)**: must be able to afford at least 1 unit at the current best ask price; execution stops when the next unit is unaffordable.
- **Ask (limit)**: must have sufficient inventory *after accounting for existing resting asks*.
    - Available-to-sell is `inventory - reservedAsks`.
- **Ask (market)**: must have sufficient available inventory (same rule as above).

Guarantee: in finite mode, a user’s per-ticker position cannot go negative via order placement.

**Bots (finite)**

- Bots bypass finite-mode **cash** and **inventory** constraints.
    - Bots may buy without cash and may sell without inventory (can short).
- Bots still must satisfy shared sanity checks (valid ticker, positive price/volume, ACTIVE status).

#### Infinite mode rules

**Users (infinite)**

- Cash does not constrain order placement.
- A per-ticker **position limit** constrains exposure, including existing resting orders:
    - **Bid capacity**: `positionLimit - currentPosition - reservedBids`
    - **Ask capacity** (short capacity): `positionLimit + currentPosition - reservedAsks`
- Users can short (negative position) within `±positionLimit`.

**Bots (infinite)**

- Bots bypass position-limit, cash, and inventory constraints.
- Bots still must satisfy shared sanity checks.

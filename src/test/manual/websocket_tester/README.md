# Websocket tester (localhost)

This folder contains a small browser UI to connect to the exchange STOMP websocket.

## 1) Start the Java server

From the repo root:

```bash
mvn spring-boot:run
```

By default the API should be on `http://localhost:8080` and the websocket endpoint is:

- `ws://localhost:8080/exchange-socket`

## 2) Start the tester UI server

From this folder:

```bash
python3 serve.py
```

Then open:

- `http://localhost:5173/`

## 3) Get a session + connect

In the page:

1. Fill `Username` + `API key`
2. Click **Buildup** (calls `POST /buildup` and fills `Session-ID`)
3. Click **Connect**
4. Click **Start stream** (admin-only) if you want the server to start pushing updates

Notes:
- The websocket handshake requires query params `Session-ID` and `Username`.
- Defaults can be adjusted in `config.js`.

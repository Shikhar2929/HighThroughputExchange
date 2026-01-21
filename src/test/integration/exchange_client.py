from __future__ import annotations

import os
from dataclasses import dataclass
from typing import Any

import requests
from requests import Response
from requests.exceptions import RequestException


class ApiError(RuntimeError):
    pass


def _require_env(name: str) -> str:
    value = os.getenv(name)
    if not value:
        raise ApiError(f"Missing required env var: {name}")
    return value


def _assert_success_message(resp_json: dict[str, Any]) -> None:
    # Backend uses @JsonRawValue -> message is a JSON object (not a string)
    msg = resp_json.get("message")
    if not isinstance(msg, dict) or "errorCode" not in msg:
        raise ApiError(f"Unexpected response shape: {resp_json}")
    if msg.get("errorCode") != 0:
        raise ApiError(f"API error: {msg}")


def _assert_success_message_or_none(resp_json: dict[str, Any]) -> None:
    msg = resp_json.get("message")
    if msg is None:
        # Some endpoints return message=null for successful no-op operations.
        return
    _assert_success_message(resp_json)


@dataclass(frozen=True)
class Session:
    username: str
    session_token: str


@dataclass(frozen=True)
class BotSession:
    username: str
    session_token: str


class ExchangeClient:
    def __init__(self, base_url: str):
        self.base_url = base_url.rstrip("/")
        self._session = requests.Session()

    def close(self) -> None:
        self._session.close()

    def _request(
        self,
        method: str,
        path: str,
        *,
        params: dict[str, Any] | None = None,
        payload: dict[str, Any] | None = None,
        timeout_s: float = 5.0,
        max_retries: int = 6,
    ) -> Response:
        backoff_s = 0.05
        last_exc: Exception | None = None

        for attempt in range(max_retries):
            try:
                r = self._session.request(
                    method=method,
                    url=f"{self.base_url}{path}",
                    params=params,
                    json=payload,
                    timeout=timeout_s,
                    headers={"User-Agent": "hte-integration-tests"},
                )

                # Rate limiter can trip in tight CI runs; retry a few times.
                if r.status_code == 429 and attempt < max_retries - 1:
                    import time

                    time.sleep(backoff_s)
                    backoff_s = min(backoff_s * 2, 0.5)
                    continue

                return r
            except RequestException as e:
                last_exc = e
                if attempt >= max_retries - 1:
                    break
                import time

                time.sleep(backoff_s)
                backoff_s = min(backoff_s * 2, 0.5)

        raise ApiError(f"Request failed: {method} {path}: {last_exc}")

    def _get_json(
        self,
        path: str,
        *,
        params: dict[str, Any] | None = None,
        timeout_s: float = 5.0,
        allow_http_error: bool = False,
    ) -> tuple[int, dict[str, Any]]:
        r = self._request("GET", path, params=params, timeout_s=timeout_s)
        status = int(r.status_code)

        try:
            data = r.json()
        except Exception as e:
            raise ApiError(f"Non-JSON response ({status}): {r.text}") from e

        if not allow_http_error and status >= 400:
            raise ApiError(f"HTTP {status}: {data}")
        return status, data

    def _post(
        self, path: str, payload: dict[str, Any], timeout_s: float = 5.0
    ) -> dict[str, Any]:
        r = self._request("POST", path, payload=payload, timeout_s=timeout_s)

        # For failures, the server still returns JSON most of the time.
        try:
            data = r.json()
        except Exception as e:
            raise ApiError(f"Non-JSON response ({r.status_code}): {r.text}") from e

        if r.status_code >= 400:
            raise ApiError(f"HTTP {r.status_code}: {data}")
        return data

    def post_allow_http_error(
        self,
        path: str,
        payload: dict[str, Any],
        *,
        timeout_s: float = 5.0,
    ) -> tuple[int, dict[str, Any]]:
        r = self._request("POST", path, payload=payload, timeout_s=timeout_s)

        try:
            data = r.json()
        except Exception as e:
            raise ApiError(f"Non-JSON response ({r.status_code}): {r.text}") from e

        return int(r.status_code), data

    def get_state(self) -> int:
        status, data = self._get_json("/get_state", timeout_s=2.0)
        if status != 200:
            raise ApiError(f"Unexpected status for /get_state: {status} {data}")
        return int(data["state"])

    # --- SeqController endpoints ---

    def latest_seq(self) -> int:
        status, data = self._get_json("/latestSeq", timeout_s=2.0)
        if status != 200:
            raise ApiError(f"Unexpected status for /latestSeq: {status} {data}")
        latest = data.get("latestSeq")
        if latest is None:
            raise ApiError(f"Missing latestSeq: {data}")
        return int(latest)

    def snapshot(self) -> dict[str, Any]:
        r = self._request("POST", "/snapshot", timeout_s=5.0)
        try:
            data = r.json()
        except Exception as e:
            raise ApiError(f"Non-JSON response ({r.status_code}): {r.text}") from e
        if r.status_code >= 400:
            raise ApiError(f"HTTP {r.status_code}: {data}")
        return data

    def update_allow_missing(self, seq: int) -> tuple[int, dict[str, Any]]:
        return self._get_json(
            "/updates",
            params={"seq": int(seq)},
            timeout_s=5.0,
            allow_http_error=True,
        )

    def admin_page(self) -> None:
        payload = {
            "adminUsername": _require_env("ADMIN_USERNAME"),
            "adminPassword": _require_env("ADMIN_PASSWORD"),
        }
        data = self._post("/admin_page", payload)
        _assert_success_message(data)

    def private_page(self, session: Session) -> None:
        data = self._post(
            "/privatePage",
            {"username": session.username, "sessionToken": session.session_token},
        )
        _assert_success_message(data)

    def admin_set_state(self, target_state: int) -> int:
        payload = {
            "adminUsername": _require_env("ADMIN_USERNAME"),
            "adminPassword": _require_env("ADMIN_PASSWORD"),
            "targetState": target_state,
        }
        data = self._post("/set_state", payload)
        _assert_success_message(data)
        return int(data["newState"])

    def admin_leaderboard(self) -> list[dict[str, Any]]:
        payload = {
            "adminUsername": _require_env("ADMIN_USERNAME"),
            "adminPassword": _require_env("ADMIN_PASSWORD"),
        }
        data = self._post("/leaderboard", payload)
        _assert_success_message(data)
        entries = data.get("data")
        if entries is None:
            return []
        if not isinstance(entries, list):
            raise ApiError(f"Unexpected leaderboard data: {data}")
        return entries

    def admin_set_price(self, prices: dict[str, int]) -> None:
        payload = {
            "adminUsername": _require_env("ADMIN_USERNAME"),
            "adminPassword": _require_env("ADMIN_PASSWORD"),
            "prices": prices,
        }

        # /set_price is inconsistent across environments in this repo:
        # sometimes it returns a proper AbstractMessageResponse JSON payload,
        # and sometimes it returns a non-JSON string like:
        #   {"message":SUCCESS ALL CLEARED}
        # Keep tests resilient by accepting either form.
        r = self._request("POST", "/set_price", payload=payload, timeout_s=5.0)
        if r.status_code >= 400:
            raise ApiError(f"HTTP {r.status_code}: {r.text}")

        try:
            data = r.json()
        except Exception:
            text = (r.text or "").strip()
            # Success is indicated by these substrings in current server output.
            if "SUCCESS" in text or "ALL CLEARED" in text:
                return
            raise ApiError(f"Non-JSON response (200): {text}")

        _assert_success_message(data)

    def wait_for_updates_with_ticker(
        self,
        *,
        ticker: str,
        timeout_s: float = 8.0,
        start_seq: int = 0,
    ) -> dict[str, Any]:
        """Poll /updates until we observe a PriceChange for the given ticker."""
        import time

        deadline = time.time() + timeout_s
        seq = int(start_seq)
        last_status: int | None = None
        last_data: dict[str, Any] | None = None

        while time.time() < deadline:
            status, data = self.update_allow_missing(seq)
            last_status, last_data = status, data

            if status == 200:
                update = data.get("update")
                if not isinstance(update, dict):
                    raise ApiError(f"Unexpected update payload: {data}")

                got_seq = int(update.get("seq"))
                seq = got_seq + 1

                for pc in update.get("priceChanges", []) or []:
                    if pc.get("ticker") == ticker:
                        return update

                time.sleep(0.05)
                continue

            if status == 400:
                msg = data.get("message")
                if not isinstance(msg, dict):
                    raise ApiError(f"Unexpected error payload: {data}")
                time.sleep(0.1)
                continue

            time.sleep(0.2)

        raise ApiError(
            f"/updates never returned an update for ticker={ticker} (last={last_status}"
            f" {last_data})"
        )

    def admin_add_user(self, username: str, name: str, email: str) -> str:
        payload = {
            "adminUsername": _require_env("ADMIN_USERNAME"),
            "adminPassword": _require_env("ADMIN_PASSWORD"),
            "username": username,
            "name": name,
            "email": email,
        }
        data = self._post("/add_user", payload)
        _assert_success_message(data)
        api_key = data.get("apiKey")
        if not api_key:
            raise ApiError(f"No apiKey returned: {data}")
        return str(api_key)

    def admin_add_bot(self, username: str, name: str) -> str:
        payload = {
            "adminUsername": _require_env("ADMIN_USERNAME"),
            "adminPassword": _require_env("ADMIN_PASSWORD"),
            "username": username,
            "name": name,
        }
        data = self._post("/add_bot", payload)
        _assert_success_message(data)
        api_key = data.get("apiKey")
        if not api_key:
            raise ApiError(f"No apiKey returned: {data}")
        return str(api_key)

    def buildup(self, username: str, api_key: str) -> Session:
        data = self._post("/buildup", {"username": username, "apiKey": api_key})
        _assert_success_message(data)
        token = data.get("sessionToken")
        if not token:
            raise ApiError(f"No sessionToken returned: {data}")
        return Session(username=username, session_token=str(token))

    def get_details(self, session: Session) -> str:
        data = self._post(
            "/get_details",
            {"username": session.username, "sessionToken": session.session_token},
        )
        _assert_success_message(data)
        details = data.get("userDetails")
        if details is None:
            return ""
        return str(details)

    def teardown(self, session: Session) -> None:
        data = self._post(
            "/teardown",
            {"username": session.username, "sessionToken": session.session_token},
        )
        _assert_success_message(data)

    def remove_all(self, session: Session) -> None:
        data = self._post(
            "/remove_all",
            {"username": session.username, "sessionToken": session.session_token},
        )
        _assert_success_message_or_none(data)

    def remove(self, session: Session, order_id: int) -> None:
        data = self._post(
            "/remove",
            {
                "username": session.username,
                "sessionToken": session.session_token,
                "orderId": int(order_id),
            },
        )
        _assert_success_message(data)

    def limit_order(
        self, session: Session, ticker: str, volume: int, price: int, is_bid: bool
    ) -> dict[str, Any]:
        payload = {
            "username": session.username,
            "sessionToken": session.session_token,
            "ticker": ticker,
            "volume": volume,
            "price": price,
            "bid": is_bid,
        }
        data = self._post("/limit_order", payload)
        _assert_success_message(data)
        return data

    def market_order(
        self, session: Session, ticker: str, volume: int, is_bid: bool
    ) -> dict[str, Any]:
        payload = {
            "username": session.username,
            "sessionToken": session.session_token,
            "ticker": ticker,
            "volume": volume,
            "bid": is_bid,
        }
        data = self._post("/market_order", payload)
        _assert_success_message(data)
        return data

    def bot_buildup(self, username: str, api_key: str) -> BotSession:
        data = self._post("/bot_buildup", {"username": username, "apiKey": api_key})
        _assert_success_message(data)
        token = data.get("sessionToken")
        if not token:
            raise ApiError(f"No sessionToken returned: {data}")
        return BotSession(username=username, session_token=str(token))

    def bot_limit_order(
        self, session: BotSession, ticker: str, volume: int, price: int, is_bid: bool
    ) -> dict[str, Any]:
        payload = {
            "username": session.username,
            "sessionToken": session.session_token,
            "ticker": ticker,
            "volume": volume,
            "price": price,
            "bid": is_bid,
        }
        data = self._post("/bot_limit_order", payload)
        _assert_success_message(data)
        return data

    def bot_market_order(
        self, session: BotSession, ticker: str, volume: int, is_bid: bool
    ) -> dict[str, Any]:
        payload = {
            "username": session.username,
            "sessionToken": session.session_token,
            "ticker": ticker,
            "volume": volume,
            "bid": is_bid,
        }
        data = self._post("/bot_market_order", payload)
        _assert_success_message(data)
        return data

    def bot_remove_all(self, session: BotSession) -> None:
        data = self._post(
            "/bot_remove_all",
            {"username": session.username, "sessionToken": session.session_token},
        )
        _assert_success_message_or_none(data)

    def bot_remove(self, session: BotSession, order_id: int) -> None:
        data = self._post(
            "/bot_remove",
            {
                "username": session.username,
                "sessionToken": session.session_token,
                "orderId": int(order_id),
            },
        )
        _assert_success_message(data)

    def bid_auction(self, session: Session, bid: int) -> dict[str, Any]:
        data = self._post(
            "/bid_auction",
            {
                "username": session.username,
                "sessionToken": session.session_token,
                "bid": int(bid),
            },
        )
        _assert_success_message(data)
        return data

    def get_leading_auction_bid(self) -> dict[str, Any]:
        payload = {
            "adminUsername": _require_env("ADMIN_USERNAME"),
            "adminPassword": _require_env("ADMIN_PASSWORD"),
        }
        data = self._post("/get_leading_auction_bid", payload)
        _assert_success_message(data)
        return data

    def terminate_auction(self) -> dict[str, Any]:
        payload = {
            "adminUsername": _require_env("ADMIN_USERNAME"),
            "adminPassword": _require_env("ADMIN_PASSWORD"),
        }
        data = self._post("/terminate_auction", payload)
        _assert_success_message(data)
        return data

    def batch(
        self, bot_session: BotSession, operations: list[dict[str, Any]]
    ) -> dict[str, Any]:
        data = self._post(
            "/batch",
            {
                "username": bot_session.username,
                "sessionToken": bot_session.session_token,
                "operations": operations,
            },
            timeout_s=10.0,
        )
        # BatchResponse does not use AbstractMessageResponse. Expect: {status, results}
        if not isinstance(data.get("status"), str):
            raise ApiError(f"Unexpected batch response: {data}")
        return data

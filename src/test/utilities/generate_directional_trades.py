#!/usr/bin/env python3
"""Generate directional trades for frontend charting.

This script creates a small set of bots via admin endpoints and generates a
sequence of trades whose prices follow a trend (up/down/flat) or a two-leg trend
(up→down, down→up) + noise. The goal is to produce realistic-looking trade
prints so you can evaluate the UI chart.

Requirements:
- Backend running (default: http://localhost:8080)
- Admin creds available via env vars (ADMIN_USERNAME, ADMIN_PASSWORD)

Examples:
  python src/test/utilities/generate_directional_trades.py \
    --ticker A --direction up --trades 400 --start-price 80 --end-price 140

    # Two-leg trend (up then down) to stress-test chart shape
    python src/test/utilities/generate_directional_trades.py \
        --ticker A --direction updown --trades 600 --start-price 100 --swing 35 --turn 0.55

  HTTP_URL=http://localhost:8080 ADMIN_USERNAME=admin ADMIN_PASSWORD=admin \
    python src/test/utilities/generate_directional_trades.py --ticker A --direction down

Notes:
- Uses existing helper: src/test/integration/exchange_client.py
- Uses env loading helper: src/test/utilities/env_loader.py
"""

from __future__ import annotations

import argparse
import os
import random
import sys
import time
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable


# --- Import repo-local helpers (works when running as a script) ---
_THIS_FILE = Path(__file__).resolve()
_TEST_DIR = _THIS_FILE.parents[1]  # .../src/test
_INTEGRATION_DIR = _TEST_DIR / "integration"
_UTILITIES_DIR = _TEST_DIR / "utilities"

sys.path.insert(0, str(_INTEGRATION_DIR))
sys.path.insert(0, str(_UTILITIES_DIR))

from exchange_client import ApiError, BotSession, ExchangeClient  # noqa: E402
from env_loader import getenv, load_env  # noqa: E402


@dataclass(frozen=True)
class Bot:
    username: str
    session: BotSession


def _load_env_best_effort() -> None:
    # Prefer a real .env if present (often gitignored), otherwise fall back to
    # the committed .env.example.
    load_env(".env")
    load_env(".env.example")


def _parse_tickers(values: list[str]) -> list[str]:
    tickers: list[str] = []
    for v in values:
        for part in v.split(","):
            p = part.strip()
            if p:
                tickers.append(p)
    # preserve order, de-dupe
    out: list[str] = []
    seen: set[str] = set()
    for t in tickers:
        if t not in seen:
            out.append(t)
            seen.add(t)
    return out


def _clamp_int(x: int, lo: int, hi: int) -> int:
    return max(lo, min(hi, x))


def _clamp_float(x: float, lo: float, hi: float) -> float:
    return max(lo, min(hi, x))


def _pivot_index(trades: int, turn: float) -> int:
    """Return a pivot index in [1, trades-1]."""
    if trades <= 1:
        return 0
    # Interpret turn as a fraction of the series length.
    idx = int(round((trades - 1) * _clamp_float(turn, 0.0, 1.0)))
    return int(_clamp_int(idx, 1, trades - 1))


def _force_monotone_segment(
    prices: list[int],
    *,
    start: int,
    end: int,
    direction: str,
    min_price: int,
    max_price: int,
) -> None:
    """Nudge rounded/noisy prices to keep the intended direction within a segment."""
    if end - start < 1:
        return
    if direction not in {"up", "down"}:
        return
    step = 1 if direction == "up" else -1
    for i in range(start + 1, end + 1):
        if direction == "up" and prices[i] < prices[i - 1]:
            prices[i] = _clamp_int(prices[i - 1] + step, min_price, max_price)
        elif direction == "down" and prices[i] > prices[i - 1]:
            prices[i] = _clamp_int(prices[i - 1] + step, min_price, max_price)


def _make_price_path(
    *,
    trades: int,
    start_price: int,
    end_price: int | None,
    direction: str,
    turn: float,
    swing: int | None,
    noise_sigma: float,
    min_price: int,
    max_price: int,
    rng: random.Random,
) -> list[int]:
    if trades <= 0:
        return []

    # Two-leg shapes for clearer chart visuals.
    if direction in {"updown", "downup"}:
        pivot = _pivot_index(trades, turn)

        final_price = start_price if end_price is None else int(end_price)

        if swing is None:
            # Default: make the swing visible even with noise.
            baseline = abs(final_price - start_price)
            swing = max(5, baseline if baseline > 0 else max(5, trades // 12))

        mid_price = (
            start_price + int(swing)
            if direction == "updown"
            else start_price - int(swing)
        )
        mid_price = _clamp_int(int(mid_price), min_price, max_price)
        final_price = _clamp_int(int(final_price), min_price, max_price)

        # Build piecewise-linear base path, then add noise.
        bases: list[float] = []
        for i in range(trades):
            if i <= pivot:
                denom = max(1, pivot)
                a = i / denom
                base = start_price + (mid_price - start_price) * a
            else:
                denom = max(1, (trades - 1 - pivot))
                a = (i - pivot) / denom
                base = mid_price + (final_price - mid_price) * a
            bases.append(base)

        prices = [
            _clamp_int(
                int(round(b + rng.gauss(0.0, noise_sigma))), min_price, max_price
            )
            for b in bases
        ]

        # Enforce the intended direction within each leg.
        if direction == "updown":
            _force_monotone_segment(
                prices,
                start=0,
                end=pivot,
                direction="up",
                min_price=min_price,
                max_price=max_price,
            )
            _force_monotone_segment(
                prices,
                start=pivot,
                end=trades - 1,
                direction="down",
                min_price=min_price,
                max_price=max_price,
            )
        else:
            _force_monotone_segment(
                prices,
                start=0,
                end=pivot,
                direction="down",
                min_price=min_price,
                max_price=max_price,
            )
            _force_monotone_segment(
                prices,
                start=pivot,
                end=trades - 1,
                direction="up",
                min_price=min_price,
                max_price=max_price,
            )

        return prices

    if end_price is None:
        if direction == "up":
            end_price = start_price + max(1, trades // 20)
        elif direction == "down":
            end_price = start_price - max(1, trades // 20)
        else:
            end_price = start_price

    # Linear drift between start and end, plus Gaussian noise.
    drift_per_trade = 0.0 if trades == 1 else (end_price - start_price) / (trades - 1)
    prices: list[int] = []
    for i in range(trades):
        base = start_price + drift_per_trade * i
        noisy = base + rng.gauss(0.0, noise_sigma)
        prices.append(_clamp_int(int(round(noisy)), min_price, max_price))

    # Avoid long flat segments from rounding by forcing minimal movement
    # in the intended direction.
    if direction in {"up", "down"} and trades >= 2:
        _force_monotone_segment(
            prices,
            start=0,
            end=trades - 1,
            direction=direction,
            min_price=min_price,
            max_price=max_price,
        )

    return prices


def _create_bots(
    client: ExchangeClient,
    *,
    prefix: str,
    count: int,
) -> list[Bot]:
    bots: list[Bot] = []
    for i in range(count):
        username = f"{prefix}_{i}"
        api_key = client.admin_add_bot(username=username, name=f"Chart Bot {i}")
        session = client.bot_buildup(username, api_key)
        bots.append(Bot(username=username, session=session))
        time.sleep(0.03)
    return bots


def _trade_at_price(
    client: ExchangeClient,
    *,
    ticker: str,
    price: int,
    volume: int,
    maker: Bot,
    taker: Bot,
    taker_is_buy: bool,
    max_attempts: int = 3,
) -> None:
    """Execute one trade at (approximately) a target price.

    Primary path: maker posts a limit at `price`, taker hits via market order.
    Fallback: taker uses a crossing limit order if market order fails.
    """

    for attempt in range(max_attempts):
        try:
            if taker_is_buy:
                # maker sells at price; taker buys
                client.bot_limit_order(
                    maker.session,
                    ticker=ticker,
                    volume=volume,
                    price=price,
                    is_bid=False,
                )
                client.bot_market_order(
                    taker.session, ticker=ticker, volume=volume, is_bid=True
                )
            else:
                # maker buys at price; taker sells
                client.bot_limit_order(
                    maker.session,
                    ticker=ticker,
                    volume=volume,
                    price=price,
                    is_bid=True,
                )
                client.bot_market_order(
                    taker.session, ticker=ticker, volume=volume, is_bid=False
                )
            return
        except ApiError:
            # small pause then retry
            time.sleep(0.05 * (attempt + 1))

        # Fallback: crossing limit as taker (often more deterministic)
        try:
            if taker_is_buy:
                client.bot_limit_order(
                    maker.session,
                    ticker=ticker,
                    volume=volume,
                    price=price,
                    is_bid=False,
                )
                client.bot_limit_order(
                    taker.session,
                    ticker=ticker,
                    volume=volume,
                    price=price + 1,
                    is_bid=True,
                )
            else:
                client.bot_limit_order(
                    maker.session,
                    ticker=ticker,
                    volume=volume,
                    price=price,
                    is_bid=True,
                )
                client.bot_limit_order(
                    taker.session,
                    ticker=ticker,
                    volume=volume,
                    price=price - 1,
                    is_bid=False,
                )
            return
        except ApiError:
            time.sleep(0.05 * (attempt + 1))

    raise ApiError(f"Failed to execute trade: {ticker} price={price} volume={volume}")


def _chunks(items: list[int], n: int) -> Iterable[list[int]]:
    for i in range(0, len(items), n):
        yield items[i : i + n]


def _taker_is_buy_for_trade(
    *, direction: str, i: int, trades: int, turn: float
) -> bool:
    if direction == "flat":
        return i % 2 == 0
    if direction == "up":
        return True
    if direction == "down":
        return False
    if direction in {"updown", "downup"}:
        pivot = _pivot_index(trades, turn)
        first_leg = i <= pivot
        if direction == "updown":
            return True if first_leg else False
        return False if first_leg else True
    return True


def main() -> int:
    _load_env_best_effort()

    parser = argparse.ArgumentParser(
        description="Generate directional trades (bots) for frontend charting",
        formatter_class=argparse.ArgumentDefaultsHelpFormatter,
    )
    parser.add_argument(
        "--base-url",
        default=(os.getenv("HTTP_URL") or "http://localhost:8080"),
        help="Base HTTP URL for the exchange backend",
    )
    parser.add_argument(
        "--ticker",
        action="append",
        default=["A"],
        help="Ticker to trade; can be repeated or comma-separated (e.g. --ticker A,B)",
    )
    parser.add_argument(
        "--direction",
        choices=["up", "down", "flat", "updown", "downup"],
        default="up",
        help="Overall market direction",
    )
    parser.add_argument("--trades", type=int, default=300, help="Trades per ticker")
    parser.add_argument("--start-price", type=int, default=100)
    parser.add_argument(
        "--end-price",
        type=int,
        default=None,
        help=(
            "Optional end price. For up/down: end of drift. For updown/downup: final price "
            "after the second leg (defaults to start price)."
        ),
    )
    parser.add_argument(
        "--turn",
        type=float,
        default=0.5,
        help="Pivot point for updown/downup as a fraction of the series (0..1)",
    )
    parser.add_argument(
        "--swing",
        type=int,
        default=None,
        help=(
            "Swing size for updown/downup (distance from start to pivot price). "
            "If omitted, picks a reasonable default."
        ),
    )
    parser.add_argument(
        "--noise-sigma",
        type=float,
        default=1.5,
        help="Gaussian noise added around the drift (in price units)",
    )
    parser.add_argument("--volume-min", type=int, default=1)
    parser.add_argument("--volume-max", type=int, default=6)
    parser.add_argument(
        "--bots",
        type=int,
        default=4,
        help="How many bots to create (trades cycle through bots)",
    )
    parser.add_argument(
        "--prefix",
        default=None,
        help="Bot username prefix (defaults to chartbot_<epoch>)",
    )
    parser.add_argument(
        "--sleep-ms",
        type=int,
        default=15,
        help="Sleep between trades (helps avoid rate limiting)",
    )
    parser.add_argument(
        "--set-initial-price",
        action="store_true",
        help="Call admin /set_price for the tickers to set a baseline",
    )
    parser.add_argument(
        "--seed",
        type=int,
        default=1,
        help="RNG seed for repeatable paths",
    )
    parser.add_argument(
        "--cleanup-every",
        type=int,
        default=0,
        help="If >0, bots call /bot_remove_all every N trades (slow, but keeps books clean)",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Print planned price path and exit without calling the API",
    )

    args = parser.parse_args()

    tickers = _parse_tickers(args.ticker)

    # Price bounds from env (optional)
    min_price = int(os.getenv("ORDER_MIN_PRICE") or 0)
    max_price = int(os.getenv("ORDER_MAX_PRICE") or 1000)

    if args.volume_min <= 0 or args.volume_max < args.volume_min:
        raise SystemExit("Invalid volume range")

    if args.dry_run:
        rng = random.Random(args.seed)
        for t in tickers:
            prices = _make_price_path(
                trades=args.trades,
                start_price=args.start_price,
                end_price=args.end_price,
                direction=args.direction,
                turn=args.turn,
                swing=args.swing,
                noise_sigma=args.noise_sigma,
                min_price=min_price,
                max_price=max_price,
                rng=rng,
            )
            print(f"{t}: {len(prices)} trades, prices[{min(prices)}..{max(prices)}]")
            for chunk in _chunks(prices[:60], 20):
                print("  " + ", ".join(map(str, chunk)))
            if len(prices) > 60:
                print("  ...")
        return 0

    # Validate env quickly with friendlier messages than deep stack traces.
    if not (getenv("ADMIN_USERNAME") and getenv("ADMIN_PASSWORD")):
        raise SystemExit(
            "Missing ADMIN_USERNAME/ADMIN_PASSWORD. Set them in env or in a .env file."
        )

    client = ExchangeClient(args.base_url)
    try:
        client.admin_set_state(1)

        if args.set_initial_price:
            client.admin_set_price({t: int(args.start_price) for t in tickers})

        prefix = args.prefix or f"chartbot_{int(time.time())}"
        bots = _create_bots(client, prefix=prefix, count=int(args.bots))
        if len(bots) < 2:
            raise SystemExit("Need at least 2 bots to generate trades")

        rng = random.Random(args.seed)

        total = 0
        started = time.time()

        for ticker in tickers:
            prices = _make_price_path(
                trades=int(args.trades),
                start_price=int(args.start_price),
                end_price=args.end_price,
                direction=str(args.direction),
                turn=float(args.turn),
                swing=args.swing,
                noise_sigma=float(args.noise_sigma),
                min_price=min_price,
                max_price=max_price,
                rng=rng,
            )

            for i, price in enumerate(prices):
                maker = bots[(i) % len(bots)]
                taker = bots[(i + 1) % len(bots)]

                volume = rng.randint(int(args.volume_min), int(args.volume_max))

                taker_is_buy = _taker_is_buy_for_trade(
                    direction=str(args.direction),
                    i=int(i),
                    trades=len(prices),
                    turn=float(args.turn),
                )

                # Swap roles sometimes to avoid one-sided exposure.
                if i % 5 == 0:
                    maker, taker = taker, maker

                _trade_at_price(
                    client,
                    ticker=str(ticker),
                    price=int(price),
                    volume=int(volume),
                    maker=maker,
                    taker=taker,
                    taker_is_buy=bool(taker_is_buy),
                )

                total += 1

                if args.cleanup_every and args.cleanup_every > 0:
                    if total % int(args.cleanup_every) == 0:
                        # Best-effort cleanup (ignoring failures) to keep the
                        # book tidy if any partial fills happened.
                        for b in bots:
                            try:
                                client.bot_remove_all(b.session)
                            except Exception:
                                pass

                if args.sleep_ms:
                    time.sleep(max(0.0, int(args.sleep_ms)) / 1000.0)

                # Light progress output every ~100 trades.
                if total % 100 == 0:
                    elapsed = max(0.001, time.time() - started)
                    print(f"Placed {total} trades ({total/elapsed:.1f}/s)")

        elapsed = max(0.001, time.time() - started)
        print(f"Done. Placed {total} trades in {elapsed:.1f}s ({total/elapsed:.1f}/s).")
        print(f"Bots prefix: {prefix} (count={len(bots)})")
        return 0
    finally:
        client.close()


if __name__ == "__main__":
    raise SystemExit(main())

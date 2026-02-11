#!/usr/bin/env python3
"""Generate a LocalDB JSON file compatible with HighThroughputExchange.

By default this creates the same set of tables expected by the server:
- users (team accounts)
- bots (bot accounts)
- sessions
- botSessions

The Java LocalDB loader only requires the `backing` object for each table,
but we also emit `name` and `allKeys` to match the existing `assets/data.json`.

Examples:
  python src/test/utilities/generate_data_json.py
  python src/test/utilities/generate_data_json.py --teams 250 --out assets/data.json
  python src/test/utilities/generate_data_json.py --bots tradingbot1,makerbot --with-bot-sessions
  python src/test/utilities/generate_data_json.py --seed 12345 --out /tmp/data.json
"""

from __future__ import annotations

import argparse
import json
import random
import secrets
import string
from pathlib import Path
from typing import Any, Dict, Iterable, List, Optional


_API_KEY_ALPHABET = string.ascii_uppercase
_DEFAULT_API_KEY_LEN = 16
_DEFAULT_SESSION_TOKEN_LEN = 16


def _find_repo_root() -> Path:
    here = Path(__file__).resolve()
    for parent in [here.parent, *here.parents]:
        if (parent / "pom.xml").is_file() and (parent / "assets").is_dir():
            return parent
    # Fallback: assume current file is within the repo
    return here.parents[3]


def _rng_from_seed(seed: Optional[int]) -> Optional[random.Random]:
    if seed is None:
        return None
    return random.Random(seed)


def _rand_str(
    length: int,
    alphabet: str,
    rng: Optional[random.Random] = None,
) -> str:
    if rng is None:
        return "".join(secrets.choice(alphabet) for _ in range(length))
    return "".join(rng.choice(alphabet) for _ in range(length))


def _make_table(name: str, backing: Dict[str, Any]) -> Dict[str, Any]:
    return {
        "name": name,
        "backing": backing,
        "allKeys": list(backing.keys()),
    }


def _make_user_record(
    username: str,
    name: str,
    email: str,
    rng: Optional[random.Random],
    api_key_len: int,
) -> Dict[str, str]:
    api_key_1 = _rand_str(api_key_len, _API_KEY_ALPHABET, rng=rng)
    api_key_2 = _rand_str(api_key_len, _API_KEY_ALPHABET, rng=rng)
    return {
        "username": username,
        "name": name,
        "apiKey": api_key_1,
        "apiKey2": api_key_2,
        "email": email,
    }


def _make_session_record(
    username: str,
    rng: Optional[random.Random],
    token_len: int,
) -> Dict[str, str]:
    return {
        "sessionToken": _rand_str(token_len, _API_KEY_ALPHABET, rng=rng),
        "username": username,
    }


def _parse_csv_list(value: str) -> List[str]:
    items = [v.strip() for v in value.split(",")]
    return [v for v in items if v]


def generate_db(
    *,
    teams: int,
    team_prefix: str,
    team_email: str,
    bots: Iterable[str],
    with_bot_sessions: bool,
    with_user_sessions: bool,
    seed: Optional[int],
    api_key_len: int,
    session_token_len: int,
) -> Dict[str, Any]:
    rng = _rng_from_seed(seed)

    users_backing: Dict[str, Any] = {}
    for i in range(teams):
        username = f"{team_prefix}{i}"
        users_backing[username] = _make_user_record(
            username=username,
            name=f"Team {i}",
            email=team_email,
            rng=rng,
            api_key_len=api_key_len,
        )

    bots_backing: Dict[str, Any] = {}
    for bot_username in bots:
        bots_backing[bot_username] = _make_user_record(
            username=bot_username,
            name="",
            email="",
            rng=rng,
            api_key_len=api_key_len,
        )

    sessions_backing: Dict[str, Any] = {}
    bot_sessions_backing: Dict[str, Any] = {}

    if with_user_sessions:
        for username in users_backing.keys():
            sessions_backing[username] = _make_session_record(
                username=username,
                rng=rng,
                token_len=session_token_len,
            )

    if with_bot_sessions:
        for username in bots_backing.keys():
            bot_sessions_backing[username] = _make_session_record(
                username=username,
                rng=rng,
                token_len=session_token_len,
            )

    # Keep top-level order stable/readable.
    return {
        "bots": _make_table("bots", bots_backing),
        "sessions": _make_table("sessions", sessions_backing),
        "botSessions": _make_table("botSessions", bot_sessions_backing),
        "users": _make_table("users", users_backing),
    }


def main() -> int:
    repo_root = _find_repo_root()

    parser = argparse.ArgumentParser(description="Generate assets/data.json")
    parser.add_argument(
        "--out",
        default=str(repo_root / "assets" / "data.json"),
        help="Output path (default: assets/data.json)",
    )
    parser.add_argument(
        "--teams",
        type=int,
        default=100,
        help="Number of team users to generate (default: 100)",
    )
    parser.add_argument(
        "--team-prefix",
        default="team",
        help="Team username prefix (default: team)",
    )
    parser.add_argument(
        "--team-email",
        default="team@team.team",
        help="Email assigned to all teams (default: team@team.team)",
    )
    parser.add_argument(
        "--bots",
        default="tradingbot1",
        help="Comma-separated bot usernames (default: tradingbot1)",
    )
    parser.add_argument(
        "--with-bot-sessions",
        action="store_true",
        help="Populate botSessions with session tokens",
    )
    parser.add_argument(
        "--with-user-sessions",
        action="store_true",
        help="Populate sessions with session tokens",
    )
    parser.add_argument(
        "--seed",
        type=int,
        default=None,
        help="Seed for deterministic generation (default: random)",
    )
    parser.add_argument(
        "--api-key-len",
        type=int,
        default=_DEFAULT_API_KEY_LEN,
        help=f"API key length (default: {_DEFAULT_API_KEY_LEN})",
    )
    parser.add_argument(
        "--session-token-len",
        type=int,
        default=_DEFAULT_SESSION_TOKEN_LEN,
        help=f"Session token length (default: {_DEFAULT_SESSION_TOKEN_LEN})",
    )

    args = parser.parse_args()

    out_path = Path(args.out)
    if not out_path.is_absolute():
        out_path = (repo_root / out_path).resolve()

    db = generate_db(
        teams=args.teams,
        team_prefix=args.team_prefix,
        team_email=args.team_email,
        bots=_parse_csv_list(args.bots),
        with_bot_sessions=args.with_bot_sessions,
        with_user_sessions=args.with_user_sessions,
        seed=args.seed,
        api_key_len=args.api_key_len,
        session_token_len=args.session_token_len,
    )

    out_path.parent.mkdir(parents=True, exist_ok=True)
    with out_path.open("w", encoding="utf-8") as f:
        json.dump(db, f, indent=2, sort_keys=False)
        f.write("\n")

    print(f"Wrote {out_path}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

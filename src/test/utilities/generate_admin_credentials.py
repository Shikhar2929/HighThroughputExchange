#!/usr/bin/env python3
"""Generate admin credentials for HighThroughputExchange.

The server reads admin credentials from environment variables:
- ADMIN_USERNAME
- ADMIN_PASSWORD

This script prints new values and can optionally update a dotenv file.

Examples:
  python src/test/utilities/generate_admin_credentials.py
  python src/test/utilities/generate_admin_credentials.py --update-dotenv .env
  python src/test/utilities/generate_admin_credentials.py --username trading_club_admin --update-dotenv .env.example
"""

from __future__ import annotations

import argparse
import secrets
import string
from pathlib import Path
from typing import Dict, List, Tuple


_USERNAME_ALPHABET = string.ascii_lowercase + string.digits
_PASSWORD_ALPHABET = string.ascii_letters + string.digits


def _find_repo_root() -> Path:
    here = Path(__file__).resolve()
    for parent in [here.parent, *here.parents]:
        if (parent / "pom.xml").is_file() and (parent / "assets").is_dir():
            return parent
    return here.parents[3]


def _gen_username(prefix: str, length: int) -> str:
    suffix_len = max(1, length - len(prefix))
    suffix = "".join(secrets.choice(_USERNAME_ALPHABET) for _ in range(suffix_len))
    return (prefix + suffix)[:length]


def _gen_password(length: int) -> str:
    # Keep it shell/.env-friendly: no quotes, no spaces.
    return "".join(secrets.choice(_PASSWORD_ALPHABET) for _ in range(length))


def _parse_dotenv_lines(lines: List[str]) -> Dict[str, Tuple[int, str]]:
    """Return key -> (line_index, raw_line) for existing KEY=VALUE lines."""
    out: Dict[str, Tuple[int, str]] = {}
    for idx, raw in enumerate(lines):
        stripped = raw.strip()
        if not stripped or stripped.startswith("#"):
            continue
        if "=" not in stripped:
            continue
        key, _ = stripped.split("=", 1)
        key = key.strip()
        if key:
            out[key] = (idx, raw)
    return out


def _update_dotenv_file(dotenv_path: Path, updates: Dict[str, str]) -> None:
    existing_lines: List[str] = []
    if dotenv_path.exists():
        existing_lines = dotenv_path.read_text(encoding="utf-8").splitlines(
            keepends=True
        )

    key_map = _parse_dotenv_lines(existing_lines)

    # Replace existing keys.
    for key, value in updates.items():
        new_line = f"{key}={value}\n"
        if key in key_map:
            idx, _ = key_map[key]
            existing_lines[idx] = new_line
        else:
            # Add to end (ensure file ends with newline).
            if existing_lines and not existing_lines[-1].endswith("\n"):
                existing_lines[-1] = existing_lines[-1] + "\n"
            existing_lines.append(new_line)

    dotenv_path.parent.mkdir(parents=True, exist_ok=True)
    dotenv_path.write_text("".join(existing_lines), encoding="utf-8")


def main() -> int:
    repo_root = _find_repo_root()

    parser = argparse.ArgumentParser(
        description="Generate ADMIN_USERNAME and ADMIN_PASSWORD"
    )
    parser.add_argument(
        "--username",
        default=None,
        help="Explicit username to use (default: auto-generate)",
    )
    parser.add_argument(
        "--username-prefix",
        default="hte_admin_",
        help="Prefix used when auto-generating username (default: hte_admin_)",
    )
    parser.add_argument(
        "--username-len",
        type=int,
        default=18,
        help="Length for auto-generated username (default: 18)",
    )
    parser.add_argument(
        "--password-len",
        type=int,
        default=32,
        help="Length for generated password (default: 32)",
    )
    parser.add_argument(
        "--update-dotenv",
        default=None,
        help="Update/insert values into this dotenv file (e.g. .env or .env.example)",
    )

    args = parser.parse_args()

    username = args.username or _gen_username(args.username_prefix, args.username_len)
    password = _gen_password(args.password_len)

    print(f"ADMIN_USERNAME={username}")
    print(f"ADMIN_PASSWORD={password}")

    if args.update_dotenv:
        dotenv_path = Path(args.update_dotenv)
        if not dotenv_path.is_absolute():
            dotenv_path = (repo_root / dotenv_path).resolve()
        _update_dotenv_file(
            dotenv_path,
            {
                "ADMIN_USERNAME": username,
                "ADMIN_PASSWORD": password,
            },
        )
        print(f"Updated {dotenv_path}")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())

import os
from pathlib import Path
from typing import Optional

_loaded = False


def _find_env_file(filename: str = "public.env") -> Optional[Path]:
    """
    Walk up from this file's directory to the repo root to find the env file.
    """
    here = Path(__file__).resolve().parent
    for parent in [here, *here.parents]:
        candidate = parent / filename
        if candidate.is_file():
            return candidate
    return None


def _fallback_parse_and_load(env_path: Path) -> None:
    def _parse_line(line: str) -> Optional[tuple[str, str]]:
        line = line.strip()
        if not line or line.startswith("#"):
            return None
        if "=" not in line:
            return None
        key, val = line.split("=", 1)
        return key.strip(), val.strip()

    with env_path.open("r", encoding="utf-8") as f:
        for raw in f:
            parsed = _parse_line(raw)
            if not parsed:
                continue
            key, val = parsed
            if key not in os.environ:
                os.environ[key] = val


def load_env(filename: str = "public.env") -> None:
    """
    Load key=value pairs from the given env file into os.environ (without overriding existing keys).
    """
    global _loaded
    if _loaded:
        return
    env_path = _find_env_file(filename)
    if not env_path:
        _loaded = True
        return

    # Try python-dotenv first (better parsing, quoting, etc.)
    try:
        from dotenv import load_dotenv  # type: ignore[import-not-found]

        load_dotenv(dotenv_path=str(env_path), override=False)
    except Exception:
        # Fall back to minimal parser
        _fallback_parse_and_load(env_path)
    finally:
        _loaded = True


def getenv(key: str, default: Optional[str] = None) -> Optional[str]:
    return os.environ.get(key, default)

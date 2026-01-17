#!/usr/bin/env python3
"""Tiny local static server for the websocket tester UI.

Usage:
  python3 serve.py

Then open:
  http://localhost:5173/

This serves files from this folder.
"""

from __future__ import annotations

import http.server
import os
import socketserver
import webbrowser


DEFAULT_HOST = "127.0.0.1"
DEFAULT_PORT = 5173


def main() -> None:
    port = int(os.getenv("WS_TESTER_PORT", str(DEFAULT_PORT)))
    host = os.getenv("WS_TESTER_HOST", DEFAULT_HOST)

    class NoCacheRequestHandler(http.server.SimpleHTTPRequestHandler):
        def end_headers(self) -> None:
            # Avoid confusing dev-time caching where the browser keeps serving an old app.js.
            self.send_header("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0")
            self.send_header("Pragma", "no-cache")
            self.send_header("Expires", "0")
            super().end_headers()

    handler = NoCacheRequestHandler

    with socketserver.TCPServer((host, port), handler) as httpd:
        url = f"http://{host}:{port}/"
        print(f"Serving websocket tester at {url}")
        try:
            webbrowser.open(url)
        except Exception:
            pass
        httpd.serve_forever()


if __name__ == "__main__":
    main()

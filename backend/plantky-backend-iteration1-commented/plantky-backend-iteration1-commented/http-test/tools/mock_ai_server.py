#!/usr/bin/env python3
"""
PlantAssure Epic 1 local mock AI provider.

Run:
    python tools/mock_ai_server.py

The Spring Boot backend should use:
    PLANT_IDENTIFICATION_ENABLED=true
    PLANT_IDENTIFICATION_BASE_URL=http://localhost:8000
    PLANT_IDENTIFICATION_ENDPOINT=/identify

The server does not persist uploaded images. It reads the multipart request body
only to find the marker embedded in the supplied test image, then returns a
predictable candidate list.
"""
from http.server import BaseHTTPRequestHandler, HTTPServer
import json

HOST = "127.0.0.1"
PORT = 8000

RESPONSES = {
    "TOP3": {
        "candidates": [
            {"scientificName": "Acacia baileyana", "confidence": 0.94},
            {"scientificName": "Acacia dealbata", "confidence": 0.81},
            {"scientificName": "Example species", "confidence": 0.72},
            # Deliberately present a fourth result to prove the backend caps at <= 3.
            {"scientificName": "Berula erecta", "confidence": 0.55},
        ]
    },
    "TWO": {
        "candidates": [
            {"scientificName": "Acacia baileyana", "confidence": 0.91},
            {"scientificName": "Acacia dealbata", "confidence": 0.78},
        ]
    },
    "UNMAPPED": {
        "candidates": [
            {"scientificName": "Example species", "confidence": 0.82}
        ]
    },
    "EMPTY": {
        "candidates": []
    },
    "AMBIGUOUS": {
        "candidates": [
            {"scientificName": "Acacia longifolia var. mock", "confidence": 0.88}
        ]
    },
}

class Handler(BaseHTTPRequestHandler):
    server_version = "PlantAssureMockAI/1.0"

    def _send_json(self, status, payload):
        body = json.dumps(payload, ensure_ascii=False).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_GET(self):
        if self.path == "/health":
            self._send_json(200, {"status": "ok"})
        else:
            self._send_json(404, {"error": "not_found"})

    def do_POST(self):
        if self.path != "/identify":
            self._send_json(404, {"error": "not_found"})
            return

        try:
            length = int(self.headers.get("Content-Length", "0"))
        except ValueError:
            length = 0
        body = self.rfile.read(length)

        if b"identify_error.jpg" in body or b"MOCK_SCENARIO=ERROR" in body:
            self._send_json(503, {"error": "mock_provider_unavailable"})
            return

        if b"identify_two.jpg" in body or b"MOCK_SCENARIO=TWO" in body:
            scenario = "TWO"

        elif b"identify_unmapped.jpg" in body or b"MOCK_SCENARIO=UNMAPPED" in body:
            scenario = "UNMAPPED"

        elif b"identify_empty.jpg" in body or b"MOCK_SCENARIO=EMPTY" in body:
            scenario = "EMPTY"

        elif b"identify_ambiguous.jpg" in body or b"MOCK_SCENARIO=AMBIGUOUS" in body:
            scenario = "AMBIGUOUS"

        else:
            scenario = "TOP3"

        print(f"[mock-ai] detected scenario={scenario}")

        self._send_json(200, RESPONSES[scenario])

    def log_message(self, fmt, *args):
        print("[mock-ai] " + (fmt % args))

if __name__ == "__main__":
    print(f"PlantAssure mock AI listening on http://{HOST}:{PORT}")
    print("Health check: http://127.0.0.1:8000/health")
    HTTPServer((HOST, PORT), Handler).serve_forever()

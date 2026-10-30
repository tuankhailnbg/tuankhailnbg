#!/usr/bin/env python3
"""Read-only NRO item/map wire smoke test. Requires a running disposable server.

No account or credentials are used. Run only against your own test server.
"""
import argparse
import io
import json
import socket
import struct


def exact(sock, count):
    chunks = []
    while count:
        chunk = sock.recv(count)
        if not chunk:
            raise EOFError("Server closed connection")
        chunks.append(chunk)
        count -= len(chunk)
    return b"".join(chunks)


def frame(sock):
    command = struct.unpack("b", exact(sock, 1))[0]
    if command in {-32, -66, -74, 11, -67, -87, 66}:
        raise AssertionError(f"Unexpected large-frame command {command}")
    size = struct.unpack(">H", exact(sock, 2))[0]
    return command, exact(sock, size)


def request(sock, command, payload=b""):
    sock.sendall(struct.pack(">bH", command, len(payload)) + payload)


def verify(host, port, expected):
    with socket.create_connection((host, port), 5) as sock:
        sock.settimeout(5)
        request(sock, -27)
        command, key = frame(sock)
        if command != -27 or key[:2] != b"\x01\x00":
            raise AssertionError("This smoke test requires the repository's zero-key protocol")
        request(sock, -28, b"\x08")
        next_id = 0
        sizes = []
        while next_id < expected:
            command, data = frame(sock)
            assert command == -28 and data[0] == 8, "Item stream desynchronized"
            assert len(data) <= 30000, "Item packet exceeds safe frame budget"
            sizes.append(len(data))
            reader = io.BytesIO(data)
            reader.seek(3)
            mode = data[2]
            if mode == 0:
                continue
            if mode == 1:
                assert next_id == 0
                end = struct.unpack(">H", reader.read(2))[0]
            else:
                assert mode == 2, f"Invalid item subtype {mode}"
                start, end = struct.unpack(">HH", reader.read(4))
                assert start == next_id, "Skipped or duplicated item range"
            assert next_id < end <= expected
            for item_id in range(next_id, end):
                assert len(reader.read(2)) == 2
                for _ in range(2):
                    size = struct.unpack(">H", reader.read(2))[0]
                    assert len(reader.read(size)) == size, f"Truncated text for item {item_id}"
                assert len(reader.read(10)) == 10, f"Truncated item {item_id}"
            assert reader.read() == b"", "Trailing item bytes"
            next_id = end
        request(sock, -28, b"\x06")
        command, data = frame(sock)
        assert command == -28 and data[0] == 6, "Next map packet was corrupted"
        return {"items": next_id, "packet_sizes": sizes, "next_map_packet": "valid"}


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--host", default="127.0.0.1")
    parser.add_argument("--port", type=int, default=14445)
    parser.add_argument("--items", type=int, default=2421)
    args = parser.parse_args()
    print(json.dumps(verify(args.host, args.port, args.items), ensure_ascii=False))

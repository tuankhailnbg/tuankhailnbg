#!/usr/bin/env python3
"""Read-only NRO item/map wire smoke test using the real MOD_DP 2.4.x parser shape."""
import argparse
import io
import json
import socket
import struct

LARGE = {-32, -66, -74, 11, -67, -87, 66, 12}


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
    if command in LARGE:
        header = exact(sock, 3)
        size = sum(((b + 128) & 255) << (8 * i) for i, b in enumerate(header))
    else:
        size = struct.unpack(">H", exact(sock, 2))[0]
    return command, exact(sock, size)


def request(sock, command, payload=b""):
    sock.sendall(struct.pack(">bH", command, len(payload)) + payload)


def read_utf(reader):
    raw = reader.read(2)
    assert len(raw) == 2
    size = struct.unpack(">H", raw)[0]
    data = reader.read(size)
    assert len(data) == size
    return data.decode("utf-8")


def parse_option_packet(data):
    reader = io.BytesIO(data)
    assert reader.read(1) == b"\x08"
    version = reader.read(1)[0]
    assert reader.read(1) == b"\x00"
    # Exact MOD_DP loadItemNew(type 0): readShort option count.
    count = struct.unpack(">H", reader.read(2))[0]
    for _ in range(count):
        read_utf(reader)
        assert len(reader.read(1)) == 1
    assert reader.read() == b""
    return version, count


def parse_template_packet(data, expected):
    reader = io.BytesIO(data)
    # Exact MOD_DP read_cmdExtraBig -> loadItemNew(type 1).
    assert reader.read(1) == b"\x00"
    version = reader.read(1)[0]
    assert reader.read(1) == b"\x01"
    count = struct.unpack(">H", reader.read(2))[0]
    assert count == expected
    for item_id in range(count):
        assert len(reader.read(2)) == 2, f"truncated type/gender item {item_id}"
        read_utf(reader)
        read_utf(reader)
        assert len(reader.read(10)) == 10, f"truncated item {item_id}"
    assert reader.read() == b""
    return version, count


def verify(host, port, expected):
    with socket.create_connection((host, port), 5) as sock:
        sock.settimeout(10)
        request(sock, -27)
        command, key = frame(sock)
        if command != -27 or key[:2] != b"\x01\x00":
            raise AssertionError("This smoke test requires the repository's zero-key protocol")

        request(sock, -28, b"\x08")

        command, options = frame(sock)
        assert command == -28, f"expected -28 item options, got {command}"
        version0, option_count = parse_option_packet(options)

        command, templates = frame(sock)
        assert command == 12, f"expected command 12 item templates, got {command}"
        version1, item_count = parse_template_packet(templates, expected)
        assert version0 == version1

        request(sock, -28, b"\x06")
        command, data = frame(sock)
        assert command == -28 and data[0] == 6, "Next map packet was corrupted"

        return {
            "items": item_count,
            "options": option_count,
            "version": version0,
            "template_packet_bytes": len(templates),
            "next_map_packet": "valid",
        }


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--host", default="127.0.0.1")
    parser.add_argument("--port", type=int, default=14445)
    parser.add_argument("--items", type=int, default=2421)
    args = parser.parse_args()
    print(json.dumps(verify(args.host, args.port, args.items), ensure_ascii=False))

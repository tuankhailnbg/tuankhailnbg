#!/usr/bin/env python3
"""Disposable MariaDB + Login + Game Server authentication/character smoke.

Creates and drops only a uniquely named test database. Never uses player
credentials or changes the checkout's connection settings. Requires built JARs,
the repository resources, pymysql, and a local disposable MariaDB service.
"""
import io
import json
import os
from pathlib import Path
import shutil
import socket
import struct
import subprocess
import tempfile
import time
import uuid

import pymysql
from pymysql.constants import CLIENT
from test_nro_item_sync_wire import exact, request

ROOT = Path(__file__).resolve().parents[1]
GAME = ROOT / "Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea"
LARGE = {-32, -66, -74, 11, -67, -87, 66}


def utf(text):
    data = text.encode("utf-8")
    return struct.pack(">H", len(data)) + data


def frame(sock):
    command = struct.unpack("b", exact(sock, 1))[0]
    if command in LARGE:
        header = exact(sock, 3)
        size = sum(((b + 128) & 255) << (8 * i) for i, b in enumerate(header))
    else:
        size = struct.unpack(">H", exact(sock, 2))[0]
    return command, exact(sock, size)


def until(sock, predicate):
    for _ in range(300):
        command, data = frame(sock)
        if command == -26:
            raise AssertionError("Client-visible login error: " + data[2:].decode("utf-8"))
        if predicate(command, data):
            return data
    raise AssertionError("Expected login response was never received")


def client():
    sock = socket.create_connection(("127.0.0.1", 14445), 5)
    sock.settimeout(15)
    request(sock, -27)
    assert frame(sock)[:1] == (-27,)
    request(sock, -29, b"\x02\x01\x02\x00" + struct.pack(">ii??", 1024, 600, True, True)
            + utf("Windows|5.0.0"))
    return sock


def authenticate(sock, username, password):
    request(sock, -29, b"\x00" + utf(username) + utf(password))
    versions = until(sock, lambda c, d: c == -28 and d[:1] == b"\x04")
    assert versions[4] == 10, "Client did not receive the corrected item catalog version"


def verify_catalog(sock):
    request(sock, -28, b"\x08")
    next_id, sizes, record_sizes = 0, [], []
    while next_id < 2421:
        data = until(sock, lambda c, d: c == -28 and d[:1] == b"\x08")
        assert len(data) <= 30000
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
            assert mode == 2
            start, end = struct.unpack(">HH", reader.read(4))
            assert start == next_id
        for _ in range(next_id, end):
            offset = reader.tell()
            assert len(reader.read(2)) == 2
            for _ in range(2):
                size = struct.unpack(">H", reader.read(2))[0]
                assert len(reader.read(size)) == size
            assert len(reader.read(10)) == 10
            record_sizes.append(reader.tell() - offset)
        assert reader.read() == b""
        next_id = end
    assert next_id == 2421
    # Report the legacy split using this exact fixture. Live databases can
    # contain longer descriptions; do not assume their sizes match this dump.
    legacy_bytes = [5 + sum(record_sizes[:750]), 7 + sum(record_sizes[750:2154])]
    for command, payload, response in [(-87, b"", -87), (-28, b"\x06", -28), (-28, b"\x07", -28)]:
        request(sock, command, payload)
        until(sock, lambda c, d: c == response)
    return sizes, legacy_bytes


def read_utf(reader):
    size = struct.unpack(">H", reader.read(2))[0]
    data = reader.read(size)
    assert len(data) == size
    return data.decode("utf-8")


def read_menu(data, npc_id):
    reader = io.BytesIO(data)
    assert struct.unpack(">H", reader.read(2))[0] == npc_id
    text = read_utf(reader)
    count = reader.read(1)[0]
    choices = [read_utf(reader) for _ in range(count)]
    assert reader.read() == b"", "Malformed NPC menu payload"
    return text, choices


def verify_gameplay(sock, player_id):
    # The actual client acknowledges map load and requests its character again.
    request(sock, -39)
    request(sock, -28, b"\x0d")
    data = until(sock, lambda c, d: c == -30 and d[:1] == b"\x00")
    assert struct.unpack(">i", data[1:5])[0] == player_id

    request(sock, 33, struct.pack(">H", 80))
    data = until(sock, lambda c, d: c == 32)
    _, choices = read_menu(data, 80)
    assert len(choices) == 8
    request(sock, 32, struct.pack(">HB", 80, 6))
    data = until(sock, lambda c, d: c == 32)
    guide, _ = read_menu(data, 80)
    assert "10 Bút + 5 Thiệp trắng" in guide

    # Selecting a skill must not stop later commands, even if there is no target.
    request(sock, 34, struct.pack(">H", 0))
    request(sock, -7, struct.pack(">Bhh", 0, 610, 408))
    request(sock, 29)
    zones = until(sock, lambda c, d: c == 29)
    assert zones[0] >= 2 and len(zones) == 1 + zones[0] * 5
    request(sock, 21, b"\x01")
    data = until(sock, lambda c, d: c == -24)
    reader = io.BytesIO(data)
    assert reader.read(1) == b"\x05", "Zone change left Kame map"
    reader.read(4)
    read_utf(reader)
    assert reader.read(1) == b"\x01", "Zone change was not processed"
    assert struct.unpack(">hh", reader.read(4)) == (610, 408), "Movement did not reach server"
    request(sock, -39)

    request(sock, 44, utf("wire-gameplay-smoke"))
    data = until(sock, lambda c, d: c == 44)
    reader = io.BytesIO(data)
    assert struct.unpack(">i", reader.read(4))[0] == player_id
    assert read_utf(reader) == "wire-gameplay-smoke"
    return {"npc_open": "ok", "npc_selection": "ok", "move_then_change_zone": "ok", "chat_roundtrip": "ok"}


def wait_port(port, process):
    deadline = time.monotonic() + 90
    while time.monotonic() < deadline:
        if process.poll() is not None:
            raise AssertionError(f"Java process exited before opening {port}")
        try:
            with socket.create_connection(("127.0.0.1", port), 0.2):
                return
        except OSError:
            time.sleep(0.2)
    raise AssertionError(f"Port {port} did not open")


def run():
    database = "nro_login_smoke_" + uuid.uuid4().hex[:12]
    password = uuid.uuid4().hex
    db_user, db_password = os.environ.get("NRO_SQL_USER", "root"), os.environ.get("NRO_SQL_PASSWORD", "")
    connection = pymysql.connect(host="127.0.0.1", port=int(os.environ.get("NRO_SQL_PORT", "3306")),
                                 user=db_user, password=db_password, charset="utf8mb4",
                                 autocommit=True, client_flag=CLIENT.MULTI_STATEMENTS)
    processes, logs = [], []
    with tempfile.TemporaryDirectory(prefix="nro-login-smoke-") as temp:
        temp = Path(temp)
        try:
            with connection.cursor() as cursor:
                cursor.execute(f"CREATE DATABASE `{database}` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
                cursor.execute(f"USE `{database}`")
                cursor.execute((GAME / "Tutorial/Database/ngocrong.sql").read_text(encoding="utf-8"))
                while cursor.nextset():
                    pass
                # Use one complete character fixture, with synthetic credentials.
                cursor.execute("SELECT account_id,id FROM player ORDER BY id LIMIT 1")
                account_id, player_id = cursor.fetchone()
                cursor.execute("UPDATE account SET username='smoketest',password=%s,is_admin=0,active=1,ban=0,server_login=1,last_time_logout='2002-05-07 07:00:00' WHERE id=%s", (password, account_id))
                cursor.execute("UPDATE player SET name='smoketest',clan_id_sv1=-1,clan_id_sv2=-1,data_location='[600,408,5]' WHERE id=%s", (player_id,))
            login_dir, server_dir = temp / "Login", temp / "Server"
            login_dir.mkdir(); server_dir.mkdir()
            resources = server_dir / "resources"
            resources.mkdir()
            for source in (GAME / "Server/resources").iterdir():
                if source.name == "config":
                    shutil.copytree(source, resources / "config")
                else:
                    (resources / source.name).symlink_to(source, target_is_directory=source.is_dir())
            # Production is Windows; reproduce its case-insensitive asset path
            # on Linux without renaming or changing deployed resources.
            if not (resources / "khanhdtk").exists():
                (resources / "khanhdtk").symlink_to(resources / "KhanhDTK", target_is_directory=True)
            for folder in ["mob", "Eff"]:
                (server_dir / folder).symlink_to(GAME / "Server" / folder, target_is_directory=True)
            (server_dir / "log").mkdir()
            sql_port = os.environ.get("NRO_SQL_PORT", "3306")
            (login_dir / "server.ini").write_text(f"server.port=3105\ndb.driver=com.mysql.cj.jdbc.Driver\ndb.host=127.0.0.1\ndb.port={sql_port}\ndb.name={database}\ndb.user={db_user}\ndb.password={db_password}\nadmin.mode=0\nwait.login=0\n")
            config = resources / "config/server.properties"
            config.write_text(config.read_text() + f"\nserver.db.ip=127.0.0.1\nserver.db.port={sql_port}\nserver.db.name={database}\nserver.db.us={db_user}\nserver.db.pw={db_password}\nserver.sv=1\nserver.port=14445\nserver.sv1=Smoke:127.0.0.1:14445\nserver.maxperip=30\napi.port=0\nlogin.host=127.0.0.1\n")
            for folder, artifact in [(login_dir, "ServerLogin"), (server_dir, "TeaMobi")]:
                jar = GAME / folder.name / "target" / f"{artifact}-1.0-RELEASE-jar-with-dependencies.jar"
                log = open(temp / (folder.name + ".log"), "w+")
                logs.append(log)
                processes.append(subprocess.Popen([os.environ.get("NRO_JAVA", "java"), "-Xmx1g", "-jar", str(jar)], cwd=folder,
                                                  stdin=subprocess.PIPE, stdout=log, stderr=subprocess.STDOUT))
                wait_port(3105 if artifact == "ServerLogin" else 14445, processes[-1])
            # Allow the internal Login handshake to finish after the port opens.
            time.sleep(1)
            with client() as sock:
                request(sock, -29, b"\x00" + utf("smoketest") + utf("incorrect-password"))
                rejected = False
                for _ in range(20):
                    command, data = frame(sock)
                    if command == -26:
                        assert "không chính xác" in data[2:].decode("utf-8")
                        rejected = True; break
                assert rejected, "Wrong password did not receive an explicit rejection"
                authenticate(sock, "smoketest", password)  # Retry on the SAME connection.
                sizes, legacy_bytes = verify_catalog(sock)
                request(sock, -38)
                data = until(sock, lambda c, d: c == -30 and d[:1] == b"\x00")
                assert struct.unpack(">i", data[1:5])[0] == player_id
                # Keep reading until the actual server enter pipeline completes.
                deadline = time.monotonic() + 5
                while time.monotonic() < deadline:
                    logs[1].flush()
                    if "stage=ENTER_GAME_OK" in Path(logs[1].name).read_text():
                        break
                    time.sleep(0.05)
                else:
                    raise AssertionError("Character response sent but server did not finish entering game")
                gameplay = verify_gameplay(sock, player_id)
            print(json.dumps({"login": "ok", "wrong_password_and_same_socket_retry": "ok", "items": 2421,
                              "packet_sizes": sizes, "legacy_packet_bytes_without_event": legacy_bytes,
                              "character_loaded": True, "event_restored": True, "gameplay": gameplay}))
        except Exception:
            # Diagnostics only: do not print full config logs containing credentials.
            for log in logs:
                log.flush()
                for line in Path(log.name).read_text(errors="replace").splitlines():
                    if "[LOGIN]" in line or "[PLAY]" in line or "Exception" in line or line.lstrip().startswith("at ") or "ERROR" in line:
                        print(line)
            raise
        finally:
            for process in reversed(processes):
                process.terminate()
                try:
                    process.wait(10)
                except subprocess.TimeoutExpired:
                    process.kill(); process.wait()
            for log in logs:
                log.close()
            with connection.cursor() as cursor:
                cursor.execute(f"DROP DATABASE IF EXISTS `{database}`")
            connection.close()


if __name__ == "__main__":
    run()

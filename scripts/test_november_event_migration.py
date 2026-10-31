#!/usr/bin/env python3
"""Validate November assets and optionally smoke-test a disposable MariaDB database.

--db creates and removes a uniquely named test database, never the game database.
"""
import argparse
import json
import os
import re
import uuid
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
GAME = ROOT / "Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea"
MIGRATION = GAME / "database/migrations/20261030_november_tri_an.sql"
DUMP = GAME / "Tutorial/Database/ngocrong.sql"


def check(condition, message):
    if not condition:
        raise AssertionError(message)
    print("PASS", message)


def rows(table):
    text = DUMP.read_text(encoding="utf-8")
    result = {}
    for match in re.finditer(r"INSERT INTO `" + table + r"`[^\n]+ VALUES\n(.*?);\n", text, re.S):
        for line in match.group(1).splitlines():
            value = re.match(r"^\((\d+),", line)
            if value:
                result[int(value.group(1))] = line.rstrip(",;")
    return result


def statements():
    text = "\n".join(line for line in MIGRATION.read_text(encoding="utf-8").splitlines()
                     if not line.lstrip().startswith("--"))
    return [s.strip() for s in text.split(";") if s.strip()]


def sources():
    sql = MIGRATION.read_text(encoding="utf-8")
    item_sources = []
    costume_sources = {}
    for line in sql.splitlines():
        if line.startswith("INSERT INTO item_template"):
            item_sources.append(int(re.search(r"source.id=(\d+)", line).group(1)))
        if line.startswith("INSERT INTO cai_trang"):
            destination = int(re.search(r"SELECT (\d+),", line).group(1))
            costume_sources[destination] = int(re.search(r"source.id_temp=(\d+)", line).group(1))
    return sorted(set(item_sources)), costume_sources


def static_checks():
    items = rows("item_template")
    skins = rows("cai_trang")
    item_sources, costume_sources = sources()
    check(all(source in items for source in item_sources), "all event icons reference existing item templates")
    check(len(costume_sources) == 10 and all(source in skins for source in costume_sources.values()),
          "all five normal and five top costumes reference real costume artwork")
    check(not any(i in items for i in range(2400, 2421)), "event item range is unused in source database")
    check(80 not in rows("npc_template") and 251 not in rows("item_option_template"),
          "NPC 80 and option 251 do not overwrite source data")
    check(len(statements()) == 33, "complete idempotent migration contains expected statements")


def database_smoke():
    import pymysql
    database = "nro_november_ci_" + uuid.uuid4().hex[:12]
    connection = pymysql.connect(host=os.environ.get("NRO_SQL_HOST", "127.0.0.1"),
                                 user=os.environ.get("NRO_SQL_USER", "root"),
                                 password=os.environ["NRO_SQL_PASSWORD"],
                                 charset="utf8mb4", autocommit=True)
    try:
        with connection.cursor() as cursor:
            cursor.execute(f"CREATE DATABASE `{database}` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
            cursor.execute(f"USE `{database}`")
            cursor.execute("""CREATE TABLE item_template (
                id INT PRIMARY KEY,TYPE INT,gender INT,NAME VARCHAR(255),description TEXT,
                icon_id INT,part INT,is_up_to_up INT,power_require BIGINT
            ) ENGINE=InnoDB""")
            cursor.execute("CREATE TABLE cai_trang(id_temp INT PRIMARY KEY,head INT,body INT,leg INT,bag INT) ENGINE=InnoDB")
            cursor.execute("CREATE TABLE npc_template(id INT PRIMARY KEY,NAME VARCHAR(255),head INT,body INT,leg INT) ENGINE=InnoDB")
            cursor.execute("CREATE TABLE item_option_template(id INT PRIMARY KEY,NAME VARCHAR(255),TYPE INT) ENGINE=InnoDB")
            item_sources, costume_sources = sources()
            for table, ids in [("item_template", item_sources), ("cai_trang", set(costume_sources.values())),
                               ("npc_template", [13]), ("item_option_template", [250])]:
                originals = rows(table)
                for source in ids:
                    cursor.execute(f"INSERT INTO `{table}` VALUES " + originals[source])
            for _ in range(2):
                for statement in statements():
                    cursor.execute(statement)
            cursor.execute("SELECT COUNT(*) FROM item_template WHERE id BETWEEN 2400 AND 2420")
            check(cursor.fetchone()[0] == 17, "running migration twice creates exactly 17 event items")
            cursor.execute("SELECT COUNT(*) FROM cai_trang WHERE id_temp BETWEEN 2410 AND 2419")
            check(cursor.fetchone()[0] == 10, "all ten costume mappings installed")
            for destination, source in costume_sources.items():
                cursor.execute("SELECT head,body,leg,bag FROM cai_trang WHERE id_temp=%s", (destination,))
                copied = cursor.fetchone()
                cursor.execute("SELECT head,body,leg,bag FROM cai_trang WHERE id_temp=%s", (source,))
                check(copied == cursor.fetchone(), f"costume {destination} preserves source {source} artwork")
            cursor.execute("SELECT NAME FROM npc_template WHERE id=80")
            check(cursor.fetchone()[0] == "Sứ Giả Tri Ân", "dedicated event NPC installed")
            cursor.execute("SELECT ENGINE FROM information_schema.tables WHERE table_schema=%s AND table_name LIKE 'november2026_%%'", (database,))
            check([row[0] for row in cursor.fetchall()] == ["InnoDB"] * 3, "all progress and settlement tables are transactional")
            state = json.dumps({"coins": 1500, "score": 1500, "date": "2026-11-15"})
            cursor.execute("INSERT INTO november2026_players(player_id,player_name,score,reached_at,data) VALUES(1,'Người kiểm tra',1500,1,%s)", (state,))
            connection.begin()
            cursor.execute("SELECT data FROM november2026_players WHERE player_id=1 FOR UPDATE")
            original = cursor.fetchone()[0]
            cursor.execute("UPDATE november2026_players SET score=0,data='{}' WHERE player_id=1")
            connection.rollback()
            cursor.execute("SELECT data,score FROM november2026_players WHERE player_id=1")
            check(cursor.fetchone() == (original, 1500), "rollback preserves persistent event balances")
    finally:
        try:
            with connection.cursor() as cursor:
                cursor.execute(f"DROP DATABASE IF EXISTS `{database}`")
        finally:
            connection.close()


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--db", action="store_true")
    args = parser.parse_args()
    static_checks()
    if args.db:
        database_smoke()

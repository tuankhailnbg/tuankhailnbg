#!/usr/bin/env python3
"""Validate the current referral costume migration.
Run with --db against a disposable MariaDB 10.11 instance only.
"""
import argparse
import json
import os
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
EVENT = ROOT / "database/migrations/20261028_referral_event_kame.sql"
MANUAL = ROOT / "database/migrations/20261028_referral_manual_points.sql"
NONSHOP = ROOT / "database/migrations/20261028_referral_nonshop_costumes.sql"

EXPECTED = [
    (5, 1304, 7, "Goku SSJ4"),
    (10, 1305, 7, "Vegeta SSJ4"),
    (15, 1306, None, "Omega"),
    (20, 2045, None, "Cumber"),
]

def check(cond, msg):
    assert cond, msg
    print("PASS", msg)

def sql_iter(text):
    delimiter = ";"
    buffer = []
    for line in text.splitlines():
        stripped = line.strip()
        if not stripped or stripped.startswith("--"):
            continue
        if stripped.upper().startswith("DELIMITER "):
            check(not buffer, "delimiter switch at statement boundary")
            delimiter = stripped.split(maxsplit=1)[1]
            continue
        buffer.append(line)
        if stripped.endswith(delimiter):
            body = "\n".join(buffer).rstrip()
            yield body[:-len(delimiter)].strip()
            buffer.clear()
    check(not buffer, "all SQL statements terminate")

def execute_file(cur, path):
    for stmt in sql_iter(path.read_text(encoding="utf-8")):
        cur.execute(stmt)

def smoke():
    sql = NONSHOP.read_text(encoding="utf-8")
    for _, item_id, _, name in EXPECTED:
        check(str(item_id) in sql, f"current reward references {name} ID {item_id}")
    check("FROM item_shop" in sql, "migration checks item_shop collisions")
    check("JSON_LENGTH(DATA)=3" in sql, "head animation requires 3 frames")
    check("JSON_LENGTH(DATA)=17" in sql, "body animation requires 17 frames")
    check("JSON_LENGTH(DATA)=14" in sql, "leg animation requires 14 frames")
    check("JSON_EXTRACT" in sql, "icon is validated against the head part")
    statements = list(sql_iter(sql))
    check(len(statements) >= 6, "current non-shop migration parses correctly")
    return statements

def test_database():
    import pymysql

    smoke()
    con = pymysql.connect(
        host=os.environ.get("NRO_SQL_HOST", "127.0.0.1"),
        user="root",
        password=os.environ["NRO_SQL_PASSWORD"],
        database="ngocrong",
        autocommit=True,
        charset="utf8mb4",
    )

    with con:
        with con.cursor() as cur:
            cur.execute("""CREATE TABLE IF NOT EXISTS item_template (
                id INT PRIMARY KEY,
                TYPE INT NOT NULL,
                gender SMALLINT NOT NULL,
                NAME VARCHAR(255) NOT NULL,
                description VARCHAR(255) NOT NULL,
                icon_id INT NOT NULL,
                part INT NOT NULL,
                is_up_to_up TINYINT NOT NULL,
                power_require INT NOT NULL
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4""")

            cur.execute("""CREATE TABLE IF NOT EXISTS cai_trang (
                id_temp INT PRIMARY KEY,
                head INT NOT NULL,
                body INT NOT NULL,
                leg INT NOT NULL,
                bag INT NOT NULL
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4""")

            cur.execute("""CREATE TABLE IF NOT EXISTS part (
                id INT PRIMARY KEY,
                TYPE INT NOT NULL,
                DATA LONGTEXT NOT NULL
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4""")

            cur.execute("""CREATE TABLE IF NOT EXISTS item_shop (
                id INT PRIMARY KEY AUTO_INCREMENT,
                tab_id INT NOT NULL DEFAULT 0,
                temp_id INT NOT NULL,
                gold INT NOT NULL DEFAULT 0,
                gem INT NOT NULL DEFAULT 0,
                is_new TINYINT NOT NULL DEFAULT 1,
                is_sell TINYINT NOT NULL DEFAULT 1,
                item_exchange INT NOT NULL DEFAULT -1,
                quantity_exchange INT NOT NULL DEFAULT 0,
                create_time TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4""")

            execute_file(cur, EVENT)
            execute_file(cur, MANUAL)

            items = [
                (1304,5,3,"Cải trang Goku SSJ4","Nro Yanii",20371,-1,0,0),
                (1305,5,3,"Cải trang Vegeta SSJ4","Nro Yanii",20403,-1,0,0),
                (1306,5,3,"Cải trang Omega","Nro Yanii",20339,-1,0,0),
                (2045,5,3,"Cải trang Cumber","Cải trang thành Cumber",11379,-1,0,0),
            ]
            cur.executemany("""INSERT INTO item_template
                (id,TYPE,gender,NAME,description,icon_id,part,is_up_to_up,power_require)
                VALUES(%s,%s,%s,%s,%s,%s,%s,%s,%s)
                ON DUPLICATE KEY UPDATE
                TYPE=VALUES(TYPE),gender=VALUES(gender),NAME=VALUES(NAME),
                description=VALUES(description),icon_id=VALUES(icon_id),
                part=VALUES(part),is_up_to_up=VALUES(is_up_to_up),
                power_require=VALUES(power_require)""", items)

            mappings = [
                (1304,1345,1346,1347,-1),
                (1305,1348,1349,1350,-1),
                (1306,1342,1343,1344,-1),
                (2045,1387,1388,1389,-1),
            ]
            cur.executemany("""INSERT INTO cai_trang(id_temp,head,body,leg,bag)
                VALUES(%s,%s,%s,%s,%s)
                ON DUPLICATE KEY UPDATE head=VALUES(head),body=VALUES(body),
                leg=VALUES(leg),bag=VALUES(bag)""", mappings)

            part_rows = []
            for head, body, leg, icon in [
                (1345,1346,1347,20371),
                (1348,1349,1350,20403),
                (1342,1343,1344,20339),
                (1387,1388,1389,11379),
            ]:
                head_data = json.dumps([[icon,0,0],[icon+1,0,0],[icon+2,0,0]])
                body_data = json.dumps([[30000+n,0,0] for n in range(17)])
                leg_data = json.dumps([[31000+n,0,0] for n in range(14)])
                part_rows.extend([
                    (head,0,head_data),
                    (body,1,body_data),
                    (leg,2,leg_data),
                ])

            cur.executemany("""INSERT INTO part(id,TYPE,DATA) VALUES(%s,%s,%s)
                ON DUPLICATE KEY UPDATE TYPE=VALUES(TYPE),DATA=VALUES(DATA)""", part_rows)

            # Old costume claims should not block the replacement rewards.
            cur.execute("""INSERT INTO referral_costume_claims
                (inviter_player_id,milestone,item_template_id)
                VALUES (999,5,904),(999,10,2117),(999,15,2142),(999,20,1276)
                ON DUPLICATE KEY UPDATE item_template_id=VALUES(item_template_id)""")

            execute_file(cur, NONSHOP)

            cur.execute("""SELECT milestone,item_template_id,duration_days,costume_name
                FROM referral_costume_catalog
                WHERE milestone IN (5,10,15,20)
                ORDER BY milestone""")
            check(cur.fetchall() == tuple(EXPECTED),
                  "catalog uses the four current non-shop costumes")

            cur.execute("SELECT COUNT(*) FROM item_shop WHERE temp_id IN (1304,1305,1306,2045)")
            check(cur.fetchone()[0] == 0,
                  "current referral costumes are not present in item_shop")

            cur.execute("""SELECT COUNT(*) FROM referral_costume_claims
                WHERE inviter_player_id=999 AND milestone IN (5,10,15,20)""")
            check(cur.fetchone()[0] == 0,
                  "stale old-costume claim rows are cleared")

            cur.execute("""SELECT COUNT(DISTINCT item_template_id)
                FROM referral_costume_catalog WHERE milestone IN (5,10,15,20)""")
            check(cur.fetchone()[0] == 4,
                  "all four milestone rewards use distinct item IDs")

    print("All current referral costume migration tests passed.")

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--db", action="store_true")
    args = parser.parse_args()
    if args.db:
        test_database()
    else:
        smoke()

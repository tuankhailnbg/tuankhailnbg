#!/usr/bin/env python3
"""Validate referral costumes using client-known item IDs.
Run with --db against a disposable MariaDB 10.11 instance only.
"""
import argparse
import json
import os
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MIGRATION = ROOT / "database/migrations/20261028_referral_costumes_test.sql"
FIX = ROOT / "database/migrations/20261028_fix_referral_costume_client_freeze.sql"
REPAIR = ROOT / "database/migrations/20261028_repair_partial_referral_costumes.sql"
NONSHOP = ROOT / "database/migrations/20261028_referral_nonshop_costumes.sql"

EXPECTED = [
    (5, 904, 7, "Goku SSJ3", (20,20,20,4,10)),
    (10,2117,7, "Vegito Blue",(23,23,23,6,13)),
    (15,2142,None,"Gogeta Blue",(25,25,25,8,17)),
    (20,1276,None,"Zeno",(27,27,27,10,20)),
]
LEGACY = [2154,2155,2156,2157]
CODE = "nrotest4ct2026"
NONSHOP_EXPECTED = [
    (5,1304,7,"Goku SSJ4"),
    (10,1305,7,"Vegeta SSJ4"),
    (15,1306,None,"Omega"),
    (20,2129,None,"Fu"),
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

def smoke():
    sql = MIGRATION.read_text(encoding="utf-8")
    check("2154, 5, 3" not in sql and "2155, 5, 3" not in sql,
          "migration no longer creates client-unknown item templates")
    for _, item_id, _, _, _ in EXPECTED:
        check(str(item_id) in sql, f"client-known item {item_id} referenced")
    check("gift_codes" in sql and CODE in sql, "test giftcode present")
    statements = list(sql_iter(sql))
    check(len(statements) >= 8, "SQL parser handles compatibility migration")
    return statements

def execute_file(cur, path):
    for stmt in sql_iter(path.read_text(encoding="utf-8")):
        cur.execute(stmt)

def test_database():
    import pymysql
    statements = smoke()
    con = pymysql.connect(
        host=os.environ.get("NRO_SQL_HOST", "127.0.0.1"),
        user="root",
        password=os.environ["NRO_SQL_PASSWORD"],
        database="ngocrong",
        autocommit=True, charset="utf8mb4",
    )
    with con:
        with con.cursor() as cur:
            cur.execute("""CREATE TABLE IF NOT EXISTS item_template (
                id INT PRIMARY KEY, TYPE INT NOT NULL, gender SMALLINT NOT NULL,
                NAME VARCHAR(255) NOT NULL, description VARCHAR(255) NOT NULL,
                icon_id INT NOT NULL, part INT NOT NULL,
                is_up_to_up TINYINT NOT NULL, power_require INT NOT NULL
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4""")
            cur.execute("""CREATE TABLE IF NOT EXISTS cai_trang (
                id_temp INT PRIMARY KEY, head INT NOT NULL, body INT NOT NULL,
                leg INT NOT NULL, bag INT NOT NULL
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4""")
            cur.execute("""CREATE TABLE IF NOT EXISTS gift_codes (
                id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                type TINYINT NOT NULL, code VARCHAR(255) NOT NULL,
                gold INT DEFAULT 0, gem INT DEFAULT 0, ruby INT DEFAULT 0,
                items LONGTEXT, status TINYINT NOT NULL, active INT NOT NULL,
                expires_at TIMESTAMP NULL DEFAULT NULL,
                created_at TIMESTAMP NULL DEFAULT NULL,
                updated_at TIMESTAMP NULL DEFAULT NULL
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4""")
            cur.execute("""CREATE TABLE IF NOT EXISTS player (
                id INT PRIMARY KEY,
                items_bag LONGTEXT NOT NULL,
                items_body LONGTEXT NOT NULL,
                items_box LONGTEXT NOT NULL,
                pet_body LONGTEXT NOT NULL
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4""")

            # Baseline item_template IDs 0..2153 so stable IDs are client-known.
            cur.execute("SELECT COUNT(*) FROM item_template")
            if cur.fetchone()[0] == 0:
                cur.executemany(
                    "INSERT INTO item_template VALUES (%s,5,3,%s,'seed',1,-1,0,0)",
                    [(n,f"base item {n}") for n in range(2154)],
                )
            # Existing appearance mappings corresponding to the real baseline dump.
            for row in [
                (904,906,880,881,-1),
                (2117,1053,1054,1055,-1),
                (2142,1434,1435,1436,-1),
                (1276,838,839,840,-1),
            ]:
                cur.execute("""INSERT INTO cai_trang(id_temp,head,body,leg,bag)
                    VALUES(%s,%s,%s,%s,%s)
                    ON DUPLICATE KEY UPDATE head=VALUES(head),body=VALUES(body),
                    leg=VALUES(leg),bag=VALUES(bag)""", row)

            # Prerequisite referral tables/catalog.
            execute_file(cur, ROOT / "database/migrations/20261028_referral_event_kame.sql")
            execute_file(cur, ROOT / "database/migrations/20261028_referral_manual_points.sql")

            for stmt in statements:
                cur.execute(stmt)

            cur.execute("SELECT milestone,item_template_id,duration_days,options_json FROM referral_costume_catalog ORDER BY milestone")
            catalog=cur.fetchall()
            check(len(catalog)==4,"all 4 special milestones exist")
            for row, expected in zip(catalog, EXPECTED):
                milestone,item,days,opts=row
                m,i,d,name,stats=expected
                check((milestone,item,days)==(m,i,d),f"milestone {m}: client-known ID {i}, duration {d}")
                value=dict((int(opt[0]),int(opt[1])) for opt in json.loads(opts))
                check(tuple(value[k] for k in [77,103,50,14,101])==stats,f"milestone {m}: approved stats")
                check(value[30]==0,f"milestone {m}: nontrade")

            cur.execute("SELECT type,items,status,active FROM gift_codes WHERE code=%s", (CODE,))
            codes=cur.fetchall()
            check(len(codes)==1,"one matching test giftcode")
            typ,items,status,active=codes[0]
            check((typ,status,active)==(1,0,0),"giftcode can be claimed once per character")
            gifts=json.loads(items)
            check([item["id"] for item in gifts]==[904,2117,2142,1276],
                  "giftcode grants client-known costume IDs")

            # Reproduce old production state: legacy item IDs already exist in player JSON/catalog.
            cur.execute("UPDATE referral_costume_catalog SET item_template_id=CASE milestone "
                        "WHEN 5 THEN 2154 WHEN 10 THEN 2155 WHEN 15 THEN 2156 WHEN 20 THEN 2157 END")
            cur.execute("""INSERT INTO player(id,items_bag,items_body,items_box,pet_body)
                VALUES(999,
                '[{"temp_id":2154,"quantity":1,"create_time":1,"option":[[77,20]]}]',
                '[{"temp_id":2155,"quantity":1,"create_time":1,"option":[[77,23]]}]',
                '[{"temp_id":2156,"quantity":1,"create_time":1,"option":[[77,25]]}]',
                '[{"temp_id":2157,"quantity":1,"create_time":1,"option":[[77,27]]}]')
                ON DUPLICATE KEY UPDATE items_bag=VALUES(items_bag),items_body=VALUES(items_body),
                items_box=VALUES(items_box),pet_body=VALUES(pet_body)""")

            execute_file(cur, FIX)

            cur.execute("SELECT milestone,item_template_id FROM referral_costume_catalog ORDER BY milestone")
            check(cur.fetchall()==((5,904),(10,2117),(15,2142),(20,1276)),
                  "freeze fix remaps catalog to client-known IDs")
            cur.execute("SELECT items_bag,items_body,items_box,pet_body FROM player WHERE id=999")
            fields=cur.fetchone()
            check('"temp_id":904' in fields[0], "legacy bag item converted to 904")
            check('"temp_id":2117' in fields[1], "legacy equipped item converted to 2117")
            check('"temp_id":2142' in fields[2], "legacy box item converted to 2142")
            check('"temp_id":1276' in fields[3], "legacy pet item converted to 1276")
            check(all(f'"temp_id":{x}' not in "".join(fields) for x in LEGACY),
                  "no legacy costume IDs remain in tested player JSON")

            # Repair script must also prefer compatible IDs.
            cur.execute("UPDATE referral_costume_catalog SET item_template_id=NULL")
            execute_file(cur, REPAIR)
            cur.execute("SELECT milestone,item_template_id FROM referral_costume_catalog ORDER BY milestone")
            check(cur.fetchall()==((5,904),(10,2117),(15,2142),(20,1276)),
                  "partial-import repair uses client-known IDs")

            # Idempotency.
            for stmt in statements:
                cur.execute(stmt)
            execute_file(cur, FIX)
            cur.execute("SELECT COUNT(*) FROM gift_codes WHERE code=%s",(CODE,))
            check(cur.fetchone()[0]==1,"giftcode migration remains idempotent")

            # ------------------------------------------------------------
            # Verify the new event selection against a disposable DB:
            # existing client IDs, no item_shop collision, unique appearance,
            # matching icon/head and complete player animation frames.
            # ------------------------------------------------------------
            cur.execute("""CREATE TABLE IF NOT EXISTS part (
                id INT PRIMARY KEY, TYPE INT NOT NULL, DATA LONGTEXT NOT NULL
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4""")
            cur.execute("""CREATE TABLE IF NOT EXISTS item_shop (
                id INT PRIMARY KEY AUTO_INCREMENT,
                tab_id INT NOT NULL DEFAULT 0,
                temp_id INT NOT NULL
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4""")

            new_items = [
                (1304,"Cải trang Goku SSJ4",20371),
                (1305,"Cải trang Vegeta SSJ4",20403),
                (1306,"Cải trang Omega",20339),
                (2129,"Cải Trang Fu",32116),
            ]
            for item_id,name,icon in new_items:
                cur.execute("UPDATE item_template SET NAME=%s,icon_id=%s,TYPE=5 WHERE id=%s",
                            (name,icon,item_id))

            for row in [
                (1304,1345,1346,1347,-1),
                (1305,1348,1349,1350,-1),
                (1306,1342,1343,1344,-1),
                (2129,1425,1426,1427,-1),
            ]:
                cur.execute("""INSERT INTO cai_trang(id_temp,head,body,leg,bag)
                    VALUES(%s,%s,%s,%s,%s)
                    ON DUPLICATE KEY UPDATE head=VALUES(head),body=VALUES(body),
                    leg=VALUES(leg),bag=VALUES(bag)""", row)

            # Each head begins with the same small-image ID as item_template.icon_id.
            part_rows = []
            for head,body,leg,icon in [
                (1345,1346,1347,20371),
                (1348,1349,1350,20403),
                (1342,1343,1344,20339),
                (1425,1426,1427,32116),
            ]:
                head_data = json.dumps([[icon,0,0],[icon+1,0,0],[icon+2,0,0]])
                body_data = json.dumps([[30000+n,0,0] for n in range(17)])
                leg_data = json.dumps([[31000+n,0,0] for n in range(14)])
                part_rows.extend([(head,0,head_data),(body,1,body_data),(leg,2,leg_data)])
            cur.executemany("""INSERT INTO part(id,TYPE,DATA) VALUES(%s,%s,%s)
                ON DUPLICATE KEY UPDATE TYPE=VALUES(TYPE),DATA=VALUES(DATA)""", part_rows)

            # Stale costume claims from the previous reward set must not block
            # receiving the replacement costumes. Gold-bar claims are separate.
            cur.execute("""INSERT INTO referral_costume_claims
                (inviter_player_id,milestone,item_template_id)
                VALUES (999,5,904),(999,10,2117),(999,15,2142),(999,20,1276)
                ON DUPLICATE KEY UPDATE item_template_id=VALUES(item_template_id)""")

            execute_file(cur, NONSHOP)

            cur.execute("""SELECT milestone,item_template_id,duration_days,costume_name
                FROM referral_costume_catalog ORDER BY milestone""")
            check(cur.fetchall()==tuple(NONSHOP_EXPECTED),
                  "new referral rewards use four distinct non-shop costume IDs")

            cur.execute("SELECT COUNT(*) FROM item_shop WHERE temp_id IN (1304,1305,1306,2129)")
            check(cur.fetchone()[0]==0,"new referral costume IDs are absent from item_shop")

            cur.execute("""SELECT COUNT(*) FROM referral_costume_claims
                WHERE inviter_player_id=999 AND milestone IN (5,10,15,20)""")
            check(cur.fetchone()[0]==0,
                  "stale old-costume claim rows are cleared without touching gold claims")

    print("All referral costume client-compatibility tests passed.")

if __name__=="__main__":
    p=argparse.ArgumentParser()
    p.add_argument("--db",action="store_true")
    args=p.parse_args()
    if args.db:
        test_database()
    else:
        smoke()

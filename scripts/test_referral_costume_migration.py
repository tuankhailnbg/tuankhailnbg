#!/usr/bin/env python3
"""Validate the 4 new referral costumes and the one-time test giftcode.
Run with --db against a fresh disposable MariaDB 10.11 instance only.
"""
import argparse
import json
import os
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MIGRATION = ROOT / "database/migrations/20261028_referral_costumes_test.sql"
EXPECTED = [
    (5, 2154, 7, "Goku SSJ3", (20,20,20,4,10)),
    (10,2155, 7, "Vegito Blue",(23,23,23,6,13)),
    (15,2156, None,"Gogeta Blue",(25,25,25,8,17)),
    (20,2157, None,"Zeno",(27,27,27,10,20)),
]
CODE = "nrotest4ct2026"

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
    check("ON DUPLICATE KEY UPDATE id=VALUES(id)" in sql, "idempotent IDs")
    check("DELIMITER $$" in sql, "MariaDB procedure delimiter")
    check("item_template" in sql and "cai_trang" in sql, "costume item and appearance mappings")
    check("gift_codes" in sql and CODE in sql, "test giftcode present")
    statements = list(sql_iter(sql))
    check(len(statements) >= 10, "SQL parser handles procedure and updates")
    assert all(str(e[1]) in sql for e in EXPECTED)
    return statements

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
            # Dedicated disposable MariaDB database created by CI: only minimal
            # columns needed by costume migrations and their prerequisites.
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
            cur.execute("SELECT COUNT(*) FROM item_template")
            if cur.fetchone()[0] == 0:
                cur.executemany(
                    "INSERT INTO item_template VALUES (%s,5,3,%s,'seed',1,-1,0,0)",
                    [(n,f"base item {n}") for n in range(2154)],
                )
            for stmt in statements:
                cur.execute(stmt)
            cur.execute("SELECT id,TYPE,NAME,icon_id FROM item_template WHERE id BETWEEN 2154 AND 2157 ORDER BY id")
            rows=cur.fetchall()
            check(len(rows)==4 and [r[0] for r in rows]==[2154,2155,2156,2157], "four sequential IDs stored")
            check(all(row[1]==5 for row in rows), "each event item is a costume (TYPE 5)")
            cur.execute("SELECT id_temp,head,body,leg FROM cai_trang WHERE id_temp BETWEEN 2154 AND 2157 ORDER BY id_temp")
            sprites=cur.fetchall()
            check(len(sprites)==4 and all(all(v>0 for v in p[1:]) for p in sprites),"four preview costume appearance mappings")
            cur.execute("SELECT milestone,item_template_id,duration_days,options_json FROM referral_costume_catalog ORDER BY milestone")
            catalog=cur.fetchall()
            check(len(catalog)==4,"all 4 special milestones mapped to new item IDs")
            for row, expected in zip(catalog, EXPECTED):
                milestone,item,days,opts=row
                m,i,d,name,stats=expected
                check((milestone,item,days)==(m,i,d),f"milestone {m}: ID {i}, duration {d}")
                value=dict((int(opt[0]),int(opt[1])) for opt in json.loads(opts))
                check(tuple(value[k] for k in [77,103,50,14,101])==stats,f"milestone {m}: approved stats")
                check(value[30]==0,f"milestone {m}: nontrade")
            cur.execute("SELECT type,items,status,active FROM gift_codes WHERE code=%s", (CODE,))
            codes=cur.fetchall()
            check(len(codes)==1,"one matching test giftcode")
            typ,items,status,active=codes[0]
            check((typ,status,active)==(1,0,0),"giftcode can be claimed once per character")
            gifts=json.loads(items)
            check([item["id"] for item in gifts]==[2154,2155,2156,2157],"giftcode grants all four event costumes")
            for item,expected in zip(gifts,EXPECTED):
                m,i,d,name,stats=expected
                opts={p["id"]:p["param"] for p in item["options"]}
                check((opts.get(93)==7) if d==7 else (93 not in opts),f"giftcode item {i} duration")
                check(tuple(opts[k] for k in [77,103,50,14,101])==stats,f"giftcode item {i} stats")
            # Reproduce interrupted VPS import: items exist, but catalog links were never applied.
            cur.execute("UPDATE referral_costume_catalog SET item_template_id=NULL WHERE milestone IN (5,10,15,20)")
            repair = (ROOT / "database/migrations/20261028_repair_partial_referral_costumes.sql").read_text(encoding="utf-8")
            for stmt in sql_iter(repair):
                cur.execute(stmt)
            cur.execute("SELECT milestone,item_template_id FROM referral_costume_catalog ORDER BY milestone")
            check(cur.fetchall() == ((5,2154),(10,2155),(15,2156),(20,2157)),
                  "partial VPS import repair restores all four catalog links")
            for stmt in statements:
                cur.execute(stmt)
            cur.execute("SELECT COUNT(*) FROM item_template WHERE id BETWEEN 2154 AND 2157")
            check(cur.fetchone()[0]==4,"idempotent item migration")
            cur.execute("SELECT COUNT(*) FROM gift_codes WHERE code=%s",(CODE,))
            check(cur.fetchone()[0]==1,"idempotent giftcode migration")
    print("All NRO referral costume migration smoke tests passed.")

if __name__=="__main__":
    p=argparse.ArgumentParser()
    p.add_argument("--db",action="store_true")
    args=p.parse_args()
    if args.db: test_database()
    else: smoke()

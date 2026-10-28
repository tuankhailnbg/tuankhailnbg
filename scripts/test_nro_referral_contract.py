#!/usr/bin/env python3
"""Regression tests for manual-admin referral contract and MariaDB migrations."""
import argparse
import json
import os
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
GAME = ROOT / "Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea" / "Server" / "src" / "main" / "java" / "nro"

def check(ok, text):
    if not ok:
        raise AssertionError(text)
    print("PASS", text)

def static_contract():
    source = (GAME / "services" / "ReferralService.java").read_text(encoding="utf-8")
    npc = (GAME / "models" / "npc" / "NpcFactory.java").read_text(encoding="utf-8")
    controller = (GAME / "server" / "Controller.java").read_text(encoding="utf-8")
    manager = (GAME / "services" / "Service.java").read_text(encoding="utf-8")
    user_input = (GAME / "services" / "func" / "Input.java").read_text(encoding="utf-8")
    inventory = (GAME / "services" / "InventoryService.java").read_text(encoding="utf-8")
    catalog = (ROOT / "database" / "migrations" / "20261028_referral_event_kame.sql").read_text(encoding="utf-8")
    check("private static final int MAX_MILESTONE = 20" in source, "exactly 20 referral milestones")
    check("100+(milestone-1)*25" in source and "100+(n-1)*25" in source, "gold reward formula is 100 + 25*(n-1)")
    check(sum(100+25*i for i in range(20)) == 6750, "total 20-milestone payout is 6750 bars")
    check('case ConstNpc.EVENT ->' in npc and '"Đua top"' in npc, "NPC has two main event options")
    check('sendLeaderboard(player)' in npc and 'sendRewardPanel(player)' in npc, "referral submenu opens top and reward panel")
    check('createFormReferralCode(player)' in npc and '"Nhập mã\\ngiới thiệu"' in npc,
          "referral code form can be reopened explicitly from event NPC")
    check('for(int n=1;n<=MAX_MILESTONE;n++)' in source, "reward panel generates all 20 rows")
    check("case ADMIN_REFERRAL_POINTS:" in user_input and 'player.isAdmin()' in user_input,
          "admin-only referral score input")
    check("adminGrantPoints(" in source and "referral_points_audit" in source,
          "manual grants are audit logged")
    check("SELECT points FROM referral_points WHERE player_id=?" in source,
          "claims depend on admin-managed score")
    check("CREATE TABLE IF NOT EXISTS referral_points" in source
          and "CREATE TABLE IF NOT EXISTS referral_claims" in source
          and "SCHEMA_READY" in source,
          "core referral tables bootstrap when deployment skipped SQL migration")
    check("expiresAt < System.currentTimeMillis()" in source
          and "REWARD_PANELS.remove(p.id, expiresAt)" in source,
          "stale referral achievement routing expires safely")
    check("private static final Map<Long, Activity>" not in source and "active_seconds=active_seconds+" not in source,
          "no automatic score gain from referral progress")
    check("case Cmd.ACHIEVEMENT:" in controller and "onRewardSelection" in controller,
          "clicks are routed to referral milestone claims")
    check('dataItem.put("create_time", item.createTime)' in inventory,
          "costume expiry timestamp retained in bag serialization")
    check("milestone == 5 || milestone == 10 || milestone == 15 || milestone == 20" in source,
          "bonus-costume milestones are 5, 10, 15, 20")
    check("selectedMilestone != milestone" in source and "costumeAlreadyClaimed" in source,
          "costume claim occurs only at selected milestone")
    check("referral_claims(inviter_player_id,milestone,gold_bars,status)" in source,
          "gold bar ledger deduplicates claims")
    check('"Cấp điểm\\ngiới thiệu"' in manager, "admin chat menu offers points grant")
    for milestone, days, name in [(5, 7, "Goku SSJ3"), (10, 7, "Vegito Blue"), (15, None, "Gogeta Blue"), (20, None, "Zeno")]:
        flag = str(days) if days is not None else "NULL"
        check(f"({milestone},NULL,'{name}',{flag}," in catalog,
              f"catalog includes {name}, duration {flag}")
    check("item.template.type != 5" in source, "costume claim rejects non-costume item templates")
    check("validateCatalogCostume(c, milestone)" in source
          and "Switched milestone" not in source
          and "case 10 -> 2117" not in source,
          "Java keeps referral catalog item IDs instead of replacing them with shop costumes")
    check("rc.item_template_id=cat.item_template_id" in source,
          "only the currently configured costume counts as already claimed")
    check("staleClaim = true" in source
          and "UPDATE referral_costume_claims" in source,
          "old costume claim rows are refreshed when the event costume changes")
    check("Cải trang CHƯA được nhận:" in source,
          "special-milestone claim reports pending costume after receiving gold bars")
    check("getItemOptionTemplate(id) == null" in source,
          "costume options are checked against loaded server templates")
    credit_form = user_input.split("public void createFormAdminCredit(Player pl) {",1)[1].split("\n    }",1)[0]
    check(credit_form.count("new SubInput(") == 2,
          "admin credit form shows only recipient and amount")
    print("All static contract checks passed.")

def read_sql(name):
    source = (ROOT / "database" / "migrations" / name).read_text(encoding="utf-8")
    return "\n".join(line for line in source.splitlines() if not line.lstrip().startswith("--"))

def database_smoke():
    import pymysql
    conn = pymysql.connect(host=os.environ.get("NRO_SQL_HOST", "127.0.0.1"),
                           user="root", password=os.environ["NRO_SQL_PASSWORD"],
                           database="ngocrong", charset="utf8mb4", autocommit=False)
    try:
        with conn.cursor() as cur:
            for migration in ["20261028_referral_event_kame.sql", "20261028_referral_manual_points.sql"]:
                for sql in read_sql(migration).split(";"):
                    if sql.strip():
                        cur.execute(sql)
            conn.commit()
            cur.execute("SELECT milestone,costume_name,duration_days,options_json FROM referral_costume_catalog ORDER BY milestone")
            catalog = cur.fetchall()
            check([row[0] for row in catalog] == [5,10,15,20], "four costume milestone rows exist")
            for milestone, name, days, opts in catalog:
                options = json.loads(opts)
                check(all(len(opt)==2 for opt in options), f"{name} has valid options JSON")
                check(next(p for id,p in options if id==14) <= 10, f"{name} has <=10% critical")
                check(next(p for id,p in options if id==101) <= 20, f"{name} has <=20% TNSM")
                check(max(p for id,p in options if id in (77,103,50)) <= 27, f"{name} capped 27% damage/HP/KI")
                check((days == 7) if milestone in (5,10) else (days is None),
                      f"{name} duration: 7 days for 5/10, permanent for 15/20")
            cur.execute("INSERT INTO referral_points(player_id,points) VALUES (101, 0)")
            cur.execute("UPDATE referral_points SET points=points+5 WHERE player_id=101")
            cur.execute("SELECT points FROM referral_points WHERE player_id=101")
            check(cur.fetchone()[0] == 5, "manually granted 5 points persist")
            cur.execute("INSERT INTO referral_points_audit(admin_player_id,player_id,delta_points,previous_points,current_points,reason) VALUES(1,101,5,0,5,'ci-test')")
            cur.execute("SELECT COUNT(*) FROM referral_points_audit WHERE player_id=101")
            check(cur.fetchone()[0] == 1, "admin grant is audit logged")
            for i in range(1, 21):
                cur.execute("INSERT INTO referral_claims(inviter_player_id,milestone,gold_bars,status) VALUES(101,%s,%s,1)", (i,100+25*(i-1)))
            cur.execute("SELECT COUNT(*),SUM(gold_bars) FROM referral_claims WHERE inviter_player_id=101")
            check(cur.fetchone() == (20,6750), "20 reward ledger entries sum to 6750 bars")
            try:
                cur.execute("INSERT INTO referral_claims(inviter_player_id,milestone,gold_bars,status) VALUES(101,5,200,1)")
            except pymysql.err.IntegrityError:
                conn.rollback()
                print("PASS duplicate milestone claim blocked by primary key")
            else:
                raise AssertionError("duplicate reward was not blocked")
        print("All MariaDB smoke tests passed.")
    finally:
        conn.close()

if __name__=="__main__":
    parser=argparse.ArgumentParser()
    parser.add_argument("--db", action="store_true")
    args=parser.parse_args()
    static_contract()
    if args.db:
        database_smoke()

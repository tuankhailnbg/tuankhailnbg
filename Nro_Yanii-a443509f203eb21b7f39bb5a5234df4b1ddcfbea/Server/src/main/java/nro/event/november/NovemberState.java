package nro.event.november;

import java.time.LocalDate;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public final class NovemberState {
    public long coins, score, reachedAt;
    public String date = "";
    public int kills, bosses, gifts, claims;
    public boolean participation, topClaimed, testGiftClaimed;

    public void day(LocalDate today) {
        if (!today.toString().equals(date)) {
            date = today.toString(); kills = 0; bosses = 0; gifts = 0; claims = 0;
        }
    }
    public void score(long points, long now) {
        score = Math.addExact(score, points); reachedAt = now;
    }
    public String json() {
        JSONObject o = new JSONObject();
        o.put("coins",coins); o.put("score",score); o.put("reachedAt",reachedAt); o.put("date",date);
        o.put("kills",kills); o.put("bosses",bosses); o.put("gifts",gifts); o.put("claims",claims);
        o.put("participation",participation); o.put("topClaimed",topClaimed);
        o.put("testGiftClaimed",testGiftClaimed);
        return o.toJSONString();
    }
    public static NovemberState parse(String text) {
        Object parsed = JSONValue.parse(text);
        if (!(parsed instanceof JSONObject o)) throw new IllegalArgumentException("Dữ liệu sự kiện bị lỗi");
        NovemberState s = new NovemberState();
        s.coins=num(o,"coins"); s.score=num(o,"score"); s.reachedAt=num(o,"reachedAt");
        s.date=String.valueOf(o.getOrDefault("date","")); s.kills=(int)num(o,"kills");
        s.bosses=(int)num(o,"bosses"); s.gifts=(int)num(o,"gifts"); s.claims=(int)num(o,"claims");
        s.participation=Boolean.TRUE.equals(o.get("participation"));
        s.topClaimed=Boolean.TRUE.equals(o.get("topClaimed"));
        s.testGiftClaimed=Boolean.TRUE.equals(o.get("testGiftClaimed")); return s;
    }
    private static long num(JSONObject o, String key) {
        Object v=o.get(key); return v instanceof Number n ? n.longValue() : 0L;
    }
}

package nro.event.november;

import java.time.*;

/** The approved November 2026 rules. All boundaries use Vietnamese local time. */
public final class NovemberRules {
    private NovemberRules() { }
    public static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    public static final Instant START = LocalDate.of(2026, 11, 1).atStartOfDay(ZONE).toInstant();
    public static final Instant END = LocalDate.of(2026, 12, 1).atStartOfDay(ZONE).toInstant();
    public static final Instant CLOSE = LocalDate.of(2026, 12, 4).atStartOfDay(ZONE).toInstant();
    public static final int PEN = 2400, PAPER = 2401, BADGE = 2402, CARD = 2403;
    public static final int BOX = 2404, DAILY_BOX = 2405, COSTUME = 2410, TOP_COSTUME = 2415;
    public static final int TITLE = 2420, PENALTY_OPTION = 251, NPC = 80;
    public static final String TEST_GIFT_CODE = "trian11test2026";
    public static final int TEST_GIFT_SLOTS = 17;
    public static final int MATERIAL_RATE = 4, BADGE_RATE = 30;
    public static final int[] DAYS = {3, 7, 30, 0};
    public static final int[] PRICES = {50, 150, 500, 1500};
    public static final String[] NAMES = {"Học Trò Chiến Binh", "Ảnh Phong Học Giả",
            "Sư Phụ Kim Cang", "Đệ Tử Sinh Lực", "Thần Tài Tu Luyện"};
    public static final int[][][] OPTIONS = {
        {{50,32},{77,25},{103,25},{14,8}},
        {{50,26},{77,27},{103,27},{108,18},{114,10}},
        {{50,24},{77,30},{103,24},{94,32},{97,10}},
        {{50,25},{77,32},{103,32},{80,3},{81,3}},
        {{PENALTY_OPTION,50},{100,150},{101,20},{104,5}}
    };
    public enum Phase { BEFORE, ACTIVE, EXCHANGE, CLOSED }
    public static Phase phase(Instant now) {
        if (now.isBefore(START)) return Phase.BEFORE;
        if (now.isBefore(END)) return Phase.ACTIVE;
        if (now.isBefore(CLOSE)) return Phase.EXCHANGE;
        return Phase.CLOSED;
    }
    public static int price(int duration) {
        if (duration < 0 || duration >= PRICES.length) throw new IllegalArgumentException("Thời hạn không hợp lệ");
        return PRICES[duration];
    }
    public static int rewardTier(int roll) {
        if (roll < 0 || roll >= 10000) throw new IllegalArgumentException("Invalid roll");
        if (roll < 4000) return 0;
        if (roll < 7000) return 1;
        if (roll < 9000) return 2;
        if (roll < 9700) return 3;
        if (roll < 9950) return 4;
        return 5;
    }
    public static int goldWithBonus(int amount, int percent) {
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0L, (long) amount)
                * (100L + Math.max(0, percent)) / 100L);
    }
    public static int penalize(int amount, int percent) {
        return (int) Math.max(1L, (long) amount * (100 - Math.max(0, Math.min(100, percent))) / 100);
    }
    public static void checkQuantity(int count) {
        if (count < 1 || count > 1000) throw new IllegalArgumentException("Số lượng phải từ 1 đến 1.000");
    }
}

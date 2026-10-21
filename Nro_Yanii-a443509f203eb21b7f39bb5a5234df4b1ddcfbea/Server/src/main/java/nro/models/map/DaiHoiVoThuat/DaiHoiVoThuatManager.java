package nro.models.map.DaiHoiVoThuat;

import java.util.ArrayList;
import nro.models.map.Map;
import nro.models.player.Player;

/**
 *
 * @author louis
 */
public class DaiHoiVoThuatManager {

    private static DaiHoiVoThuatManager instance;

    public boolean openDHVT = false;
    public ArrayList<Long> lstIDPlayers = new ArrayList<>();
    public ArrayList<Long> lstIDPlayers2 = new ArrayList<>();
    public ArrayList<Player> lstPlayers = new ArrayList<>();
    public ArrayList<Player> lstPlayers2 = new ArrayList<>();
    public Map mapDhvt;
    public long tOpenDHVT = 0;
    public long tNextRound = 0;
    public byte roundNow = 0;
    public byte typeDHVT = 0;

    public int hourDHVT = 0;

    public static DaiHoiVoThuatManager gI() {
        if (instance == null) {
            instance = new DaiHoiVoThuatManager();
        }
        return instance;
    }

    public String nameRoundDHVT() {
        if (typeDHVT == (byte) 1) {
            return "Nhi Đồng";
        } else if (typeDHVT == (byte) 2) {
            return "Siêu Cấp 1";
        } else if (typeDHVT == (byte) 3) {
            return "Siêu Cấp 2";
        } else if (typeDHVT == (byte) 4) {
            return "Siêu Cấp 3";
        } else if (typeDHVT == (byte) 5) {
            return "Ngoại Hạng";
        }
        return "Ngoại Hạng";
    }

    public String timeDHVTnext(int hour) {
        switch (hour) {
            case 8:
                return " vào lúc 9h";
            case 9:
                return " vào lúc 10h";
            case 10:
                return " vào lúc 11h";
            case 11:
                return " vào lúc 12h";
            case 12:
                return " vào lúc 13h";
            case 13:
                return " vào lúc 14h";
            case 15:
                return " vào lúc 15h";
            case 16:
                return " vào lúc 17h";
            case 17:
                return " vào lúc 18h";
            case 18:
                return " vào lúc 19h";
            case 19:
                return " vào lúc 20h";
            case 20:
                return " vào lúc 21h";
            case 21:
                return " vào lúc 22h";
            case 22:
                return " vào lúc 23h";
            case 23:
                return " vào lúc 8h";
            default:
                return "";
        }
    }

    public String costRoundDHVT() {
        if (typeDHVT == (byte) 1) {
            return "2 ngọc";
        } else if (typeDHVT == (byte) 2) {
            return "4 ngọc";
        } else if (typeDHVT == (byte) 3) {
            return "6 ngọc";
        } else if (typeDHVT == (byte) 4) {
            return "8 ngọc";
        } else if (typeDHVT == (byte) 5) {
            return "10000 vàng";
        }
        return "10000 vàng";
    }

    public boolean isAssignDHVT(long id) {
        for (Long lstIDPlayer : lstIDPlayers) {
            if (lstIDPlayer == id) {
                return true;
            }
        }
        return false;
    }
}

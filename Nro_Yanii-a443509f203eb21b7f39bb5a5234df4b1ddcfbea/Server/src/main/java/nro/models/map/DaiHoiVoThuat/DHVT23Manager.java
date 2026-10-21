package nro.models.map.DaiHoiVoThuat;

import nro.utils.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author outcast c-cute hột me 😳
 */
public class DHVT23Manager {

    private static DHVT23Manager i;
    private long lastUpdate;
    private static List<DHVT23> list = new ArrayList<>();
    private static List<DHVT23> toRemove = new ArrayList<>();

    public static DHVT23Manager gI() {
        if (i == null) {
            i = new DHVT23Manager();
        }
        return i;
    }

    public void update() {
        if (Util.canDoWithTime(lastUpdate, 1000)) {
            lastUpdate = System.currentTimeMillis();
            synchronized (list) {
                for (DHVT23 mc : list) {
                    try {
                        mc.update();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                list.removeAll(toRemove);
            }
        }
    }

    public void add(DHVT23 mc) {
        synchronized (list) {
            list.add(mc);
        }
    }

    public void remove(DHVT23 mc) {
        synchronized (toRemove) {
            toRemove.add(mc);
        }
    }
}

package nro.models.map.VoDaiSinhTu;

import nro.utils.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author outcast c-cute hột me 😳
 */
public class VoDaiSinhTuManager {

    private static VoDaiSinhTuManager i;
    private long lastUpdate;
    private static List<VoDaiSinhTu> list = new ArrayList<>();
    private static List<VoDaiSinhTu> toRemove = new ArrayList<>();

    public static VoDaiSinhTuManager gI() {
        if (i == null) {
            i = new VoDaiSinhTuManager();
        }
        return i;
    }

    public void update() {
        if (Util.canDoWithTime(lastUpdate, 1000)) {
            lastUpdate = System.currentTimeMillis();
            synchronized (list) {
                for (VoDaiSinhTu mc : list) {
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

    public void add(VoDaiSinhTu mc) {
        synchronized (list) {
            list.add(mc);
        }
    }

    public void remove(VoDaiSinhTu mc) {
        synchronized (toRemove) {
            toRemove.add(mc);
        }
    }
}

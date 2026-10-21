package nro.services;

import nro.models.item.Item;
import nro.models.map.Zone;
import nro.models.map.phoban.KhiGas;
import nro.models.mob.Mob;
import nro.models.player.Player;

/**
 *
 * @author 💖 Nro Yanii 💖
 * 
 *
 */
public class KhiGasHuyDietService {

    private static KhiGasHuyDietService i;

    private KhiGasHuyDietService() {

    }

    public static KhiGasHuyDietService gI() {
        if (i == null) {
            i = new KhiGasHuyDietService();
        }
        return i;
    }

    public Zone getMapKhiGasHuyDiet(Player player, int mapId) {
        if (MapService.gI().isMapKhiGas(player.zone.map.mapId) && !player.isAdmin()) {
            boolean canJoin = true;
            for (Mob mob : player.zone.mobs) {
                if (!mob.isDie()) {
                    canJoin = false;
                    break;
                }
            }
            if (canJoin) {
                for (Player boss : player.zone.getBosses()) {
                    if (!boss.isDie()) {
                        canJoin = false;
                        break;
                    }
                }
            }
            if (!canJoin) {
                return null;
            }
        }
        Zone zone = null;
        if (player.clan != null && player.clan.khiGas != null) {
            for (Zone z : player.clan.khiGas.zones) {
                if (z.map.mapId == mapId) {
                    zone = z;
                    break;
                }
            }
        }
        return zone;
    }

    public void openKhiGas(Player player, byte level) {
        if (level >= 1 && level <= 110) {
            if (player.clan != null && player.clan.khiGas == null) {
                Item item = InventoryService.gI().findItemBagByTemp(player, 611);
                KhiGas khiGas = null;
                for (KhiGas kghd : KhiGas.KHI_GAS) {
                    if (!kghd.isOpened) {
                        khiGas = kghd;
                        break;
                    }
                }
                if (khiGas != null) {
                    InventoryService.gI().subQuantityItemsBag(player, item, 1);
                    InventoryService.gI().sendItemBags(player);
                    khiGas.openKhiGasHuyDiet(player, player.clan, level);
                } else {
                    Service.getInstance().sendThongBao(player, "Đang quá tải, hãy quay lại sau 30 phút");
                }
            } else {
                Service.getInstance().sendThongBao(player, "Không thể thực hiện");
            }
        } else {
            Service.getInstance().sendThongBao(player, "Không thể thực hiện");
        }
    }
}

package nro.services;

import nro.models.item.Item;
import nro.models.map.Zone;
import nro.models.map.phoban.BanDoKhoBau;
import nro.models.mob.Mob;
import nro.models.player.Player;

/**
 *
 * @author 💖 Nro Yanii 💖
 * 
 *
 */
public class BanDoKhoBauService {

    private static BanDoKhoBauService i;

    private BanDoKhoBauService() {

    }

    public static BanDoKhoBauService gI() {
        if (i == null) {
            i = new BanDoKhoBauService();
        }
        return i;
    }

    public Zone getMapBanDoKhoBau(Player player, int mapId) {
        if (MapService.gI().isMapBanDoKhoBau(player.zone.map.mapId) && !player.isAdmin()) {
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
        if (player.clan != null && player.clan.banDoKhoBau != null) {
            for (Zone z : player.clan.banDoKhoBau.zones) {
                if (z.map.mapId == mapId) {
                    zone = z;
                    break;
                }
            }
        }
        return zone;
    }

    public void openBanDoKhoBau(Player player, byte level) {
        if (level >= 1 && level <= 110) {
            if (player.clan != null && player.clan.banDoKhoBau == null) {
                Item item = InventoryService.gI().findItemBagByTemp(player, 611);
                if (item != null && item.quantity > 0) {
                    BanDoKhoBau banDoKhoBau = null;
                    for (BanDoKhoBau bdkb : BanDoKhoBau.BAN_DO_KHO_BAUS) {
                        if (!bdkb.isOpened) {
                            banDoKhoBau = bdkb;
                            break;
                        }
                    }
                    if (banDoKhoBau != null) {
                        InventoryService.gI().subQuantityItemsBag(player, item, 1);
                        InventoryService.gI().sendItemBags(player);
                        banDoKhoBau.openBanDoKhoBau(player, player.clan, level);
                    } else {
                        Service.getInstance().sendThongBao(player, "Bản đồ kho báu đã đầy, vui lòng quay lại sau");
                    }
                } else {
                    Service.getInstance().sendThongBao(player, "Yêu cầu có bản đồ kho báu");
                }
            } else {
                Service.getInstance().sendThongBao(player, "Không thể thực hiện");
            }
        } else {
            Service.getInstance().sendThongBao(player, "Không thể thực hiện");
        }
    }
}

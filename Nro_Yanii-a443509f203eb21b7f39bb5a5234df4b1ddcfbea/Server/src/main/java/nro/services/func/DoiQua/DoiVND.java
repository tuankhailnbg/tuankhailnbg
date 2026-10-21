package nro.services.func.DoiQua;

import nro.jdbc.daos.PlayerDAO;
import nro.models.item.Item;
import nro.models.player.Player;
import nro.services.InventoryService;
import nro.services.ItemService;
import nro.services.Service;

public class DoiVND {
    public static void MuaDeTu(Player player, int vND, int iD) {
        if (player.soDuVND < vND) {
            Service.gI().sendThongBao(player, "Bạn không đủ số dư để mua vật phẩm này");
            return;
        }

        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
            Service.gI().sendThongBao(player, "Cần ít nhất 1 ô trống trong hành trang");
            return;
        }
        Item phanQua = ItemService.gI().createNewItem((short) iD);
        InventoryService.gI().addItemBag(player, phanQua, 999);
        PlayerDAO.subVndBar(player, vND);
        player.soDuVND -= vND;

        InventoryService.gI().sendItemBags(player);
        Service.gI().sendThongBao(player, "Bạn nhận được " + phanQua.template.name);
        InventoryService.gI().sendItemBags(player);
    }

}

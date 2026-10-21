package nro.services.func.lr;

import nro.models.item.Item;
import nro.models.player.Player;
import nro.services.RewardService;
import nro.services.Service;

import java.util.List;

/**
 *
 * @author 💖 Nro Yanii 💖
 * 
 *
 */
public class LuckyRoundGem extends AbsLuckyRound {

    private static LuckyRoundGem i;

    public static LuckyRoundGem gI() {
        if (i == null) {
            i = new LuckyRoundGem();
        }
        return i;
    }

    private LuckyRoundGem() {
        this.price = 4;
        this.ticket = 821;
        this.icons.add(8579);
        this.icons.add(8580);
        this.icons.add(8581);
        this.icons.add(8582);
        this.icons.add(8583);
        this.icons.add(8584);
        this.icons.add(8585);
    }

    @Override
    public List<Item> reward(Player player, byte quantity) {
        List<Item> list = RewardService.gI().getListItemLuckyRound(player, quantity);
        addItemToBox(player, list);
        return list;
    }

    @Override
    public boolean checkMoney(Player player, int price) {
        if (player.inventory.getGem() < price) {
            Service.getInstance().sendThongBao(player, "Bạn không đủ ngọc");
            return false;
        }
        return true;
    }

    @Override
    public void payWithMoney(Player player, int price) {
        player.inventory.subGem(price);
    }

}

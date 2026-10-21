/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nro.services.func.lr;

import nro.models.item.Item;
import nro.models.player.Player;
import nro.services.RewardService;
import nro.services.Service;

import java.util.List;

/**
 *
 * @author 💖 Nro Yanii 💖
 */
public class LuckyRoundGold extends AbsLuckyRound {

    private static LuckyRoundGold i;

    public static LuckyRoundGold gI() {
        if (i == null) {
            i = new LuckyRoundGold();
        }
        return i;
    }

    private LuckyRoundGold() {
        this.price = 50000000;
        this.ticket = 821;
        this.icons.add(419);
        this.icons.add(420);
        this.icons.add(421);
        this.icons.add(422);
        this.icons.add(423);
        this.icons.add(424);
        this.icons.add(425);
    }

    @Override
    public List<Item> reward(Player player, byte quantity) {
        List<Item> list = RewardService.gI().getListItemLuckyRound(player, quantity);
        addItemToBox(player, list);
        return list;
    }

    @Override
    public boolean checkMoney(Player player, int price) {
        if (player.inventory.getGold() < price) {
            Service.getInstance().sendThongBao(player, "Bạn không đủ vàng");
            return false;
        }
        return true;
    }

    @Override
    public void payWithMoney(Player player, int price) {
        player.inventory.subGold(price);
    }

}

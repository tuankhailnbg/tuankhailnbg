package nro.models.shop;

import java.util.ArrayList;
import java.util.List;
import nro.models.player.Player;

/**
 *
 * @author 💖 Nro Yanii 💖
 * 
 *
 */
public class Shop {

    public int id;

    public byte npcId;

    public byte shopOrder;

    public List<TabShop> tabShops;

    public Shop() {
        this.tabShops = new ArrayList<>();
    }

    public Shop(Player player, Shop shop, int gender) {
        this.tabShops = new ArrayList<>();
        this.id = shop.id;
        this.npcId = shop.npcId;
        this.shopOrder = shop.shopOrder;
        for (TabShop tabShop : shop.tabShops) {
            this.tabShops.add(new TabShop(player, tabShop, gender));
        }
    }

    public Shop(Shop shop) {
        this.id = shop.id;
        this.npcId = shop.npcId;
        this.shopOrder = shop.shopOrder;
        this.tabShops = new ArrayList<>();
        for (TabShop tabShop : shop.tabShops) {
            this.tabShops.add(new TabShop(tabShop));
        }
    }

}

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
public class TabShop {

    public Shop shop;

    public int id;

    public String name;

    public List<ItemShop> itemShops;

    public TabShop() {
        this.itemShops = new ArrayList<>();
    }

    public TabShop(Player player, TabShop tabShop, int gender) {
        this.itemShops = new ArrayList();
        this.shop = tabShop.shop;
        this.id = tabShop.id;
        this.name = tabShop.name;

        for (ItemShop itemShop : tabShop.itemShops) {
            if (itemShop.temp.gender == gender || itemShop.temp.gender > 2) {
                this.itemShops.add(itemShop);
            }
        }
    }

    public TabShop(TabShop tabShop, int gender) {
        this.itemShops = new ArrayList();
        this.shop = tabShop.shop;
        this.id = tabShop.id;
        this.name = tabShop.name;

        for (ItemShop itemShop : tabShop.itemShops) {
            if (itemShop.temp.gender == gender || itemShop.temp.gender > 2) {
                this.itemShops.add(itemShop);
            }
        }
    }

    public TabShop(TabShop tabShop) {
        this.itemShops = new ArrayList<>();
        this.shop = tabShop.shop;
        this.id = tabShop.id;
        this.name = tabShop.name;
        for (ItemShop itemShop : tabShop.itemShops) {
            this.itemShops.add(new ItemShop(itemShop));
        }
    }
}

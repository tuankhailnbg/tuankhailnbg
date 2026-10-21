/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nro.services.func.lr;

import nro.consts.Cmd;
import nro.models.item.Item;
import nro.models.player.Player;
import nro.server.io.Message;
import nro.services.InventoryService;
import nro.services.Service;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author 💖 Nro Yanii 💖
 */
public abstract class AbsLuckyRound {

    protected int price;
    protected int ticket;
    protected List<Integer> icons = new ArrayList<>();

    public void payAndGetStarted(Player player, byte quantity) {

        Item itm = InventoryService.gI().findItemBagByTemp(player, ticket);
        int numTicket = 0;
        if (itm != null) {
            numTicket = itm.quantity;
        }
        int q = quantity;
        if (numTicket > q) {
            numTicket = q;
        }
        q -= numTicket;
        int price = q * this.price;
        boolean r = checkMoney(player, price);
        if (r) {
            if (quantity > InventoryService.gI().getCountEmptyListItem(player.inventory.itemsBoxCrackBall)) {
                Service.getInstance().sendThongBao(player, "Rương phụ đã đầy");
                return;
            }
            if (price > 0) {
                payWithMoney(player, price);
                Service.getInstance().sendMoney(player);
            }
            if (itm != null) {
                InventoryService.gI().subQuantityItemsBag(player, itm, numTicket);
                InventoryService.gI().sendItemBags(player);
            }
            List<Item> list = reward(player, quantity);
            result(player, list);
        }
    }

    public abstract boolean checkMoney(Player player, int price);

    public abstract void payWithMoney(Player player, int price);

    public abstract List<Item> reward(Player player, byte quantity);

    public void openUI(Player player, byte type) {
        try {
            Message ms = new Message(Cmd.LUCKY_ROUND);
            DataOutputStream ds = ms.writer();
            ds.writeByte(0);
            ds.writeByte(icons.size());
            for (int icon : icons) {
                ds.writeShort(icon);
            }
            ds.writeByte(type); // type price
            ds.writeInt(price); // price
            ds.writeShort(ticket); // id ticket
            player.sendMessage(ms);
            ms.cleanup();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void result(Player player, List<Item> items) {
        try {
            Message ms = new Message(Cmd.LUCKY_ROUND);
            DataOutputStream ds = ms.writer();
            ds.writeByte(1);
            ds.writeByte(items.size());
            for (Item item : items) {
                ds.writeShort(item.template.iconID);
            }
            player.sendMessage(ms);
            ms.cleanup();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void addItemToBox(Player player, List<Item> items) {
        for (Item item : items) {
            InventoryService.gI().addItemNotUpToUpQuantity(player.inventory.itemsBoxCrackBall, item);
        }
    }
}

package nro.services.func;

import nro.card.Card;
import nro.card.CardTemplate;
import nro.card.CollectionBook;
import nro.consts.Cmd;
import nro.models.item.Item;
import nro.models.item.ItemOption;
import nro.models.player.Player;
import nro.server.io.Message;
import nro.services.InventoryService;
import nro.services.Service;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author 💖 Nro Yanii 💖
 * 
 *
 */
public class RadaService {

    private static RadaService instance;

    private RadaService() {

    }

    public static RadaService getInstance() {
        if (instance == null) {
            instance = new RadaService();
        }
        return instance;
    }

    public void controller(Player player, Message msg) {
        try {
            byte type = msg.reader().readByte();
            int id = -1;
            if (msg.reader().available() > 0) {
                id = msg.reader().readShort();
            }
            switch (type) {
                case 0:
                    viewCollectionBook(player);
                    break;

                case 1:
                    cardAction(player, id);
                    break;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void cardAction(Player player, int id) {
        CollectionBook book = player.getCollectionBook();
        Card c = book.findWithItemID(id);
        if (c != null) {
            if (c.getLevel() > 0) {
                if (!c.isUse()) {
                    long size = book.getCards().stream().filter(a -> a.isUse()).count();
                    if (size >= 3) {
                        return;
                    }
                }
                byte auraOld = player.getAura();
                c.setUse(!c.isUse());
                byte auraNew = player.getAura();
                useCard(player, c);
                player.nPoint.calPoint();
                Service.getInstance().point(player);
                if (auraOld != auraNew) {
                    setIDAuraEff(player, auraNew);
                }
            }
            return;
        }
    }

    public void useCard(Player player, Card card) {
        try {
            Message mss = new Message(Cmd.RADA_CARD);
            DataOutputStream ds = mss.writer();
            ds.writeByte(1);
            ds.writeShort(card.getCardTemplate().getItemID());
            ds.writeBoolean(card.isUse());
            ds.flush();
            player.sendMessage(mss);
            mss.cleanup();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void viewCollectionBook(Player player) {
        try {
            CollectionBook book = player.getCollectionBook();
            Message mss = new Message(Cmd.RADA_CARD);
            DataOutputStream ds = mss.writer();
            ds.writeByte(0);
            List<Card> cards = book.getCards();
            ds.writeShort(cards.size());
            for (Card card : cards) {
                CardTemplate cardT = card.getCardTemplate();
                ds.writeShort(cardT.getItemID());
                ds.writeShort(cardT.getIcon());
                ds.writeByte(cardT.getRank());
                ds.writeByte(card.getAmount());
                ds.writeByte(cardT.getMaxAmount());
                ds.writeByte(cardT.getType());
                if (cardT.getType() == 0) {
                    ds.writeShort(cardT.getMobID());
                } else {
                    ds.writeShort(cardT.getHead());
                    ds.writeShort(cardT.getBody());
                    ds.writeShort(cardT.getLeg());
                    ds.writeShort(cardT.getBag());
                }
                ds.writeUTF(cardT.getName());
                ds.writeUTF(cardT.getInfo());
                ds.writeByte(card.getLevel());
                ds.writeBoolean(card.isUse());
                List<ItemOption> options = cardT.getOptions();
                ds.writeByte(options.size());
                for (ItemOption option : options) {
                    ds.writeByte(option.optionTemplate.id);
                    ds.writeShort(option.param);
                    ds.writeByte(option.activeCard);
                }
            }
            ds.flush();
            player.sendMessage(mss);
            mss.cleanup();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void useItemCard(Player player, Item item) {
        CollectionBook book = player.getCollectionBook();
        Card card = book.findWithItemID(item.template.id);
        if (card != null) {
            InventoryService.gI().subQuantityItemsBag(player, item, 1);
            int cardLevelOld = card.getLevel();
            card.addAmount(1);
            int cardLevelNew = card.getLevel();
            if (cardLevelOld != cardLevelNew) {
                if (card.isUse()) {
                    player.nPoint.calPoint();
                    Service.getInstance().point(player);
                }

            }
            setCardLevel(player, card);
        }
    }

    public void setCardLevel(Player player, Card card) {
        try {
            Message mss = new Message(Cmd.RADA_CARD);
            DataOutputStream ds = mss.writer();
            ds.writeByte(2);
            ds.writeShort(card.getCardTemplate().getItemID());
            ds.writeByte(card.getLevel());
            ds.flush();
            player.sendMessage(mss);
            mss.cleanup();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void setIDAuraEff(Player player, byte aura) {
        // System.out.println("Player " + player.name + " aura " + aura);
        try {
            Message mss = new Message(Cmd.RADA_CARD);
            DataOutputStream ds = mss.writer();
            ds.writeByte(4);
            ds.writeInt((int) player.id);
            ds.writeShort(aura);
            ds.writeByte(-1);
            ds.flush();
            Service.getInstance().sendMessAllPlayerInMap(player, mss);
            mss.cleanup();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void RadarSetAura(Player pl) {
        try {
            Message message = new Message(127);
            message.writer().writeByte(4);
            message.writer().writeInt((int) pl.id);
            message.writer().writeShort(pl.getAura());
            message.writer().writeByte(-1);
            message.writer().flush();
            pl.sendMessage(message);
            message.cleanup();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
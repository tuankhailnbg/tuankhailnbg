package nro.models.kigui;

import lombok.Getter;
import nro.dialog.ConfirmDialog;
import nro.models.item.Item;
import nro.models.item.ItemOption;
import nro.models.player.Inventory;
import nro.models.player.Player;
import nro.server.io.Message;
import nro.services.InventoryService;
import nro.services.ItemService;
import nro.services.NpcService;
import nro.services.Service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * @author outcast c-cute hột me 😳
 */
public class KiGuiShop {

    private static final KiGuiShop INSTANCE = new KiGuiShop();

    private static final byte CONSIGN = 0;

    private static final byte CCANCEL_CONSIGN = 1;

    private static final byte GET_MONEY = 2;

    private static final byte BUY = 3;

    private static final byte NEXT_PAGE = 4;

    private static final byte UP_TOP = 5;

    public static KiGuiShop getInstance() {
        return INSTANCE;
    }

    @Getter
    private List<KiGuiItem> list = new ArrayList<>();

    private Map<Long, KiGuiItem> mapItemsExpired = new HashMap<>();

    public String[] tabName = {"Trang bị", "Phụ kiện", "Hỗ trợ", "Linh tinh", ""};

    public String[] tabNameSuKien = {"1", "2", "3", "4", ""};

    public void handler(Player player, Message m) {
        try {
            DataInputStream dis = m.reader();
            byte action = dis.readByte();
            switch (action) {
                case CONSIGN: {
                    short itemID = dis.readShort();
                    byte monneyType = dis.readByte();
                    int money = dis.readInt();
                    int quantity = 0;
                    if (player.isVersionAbove(222)) {
                        quantity = dis.readInt();
                    } else {
                        quantity = dis.readByte();
                    }
                    consign(player, itemID, monneyType, money, quantity);
                }
                return;
                case BUY: {
                    short itemID = dis.readShort();
                    byte monneyType = dis.readByte();
                    int money = dis.readInt();
                    buy(player, itemID, monneyType, money);
                }
                return;
                case GET_MONEY: {
                    short itemID = dis.readShort();
                    getMoney(player, itemID);
                }
                return;
                case CCANCEL_CONSIGN: {
                    short itemID = dis.readShort();
                    cancelConsign(player, itemID);
                }
                return;
                case NEXT_PAGE: {
                    byte tab = dis.readByte();
                    byte page = dis.readByte();
                    nextPage(player, tab, page);
                }
                return;
                case UP_TOP: {
                    short itemID = dis.readShort();
                    upTop(player, itemID);
                }
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void upTop(Player player, short itemID) {
        ConfirmDialog confirmDialog = new ConfirmDialog("Bạn có muốn đưa vật phẩm này của bản thân lên trang đầu?\nYêu cầu 50 hồng ngọc.", () -> {
            if (player.inventory.ruby < 50) {
                Service.getInstance().sendThongBao(player, "Bạn không đủ hồng ngọc");
                show(player);
                return;
            }
            KiGuiItem consignmentItem = findItemConsign(player.id, itemID);
            if (consignmentItem.isUpTop()) {
                Service.getInstance().sendThongBao(player, "Vật phẩm này đã up top rồi");
                show(player);
                return;
            }
            if (consignmentItem == null || consignmentItem.getSold() == 1) {
                Service.getInstance().sendThongBao(player, "Vật phẩm không tồn tại hoặc đã được bán");
                show(player);
                return;
            }
            player.inventory.subRuby(50);
            Service.getInstance().sendMoney(player);
            consignmentItem.setUpTop(true);
            Service.getInstance().sendThongBao(player, "Vật phẩm " + consignmentItem.template.name + " của bạn đã up top thành công");
            show(player);
        });
        confirmDialog.show(player);
    }

    private void cancelConsign(Player player, short itemID) {
        KiGuiItem item = findItemConsign(player.id, itemID);
        if (item == null) {
            Service.getInstance().sendThongBao(player, "không tìm thấy vật phẩm");
            show(player);
            return;
        }
        if (item.getSold() == 1) {
            Service.getInstance().sendThongBao(player, "Vật phẩm đã đã được bán");
            show(player);
            return;
        }
        InventoryService.gI().addItemBag(player, item, item.quantity);
        InventoryService.gI().sendItemBags(player);
        Service.getInstance().sendThongBao(player, "Hủy kí gửi thành công");
        removeItem(item);
        show(player);
    }

    public void addItem(KiGuiItem item) {
        synchronized (list) {
            list.add(item);
        }
    }

    public void addExpiredItem(KiGuiItem item) {
        mapItemsExpired.put(item.getConsignorID(), item);
    }

    public void removeItem(KiGuiItem item) {
        synchronized (list) {
            list.remove(item);
        }
    }

    private void consign(Player player, short itemID, byte monneyType, int money, int quantity) {
        Item thoivang = ItemService.gI().createNewItem((short) 457);
        if (thoivang == null && thoivang.quantity < 1) {
            Service.getInstance().sendThongBao(player, "Phí kí gửi là 1 thỏi vàng");
            return;
        }
        if (quantity < 0 || quantity > 99) {
            Service.getInstance().sendThongBao(player, "Chỉ có thể kí gửi tối đa x99");
            return;
        }
        if (money <= 1 || money > 2000000000) {
            Service.getInstance().sendThongBao(player, "Chỉ có thể ký gửi từ 1 - 200k thỏi vàng");
            return;
        }
        Item item = InventoryService.gI().findItem(player, itemID, quantity);
        if (item == null) {
            Service.getInstance().sendThongBao(player, "Không tìm thấy vật phẩm");
            return;
        }
        KiGuiItem consignmentItem = ItemService.gI().convertToConsignmentItem(item);
        if (monneyType == 0) {
            consignmentItem.setPriceGold(money);
        } else {
            consignmentItem.setPriceGem(money);
        }
        consignmentItem.createTime = System.currentTimeMillis();
        consignmentItem.setConsignorID(player.id);
        consignmentItem.setTab(getTabByType(consignmentItem.template.type));
        consignmentItem.quantity = quantity;
        consignmentItem.setConsignName(player.name);

        if (player.isShopKiGuiSuKien) {
            consignmentItem.setSuKien(true);
        } else {
            consignmentItem.setSuKien(false);
        }

        addItem(consignmentItem);
        InventoryService.gI().subQuantityItemsBag(player, thoivang, 1);
        InventoryService.gI().subQuantityItemsBag(player, item, quantity);
        InventoryService.gI().sendItemBags(player);
        Service.getInstance().sendMoney(player);
        show(player);
        Service.getInstance().sendThongBao(player, "Kí gửi vật phẩm thành công");
    }

    public KiGuiItem findItemConsign(long consignerID, short itemID) {
        for (KiGuiItem consignmentItem : list) {
            if (consignmentItem.getConsignorID() == consignerID && consignmentItem.template.id == itemID) {
                return consignmentItem;
            }
        }
        return null;
    }

    private List<KiGuiItem> getItemConsignByTab(Player player, byte tab, int... max) {
        if (player.isShopKiGuiSuKien) {
            List<KiGuiItem> items = new ArrayList<>();
            List<KiGuiItem> listSort = new ArrayList<>();
            List<KiGuiItem> listSort2 = new ArrayList<>();

            for (KiGuiItem item : list) {
                if (item != null && item.getTab() == tab && item.getSold() == 0 && item.isSuKien() == true) {
                    items.add(item);
                }
            }

            Collections.sort(items, (item1, item2) -> Boolean.compare(item2.template.isUpToUp, item1.template.isUpToUp));

            if (max.length == 2) {
                int startIndex = Math.min(max[0], items.size());
                int endIndex = Math.min(max[1], items.size());
                listSort.addAll(items.subList(startIndex, endIndex));
            } else if (max.length == 1) {
                int endIndex = Math.min(max[0], items.size());
                listSort.addAll(items.subList(0, endIndex));
            } else {
                listSort.addAll(items);
            }

            for (KiGuiItem item : listSort) {
                if (item != null) {
                    listSort2.add(item);
                }
            }
            return listSort2;
        } else {
            List<KiGuiItem> items = new ArrayList<>();
            List<KiGuiItem> listSort = new ArrayList<>();
            List<KiGuiItem> listSort2 = new ArrayList<>();

            for (KiGuiItem item : list) {
                if (item != null && item.getTab() == tab && item.getSold() == 0 && item.isSuKien() == false) {
                    items.add(item);
                }
            }

            Collections.sort(items, (item1, item2) -> Boolean.compare(item2.template.isUpToUp, item1.template.isUpToUp));

            if (max.length == 2) {
                int startIndex = Math.min(max[0], items.size());
                int endIndex = Math.min(max[1], items.size());
                listSort.addAll(items.subList(startIndex, endIndex));
            } else if (max.length == 1) {
                int endIndex = Math.min(max[0], items.size());
                listSort.addAll(items.subList(0, endIndex));
            } else {
                listSort.addAll(items);
            }

            for (KiGuiItem item : listSort) {
                if (item != null) {
                    listSort2.add(item);
                }
            }
            return listSort2;
        }

    }

    private List<KiGuiItem> getItemCanConsign(Player player) {
        if (!player.isShopKiGuiSuKien) {
            List<KiGuiItem> items = new ArrayList<>();
            list.stream().filter((it) -> (it != null && it.getConsignorID() == player.id && it.isSuKien() == false)).forEachOrdered((it) -> {
                items.add(it);
            });
            player.inventory.itemsBag.stream().filter((item) -> (item.isNotNullItem() && canConsign(item.template.type) && item.canConsign())).forEachOrdered((it) -> {
                KiGuiItem consignmentItem = ItemService.gI().convertToConsignmentItem(it);
                consignmentItem.setConsignorID(-1);
                consignmentItem.setTab((byte) 4);
                consignmentItem.setPriceGem(-1);
                consignmentItem.setPriceGold(-1);
                consignmentItem.setSold(0);
                items.add(consignmentItem);
            });
            return items;
        } else {
            List<KiGuiItem> items = new ArrayList<>();
            list.stream().filter((it) -> (it != null && it.getConsignorID() == player.id && it.isSuKien() == true)).forEachOrdered((it) -> {
                items.add(it);
            });
            player.inventory.itemsBag.stream().filter((item) -> (item.isNotNullItem() && canConsign2(item.template.type) && item.canConsign2())).forEachOrdered((it) -> {
                KiGuiItem consignmentItem = ItemService.gI().convertToConsignmentItem(it);
                consignmentItem.setConsignorID(-1);
                consignmentItem.setTab((byte) 4);
                consignmentItem.setPriceGem(-1);
                consignmentItem.setPriceGold(-1);
                consignmentItem.setSold(0);
                items.add(consignmentItem);
            });
            return items;
        }
    }

    private boolean canConsign(int type) {
        return type != 5 || type != 11 || type != 21 || type != 23 || type != 24;
    }

    private boolean canConsign2(int type) {
        return type == 5;
    }

    private byte getTabByType(byte type) {
        byte tab = -1;

        if (type >= 0 && type < 2 || type == 3) {
            tab = 0;
        } else if (type == 2 || type == 4) {
            tab = 1;
        } else if (type == 12) {
            tab = 3;
        } else {
            tab = 2;
        }
        return tab;
    }

    public void buy(Player player, short itemID, byte monneyType, int money) {
        for (KiGuiItem item : list) {

            Item thoivang = InventoryService.gI().findItemBagByTemp(player, (short) 457); // đơn vị tiền tệ

            if (item.template.id == itemID && monneyType == monneyType && money == money) {
                if (item.getConsignorID() != player.id) {
                    if (item.getSold() != 1) {
                        if (InventoryService.gI().getCountEmptyBag(player) > 0) {
                            if (monneyType == 0) {
                                if (thoivang == null) {
                                    Service.getInstance().sendThongBao(player, "Bạn còn thiếu " + (money - thoivang.quantity) + "thỏi vàng");
                                    show(player);
                                    return;
                                } else if (thoivang.quantity < money) {
                                    Service.getInstance().sendThongBao(player, "Bạn còn thiếu " + (money - thoivang.quantity) + "thỏi vàng");
                                    show(player);
                                    return;
                                }
                            }
                            InventoryService.gI().subQuantityItemsBag(player, thoivang, money);
                            InventoryService.gI().addItemBag(player, item, 999);
                            InventoryService.gI().sendItemBags(player);
                            Service.getInstance().sendThongBao(player, "Bạn đã mua thành công " + item.getName() + " với giá " + money + " thỏi vàng");
                            item.setSold(1);
                            show(player);
                            return;
                        } else {
                            Service.getInstance().sendThongBao(player, "Yêu cầu một ô hành trang mới có thể mua");
                            show(player);
                            return;
                        }
                    } else {
                        Service.getInstance().sendThongBao(player, "Vật phẩm kí gửi này đã được bán");
                        show(player);
                        return;
                    }
                } else {
                    Service.getInstance().sendThongBao(player, "Bạn không thể mua vật phẩm kí gửi của chính bản thân mình");
                    show(player);
                    return;
                }
            }
        }
    }

    public void getMoney(Player player, short itemID) {
        for (KiGuiItem item : list) {
            if (item.template.id == itemID && item.getConsignorID() == player.id && item.getSold() == 1) {
                Item thoivang = ItemService.gI().createNewItem((short) 457);
                if (item.getPriceGold() > 1) {
                    thoivang.quantity = item.getPriceGold() - 1; // số thỏi vàng = số thỏi vàng - 1 thỏi vàng phí
                    InventoryService.gI().addItemBag(player, thoivang, 999);
                }
                removeItem(item);
                InventoryService.gI().sendItemBags(player);
                Service.getInstance().sendMoney(player);
                Service.getInstance().sendThongBao(player, "Nhận được thỏi vàng phí ký gửi là 1 thỏi vàng");
                show(player);
                return;
            }
        }
    }

    public void nextPage(Player player, byte tab, int page) {
        Message msg = new Message(-100);
        try {
            int maxPage = (byte) (list.size() / 20 > 0 ? list.size() / 20 : 1);
            DataOutputStream ds = msg.writer();
            ds.writeByte(tab);
            ds.writeByte(maxPage);
            ds.writeByte(page);
            List<KiGuiItem> list = getItemConsignByTab(player, tab, (byte) (page * 20), (byte) (page * 20 + 20));
            for (KiGuiItem item : list) {
                ds.writeShort(item.template.id);
                ds.writeShort(item.template.id);
                ds.writeInt(item.getPriceGold());
                ds.writeInt(item.getPriceGem());

                ds.writeByte(0);

                if (player.isVersionAbove(222)) {
                    ds.writeInt(item.quantity);
                } else {
                    ds.writeByte(item.quantity);
                }
                ds.writeByte(item.getConsignorID() == player.id ? 0 : 1); // isMe
                ds.writeByte(item.itemOptions.size());
                for (ItemOption option : item.itemOptions) {
                    ds.writeByte(option.optionTemplate.id);
                    ds.writeShort(option.param);
                }
                ds.writeByte(0);
                ds.writeByte(0);
                if (player.isVersionAbove(237)) {
                    ds.writeUTF(item.getConsignName());
                }
            }
            showItemCanConsign(player, ds);
            ds.flush();
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void show(Player player) {
        Message msg = new Message(-44);
        try {
            int tabLength = tabName.length;
            int maxPage = (byte) (list.size() / 20 > 0 ? list.size() / 20 : 1);
            DataOutputStream ds = msg.writer();
            ds.writeByte(2);
            ds.writeByte(tabLength);
            for (byte i = 0; i < tabLength - 1; i++) {
                List<KiGuiItem> list = getItemConsignByTab(player, i);
                if (player.isShopKiGuiSuKien) {
                    ds.writeUTF(tabNameSuKien[i]);
                } else {
                    ds.writeUTF(tabName[i]);
                }
                ds.writeByte(maxPage); // max page
                ds.writeByte(list.size());
                for (KiGuiItem item : list) {
                    ds.writeShort(item.template.id);
                    ds.writeShort(item.template.id);
                    ds.writeInt(item.getPriceGold());
                    ds.writeInt(item.getPriceGem());

                    ds.writeByte(0);

                    if (player.isVersionAbove(222)) {
                        ds.writeInt(item.quantity);
                    } else {
                        ds.writeByte(item.quantity);
                    }
                    if (item.getConsignorID() == player.id) {
                        ds.writeByte(1);
                    } else {
                        ds.writeByte(0); // isMe
                    }
                    ds.writeByte(item.itemOptions.size());
                    for (ItemOption option : item.itemOptions) {
                        ds.writeByte(option.optionTemplate.id);
                        ds.writeShort(option.param);
                    }
                    ds.writeByte(0);
                    ds.writeByte(0);
                    if (player.isVersionAbove(237)) {
                        ds.writeUTF(item.getConsignName());
                    }
                }
            }
            showItemCanConsign(player, ds);
            ds.flush();
            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showItemCanConsign(Player player, DataOutputStream ds) throws IOException {
        List<KiGuiItem> items = getItemCanConsign(player);
        ds.writeUTF("ADU VIP PRO");
        ds.writeByte(0); // max page
        ds.writeByte(items.size());
        for (KiGuiItem item : items) {
            ds.writeShort(item.template.id);
            ds.writeShort(item.template.id);
            ds.writeInt(item.getPriceGold());
            ds.writeInt(item.getPriceGem());

            if (item.getConsignorID() == -1) {
                ds.writeByte(0);
            } else if (item.getSold() == 1) {
                ds.writeByte(2);
            } else {
                ds.writeByte(1);
            }
            if (player.isVersionAbove(222)) {
                ds.writeInt(item.quantity);
            } else {
                ds.writeByte(item.quantity);
            }
            ds.writeByte(item.getConsignorID() == player.id ? 0 : 1); // isMe
            ds.writeByte(item.itemOptions.size());
            for (ItemOption option : item.itemOptions) {
                ds.writeByte(option.optionTemplate.id);
                ds.writeShort(option.param);
            }
            ds.writeByte(0);
            ds.writeByte(0);
            if (player.isVersionAbove(237)) {
                ds.writeUTF(item.getConsignName());
            }
        }
    }

    public int getDaysExpried(Long createTime) {
        long now = System.currentTimeMillis();
        long elapsedTimeMillis = now - createTime;
        long elapsedDays = elapsedTimeMillis / (24 * 60 * 60 * 1000);
        return (int) elapsedDays;
    }

    public void showExpiringItems(Player player) {
        if (mapItemsExpired.containsKey(player.id)) {
            StringBuilder sb = new StringBuilder();
            sb.append("|1|Danh sách vật phẩm sắp hết hạn:\n\n");
            for (Map.Entry<Long, KiGuiItem> entry : mapItemsExpired.entrySet()) {
                KiGuiItem item = entry.getValue();
                sb.append("- ").append(item.template.name).append("\n");
            }
            sb.append("Vật phẩm sẽ bị xóa nếu quá hạn 2 ngày");
            NpcService.gI().createMenuConMeo(player, -1, -1, sb.toString(), "OK");
            return;
        }
        Service.getInstance().sendThongBao(player, "Không có vật phẩm nào sắp hết hạn kí gửi");
    }

    public void sendExpirationNotification(Player player) {
        if (mapItemsExpired.containsKey(player.id)) {
            Service.getInstance().sendThongBao(player, "Bạn có vật phẩm sắp hết hạn đang kí gửi tại siêu thị");
        }
    }
}

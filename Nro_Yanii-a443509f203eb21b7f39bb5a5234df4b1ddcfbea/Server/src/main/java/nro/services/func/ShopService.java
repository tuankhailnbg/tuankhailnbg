package nro.services.func;

import java.io.IOException;
import nro.consts.ConstAchive;
import nro.consts.ConstItem;
import nro.consts.ConstNpc;
import nro.jdbc.daos.PlayerDAO;
import nro.models.item.CaiTrang;
import nro.models.item.Item;
import nro.models.item.ItemOption;
import nro.models.npc.Npc;
import nro.models.player.Inventory;
import nro.models.player.Player;
import nro.models.shop.ItemShop;
import nro.models.shop.Shop;
import nro.models.shop.TabShop;
import nro.server.Manager;
import nro.server.io.Message;
import nro.services.InventoryService;
import nro.services.ItemService;
import nro.services.PlayerService;
import nro.services.Service;
import nro.utils.Log;
import nro.utils.Util;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import nro.server.MenuController;
import java.util.ArrayList;

/**
 * @author 💖 Nro Yanii 💖
 * 
 */
public class ShopService {

    private static final int COST_GOLD_BAR = 500000000;
    private static final int COST_LOCK_GOLD_BAR = 300000000;

    private static final byte NORMAL_SHOP = 0;
    private static final byte SPEC_SHOP = 3;

    private static ShopService i;

    public static ShopService gI() {
        if (i == null) {
            i = new ShopService();
        }
        return i;
    }

    // Lấy ra itemshop khi mua
    private ItemShop getItemShop(Player player, int shopId, int tempId) {
        ItemShop itemShop = null;
        Shop shop = null;
        switch (shopId) {
            case ConstNpc.SHOP_BUNMA_QK_0:
                shop = getShop(player, ConstNpc.BUNMA, 0, -1);
                break;
            case ConstNpc.SHOP_DENDE_0:
                shop = getShop(player, ConstNpc.DENDE, 0, -1);
                break;
            case ConstNpc.SHOP_APPULE_0:
                shop = getShop(player, ConstNpc.APPULE, 0, -1);
                break;
            case ConstNpc.SHOP_URON_0:
                shop = getShop(player, ConstNpc.URON, 0, -1);
                break;
            case ConstNpc.SHOP_SANTA_0:
                shop = getShop(player, ConstNpc.SANTA, 0, -1);
                break;
            case ConstNpc.SHOP_SANTA_1:
                shop = getShop(player, ConstNpc.SANTA, 1, -1);
                break;
            case ConstNpc.SHOP_SANTA_2:
                shop = getShop(player, ConstNpc.SANTA, 2, -1);
                break;
            case ConstNpc.SHOP_SANTA_3:
                shop = getShop(player, ConstNpc.SANTA, 3, -1);
                break;
            case ConstNpc.SHOP_BA_HAT_MIT_0:
                shop = getShop(player, ConstNpc.BA_HAT_MIT, 0, -1);
                break;
            case ConstNpc.SHOP_BA_HAT_MIT_1:
                shop = getShop(player, ConstNpc.BA_HAT_MIT, 1, -1);
                break;
            case ConstNpc.SHOP_BA_HAT_MIT_2:
                shop = getShop(player, ConstNpc.BA_HAT_MIT, 2, -1);
                break;
            case ConstNpc.SHOP_BA_HAT_MIT_3:
                shop = getShop(player, ConstNpc.BA_HAT_MIT, 3, -1);
                break;
            case ConstNpc.SHOP_BUNMA_TL_0:
                shop = getShop(player, ConstNpc.BUNMA_TL, 0, -1);
                break;
            case ConstNpc.SHOP_BILL_HUY_DIET_0:
                shop = getShop(player, ConstNpc.BILL, 0, -1);
                break;
            case ConstNpc.SHOP_WHIS_THIEN_SU:
                shop = getShop(player, ConstNpc.WHIS, 0, -1);
                break;
            case ConstNpc.SHOP_HONG_NGOC:
                shop = getShop(player, ConstNpc.QUY_LAO_KAME, 0, -1);
                break;
            case ConstNpc.SHOP_LY_TIEU_NUONG:
                shop = getShop(player, ConstNpc.LY_TIEU_NUONG, 0, -1);
                break;
            case ConstNpc.SHOP_SU_KIEN_TET:
                shop = getShop(player, ConstNpc.QUY_LAO_KAME, 1, -1);
                break;
            case ConstNpc.SHOP_TORIBOT:
                shop = getShop(player, ConstNpc.TORIBOT, 0, -1);
                break;
        }
        if (shop != null) {
            for (TabShop tab : shop.tabShops) {
                for (ItemShop is : tab.itemShops) {
                    if (is.temp.id == tempId) {
                        itemShop = is;
                        break;
                    }
                }
                if (itemShop != null) {
                    break;
                }
            }
        }
        return itemShop;
    }

    private Shop getShop(Player player, int npcId, int order, int gender) {
        for (Shop shop : Manager.SHOPS) {
            if (shop.npcId == npcId && shop.shopOrder == order) {
                if (gender != -1) {
                    return new Shop(player, shop, gender);
                } else {
                    return shop;
                }
            }
        }
        return null;
    }

    private Shop getShopHuyDiet(Player player, Shop s) {
        Shop shop = new Shop(s);
        for (TabShop tabShop : shop.tabShops) {
            for (ItemShop item : tabShop.itemShops) {
                item.iconSpec = 15012 + item.temp.type;
                item.costSpec = 1;
            }
        }
        return shop;
    }

    private Shop getShoDanhHieu(Player player, Shop s) {
        Shop shop = new Shop(s);
        for (TabShop tabShop : shop.tabShops) {
            for (ItemShop item : tabShop.itemShops) {
                switch (item.temp.id) {
                    default:
                        Service.gI().sendThongBao(player, "Bạn chưa mở khóa danh hiệu này");
                }
            }
        }
        return shop;
    }

    private Shop getShopBua(Player player, Shop s) {
        Shop shop = new Shop(s);
        for (TabShop tabShop : shop.tabShops) {
            for (ItemShop item : tabShop.itemShops) {
                long min = 0;
                switch (item.temp.id) {
                    case 213:
                        long timeTriTue = player.charms.tdTriTue;
                        long current = System.currentTimeMillis();
                        min = (timeTriTue - current) / 60000;
                        break;
                    case 214:
                        min = (player.charms.tdManhMe - System.currentTimeMillis()) / 60000;
                        break;
                    case 215:
                        min = (player.charms.tdDaTrau - System.currentTimeMillis()) / 60000;
                        break;
                    case 216:
                        min = (player.charms.tdOaiHung - System.currentTimeMillis()) / 60000;
                        break;
                    case 217:
                        min = (player.charms.tdBatTu - System.currentTimeMillis()) / 60000;
                        break;
                    case 218:
                        min = (player.charms.tdDeoDai - System.currentTimeMillis()) / 60000;
                        break;
                    case 219:
                        min = (player.charms.tdThuHut - System.currentTimeMillis()) / 60000;
                        break;
                    case 522:
                        min = (player.charms.tdDeTu - System.currentTimeMillis()) / 60000;
                        break;
                    case 671:
                        min = (player.charms.tdTriTue3 - System.currentTimeMillis()) / 60000;
                        break;
                    case 672:
                        min = (player.charms.tdTriTue4 - System.currentTimeMillis()) / 60000;
                        break;
                    case 2025:
                        min = (player.charms.tdDeTuMabu - System.currentTimeMillis()) / 60000;
                        break;
                }
                if (min > 0) {
                    item.options.clear();
                    if (min >= 1440) {
                        item.options.add(new ItemOption(63, (int) min / 1440));
                    } else if (min >= 60) {
                        item.options.add(new ItemOption(64, (int) min / 60));
                    } else {
                        item.options.add(new ItemOption(65, (int) min));
                    }
                }
            }
        }
        return shop;
    }

    // shop đồ hủy diệt
    public void openShopBillHuyDiet(Player player, int shopId, int order) {
        Shop shop = getShopHuyDiet(player, getShop(player, ConstNpc.BILL, order, -1));
        openShopType0(player, shop, shopId);
    }

    public void openShopWhisThienSu(Player player, int shopId, int order) {
        Shop shop = getShop(player, ConstNpc.WHIS, order, -1);
        openShopType3(player, shop, shopId);
    }

    // shop bùa
    public void openShopBua(Player player, int shopId, int order) {
        // player.iDMark.setShopId(shopId);
        Shop s = getShop(player, ConstNpc.BA_HAT_MIT, order, -1);
        if (s != null) {
            Shop shop = getShopBua(player, s);
            openShopType0(player, shop, shopId);
        }
    }

    public void openShopDanhHieu(Player player, int shopId, int order) {
        // player.iDMark.setShopId(shopId);
        Shop s = getShop(player, ConstNpc.SANTA, order, -1);
        if (s != null) {
            Shop shop = getShoDanhHieu(player, s);
            openShopType0(player, shop, shopId);
        }
    }

    // shop normal
    public void openShopNormal(Player player, Npc npc, int shopId, int order, int gender) {
        Shop shop = getShop(player, npc.tempId, order, gender);
        openShopType0(player, shop, shopId);
    }

    public void openShopSpecial(Player player, Npc npc, int shopId, int order, int gender) {
        Shop shop = getShop(player, npc.tempId, order, gender);
        openShopType3(player, shop, shopId);
    }

    private void openShopType0(Player player, Shop shop, int shopId) {
        player.iDMark.setShopId(shopId);
        if (shop != null) {
            Message msg;
            try {
                msg = new Message(-44);
                msg.writer().writeByte(NORMAL_SHOP);
                msg.writer().writeByte(shop.tabShops.size());
                for (TabShop tab : shop.tabShops) {
                    if (tab.id == 39) {
                        msg.writer().writeUTF(tab.name + 0);
                    } else {
                        msg.writer().writeUTF(tab.name);
                    }
                    msg.writer().writeByte(tab.itemShops.size());
                    for (ItemShop itemShop : tab.itemShops) {
                        msg.writer().writeShort(itemShop.temp.id);
                        int effectiveGold = (int) Math.min(Integer.MAX_VALUE, getEffectiveShopGoldPrice(player, itemShop));
                        int effectiveGem = getEffectiveShopGemPrice(player, itemShop);
                        msg.writer().writeInt(effectiveGold);
                        msg.writer().writeInt(effectiveGem);
                        msg.writer().writeByte(itemShop.options.size());
                        for (ItemOption option : itemShop.options) {
                            msg.writer().writeByte(option.optionTemplate.id);
                            msg.writer().writeShort(option.param);
                        }
                        msg.writer().writeByte(itemShop.isNew ? 1 : 0);
                        CaiTrang caiTrang = Manager.gI().getCaiTrangByItemId(itemShop.temp.id);
                        msg.writer().writeByte(caiTrang != null ? 1 : 0);
                        if (caiTrang != null) {
                            msg.writer().writeShort(caiTrang.getID()[0]);
                            msg.writer().writeShort(caiTrang.getID()[1]);
                            msg.writer().writeShort(caiTrang.getID()[2]);
                            msg.writer().writeShort(caiTrang.getID()[3]);
                        }
                    }
                }
                player.sendMessage(msg);
                msg.cleanup();
            } catch (Exception e) {
                Log.error(ShopService.class, e);
            }
        }
    }

    private void openShopType3(Player player, Shop shop, int shopId) {
        player.iDMark.setShopId(shopId);
        if (shop != null) {
            Message msg;
            try {
                msg = new Message(-44);
                msg.writer().writeByte(SPEC_SHOP);
                msg.writer().writeByte(shop.tabShops.size());
                for (TabShop tab : shop.tabShops) {
                    msg.writer().writeUTF(tab.name);
                    msg.writer().writeByte(tab.itemShops.size());
                    // System.out.println("shopId: " + shopId);
                    // System.out.println(tab.name);
                    for (ItemShop itemShop : tab.itemShops) {
                        msg.writer().writeShort(itemShop.temp.id);
                        msg.writer().writeShort(itemShop.iconSpec);
                        msg.writer().writeInt(itemShop.costSpec);
                        msg.writer().writeByte(itemShop.options.size());
                        for (ItemOption option : itemShop.options) {
                            msg.writer().writeByte(option.optionTemplate.id);
                            msg.writer().writeShort(option.param);
                        }
                        msg.writer().writeByte(itemShop.isNew ? 1 : 0);
                        CaiTrang caiTrang = Manager.gI().getCaiTrangByItemId(itemShop.temp.id);
                        // Goten/Goku Ultra từng làm client lỗi khi mở preview trong shop đặc biệt.
                        // Chỉ tắt preview tại shop; skin thật khi mặc vẫn dùng bảng cai_trang bình thường.
                        boolean safeCostumePreview = itemShop.temp.id != 2128 && itemShop.temp.id != 2117;
                        msg.writer().writeByte(caiTrang != null && safeCostumePreview ? 1 : 0);
                        if (caiTrang != null && safeCostumePreview) {
                            msg.writer().writeShort(caiTrang.getID()[0]);
                            msg.writer().writeShort(caiTrang.getID()[1]);
                            msg.writer().writeShort(caiTrang.getID()[2]);
                            msg.writer().writeShort(caiTrang.getID()[3]);
                        }
                    }
                }
                player.sendMessage(msg);
                msg.cleanup();
            } catch (Exception e) {
                Log.error(ShopService.class, e);
            }
        }
    }

    public boolean isCharmShop(int shopId) {
        return shopId == ConstNpc.SHOP_BA_HAT_MIT_0
                || shopId == ConstNpc.SHOP_BA_HAT_MIT_1
                || shopId == ConstNpc.SHOP_BA_HAT_MIT_2
                || shopId == ConstNpc.SHOP_BA_HAT_MIT_3;
    }

    public long getEffectiveShopGoldPrice(Player player, ItemShop is) {
        if (is == null || is.temp == null) {
            return 0L;
        }
        if (is.temp.id == 518) {
            if (player == null || player.inventory == null || player.inventory.itemsBox == null) {
                return 0L;
            }
            int boxSize = player.inventory.itemsBox.size();
            long factor = Math.max(0, boxSize - 19);
            return factor * 25_000_000L;
        }
        return Math.max(0L, (long) is.gold);
    }

    public int getEffectiveShopGemPrice(Player player, ItemShop is) {
        if (is == null || is.temp == null) {
            return 0;
        }
        if (is.temp.id == 517) {
            if (player == null || player.inventory == null || player.inventory.itemsBag == null) {
                return 0;
            }
            int bagSize = player.inventory.itemsBag.size();
            long factor = Math.max(0, bagSize - 19);
            long gemPrice = factor * 50L;
            if (gemPrice > Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
            return (int) gemPrice;
        }
        return Math.max(0, is.gem);
    }

    public boolean buyItemShopNormal(Player player, ItemShop is) {
        if (player == null || is == null || is.temp == null) {
            return false;
        }

        int itemShopID = is.temp.id;

        // Static validations
        if (is.temp.id == 457 || is.temp.id == 1309 || is.temp.id == 1310) {
            Service.gI().sendThongBao(player, "Hàng chỉ trưng bày không bán");
            Service.getInstance().sendMoney(player);
            return false;
        }

        if (player.iDMark.getShopId() == ConstNpc.SHOP_SANTA_3) {
            Service.gI().sendThongBao(player, "Bạn chưa mở khóa danh hiệu này");
            Service.getInstance().sendMoney(player);
            return false;
        }

        boolean success = false;
        String thongBaoMsg = null;
        boolean sendOKDialog = false;
        boolean needsReopen = false;
        int reopenNpc = -1;
        int reopenSelect = -1;
        boolean sendItemBags = false;
        boolean sendItemBox = false;

        synchronized (player.inventory) {
            // 1. Dynamic limit checks
            if (is.temp.id == 517 && player.inventory.itemsBag.size() >= 60) {
                thongBaoMsg = "Đã đạt mức tối đa";
            } else if (is.temp.id == 518 && player.inventory.itemsBox.size() >= 60) {
                thongBaoMsg = "Đã đạt mức tối đa";
            } else if (is.temp.id == 988 && player.inventory.getGoldLimit() >= Inventory.MAX_GOLD_LIMIT) {
                thongBaoMsg = "Giới hạn vàng của bạn đã đạt tối đa";
            }

            if (thongBaoMsg == null) {
                // 2. Bill god clothes & meal preflight
                Item mealBill = null;
                if (player.iDMark.getShopId() == ConstNpc.SHOP_BILL_HUY_DIET_0) {
                    if (!player.setClothes.godClothes) {
                        thongBaoMsg = "Yêu cầu có đủ trang bị thần linh";
                    } else {
                        mealBill = InventoryService.gI().findMealChangeDestroyClothes(player);
                        if (mealBill == null || mealBill.quantity < 200) {
                            thongBaoMsg = "Yêu cầu có 200 thức ăn";
                        }
                    }
                }

                if (thongBaoMsg == null) {
                    // 3. Event points preflight
                    int pointExchange = 0;
                    if (player.iDMark.getShopId() == ConstNpc.SHOP_SU_KIEN_TET) {
                        for (ItemOption io : is.options) {
                            if (io.optionTemplate.id == 200) {
                                pointExchange = io.param;
                            }
                        }
                        if (pointExchange > 0 && player.event.getEventPoint() < pointExchange) {
                            thongBaoMsg = "Bạn không đủ điểm sự kiện";
                        }
                    }

                    if (thongBaoMsg == null) {
                        // 4. Daily buy limit check
                        int itemExchange = is.itemExchange;
                        if (itemExchange >= 0 && isLimitItem(itemShopID)) {
                            if (player.buyLimit[itemShopID - 1074] >= getBuyLimit(itemShopID)) {
                                thongBaoMsg = "Số lượt mua trong ngày đã đạt giới hạn";
                            }
                        }

                        if (thongBaoMsg == null) {
                            // 5. Item exchange check
                            Item itm = null;
                            if (itemExchange >= 0) {
                                if (itemExchange == 861) {
                                    if (!player.inventory.hasRuby(is.costSpec)) {
                                        thongBaoMsg = "Bạn không đủ ngọc hồng để trao đổi.";
                                    }
                                } else {
                                    itm = InventoryService.gI().findItemBagByTemp(player, itemExchange);
                                    if (itm == null || !itm.isNotNullItem() || itm.quantity < is.costSpec) {
                                        thongBaoMsg = "Bạn không đủ vật phẩm để trao đổi.";
                                    }
                                }
                            }

                            if (thongBaoMsg == null) {
                                // 6. Preflight bag capacity based on actual reward
                                boolean isCharm = isCharmShop(player.iDMark.getShopId());
                                Item rewardPreview = null;
                                int maxRewardQty = 99;

                                if (!isCharm) {
                                    if (player.iDMark.getShopId() == ConstNpc.SHOP_BILL_HUY_DIET_0) {
                                        rewardPreview = ItemService.gI().createItemFromItemShop(is);
                                        maxRewardQty = 200;
                                        if (mealBill != null && mealBill.quantity == 200 && InventoryService.gI().getCountEmptyBag(player) == 0) {
                                            // 200 meal will free 1 slot upon consumption
                                        } else if (!InventoryService.gI().canAddItemBag(player, rewardPreview, maxRewardQty)) {
                                            thongBaoMsg = "Hành trang đã đầy";
                                        }
                                    } else if (is.temp.id == 361) {
                                        rewardPreview = ItemService.gI().createNewItem((short) 361, 10);
                                        maxRewardQty = 9999;
                                        if (!InventoryService.gI().canAddItemBag(player, rewardPreview, maxRewardQty)) {
                                            thongBaoMsg = "Hành trang đã đầy";
                                        }
                                    } else {
                                        rewardPreview = ItemService.gI().createItemFromItemShop(is);
                                        maxRewardQty = 99;
                                        if (!InventoryService.gI().canAddItemBag(player, rewardPreview, maxRewardQty)) {
                                            thongBaoMsg = "Hành trang đã đầy";
                                        }
                                    }
                                }

                                if (thongBaoMsg == null) {
                                    // 7. Calculate CURRENT price server-side at purchase time
                                    long goldSpend = getEffectiveShopGoldPrice(player, is);
                                    int gemSpend = getEffectiveShopGemPrice(player, is);
                                    int rubySpend = (itemExchange == 861) ? is.costSpec : 0;

                                    if (goldSpend < 0 || gemSpend < 0 || rubySpend < 0) {
                                        thongBaoMsg = "Giá vật phẩm không hợp lệ";
                                    } else if (goldSpend > 0 && !player.inventory.hasGold(goldSpend)) {
                                        thongBaoMsg = "Bạn không đủ vàng, còn thiếu "
                                                + (Util.numberToMoney(goldSpend - player.inventory.gold) + " vàng");
                                        sendOKDialog = true;
                                    } else if (gemSpend > 0 && !player.inventory.hasGem(gemSpend)) {
                                        thongBaoMsg = "Bạn không đủ ngọc, còn thiếu "
                                                + (gemSpend - player.inventory.getGem()) + " ngọc";
                                    } else if (rubySpend > 0 && !player.inventory.hasRuby(rubySpend)) {
                                        thongBaoMsg = "Bạn không đủ ngọc hồng để trao đổi.";
                                    } else {
                                        // 8. Atomic payment
                                        if (!player.inventory.trySpendCurrencies(goldSpend, gemSpend, rubySpend)) {
                                            thongBaoMsg = "Giao dịch thất bại do không đủ tiền";
                                        } else {
                                            // 9. Deduct materials & limits
                                            boolean subbedItemExchange = false;
                                            if (itemExchange >= 0 && itemExchange != 861) {
                                                InventoryService.gI().subQuantityItemsBag(player, itm, is.costSpec);
                                                subbedItemExchange = true;
                                            }
                                            if (pointExchange > 0) {
                                                player.event.subEventPoint(pointExchange);
                                            }
                                            boolean incBuyLimit = false;
                                            if (itemExchange >= 0 && isLimitItem(itemShopID)) {
                                                player.buyLimit[itemShopID - 1074]++;
                                                incBuyLimit = true;
                                            }

                                            // 10. Execute action
                                            switch (player.iDMark.getShopId()) {
                                                case ConstNpc.SHOP_BA_HAT_MIT_0:
                                                    player.charms.addTimeCharms(is.temp.id, 60);
                                                    needsReopen = true;
                                                    reopenNpc = player.iDMark.getShopId();
                                                    reopenSelect = 0;
                                                    success = true;
                                                    break;
                                                case ConstNpc.SHOP_BA_HAT_MIT_1:
                                                    player.charms.addTimeCharms(is.temp.id, 60 * 8);
                                                    needsReopen = true;
                                                    reopenNpc = player.iDMark.getShopId();
                                                    reopenSelect = 1;
                                                    success = true;
                                                    break;
                                                case ConstNpc.SHOP_BA_HAT_MIT_2:
                                                    player.charms.addTimeCharms(is.temp.id, 60 * 24 * 30);
                                                    needsReopen = true;
                                                    reopenNpc = player.iDMark.getShopId();
                                                    reopenSelect = 2;
                                                    success = true;
                                                    break;
                                                case ConstNpc.SHOP_BA_HAT_MIT_3:
                                                    player.charms.addTimeCharms(is.temp.id, 60);
                                                    needsReopen = true;
                                                    reopenNpc = player.iDMark.getShopId();
                                                    reopenSelect = 3;
                                                    success = true;
                                                    break;
                                                case ConstNpc.SHOP_BILL_HUY_DIET_0:
                                                    InventoryService.gI().subQuantityItemsBag(player, mealBill, 200);
                                                    Item itemBill = ItemService.gI().createItemFromItemShop(is);
                                                    int param = 0;
                                                    if (Util.isTrue(2, 10)) {
                                                        param = Util.nextInt(10, 15);
                                                    } else if (Util.isTrue(3, 10)) {
                                                        param = Util.nextInt(0, 10);
                                                    }
                                                    for (ItemOption io : itemBill.itemOptions) {
                                                        int optId = io.optionTemplate.id;
                                                        switch (optId) {
                                                            case 47: // giáp
                                                            case 6: // hp
                                                            case 26: // hp/30s
                                                            case 22: // hp k
                                                            case 0: // sức đánh
                                                            case 7: // ki
                                                            case 28: // ki/30s
                                                            case 23: // ki k
                                                            case 14: // crit
                                                                io.param += ((long) io.param * param / 100);
                                                                break;
                                                        }
                                                    }
                                                    if (InventoryService.gI().addItemBag(player, itemBill, 200)) {
                                                        sendItemBags = true;
                                                        thongBaoMsg = "Đổi thành công " + is.temp.name;
                                                        success = true;
                                                    } else {
                                                        // Refund meal
                                                        InventoryService.gI().addItemBag(player, ItemService.gI().createNewItem(mealBill.template.id, 200), 999);
                                                    }
                                                    break;
                                                case ConstNpc.SHOP_WHIS_THIEN_SU:
                                                    Item itemWhis = (is.temp.id == 361)
                                                            ? ItemService.gI().createNewItem((short) 361, 10)
                                                            : ItemService.gI().createItemFromItemShop(is);
                                                    int maxBagWhis = (is.temp.id == 361) ? 9999 : 99;
                                                    if (InventoryService.gI().addItemBag(player, itemWhis, maxBagWhis)) {
                                                        sendItemBags = true;
                                                        thongBaoMsg = "Đổi thành công " + is.temp.name;
                                                        success = true;
                                                    }
                                                    break;
                                                case ConstNpc.SHOP_SU_KIEN_TET:
                                                    Item itemTet = (is.temp.id == 361)
                                                            ? ItemService.gI().createNewItem((short) 361, 10)
                                                            : ItemService.gI().createItemFromItemShop(is);
                                                    int maxBagTet = (is.temp.id == 361) ? 9999 : 99;
                                                    if (InventoryService.gI().addItemBag(player, itemTet, maxBagTet)) {
                                                        sendItemBags = true;
                                                        thongBaoMsg = "Bạn nhận được " + is.temp.name;
                                                        success = true;
                                                    }
                                                    break;
                                                default:
                                                    Item itemDefault = (is.temp.id == 361)
                                                            ? ItemService.gI().createNewItem((short) 361, 10)
                                                            : ItemService.gI().createItemFromItemShop(is);
                                                    int maxBagQuantity = (is.temp.id == 361) ? 9999 : 99;
                                                    if (InventoryService.gI().addItemBag(player, itemDefault, maxBagQuantity)) {
                                                        sendItemBags = true;
                                                        thongBaoMsg = "Mua thành công " + is.temp.name;
                                                        if (is.temp.id == 517 || is.temp.id == 518) {
                                                            // Purchases expand capacity directly; refresh the corresponding
                                                            // inventory packet so the client sees the new slot immediately.
                                                            sendItemBox = (is.temp.id == 518);
                                                            needsReopen = true;
                                                            reopenNpc = ConstNpc.SANTA;
                                                            reopenSelect = 1;
                                                        }
                                                        success = true;
                                                    }
                                                    break;
                                            }

                                            // 11. Rollback if action failed
                                            if (!success) {
                                                if (goldSpend > 0) {
                                                    player.inventory.addGold(goldSpend);
                                                }
                                                if (gemSpend > 0) {
                                                    player.inventory.addGem(gemSpend);
                                                }
                                                if (rubySpend > 0) {
                                                    player.inventory.addRuby(rubySpend);
                                                }
                                                if (subbedItemExchange) {
                                                    Item refundExchange = ItemService.gI().createNewItem((short) itemExchange, is.costSpec);
                                                    InventoryService.gI().addItemBag(player, refundExchange, 9999);
                                                }
                                                if (pointExchange > 0) {
                                                    player.event.addEventPoint(pointExchange);
                                                }
                                                if (incBuyLimit) {
                                                    player.buyLimit[itemShopID - 1074]--;
                                                }
                                                if (is.temp.id != 517 && is.temp.id != 518 && is.temp.id != 988) {
                                                    thongBaoMsg = "Hành trang không đủ chỗ trống, giao dịch thất bại";
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } // end synchronized (player.inventory)

        // Network responses outside lock
        if (sendItemBags) {
            InventoryService.gI().sendItemBags(player);
        }
        if (sendItemBox) {
            InventoryService.gI().sendItemBox(player);
        }
        if (thongBaoMsg != null) {
            if (sendOKDialog) {
                Service.getInstance().sendThongBaoOK(player, thongBaoMsg);
            } else {
                Service.getInstance().sendThongBao(player, thongBaoMsg);
            }
        }
        if (needsReopen) {
            if (reopenNpc == ConstNpc.SANTA && reopenSelect == 1) {
                // Send fresh dynamic prices without requiring another NPC click.
                Shop santaStorage = getShop(player, ConstNpc.SANTA, 2, player.gender);
                openShopType0(player, santaStorage, ConstNpc.SHOP_SANTA_2);
            } else if (isCharmShop(reopenNpc)) {
                openShopBua(player, player.iDMark.getShopId(), reopenSelect);
            } else {
                reopenShop(player, reopenNpc, reopenSelect);
            }
        }
        Service.getInstance().sendMoney(player);
        return success;
    }

    public void reopenShop(Player player, int npcId, int select) {
        try {
            MenuController.getInstance().doSelectMenu(player, npcId, select);
        } catch (Exception ex) {
            System.out.println("Lỗi reopen shop");
        }
    }

    private boolean isLimitItem(int id) {
        return id >= ConstItem.DA_NANG_CAP_CAP_1 && id <= ConstItem.CONG_THUC_VIP_1086;
    }

    private int getBuyLimit(int id) {
        switch (id) {
            case ConstItem.DA_NANG_CAP_CAP_1:
            case ConstItem.DA_NANG_CAP_CAP_2:
            case ConstItem.DA_MAY_MAN_CAP_1:
            case ConstItem.DA_MAY_MAN_CAP_2:
            case ConstItem.CONG_THUC_VIP:
            case ConstItem.CONG_THUC_VIP_1085:
            case ConstItem.CONG_THUC_VIP_1086:
                return 10;
            case ConstItem.DA_NANG_CAP_CAP_3:
            case ConstItem.DA_MAY_MAN_CAP_3:
                return 5;
            case ConstItem.DA_NANG_CAP_CAP_4:
            case ConstItem.DA_MAY_MAN_CAP_4:
                return 2;
            case ConstItem.DA_NANG_CAP_CAP_5:
            case ConstItem.DA_MAY_MAN_CAP_5:
                return 1;
        }
        return -1;
    }

    // item reward lucky round---------------------------------------------------
    public void openBoxItemLuckyRound(Player player) {
        player.iDMark.setShopId(ConstNpc.SIDE_BOX_LUCKY_ROUND);
        InventoryService.gI().arrangeItems(player.inventory.itemsBoxCrackBall);
        Message msg;
        try {
            msg = new Message(-44);
            msg.writer().writeByte(4);
            msg.writer().writeByte(1);
            msg.writer().writeUTF("Rương đồ");
            int n = player.inventory.itemsBoxCrackBall.size()
                    - InventoryService.gI().getCountEmptyListItem(player.inventory.itemsBoxCrackBall);
            msg.writer().writeByte(n);
            for (int i = 0; i < n; i++) {
                Item item = player.inventory.itemsBoxCrackBall.get(i);
                msg.writer().writeShort(item.template.id);
                msg.writer().writeUTF("\n|7|-----Nro Yanii-----");
                List<ItemOption> itemOptions = item.getDisplayOptions();
                msg.writer().writeByte(itemOptions.size());
                for (ItemOption io : itemOptions) {
                    msg.writer().writeByte(io.optionTemplate.id);
                    msg.writer().writeShort(io.param);
                }
                msg.writer().writeByte(1);
                CaiTrang ct = Manager.gI().getCaiTrangByItemId(item.template.id);
                msg.writer().writeByte(ct != null ? 1 : 0);
                if (ct != null) {
                    msg.writer().writeShort(ct.getID()[0]);
                    msg.writer().writeShort(ct.getID()[1]);
                    msg.writer().writeShort(ct.getID()[2]);
                    msg.writer().writeShort(ct.getID()[3]);
                }
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    private void getItemSideBoxLuckyRound(Player player, byte type, int index) {
        if (index < 0 || index >= player.inventory.itemsBoxCrackBall.size()) {
            return;
        }
        Item item = player.inventory.itemsBoxCrackBall.get(index);
        switch (type) {
            case 0: // nhận
                if (item.isNotNullItem()) {
                    if (InventoryService.gI().getCountEmptyBag(player) != 0) {
                        InventoryService.gI().addItemBag(player, item, 0);
                        Service.getInstance().sendThongBao(player,
                                "Bạn nhận được " + (item.template.id == 189
                                        ? Util.numberToMoney(item.quantity) + " vàng"
                                        : item.template.name));
                        InventoryService.gI().sendItemBags(player);
                        InventoryService.gI().removeItem(player.inventory.itemsBoxCrackBall, index);
                        openBoxItemLuckyRound(player);
                    } else {
                        Service.getInstance().sendThongBao(player, "Hành trang đã đầy");
                    }
                } else {
                    Service.getInstance().sendThongBao(player, "Không thể thực hiện");
                }
                break;
            case 1: // xóa
                InventoryService.gI().subQuantityItem(player.inventory.itemsBoxCrackBall, item, item.quantity);
                openBoxItemLuckyRound(player);
                Service.getInstance().sendThongBao(player, "Xóa vật phẩm thành công");
                break;
            case 2: // nhận hết
                for (int i = 0; i < player.inventory.itemsBoxCrackBall.size(); i++) {
                    item = player.inventory.itemsBoxCrackBall.get(i);
                    if (item.isNotNullItem()) {
                        if (InventoryService.gI().addItemBag(player, item, 0)) {
                            player.inventory.itemsBoxCrackBall.set(i, ItemService.gI().createItemNull());
                            Service.getInstance().sendThongBao(player,
                                    "Bạn nhận được " + (item.template.id == 189
                                            ? Util.numberToMoney(item.quantity) + " vàng"
                                            : item.template.name));
                        }
                    } else {
                        break;
                    }
                }
                InventoryService.gI().sendItemBags(player);
                openBoxItemLuckyRound(player);
                break;
        }
    }
    // item reward---------------------------------------------------------------

    public void openBoxItemReward(Player player) {
        if (player.getSession().itemsReward == null) {
            player.getSession().initItemsReward();
        }
        player.iDMark.setShopId(ConstNpc.SIDE_BOX_ITEM_REWARD);
        Message msg;
        try {
            msg = new Message(-44);
            msg.writer().writeByte(4);
            msg.writer().writeByte(1);
            msg.writer().writeUTF(player.getSession().itemsReward.size() + " Vật\nphẩm");
            msg.writer().writeByte(player.getSession().itemsReward.size());
            for (Item item : player.getSession().itemsReward) {
                msg.writer().writeShort(item.template.id);
                msg.writer().writeUTF("|7|-----Nro Yanii------");
                List<ItemOption> itemOptions = item.getDisplayOptions();
                msg.writer().writeByte(itemOptions.size() + 1);
                for (ItemOption io : itemOptions) {
                    msg.writer().writeByte(io.optionTemplate.id);
                    msg.writer().writeShort(io.param);
                }
                // số lượng
                
                msg.writer().writeByte(31);
                msg.writer().writeShort(item.quantity);
                //
                msg.writer().writeByte(0);
                CaiTrang ct = Manager.gI().getCaiTrangByItemId(item.template.id);
                msg.writer().writeByte(ct != null ? 1 : 0);
                if (ct != null) {
                    msg.writer().writeShort(ct.getID()[0]);
                    msg.writer().writeShort(ct.getID()[1]);
                    msg.writer().writeShort(ct.getID()[2]);
                    msg.writer().writeShort(ct.getID()[3]);
                }
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    private void getItemSideBoxReward(Player player, byte type, int index) {
        if (index < 0 || index >= player.getSession().itemsReward.size()) {
            return;
        }
        Item item = player.getSession().itemsReward.get(index);
        switch (type) {
            case 0: // nhận
                if (item.isNotNullItem()) {
                    if (InventoryService.gI().getCountEmptyBag(player) != 0) {
                        InventoryService.gI().addItemBag(player, item, 0);
                        Service.getInstance().sendThongBao(player,
                                "Bạn nhận được " + (item.template.id == 189
                                        ? Util.numberToMoney(item.quantity) + " vàng"
                                        : item.template.name));
                        InventoryService.gI().sendItemBags(player);
                        player.getSession().itemsReward.remove(index);
                        openBoxItemReward(player);
                    } else {
                        Service.getInstance().sendThongBao(player, "Hành trang đã đầy");
                    }
                } else {
                    Service.getInstance().sendThongBao(player, "Không thể thực hiện");
                }
                break;
            case 1: // xóa
                player.getSession().itemsReward.remove(index);
                openBoxItemReward(player);
                Service.getInstance().sendThongBao(player, "Xóa vật phẩm thành công");
                break;
            case 2: // nhận hết
                for (int i = player.getSession().itemsReward.size() - 1; i >= 0; i--) {
                    item = player.getSession().itemsReward.get(i);
                    if (item.isNotNullItem()) {
                        if (InventoryService.gI().addItemBag(player, item, 0)) {
                            player.getSession().itemsReward.remove(i);
                            Service.getInstance().sendThongBao(player,
                                    "Bạn nhận được " + (item.template.id == 189
                                            ? Util.numberToMoney(item.quantity) + " vàng"
                                            : item.template.name));
                        }
                    } else {
                        break;
                    }
                }
                InventoryService.gI().sendItemBags(player);
                openBoxItemReward(player);
                break;
        }
        PlayerDAO.updateItemReward(player);

    }

    // --------------------------------------------------------------------------
    // điều hướng mua
    public void buyItem(Player player, byte type, int tempId) {
        switch (player.iDMark.getShopId()) {
            case ConstNpc.SIDE_BOX_LUCKY_ROUND:
                getItemSideBoxLuckyRound(player, type, tempId);
                break;
            case ConstNpc.SIDE_BOX_ITEM_REWARD:
                getItemSideBoxReward(player, type, tempId);
                break;
            default:
                buyItemShopNormal(player, getItemShop(player, player.iDMark.getShopId(), tempId));
                break;
        }
    }

    public void showConfirmSellItem(Player pl, int where, int index) {
        Item item = null;
        index += 4;
        if (where == 0) {
            if (index < 0 || index >= pl.inventory.itemsBody.size()) {
                return;
            }
            item = pl.inventory.itemsBody.get(index);
        } else {
            if (index < 0 || index >= pl.inventory.itemsBag.size()) {
                return;
            }
            item = pl.inventory.itemsBag.get(index);

        }
        if (item.isNotNullItem()) {
            int goldReceive = 0;
            if (item.template.id == 457) {
                Input.gI().createFormBanThoiVang(pl);
                return;
            } else {
                goldReceive = item.quantity;
            }
            Message msg = new Message(7);
            try {
                msg.writer().writeByte(where);
                msg.writer().writeShort(index);
                msg.writer()
                        .writeUTF("Bạn có muốn bán\n x"
                                + (item.template.id == 457 ? 1 : item.quantity) + " "
                                + item.template.name
                                + "\nvới giá là " + Util.numberToMoney(goldReceive) + " vàng?");
                pl.sendMessage(msg);
                msg.cleanup();
            } catch (Exception e) {
            }
        }
    }

    public void sellItem(Player pl, int where, int index) {
        Item item = null;
        if (where == 0) {
            if (index < 0 || index >= pl.inventory.itemsBody.size()) {
                return;
            }
            item = pl.inventory.itemsBody.get(index);
        } else {
            if (index < 0 || index >= pl.inventory.itemsBag.size()) {
                return;
            }
            item = pl.inventory.itemsBag.get(index);
        }
        if (item != null && item.isNotNullItem()) {
            int goldReceive = 0;
            if (item.template.id == 457) {
                Input.gI().createFormBanThoiVang(pl);
                return;
            } else {
                goldReceive = item.quantity;
            }
            if (pl.inventory.gold + goldReceive <= pl.inventory.getGoldLimit()) {
                if (where == 0) {
                    InventoryService.gI().subQuantityItemsBody(pl, item, item.quantity);
                    InventoryService.gI().sendItemBody(pl);
                    Service.getInstance().Send_Caitrang(pl);
                } else {
                    if (item.template.id == 457) {
                        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                    } else {
                        InventoryService.gI().subQuantityItemsBag(pl, item, item.quantity);
                    }
                    InventoryService.gI().sendItemBags(pl);
                }
                pl.inventory.addGold(goldReceive);
                pl.playerTask.achivements.get(ConstAchive.TRUM_NHAT_VE_CHAI).count++;
                PlayerService.gI().sendInfoHpMpMoney(pl);
                Service.getInstance().sendThongBao(pl, "Đã bán " + item.template.name
                        + " thu được " + Util.numberToMoney(goldReceive) + " vàng");
            } else {
                Service.getInstance().sendThongBao(pl, "Vàng sau khi bán vượt quá giới hạn");
            }
        } else {
            Service.getInstance().sendThongBao(pl, "Không thể thực hiện");
        }
    }
}

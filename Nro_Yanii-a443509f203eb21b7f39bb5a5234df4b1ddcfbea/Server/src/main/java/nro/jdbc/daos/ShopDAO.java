package nro.jdbc.daos;

import nro.models.item.ItemOption;
import nro.models.item.ItemTemplate;
import nro.models.shop.ItemShop;
import nro.models.shop.Shop;
import nro.models.shop.TabShop;
import nro.services.ItemService;
import nro.utils.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 *
 * @author 💖 Nro Yanii 💖
 * 
 *
 */
public class ShopDAO {

    public static List<Shop> getShops(Connection con) {
        List<Shop> list = new ArrayList<>();
        try {
            PreparedStatement ps = con.prepareStatement("select * from shop order by npc_id asc, shop_order asc");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Shop shop = new Shop();
                shop.id = rs.getInt(1);
                shop.npcId = rs.getByte(2);
                shop.shopOrder = rs.getByte(3);
                loadShopTab(con, shop);
                list.add(shop);
            }
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } catch (Exception e) {
            Log.error(ShopDAO.class, e);
        }
        return list;
    }

    private static void loadShopTab(Connection con, Shop shop) {
        try {
            PreparedStatement ps = con.prepareStatement("select * from tab_shop where shop_id = ? order by id");
            ps.setInt(1, shop.id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                TabShop tab = new TabShop();
                tab.shop = shop;
                tab.id = rs.getInt(1);
                tab.name = rs.getString(3).replaceAll("<>", "\n");

                // Bỏ hoàn toàn tab "Shop Biến hình" của Santa.
                if (tab.id == 44) {
                    continue;
                }

                loadItemShop(con, tab);
                shop.tabShops.add(tab);
            }
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
                java.util.logging.Logger.getLogger(AccountDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (Exception e) {
            Log.error(ShopDAO.class, e);
        }
    }

    private static void loadItemShop(Connection con, TabShop tabShop) {
        try {
            PreparedStatement ps = con.prepareStatement("select * from item_shop where is_sell = 1 and tab_id = ? "
                    + "order by create_time desc");
            ps.setInt(1, tabShop.id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ItemShop itemShop = new ItemShop();
                itemShop.tabShop = tabShop;
                itemShop.id = rs.getInt(1);
                itemShop.temp = ItemService.gI().getTemplate(rs.getShort(3));
                itemShop.gold = rs.getInt(4);
                itemShop.gem = rs.getInt(5);
                itemShop.isNew = rs.getBoolean(6);
                itemShop.itemExchange = rs.getInt("item_exchange");
                if (itemShop.itemExchange != -1) {
                    itemShop.iconSpec = ItemService.gI().getTemplate(itemShop.itemExchange).iconID;
                    itemShop.costSpec = rs.getInt("quantity_exchange");
                }
                // thay đổi vật phẩm trong shop
                loadItemShopOption(con, itemShop);
                applySantaSpecialCostumeConfig(itemShop);
                applyChiChiShopPriceConfig(itemShop);
                tabShop.itemShops.add(itemShop);
            }

            // Chi Chi - Shop hot: phục hồi vật liệu SPL và sửa Sách Tuyệt Kĩ 1 dùng chung.
            if (tabShop.id == 30) {
                configureChiChiHotShop(tabShop);
            }

            // Santa - tab Đặc biệt (tab 17): mở bán lại Giáp tập luyện cấp 1 và 2.
            // Cấp 3 (ID 536) đang có sẵn trong DB với giá 1 tỷ vàng.
            if (tabShop.id == 17) {
                addSantaTrainArmorIfMissing(tabShop, 534, 10_000_000);
                addSantaTrainArmorIfMissing(tabShop, 535, 100_000_000);
            }

            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
                java.util.logging.Logger.getLogger(AccountDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (Exception e) {
            Log.error(ShopDAO.class, e);
        }
    }

    /**
     * Cân bằng giá 3 tab cửa hàng của NPC Chi Chi:
     * 41 = Shop đeo lưng, 42 = Shop linh thú, 43 = Shop đặc biệt.
     * Chỉ đổi đúng các mốc giá yêu cầu, không ảnh hưởng shop khác.
     */
    private static void applyChiChiShopPriceConfig(ItemShop itemShop) {
        if (itemShop == null || itemShop.tabShop == null) {
            return;
        }

        int tabId = itemShop.tabShop.id;
        if (tabId != 41 && tabId != 42 && tabId != 43) {
            return;
        }

        if (itemShop.itemExchange == 861) { // Hồng ngọc
            switch (itemShop.costSpec) {
                case 150_000:
                    itemShop.costSpec = 50_000;
                    break;
                case 100_000:
                    itemShop.costSpec = 30_000;
                    break;
                case 50_000:
                    itemShop.costSpec = 20_000;
                    break;
                default:
                    break;
            }
        } else if (itemShop.itemExchange == 457 && itemShop.costSpec == 10_000) { // Thỏi vàng
            itemShop.costSpec = 2_500;
        }
    }

    /**
     * Cấu hình cố định các cải trang ở cửa hàng đặc biệt của Santa.
     * Override lúc load để VPS chỉ cần pull/build, không phải sửa DB thủ công.
     */
    private static void configureChiChiHotShop(TabShop tabShop) {
        // Sách Tuyệt Kĩ 1 dùng chung cho cả 3 hành tinh.
        // Giữ 1285 làm bản bán chính; 1287/1289 vẫn được đổi gender=3 để
        // những sách người chơi đã sở hữu từ trước vẫn có thể trang bị bình thường.
        int[] universalBookIds = {1285, 1287, 1289};
        for (int bookId : universalBookIds) {
            ItemTemplate book = ItemService.gI().getTemplate(bookId);
            if (book != null) {
                book.gender = 3;
                book.type = 35;
                book.name = "Sách tuyệt kỹ 1";
                book.description = "Sách tuyệt kỹ dùng chung cho cả 3 hành tinh";
            }
        }
        tabShop.itemShops.removeIf(itemShop -> itemShop != null && itemShop.temp != null
                && (itemShop.temp.id == 1287 || itemShop.temp.id == 1289));

        // Khôi phục đúng template Hematite/Dùi đục của cơ chế Sao Pha Lê.
        ItemTemplate hematite = ItemService.gI().getTemplate(1423);
        if (hematite != null) {
            hematite.type = 27;
            hematite.gender = 3;
            hematite.name = "Hematite";
            hematite.description = "Nguyên liệu. Gặp Bà Hạt Mít để sử dụng";
            hematite.level = 1;
            hematite.iconID = 11759;
            hematite.part = -1;
            hematite.isUpToUp = false;
            hematite.strRequire = 0;
        }

        ItemTemplate duiDuc = ItemService.gI().getTemplate(1438);
        if (duiDuc == null) {
            return;
        }
        duiDuc.type = 27;
        duiDuc.gender = 3;
        duiDuc.name = "Dùi đục";
        duiDuc.description = "Vật phẩm hỗ trợ. Gặp Bà Hạt Mít để cường hóa lỗ Sao Pha Lê";
        duiDuc.level = 1;
        duiDuc.iconID = 12139;
        duiDuc.part = -1;
        duiDuc.isUpToUp = false;
        duiDuc.strRequire = 0;

        for (ItemShop itemShop : tabShop.itemShops) {
            if (itemShop != null && itemShop.temp != null && itemShop.temp.id == 1438) {
                itemShop.gold = 0;
                itemShop.gem = 0;
                itemShop.itemExchange = 457;
                itemShop.costSpec = 500;
                ItemTemplate goldBar = ItemService.gI().getTemplate(457);
                itemShop.iconSpec = goldBar != null ? goldBar.iconID : 0;
                itemShop.isNew = true;
                return;
            }
        }

        ItemShop itemShop = new ItemShop();
        itemShop.tabShop = tabShop;
        itemShop.id = -1438;
        itemShop.temp = duiDuc;
        itemShop.gold = 0;
        itemShop.gem = 0;
        itemShop.isNew = true;
        itemShop.itemExchange = 457;
        itemShop.costSpec = 500;
        ItemTemplate goldBar = ItemService.gI().getTemplate(457);
        itemShop.iconSpec = goldBar != null ? goldBar.iconID : 0;
        tabShop.itemShops.add(itemShop);
    }

    private static void addSantaTrainArmorIfMissing(TabShop tabShop, int itemId, int goldPrice) {
        for (ItemShop itemShop : tabShop.itemShops) {
            if (itemShop != null && itemShop.temp != null && itemShop.temp.id == itemId) {
                // Nếu DB đã có lại item thì chỉ đảm bảo nó bán đúng giá và có option thời gian.
                itemShop.gold = goldPrice;
                itemShop.gem = 0;
                itemShop.itemExchange = -1;
                if (itemShop.options.stream().noneMatch(io -> io != null
                        && io.optionTemplate != null && io.optionTemplate.id == 9)) {
                    itemShop.options.add(new ItemOption(9, 0));
                }
                return;
            }
        }

        ItemShop trainArmor = new ItemShop();
        trainArmor.tabShop = tabShop;
        trainArmor.id = -itemId;
        trainArmor.temp = ItemService.gI().getTemplate(itemId);
        if (trainArmor.temp == null) {
            return;
        }
        trainArmor.gold = goldPrice;
        trainArmor.gem = 0;
        trainArmor.isNew = false;
        trainArmor.itemExchange = -1;
        trainArmor.costSpec = 0;
        trainArmor.iconSpec = 0;
        trainArmor.options.add(new ItemOption(9, 0));
        tabShop.itemShops.add(trainArmor);
    }

    private static void applySantaSpecialCostumeConfig(ItemShop itemShop) {
        if (itemShop == null || itemShop.temp == null
                || itemShop.tabShop == null || itemShop.tabShop.id != 13) {
            return;
        }

        int exchangeItem = -1;
        int exchangeCost = 0;
        int[][] options = null;

        switch (itemShop.temp.id) {
            case 2147: // Black Super Saiyan 5
                exchangeItem = 861; // Hồng ngọc
                exchangeCost = 100_000;
                options = new int[][] {
                        {77, 30}, {103, 30}, {50, 30},
                        {101, 40}, {5, 25}, {14, 10}, {106, 0}
                };
                break;
            case 2145: // Black Goku
                exchangeItem = 861;
                exchangeCost = 75_000;
                options = new int[][] {
                        {77, 28}, {103, 28}, {50, 28},
                        {101, 30}, {5, 25}, {14, 8}, {106, 0}
                };
                break;
            case 2125: // Android 21
                exchangeItem = 861;
                exchangeCost = 100_000;
                options = new int[][] {
                        {77, 35}, {103, 30}, {50, 25},
                        {101, 40}, {5, 25}, {14, 10}, {106, 0}
                };
                break;
            case 2142: // Gogeta SSJ4
                exchangeItem = 861;
                exchangeCost = 100_000;
                options = new int[][] {
                        {77, 25}, {103, 30}, {50, 35},
                        {101, 40}, {5, 25}, {14, 10}, {106, 0}
                };
                break;
            case 2134: // Gohan
                exchangeItem = 861;
                exchangeCost = 50_000;
                options = new int[][] {
                        {77, 26}, {103, 26}, {50, 26},
                        {101, 25}, {5, 20}, {14, 5}, {106, 0}
                };
                break;
            case 2128: // Goten
                exchangeItem = 861;
                exchangeCost = 25_000;
                options = new int[][] {
                        {77, 24}, {103, 24}, {50, 24},
                        {101, 20}, {106, 0}
                };
                break;
            case 2117: // Goku Ultra
                exchangeItem = 457; // Thỏi vàng
                exchangeCost = 3_000;
                options = new int[][] {
                        {77, 22}, {103, 22}, {50, 22},
                        {101, 15}, {106, 0}
                };
                break;
            default:
                return;
        }

        itemShop.itemExchange = exchangeItem;
        itemShop.costSpec = exchangeCost;
        if (ItemService.gI().getTemplate(exchangeItem) != null) {
            itemShop.iconSpec = ItemService.gI().getTemplate(exchangeItem).iconID;
        }

        itemShop.options.clear();
        for (int[] option : options) {
            itemShop.options.add(new ItemOption(option[0], option[1]));
        }
    }

    private static void loadItemShopOption(Connection con, ItemShop itemShop) {
        try {
            PreparedStatement ps = con.prepareStatement("select * from item_shop_option where item_shop_id = ?");
            ps.setInt(1, itemShop.id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                itemShop.options.add(new ItemOption(rs.getInt(2), rs.getInt(3)));
            }
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
                java.util.logging.Logger.getLogger(AccountDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (Exception e) {
            Log.error(ShopDAO.class, e);
        }
    }

}

package nro.jdbc.daos;

import nro.models.item.ItemOption;
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
                tabShop.itemShops.add(itemShop);
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

    private static void applySantaSpecialCostumeConfig(ItemShop itemShop) {
        if (itemShop == null || itemShop.temp == null) {
            return;
        }

        switch (itemShop.temp.id) {
            case 2147: // Super Saiyan 5
                configureSantaCostume(itemShop, 861, 100_000,
                        new int[][] {
                                {77, 30}, {103, 30}, {50, 30},
                                {101, 40}, {5, 25}, {14, 10}, {106, 0}
                        });
                break;
            case 2145: // Black Goku
                configureSantaCostume(itemShop, 861, 75_000,
                        new int[][] {
                                {77, 28}, {103, 28}, {50, 28},
                                {101, 30}, {5, 25}, {14, 8}, {106, 1}
                        });
                break;
            case 2125: // Android 21
                configureSantaCostume(itemShop, 861, 100_000,
                        new int[][] {
                                {77, 35}, {103, 30}, {50, 25},
                                {101, 40}, {5, 25}, {14, 10}, {106, 1}
                        });
                break;
            case 2142: // Gogeta SSJ4
                configureSantaCostume(itemShop, 861, 100_000,
                        new int[][] {
                                {77, 25}, {103, 30}, {50, 35},
                                {101, 40}, {5, 25}, {14, 10}, {106, 0}
                        });
                break;
            case 2134: // Gohan
                configureSantaCostume(itemShop, 861, 50_000,
                        new int[][] {
                                {77, 26}, {103, 26}, {50, 26},
                                {101, 25}, {5, 20}, {14, 5}, {106, 0}
                        });
                break;
            case 2128: // Goten
                configureSantaCostume(itemShop, 861, 25_000,
                        new int[][] {
                                {77, 24}, {103, 24}, {50, 24},
                                {101, 20}, {106, 0}
                        });
                break;
            case 2117: // Goku Ultra
                configureSantaCostume(itemShop, 457, 3_000,
                        new int[][] {
                                {77, 22}, {103, 22}, {50, 22},
                                {101, 15}, {106, 0}
                        });
                break;
            default:
                break;
        }
    }

    private static void configureSantaCostume(ItemShop itemShop, int itemExchange,
            int costSpec, int[][] configuredOptions) {
        itemShop.itemExchange = itemExchange;
        itemShop.costSpec = costSpec;

        if (ItemService.gI().getTemplate(itemExchange) != null) {
            itemShop.iconSpec = ItemService.gI().getTemplate(itemExchange).iconID;
        }

        // Chỉ thay các option thuộc bộ chỉ số cải trang; giữ nguyên option khác nếu có.
        itemShop.options.removeIf(option -> option != null
                && option.optionTemplate != null
                && isSantaCostumeStatOption(option.optionTemplate.id));

        for (int[] option : configuredOptions) {
            itemShop.options.add(new ItemOption(option[0], option[1]));
        }
    }

    private static boolean isSantaCostumeStatOption(int optionId) {
        return optionId == 77   // HP %
                || optionId == 103 // KI %
                || optionId == 50  // Sức đánh %
                || optionId == 101 // TN/SM %
                || optionId == 5   // Sức đánh chí mạng %
                || optionId == 14  // Chí mạng %
                || optionId == 106; // Không ảnh hưởng bởi cái lạnh
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

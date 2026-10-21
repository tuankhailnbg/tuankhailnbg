package nro.jdbc.daos;

import nro.jdbc.DBService;
import nro.models.item.Item;
import nro.models.item.ItemOption;
import nro.models.player.Player;
import nro.utils.Log;
import nro.utils.TimeUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Level;

/**
 *
 * @author 💖 Nro Yanii 💖
 * 
 *
 */
public class HistoryTransactionDAO {

    public static void insert(Player player1, Player player2,
            int goldP1, int goldP2, List<Item> itemP1, List<Item> itemP2,
            List<Item> bag1Before, List<Item> bag2Before,
            List<Item> bag1After,
            List<Item> bag2After,
            long gold1Before, long gold2Before, long gold1After, long gold2After) {
        PreparedStatement ps = null;
        try (Connection con = DBService.gI().getConnectionForSaveHistory();) {
            String itemsTrade1 = "Gold: " + goldP1;
            String itemsTrade2 = "Gold: " + goldP2;
            for (Item item : itemP1) {
                itemsTrade1 += ", " + item.template.name + " (x" + item.quantity + ")";
            }
            for (Item item : itemP2) {
                itemsTrade2 += ", " + item.template.name + " (x" + item.quantity + ")";
            }

            String itemsBefore1 = "Gold: " + gold1Before + ", ";
            String itemsBefore2 = "Gold: " + gold2Before + ", ";
            for (Item item : bag1After) {
                if (item.isNotNullItem()) {
                    String info = item.template.name;
                    String option = "[";
                    for (ItemOption io : item.itemOptions) {
                        option += io.optionTemplate.name.replaceAll("#", io.param + "") + ",";
                    }
                    option = option.substring(0, option.length() - 1) + "]";
                    info += (" " + option + " (x" + item.quantity + "); ");
                    itemsBefore1 += info;
                }
            }
            for (Item item : bag2Before) {
                if (item.isNotNullItem()) {
                    String info = item.template.name;
                    String option = "[";
                    for (ItemOption io : item.itemOptions) {
                        option += io.optionTemplate.name.replaceAll("#", io.param + "") + ",";
                    }
                    option = option.substring(0, option.length() - 1) + "]";
                    info += (" " + option + " (x" + item.quantity + "); ");
                    itemsBefore2 += info;
                }
            }

            String itemsAfter1 = "Gold: " + gold1Before + ", ";
            String itemsAfter2 = "Gold: " + gold2Before + ", ";
            for (Item item : bag1After) {
                if (item.isNotNullItem()) {
                    String info = item.template.name;
                    String option = "[";
                    for (ItemOption io : item.itemOptions) {
                        option += io.optionTemplate.name.replaceAll("#", io.param + "") + ",";
                    }
                    option = option.substring(0, option.length() - 1) + "]";
                    info += (" " + option + " (x" + item.quantity + "); ");
                    itemsAfter1 += info;
                }
            }
            for (Item item : bag2After) {
                if (item.isNotNullItem()) {
                    String info = item.template.name;
                    String option = "[";
                    for (ItemOption io : item.itemOptions) {
                        option += io.optionTemplate.name.replaceAll("#", io.param + "") + ",";
                    }
                    option = option.substring(0, option.length() - 1) + "]";
                    info += (" " + option + " (x" + item.quantity + "); ");
                    itemsAfter2 += info;
                }
            }
            ps = con.prepareStatement("insert into history_transaction values (?,?,?,?,?,?,?,?,?)");
            ps.setString(1, player1.name + " (" + player1.id + ")");
            ps.setString(2, player2.name + " (" + player2.id + ")");
            ps.setString(3, itemsTrade1);
            ps.setString(4, itemsTrade2);
            ps.setString(5, itemsBefore1);
            ps.setString(6, itemsBefore2);
            ps.setString(7, itemsAfter1);
            ps.setString(8, itemsAfter2);
            ps.setTimestamp(9, new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
        } catch (Exception e) {
            Log.error(HistoryTransactionDAO.class, e);
        } finally {
            try {
                ps.close();
            } catch (SQLException ex) {
                java.util.logging.Logger.getLogger(HistoryTransactionDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void deleteHistory() {
        PreparedStatement ps = null;
        try (Connection con = DBService.gI().getConnectionForAutoSave();) {
            ps = con.prepareStatement("delete from history_transaction where time_tran < '"
                    + TimeUtil.getTimeBeforeCurrent(3 * 24 * 60 * 60 * 1000, "yyyy-MM-dd") + "'");
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                ps.close();
            } catch (SQLException ex) {
                // java.util.logging.Logger.getLogger(HistoryTransactionDAO.class.getName()).log(Level.SEVERE,
                // null, ex);
            }
        }
    }

}

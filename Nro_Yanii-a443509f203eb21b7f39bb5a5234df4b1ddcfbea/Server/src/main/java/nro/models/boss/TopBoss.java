package nro.models.boss;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import nro.jdbc.DBService;
import nro.jdbc.daos.PlayerDAO;
import nro.models.player.Player;
import nro.services.Service;
import nro.utils.Log;

/**
 *
 * @author Nro Yanii
 */
public class TopBoss {

    public static void update(Player pl) {
        //Luồng
        ExecutorService executor = Executors.newFixedThreadPool(1);
        //Cộng điểm
        try (
                 Connection con = DBService.gI().getConnectionForSaveData();  PreparedStatement ps = con.prepareStatement("UPDATE account SET TopBoss = TopBoss + 1 WHERE id = ?")) {
            ps.setInt(1, pl.getSession().userId); // ID của người chơi
            ps.executeUpdate();
            Service.getInstance().sendThongBao(pl, "Bạn nhận được 1 điểm giết boss ");
        } catch (Exception e) {
            Log.error(PlayerDAO.class, e, "Lỗi update top boss cho người chơi " + pl.name);
        }
         
    }
    public static void updatediemnoel(Player pl) {
        //Luồng
        ExecutorService executor = Executors.newFixedThreadPool(1);
        //Cộng điểm
        try (
                 Connection con = DBService.gI().getConnectionForSaveData();  PreparedStatement ps = con.prepareStatement("UPDATE account SET TopNoel = TopNoel + 1 WHERE id = ?")) {
            ps.setInt(1, pl.getSession().userId); // ID của người chơi
            ps.executeUpdate();
            Service.getInstance().sendThongBao(pl, "Bạn nhận được 1 điểm sự kiện noel ");
        } catch (Exception e) {
            Log.error(PlayerDAO.class, e, "Lỗi update top boss cho người chơi " + pl.name);
        }
    }
}

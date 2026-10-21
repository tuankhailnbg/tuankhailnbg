package nro.manager;

import nro.jdbc.DBService;
import nro.models.player.PetFollow;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author outcast c-cute hột me 😳
 */

public class PetFollowManager extends AbsManager<PetFollow> {

    private static final PetFollowManager INSTANCE = new PetFollowManager();

    public static PetFollowManager gI() {
        return INSTANCE;
    }

    public void load() {
        try {
            PreparedStatement ps = DBService.gI().getConnectionForGame().prepareStatement("SELECT * FROM pet_follow");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id_temp");
                int iconID = rs.getInt("icon");
                int w = rs.getInt("width");
                int h = rs.getInt("height");
                byte nFrame = rs.getByte("frame");
                add(new PetFollow(id, iconID, w, h, nFrame));
            }
            ps.close();
            rs.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PetFollow findByID(int id) {
        for (PetFollow pet : list) {
            if (pet.getId() == id) {
                return pet;
            }
        }
        return null;
    }
}

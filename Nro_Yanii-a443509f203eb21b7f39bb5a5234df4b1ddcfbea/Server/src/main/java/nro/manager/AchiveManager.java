package nro.manager;

import lombok.Getter;
import nro.jdbc.DBService;
import nro.models.task.AchivementTemplate;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author outcast c-cute hột me 😳
 */
public class AchiveManager implements IManager<AchivementTemplate> {

    private static final AchiveManager INSTANCE = new AchiveManager();

    public static AchiveManager getInstance() {
        return INSTANCE;
    }

    @Getter
    private List<AchivementTemplate> list = new ArrayList<>();

    public void load() {
        try {
            PreparedStatement ps = DBService.gI().getConnectionForGame().prepareStatement("SELECT * FROM `achivements`");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String detail = rs.getString("detail");
                int money = rs.getInt("money");
                int maxCount = rs.getInt("max_count");
                list.add(new AchivementTemplate(id,name,detail,money,maxCount));
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public AchivementTemplate findByID(int id) {
        for (AchivementTemplate template : list) {
            if (template.getId() == id) {
                return template;
            }
        }
        return null;
    }

    @Override
    public void add(AchivementTemplate achivementTemplate) {

    }

    @Override
    public void remove(AchivementTemplate achivementTemplate) {

    }
}
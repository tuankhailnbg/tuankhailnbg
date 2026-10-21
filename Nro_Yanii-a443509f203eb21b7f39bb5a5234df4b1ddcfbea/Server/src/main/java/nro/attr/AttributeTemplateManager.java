/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nro.attr;

import nro.jdbc.DBService;
import nro.utils.Log;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author 💖 Nro Yanii 💖
 */
public class AttributeTemplateManager {

    private static final AttributeTemplateManager instance = new AttributeTemplateManager();

    public static AttributeTemplateManager getInstance() {
        return instance;
    }

    private final List<AttributeTemplate> list = new ArrayList<>();

    public void load() {
        try {
            PreparedStatement ps = DBService.gI().getConnectionForGame()
                    .prepareStatement("SELECT * FROM `attribute_template`");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                AttributeTemplate at = AttributeTemplate.builder()
                        .id(id)
                        .name(name)
                        .build();
                add(at);
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            Log.error(AttributeTemplateManager.class, ex, "Load attribute template err");
        }
    }

    public void add(AttributeTemplate at) {
        list.add(at);
    }

    public void remove(AttributeTemplate at) {
        list.remove(at);
    }

    public AttributeTemplate find(int id) {
        for (AttributeTemplate at : list) {
            if (at.getId() == id) {
                return at;
            }
        }
        return null;
    }
}

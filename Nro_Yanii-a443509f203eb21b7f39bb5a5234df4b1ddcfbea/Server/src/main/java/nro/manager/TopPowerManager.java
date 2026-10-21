package nro.manager;

import lombok.Getter;
import nro.jdbc.DBService;
import nro.jdbc.daos.PlayerDAO;
import nro.models.item.Item;
import nro.models.item.ItemOption;
import nro.models.player.Player;
import nro.services.ItemService;
import nro.utils.Log;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author outcast c-cute hột me 😳
 */
public class TopPowerManager {

    @Getter
    private List<Player> list = new ArrayList<>();
    private static final TopPowerManager INSTANCE = new TopPowerManager();

    public static TopPowerManager getInstance() {
        return INSTANCE;
    }

    public void load() {
//        Log.success("load BXH Sức Mạnh success");
        list.clear();

        try (Connection con = DBService.gI().getConnectionForGetPlayer();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM player ORDER BY player.power DESC LIMIT 100");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Player player = processPlayerResultSet(rs);
                list.add(player);
            }

        } catch (SQLException e) {
            Log.error(TopPowerManager.class, e);
        }
    }

    private Player processPlayerResultSet(ResultSet rs) throws SQLException {
        Player player = new Player();

        player.id = rs.getInt("id");
        player.name = rs.getString("name");
        player.head = rs.getShort("head");
        player.gender = rs.getByte("gender");
        player.lastimelogin = rs.getTimestamp("lastimelogin");

        processPlayerDataPoint(rs.getString("data_point"), player);
        processPlayerItemsBody(rs.getString("items_body"), player);

        return player;
    }

    private void processPlayerDataPoint(String dataPoint, Player player) {
        JSONValue jv = new JSONValue();
        JSONArray dataArray = (JSONArray) jv.parse(dataPoint);
        player.nPoint.power = Long.parseLong(dataArray.get(11).toString());
        dataArray.clear();
    }

    private void processPlayerItemsBody(String itemsBody, Player player) {
        JSONValue jv = new JSONValue();
        JSONArray dataArray = (JSONArray) jv.parse(itemsBody);

        for (int i = 0; i < dataArray.size(); i++) {
            Item item = processItem(dataArray.get(i).toString());
            player.inventory.itemsBody.add(item);
        }

        dataArray.clear();
    }

    private Item processItem(String itemData) {
        JSONValue jv = new JSONValue();
        JSONObject dataObject = (JSONObject) jv.parse(itemData);
        short tempId = Short.parseShort(String.valueOf(dataObject.get("temp_id")));
        Item item;

        if (tempId != -1) {
            item = ItemService.gI().createNewItem(tempId, Integer.parseInt(String.valueOf(dataObject.get("quantity"))));
            JSONArray options = (JSONArray) jv.parse(String.valueOf(dataObject.get("option")).replaceAll("\"", ""));

            for (int j = 0; j < options.size(); j++) {
                JSONArray opt = (JSONArray) jv.parse(String.valueOf(options.get(j)));
                item.itemOptions.add(new ItemOption(Integer.parseInt(String.valueOf(opt.get(0))),
                        Integer.parseInt(String.valueOf(opt.get(1)))));
            }

            item.createTime = Long.parseLong(String.valueOf(dataObject.get("create_time")));

            if (ItemService.gI().isOutOfDateTime(item)) {
                item = ItemService.gI().createItemNull();
            }
        } else {
            item = ItemService.gI().createItemNull();
        }

        return item;
    }
}

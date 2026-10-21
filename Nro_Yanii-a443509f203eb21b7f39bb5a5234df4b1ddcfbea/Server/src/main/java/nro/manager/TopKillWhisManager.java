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
public class TopKillWhisManager {

    @Getter
    private List<Player> list = new ArrayList<>();
    private static final TopKillWhisManager INSTANCE = new TopKillWhisManager();

    public static TopKillWhisManager getInstance() {
        return INSTANCE;
    }

    public void load() {
    list.clear();

    try (Connection con = DBService.gI().getConnectionForGetPlayer();  
         PreparedStatement ps = con.prepareStatement("SELECT * FROM player WHERE player.levelKillWhis > 0 ORDER BY player.levelKillWhis DESC, player.timeKillWhis ASC LIMIT 100");
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            Player player = extractPlayerFromResultSet(rs);
            list.add(player);
        }

    } catch (SQLException e) {
        Log.error(TopKillWhisManager.class, e);
    }
}


    private Player extractPlayerFromResultSet(ResultSet rs) throws SQLException {
        Player player = new Player();

        player.id = rs.getInt("id");
        player.name = rs.getString("name");
        player.head = rs.getShort("head");
        player.gender = rs.getByte("gender");

        player.levelKillWhisDone = rs.getInt("levelKillWhis");
        player.timeKillWhis = rs.getLong("timeKillWhis");
        player.lastimelogin = rs.getTimestamp("lastimelogin");
        extractDataPoint(rs.getString("data_point"), player);
        extractItemsBody(rs.getString("items_body"), player);

        return player;
    }

    private void extractDataPoint(String dataPoint, Player player) {
        JSONValue jv = new JSONValue();
        JSONArray dataArray = (JSONArray) jv.parse(dataPoint);
        player.nPoint.power = Long.parseLong(dataArray.get(11).toString());
    }

    private void extractItemsBody(String itemsBody, Player player) {
        JSONValue jv = new JSONValue();
        JSONArray dataArray = (JSONArray) jv.parse(itemsBody);

        for (Object itemDataObject : dataArray) {
            Item item = createItemFromDataObject(itemDataObject.toString());
            player.inventory.itemsBody.add(item);
        }
    }

    private Item createItemFromDataObject(String itemData) {
        JSONValue jv = new JSONValue();
        JSONObject dataObject = (JSONObject) jv.parse(itemData);
        short tempId = Short.parseShort(String.valueOf(dataObject.get("temp_id")));
        Item item;

        if (tempId != -1) {
            item = ItemService.gI().createNewItem(tempId, Integer.parseInt(String.valueOf(dataObject.get("quantity"))));
            JSONArray options = (JSONArray) jv.parse(String.valueOf(dataObject.get("option")).replaceAll("\"", ""));

            for (Object option : options) {
                JSONArray opt = (JSONArray) jv.parse(String.valueOf(option));
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

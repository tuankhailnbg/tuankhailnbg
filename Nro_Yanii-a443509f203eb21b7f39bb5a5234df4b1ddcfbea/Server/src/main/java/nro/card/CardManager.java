/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nro.card;

import nro.jdbc.DBService;
import nro.models.item.ItemOption;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author 💖 Nro Yanii 💖
 */
public class CardManager {

    private static final CardManager instance = new CardManager();

    public static CardManager getInstance() {
        return instance;
    }

    @Getter
    private final List<CardTemplate> cardTemplates = new ArrayList<>();

    public void load() {
        try {
            PreparedStatement ps = DBService.gI().getConnectionForGame()
                    .prepareStatement("SELECT * FROM `collection_book`");
            ResultSet rs = ps.executeQuery();
            try {
                while (rs.next()) {
                    int id = rs.getShort("id");
                    int itemID = rs.getShort("item_id");
                    String name = rs.getString("name");
                    String info = rs.getString("info");
                    byte maxAmount = rs.getByte("max_amount");
                    short icon = rs.getShort("icon");
                    byte rank = rs.getByte("rank");
                    byte type = rs.getByte("type");
                    short mobID = rs.getShort("mob_id");
                    short head = rs.getShort("head");
                    short body = rs.getShort("body");
                    short leg = rs.getShort("leg");
                    short bag = rs.getShort("bag");
                    short aura = rs.getShort("aura");
                    ArrayList<ItemOption> options = new ArrayList<>();
                    JSONArray jArr = new JSONArray(rs.getString("options"));
                    for (int i = 0; i < jArr.length(); i++) {
                        JSONObject obj = jArr.getJSONObject(i);
                        int oID = obj.getInt("id");
                        int oParam = obj.getInt("param");
                        int active_card = obj.getInt("active_card");
                        ItemOption itemOption = new ItemOption(oID, oParam);
                        itemOption.activeCard = (byte) active_card;
                        options.add(itemOption);
                    }
                    CardTemplate card = CardTemplate.builder()
                            .id(id)
                            .name(name)
                            .itemID(itemID)
                            .info(info)
                            .maxAmount(maxAmount)
                            .icon(icon)
                            .rank(rank)
                            .type(type)
                            .mobID(mobID)
                            .head(head)
                            .body(body)
                            .leg(leg)
                            .bag(bag)
                            .aura(aura)
                            .options(options)
                            .build();
                    add(card);

                }
            } finally {
                rs.close();
                ps.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void add(CardTemplate cardTemplate) {
        cardTemplates.add(cardTemplate);
    }

    public void remove(CardTemplate cardTemplate) {
        cardTemplates.add(cardTemplate);
    }

    public CardTemplate find(int id) {
        for (CardTemplate card : cardTemplates) {
            if (card.getId() == id) {
                return card;
            }
        }
        return null;
    }
}

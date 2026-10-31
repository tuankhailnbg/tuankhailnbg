package nro.models.item;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import nro.manager.TopRaceManager;
import nro.services.ItemService;
import org.junit.Test;
import static org.junit.Assert.*;

public class TrainingArmorLevel4Test {

    @Test
    public void levelFourMatchesAndUsesFortyPercent() {
        ItemTemplate template = new ItemTemplate();
        template.id = 2154;
        template.type = 32;
        template.name = TrainingArmorLevel4.NAME;

        Item armor = new Item();
        armor.template = template;
        assertTrue(TrainingArmorLevel4.matches(template));
        assertTrue(ItemService.gI().isTrainArmor(armor));
        assertEquals(40, ItemService.gI().getPercentTrainArmor(armor));

        template.type = 27;
        assertFalse(TrainingArmorLevel4.matches(template));
        assertEquals(0, ItemService.gI().getPercentTrainArmor(armor));
    }

    @Test
    public void onlyTopThreeOfEachOfFourRacesGetArmor() {
        for (String type : new String[]{
                TopRaceManager.POWER, TopRaceManager.TASK,
                TopRaceManager.PET, TopRaceManager.NAP}) {
            for (int rank = 1; rank <= 3; rank++) {
                assertTrue(type + " rank " + rank,
                        TopRaceManager.awardsTrainingArmorLevel4(type, rank));
            }
            assertFalse(TopRaceManager.awardsTrainingArmorLevel4(type, 4));
        }
        assertFalse(TopRaceManager.awardsTrainingArmorLevel4("UNKNOWN", 1));
        assertFalse(TopRaceManager.awardsTrainingArmorLevel4(TopRaceManager.NAP, 0));
    }

    @Test
    public void databaseSeedIsIdempotentAndDoesNotOverwriteExistingTemplates() throws Exception {
        try (Connection con = DriverManager.getConnection(
                "jdbc:h2:mem:nro_armor4_test;MODE=MySQL;DB_CLOSE_DELAY=-1")) {
            try (Statement statement = con.createStatement()) {
                statement.execute("CREATE TABLE item_template("
                        + "id INT PRIMARY KEY, type TINYINT, gender TINYINT,"
                        + " name VARCHAR(100), description VARCHAR(255),"
                        + " icon_id INT, part INT, is_up_to_up BOOLEAN, power_require INT)");
                statement.execute("INSERT INTO item_template VALUES"
                        + "(531,32,3,'Giáp tập luyện cấp 3','Cấp 3',4431,-1,FALSE,15000000)");
            }
            TrainingArmorLevel4.ensureDatabaseTemplate(con);
            TrainingArmorLevel4.ensureDatabaseTemplate(con);

            try (Statement statement = con.createStatement();
                    ResultSet result = statement.executeQuery(
                            "SELECT id,type,icon_id,power_require,name,description"
                            + " FROM item_template WHERE id=532")) {
                assertTrue(result.next());
                assertEquals(32, result.getInt("type"));
                assertEquals(4431, result.getInt("icon_id"));
                assertEquals(150_000_000, result.getInt("power_require"));
                assertEquals(TrainingArmorLevel4.NAME, result.getString("name"));
                assertTrue(result.getString("description").contains("40%"));
            }
            try (Statement statement = con.createStatement();
                    ResultSet result = statement.executeQuery(
                            "SELECT COUNT(*) FROM item_template")) {
                assertTrue(result.next());
                assertEquals(2, result.getInt(1));
            }
        }
    }
}

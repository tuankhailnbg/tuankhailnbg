package nro.models.item;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import nro.server.Manager;

/**
 * Giap tap luyen cap 4: -40% damage when equipped, +40% after removal.
 *
 * Keep the item template's ID database-assigned because private NRO databases
 * contain differing item ranges. Existing item IDs are never overwritten.
 */
public final class TrainingArmorLevel4 {
    public static final String NAME = "Giáp tập luyện cấp 4";
    public static final String ALTERNATE_NAME = "Giáp luyện tập cấp 4";
    public static final String DESCRIPTION =
            "Khi mặc vào sẽ đánh yếu hơn bình thường 40%, khi cởi ra sẽ tăng sức đánh 40%";

    private TrainingArmorLevel4() {
    }

    public static boolean matches(ItemTemplate template) {
        return template != null
                && template.type == 32
                && (NAME.equalsIgnoreCase(template.name)
                    || ALTERNATE_NAME.equalsIgnoreCase(template.name));
    }

    public static int findTemplateId() {
        for (ItemTemplate item : Manager.ITEM_TEMPLATES) {
            if (matches(item)) {
                return item.id;
            }
        }
        return -1;
    }

    /**
     * Seed only when not already installed. Must run before Manager loads item
     * templates so the client receives the full table and the correct new ID.
     *
     * The level-3 icon is used as a compatible placeholder until a verified
     * level-4 client icon is available; this does not change gameplay stats.
     */
    public static void ensureDatabaseTemplate(Connection connection) throws SQLException {
        try (PreparedStatement find = connection.prepareStatement(
                "SELECT id FROM item_template WHERE type=32 AND "
                + "(name=? OR name=?) LIMIT 1")) {
            find.setString(1, NAME);
            find.setString(2, ALTERNATE_NAME);
            try (ResultSet existing = find.executeQuery()) {
                if (existing.next()) {
                    return;
                }
            }
        }

        int nextId;
        try (PreparedStatement max = connection.prepareStatement(
                "SELECT COALESCE(MAX(id), -1) + 1 FROM item_template");
                ResultSet rs = max.executeQuery()) {
            if (!rs.next()) {
                throw new SQLException("Cannot allocate training armor level 4 template ID");
            }
            nextId = rs.getInt(1);
        }
        if (nextId < 1 || nextId > Short.MAX_VALUE) {
            throw new SQLException("Training armor level 4 ID out of client range: " + nextId);
        }

        int icon = 4431;
        try (PreparedStatement iconQuery = connection.prepareStatement(
                "SELECT icon_id FROM item_template WHERE id=531 LIMIT 1");
                ResultSet rs = iconQuery.executeQuery()) {
            if (rs.next()) {
                icon = rs.getInt(1);
            }
        }

        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO item_template"
                + "(id,type,gender,name,description,icon_id,part,is_up_to_up,power_require)"
                + " VALUES (?,32,3,?,?,?,?,0,?)")) {
            insert.setInt(1, nextId);
            insert.setString(2, NAME);
            insert.setString(3, DESCRIPTION);
            insert.setInt(4, icon);
            insert.setInt(5, -1);
            insert.setInt(6, 150_000_000);
            insert.executeUpdate();
        }
        System.out.println("[ITEM] Created " + NAME + " ID=" + nextId
                + " (using compatible level-3 icon as placeholder)");
    }
}

package nro.data;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import nro.models.item.ItemOptionTemplate;
import nro.models.item.ItemTemplate;
import nro.server.Manager;
import nro.server.io.Message;
import nro.server.io.Session;
import nro.utils.Log;

/**
 * Item catalog wire format used by the PC 2.4.x client.
 *
 * The client handles command -28 / subcommand 8 with two item subtypes:
 *   type 0: item option templates
 *   type 1: item templates
 *
 * For a large type-1 catalog, that same client expects command 12, subcommand 0,
 * whose payload then contains the normal [vsItem][type=1] item stream. Command
 * 12 uses the 3-byte frame length in Session_ME, so the whole catalog can be
 * delivered atomically without inventing unsupported type=2 continuation
 * packets.
 */
public class ItemData {

    public static void updateItem(Session session) {
        session.traceLogin("ITEM_SYNC_START");
        Message options = null;
        Message templates = null;
        try {
            options = createOptionPacket();
            templates = createTemplatePacket();

            // The client stores NRitem0 first, then command 12/type 1 stores the
            // complete NRitem1 catalog and only then calls readOk().
            session.doSendMessage(options);
            session.doSendMessage(templates);
            session.traceLogin("ITEM_SYNC_SENT");
        } catch (IOException | RuntimeException e) {
            Log.error(ItemData.class, e, "Lỗi gửi dữ liệu vật phẩm cho client");
        } finally {
            if (options != null) {
                options.cleanup();
            }
            if (templates != null) {
                templates.cleanup();
            }
        }
    }

    static Message createOptionPacket() throws IOException {
        if (Manager.ITEM_OPTION_TEMPLATES.size() > Short.MAX_VALUE) {
            throw new IOException("Item option count exceeds client protocol");
        }

        Message packet = new Message(-28);
        packet.writer().writeByte(8);
        packet.writer().writeByte(DataGame.vsItem);
        packet.writer().writeByte(0);

        // MOD_DP / NRO PC loadItemNew(type 0) calls readShort(), not readByte().
        packet.writer().writeShort(Manager.ITEM_OPTION_TEMPLATES.size());
        for (ItemOptionTemplate option : Manager.ITEM_OPTION_TEMPLATES) {
            packet.writer().writeUTF(option.name == null ? "" : option.name);
            packet.writer().writeByte(option.type);
        }
        return packet;
    }

    static Message createTemplatePacket() throws IOException {
        if (Manager.ITEM_TEMPLATES.size() > Short.MAX_VALUE) {
            throw new IOException("Item count exceeds client protocol");
        }

        ByteArrayOutputStream records = new ByteArrayOutputStream();
        for (int index = 0; index < Manager.ITEM_TEMPLATES.size(); index++) {
            ItemTemplate item = Manager.ITEM_TEMPLATES.get(index);
            if (item == null) {
                throw new IOException("Null item template at catalog index " + index);
            }
            if (item.id != index) {
                // The PC client reconstructs IDs from loop index k; sending a
                // sparse/non-contiguous catalog would make every later ID wrong.
                throw new IOException("Non-contiguous item catalog: index=" + index + ", id=" + item.id);
            }
            records.write(encodeTemplate(item));
        }

        Message packet = new Message(12);
        packet.writer().writeByte(0); // read_cmdExtraBig subcommand
        packet.writer().writeByte(DataGame.vsItem);
        packet.writer().writeByte(1); // loadItemNew type 1
        packet.writer().writeShort(Manager.ITEM_TEMPLATES.size());
        packet.writer().write(records.toByteArray());
        return packet;
    }

    private static byte[] encodeTemplate(ItemTemplate item) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        out.writeByte(item.type);
        out.writeByte(item.gender);
        out.writeUTF(item.name == null ? "" : item.name);
        out.writeUTF(item.description == null ? "" : item.description);
        out.writeByte(item.level);
        out.writeInt(item.strRequire);
        out.writeShort(item.iconID);
        out.writeShort(item.part);
        out.writeBoolean(item.isUpToUp);
        out.flush();
        return bytes.toByteArray();
    }
}

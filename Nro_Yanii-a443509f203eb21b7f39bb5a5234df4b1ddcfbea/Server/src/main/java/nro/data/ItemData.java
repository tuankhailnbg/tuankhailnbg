package nro.data;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import nro.models.item.ItemOptionTemplate;
import nro.models.item.ItemTemplate;
import nro.server.Manager;
import nro.server.io.Message;
import nro.server.io.Session;
import nro.utils.Log;

public class ItemData {
    // Command -28 uses a two-byte length. Stay below the signed-short limit too,
    // for older clients. Split by encoded bytes: descriptions and UTF-8 vary.
    static final int MAX_TEMPLATE_PACKET_BYTES = 30000;

    public static void updateItem(Session session) {
        session.traceLogin("ITEM_SYNC_START");
        try {
            // Finish serialization before sending so malformed records cannot
            // leave the client with only half of a new catalog.
            List<Message> packets = createUpdatePackets();
            for (Message packet : packets) {
                session.doSendMessage(packet);
                packet.cleanup();
            }
            session.traceLogin("ITEM_SYNC_SENT");
        } catch (IOException | RuntimeException e) {
            Log.error(ItemData.class, e, "Lỗi gửi dữ liệu vật phẩm cho client");
        }
    }

    static List<Message> createUpdatePackets() throws IOException {
        List<Message> packets = new ArrayList<>();
        if (Manager.ITEM_OPTION_TEMPLATES.size() > 255)
            throw new IOException("Item option count exceeds client protocol");
        if (Manager.ITEM_TEMPLATES.size() > Short.MAX_VALUE)
            throw new IOException("Item count exceeds client protocol");

        Message options = header(0);
        options.writer().writeByte(Manager.ITEM_OPTION_TEMPLATES.size());
        for (ItemOptionTemplate option : Manager.ITEM_OPTION_TEMPLATES) {
            options.writer().writeUTF(option.name);
            options.writer().writeByte(option.type);
        }
        if (options.getData().length > MAX_TEMPLATE_PACKET_BYTES)
            throw new IOException("Item option packet exceeds client protocol");
        packets.add(options);

        int start = 0;
        do {
            boolean first = start == 0;
            int end = start;
            ByteArrayOutputStream records = new ByteArrayOutputStream();
            int headerSize = first ? 5 : 7;
            while (end < Manager.ITEM_TEMPLATES.size()) {
                byte[] record = encodeTemplate(Manager.ITEM_TEMPLATES.get(end));
                if (record.length + headerSize > MAX_TEMPLATE_PACKET_BYTES)
                    throw new IOException("Item template too large: " + end);
                if (records.size() + record.length + headerSize > MAX_TEMPLATE_PACKET_BYTES) break;
                records.write(record);
                end++;
            }
            Message packet = header(first ? 1 : 2);
            if (first) packet.writer().writeShort(end);
            else {
                packet.writer().writeShort(start);
                packet.writer().writeShort(end);
            }
            packet.writer().write(records.toByteArray());
            packets.add(packet);
            start = end;
        } while (start < Manager.ITEM_TEMPLATES.size());
        return packets;
    }

    private static Message header(int mode) throws IOException {
        Message packet = new Message(-28);
        packet.writer().writeByte(8);
        packet.writer().writeByte(DataGame.vsItem);
        packet.writer().writeByte(mode);
        return packet;
    }

    private static byte[] encodeTemplate(ItemTemplate item) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        out.writeByte(item.type);
        out.writeByte(item.gender);
        out.writeUTF(item.name);
        out.writeUTF(item.description);
        out.writeByte(item.level);
        out.writeInt(item.strRequire);
        out.writeShort(item.iconID);
        out.writeShort(item.part);
        out.writeBoolean(item.isUpToUp);
        return bytes.toByteArray();
    }
}

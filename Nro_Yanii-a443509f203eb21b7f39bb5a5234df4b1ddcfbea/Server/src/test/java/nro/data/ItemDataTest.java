package nro.data;

import java.io.*;
import java.util.*;
import nro.models.item.*;
import nro.server.Manager;
import nro.server.io.Message;
import org.junit.*;
import static org.junit.Assert.*;

public class ItemDataTest {
    private List<ItemTemplate> oldItems;
    private List<ItemOptionTemplate> oldOptions;

    @Before
    public void saveCatalog() {
        oldItems = new ArrayList<>(Manager.ITEM_TEMPLATES);
        oldOptions = new ArrayList<>(Manager.ITEM_OPTION_TEMPLATES);
        Manager.ITEM_TEMPLATES.clear();
        Manager.ITEM_OPTION_TEMPLATES.clear();

        ItemOptionTemplate option = new ItemOptionTemplate();
        option.name = "Giảm #% HP, KI và sức đánh";
        option.type = 0;
        Manager.ITEM_OPTION_TEMPLATES.add(option);
    }

    @After
    public void restoreCatalog() {
        Manager.ITEM_TEMPLATES.clear();
        Manager.ITEM_TEMPLATES.addAll(oldItems);
        Manager.ITEM_OPTION_TEMPLATES.clear();
        Manager.ITEM_OPTION_TEMPLATES.addAll(oldOptions);
    }

    private void addItems(int count) {
        for (int id = 0; id < count; id++) {
            ItemTemplate t = new ItemTemplate();
            t.id = (short) id;
            t.type = 27;
            t.gender = 3;
            t.name = "Vật phẩm " + id;
            t.description = "Tri Ân Sư Phụ: sách, bút, cải trang, sức mạnh và tiềm năng. ".repeat(4);
            t.level = 1;
            t.strRequire = id;
            t.iconID = (short) (id + 1);
            t.part = -1;
            Manager.ITEM_TEMPLATES.add(t);
        }
    }

    @Test
    public void optionPacketMatchesModDpTypeZeroParser() throws Exception {
        for (int i = 1; i < 300; i++) {
            ItemOptionTemplate option = new ItemOptionTemplate();
            option.name = "Option " + i;
            option.type = (byte) (i % 4);
            Manager.ITEM_OPTION_TEMPLATES.add(option);
        }

        Message packet = ItemData.createOptionPacket();
        assertEquals(-28, packet.command);

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(packet.getData()));
        assertEquals(8, in.readUnsignedByte());
        assertEquals(DataGame.vsItem, in.readUnsignedByte());
        assertEquals(0, in.readUnsignedByte());
        assertEquals(300, in.readUnsignedShort());

        for (ItemOptionTemplate option : Manager.ITEM_OPTION_TEMPLATES) {
            assertEquals(option.name, in.readUTF());
            assertEquals(option.type, in.readByte());
        }
        assertEquals(0, in.available());
    }

    @Test
    public void largeCatalogUsesCommand12AndRoundTripsAsOneTypeOneStream() throws Exception {
        addItems(2421);

        Message packet = ItemData.createTemplatePacket();
        assertEquals(12, packet.command);

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(packet.getData()));
        assertEquals(0, in.readUnsignedByte()); // read_cmdExtraBig subcommand
        assertEquals(DataGame.vsItem, in.readUnsignedByte());
        assertEquals(1, in.readUnsignedByte()); // loadItemNew type 1
        assertEquals(2421, in.readUnsignedShort());

        for (int id = 0; id < 2421; id++) {
            ItemTemplate t = Manager.ITEM_TEMPLATES.get(id);
            assertEquals(t.type, in.readByte());
            assertEquals(t.gender, in.readByte());
            assertEquals(t.name, in.readUTF());
            assertEquals(t.description, in.readUTF());
            assertEquals(t.level, in.readByte());
            assertEquals(t.strRequire, in.readInt());
            assertEquals(t.iconID, in.readShort());
            assertEquals(t.part, in.readShort());
            assertEquals(t.isUpToUp, in.readBoolean());
        }
        assertEquals(0, in.available());
        assertTrue("Fixture should prove why a 3-byte frame is required",
                packet.getData().length > 0xFFFF);
        assertTrue(packet.getData().length <= 0xFFFFFF);
    }

    @Test
    public void emptyCatalogStillSendsCompleteTypeOneReset() throws Exception {
        Message packet = ItemData.createTemplatePacket();
        assertEquals(12, packet.command);
        assertArrayEquals(new byte[] {0, DataGame.vsItem, 1, 0, 0}, packet.getData());
    }

    @Test
    public void sparseCatalogIsRejectedBecauseClientRebuildsIdsFromLoopIndex() throws Exception {
        addItems(2);
        Manager.ITEM_TEMPLATES.get(1).id = 5;
        try {
            ItemData.createTemplatePacket();
            fail("Sparse catalog must not be serialized");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("Non-contiguous"));
        }
    }
}

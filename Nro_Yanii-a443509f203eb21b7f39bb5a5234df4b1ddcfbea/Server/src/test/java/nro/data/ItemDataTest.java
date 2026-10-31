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
    @Before public void saveCatalog() {
        oldItems = new ArrayList<>(Manager.ITEM_TEMPLATES);
        oldOptions = new ArrayList<>(Manager.ITEM_OPTION_TEMPLATES);
        Manager.ITEM_TEMPLATES.clear(); Manager.ITEM_OPTION_TEMPLATES.clear();
        ItemOptionTemplate option = new ItemOptionTemplate(); option.name = "Giảm #% HP, KI và sức đánh";
        Manager.ITEM_OPTION_TEMPLATES.add(option);
    }
    @After public void restoreCatalog() {
        Manager.ITEM_TEMPLATES.clear(); Manager.ITEM_TEMPLATES.addAll(oldItems);
        Manager.ITEM_OPTION_TEMPLATES.clear(); Manager.ITEM_OPTION_TEMPLATES.addAll(oldOptions);
    }
    private void addItems(int count) {
        for (int id=0; id<count; id++) {
            ItemTemplate t = new ItemTemplate(); t.id=(short)id; t.type=27; t.gender=3;
            t.name="Vật phẩm " + id;
            t.description="Tri Ân Sư Phụ: sách, bút, cải trang, sức mạnh và tiềm năng. ".repeat(4);
            t.level=1; t.strRequire=id; t.iconID=(short)(id+1); t.part=-1;
            Manager.ITEM_TEMPLATES.add(t);
        }
    }
    @Test public void largeUtfCatalogRoundTripsThroughBoundedContiguousPackets() throws Exception {
        addItems(2421);
        List<Message> packets=ItemData.createUpdatePackets();
        assertTrue(packets.size()>3);
        int next=0;
        for (int packetNo=0; packetNo<packets.size(); packetNo++) {
            Message packet=packets.get(packetNo);
            assertEquals(-28,packet.command);
            assertTrue(packet.getData().length<=ItemData.MAX_TEMPLATE_PACKET_BYTES);
            DataInputStream in=new DataInputStream(new ByteArrayInputStream(packet.getData()));
            assertEquals(8,in.readUnsignedByte()); assertEquals(DataGame.vsItem,in.readUnsignedByte());
            int mode=in.readUnsignedByte();
            if (packetNo==0) {
                assertEquals(0,mode); assertEquals(1,in.readUnsignedByte());
                assertEquals(Manager.ITEM_OPTION_TEMPLATES.get(0).name,in.readUTF()); in.readByte();
            } else {
                int end;
                if (packetNo==1) { assertEquals(1,mode); end=in.readUnsignedShort(); }
                else { assertEquals(2,mode); assertEquals(next,in.readUnsignedShort()); end=in.readUnsignedShort(); }
                assertTrue(end>next);
                for (;next<end;next++) {
                    ItemTemplate t=Manager.ITEM_TEMPLATES.get(next);
                    assertEquals(t.type,in.readByte()); assertEquals(t.gender,in.readByte());
                    assertEquals(t.name,in.readUTF()); assertEquals(t.description,in.readUTF());
                    assertEquals(t.level,in.readByte()); assertEquals(t.strRequire,in.readInt());
                    assertEquals(t.iconID,in.readShort()); assertEquals(t.part,in.readShort());
                    assertEquals(t.isUpToUp,in.readBoolean());
                }
            }
            assertEquals("No trailing or truncated record",0,in.available());
        }
        assertEquals(2421,next);
    }
    @Test public void shortCatalogDoesNotAssume750Items() throws Exception {
        addItems(3);
        List<Message> packets=ItemData.createUpdatePackets();
        assertEquals(2,packets.size());
        DataInputStream in=new DataInputStream(new ByteArrayInputStream(packets.get(1).getData()));
        in.skipBytes(3); assertEquals(3,in.readUnsignedShort());
    }
    @Test public void emptyCatalogStillSendsReset() throws Exception {
        List<Message> packets=ItemData.createUpdatePackets();
        assertEquals(2,packets.size()); assertEquals(5,packets.get(1).getData().length);
    }
    @Test public void singleOversizedRecordFailsBeforeSendingAnyCatalog() throws Exception {
        addItems(1); Manager.ITEM_TEMPLATES.get(0).description="x".repeat(40000);
        try {ItemData.createUpdatePackets(); fail("Must reject oversized template");}
        catch(IOException expected) {assertTrue(expected.getMessage().contains("too large"));}
    }
}

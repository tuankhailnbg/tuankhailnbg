package nro.event.november;

import java.util.*;
import nro.models.item.*;
import nro.models.npc.NpcTemplate;
import nro.server.Manager;
import nro.services.ItemService;

/** Reuses installed client artwork; fixed IDs survive restarts and saved inventories. */
public final class NovemberCatalog {
    private NovemberCatalog() { }
    public static volatile boolean ready;
    private static final int[] BASE = {1304,1305,1306,1276,2129};
    private static final int[] TOP = {2142,904,985,1270,2045};
    public static synchronized void install() {
        if (ready) return;
        Map<Integer,ItemTemplate> existing = new HashMap<>();
        for (ItemTemplate t : Manager.ITEM_TEMPLATES) if (t != null) existing.put((int)t.id,t);
        int max=Math.max(2420,existing.keySet().stream().mapToInt(i->i).max().orElse(0));
        Manager.ITEM_TEMPLATES.clear();
        for (int id=0;id<=max;id++) {
            ItemTemplate t=existing.get(id);
            if (t==null) { t=new ItemTemplate(); t.id=(short)id; t.type=27; t.gender=3;
                t.name="Ô dự phòng "+id; t.description=""; t.iconID=1; t.part=-1; }
            Manager.ITEM_TEMPLATES.add(t);
        }
        material(NovemberRules.PEN,1282,"Bút tri ân");
        material(NovemberRules.PAPER,399,"Thiệp trắng");
        material(NovemberRules.BADGE,649,"Huy hiệu sư phụ");
        material(NovemberRules.CARD,1191,"Thiệp tri ân");
        material(NovemberRules.BOX,2149,"Hộp quà tri ân (tự ghép)");
        material(NovemberRules.DAILY_BOX,2149,"Hộp quà tri ân (thưởng ngày)");
        for (int i=0;i<5;i++) {
            costume(NovemberRules.COSTUME+i,BASE[i],NovemberRules.NAMES[i]);
            costume(NovemberRules.TOP_COSTUME+i,TOP[i],NovemberRules.NAMES[i]+" - Vinh Danh");
        }
        material(NovemberRules.TITLE,1293,"Danh hiệu Đệ Tử Xuất Sắc");
        ItemTemplate title=Manager.ITEM_TEMPLATES.get(NovemberRules.TITLE);
        title.description="Kỷ niệm Top Tri Ân Sư Phụ tháng 11/2026. Xem danh hiệu tại Sứ Giả Tri Ân.";
        while (Manager.ITEM_OPTION_TEMPLATES.size()<=NovemberRules.PENALTY_OPTION) {
            ItemOptionTemplate o=new ItemOptionTemplate(); o.id=Manager.ITEM_OPTION_TEMPLATES.size(); o.name="";
            Manager.ITEM_OPTION_TEMPLATES.add(o);
        }
        ItemOptionTemplate penalty=Manager.ITEM_OPTION_TEMPLATES.get(NovemberRules.PENALTY_OPTION);
        if (penalty!=null && penalty.name!=null && !penalty.name.isEmpty()
                && !penalty.name.equals("Giảm #% HP, KI và sức đánh")) throw new IllegalStateException("Trùng option 251");
        penalty=new ItemOptionTemplate(); penalty.id=251; penalty.name="Giảm #% HP, KI và sức đánh";
        Manager.ITEM_OPTION_TEMPLATES.set(251,penalty);
        if (Manager.NPC_TEMPLATES.size()>80 && !"Sứ Giả Tri Ân".equals(Manager.NPC_TEMPLATES.get(80).name))
            throw new IllegalStateException("Trùng NPC 80, cần kiểm tra trước khi bật sự kiện");
        if (Manager.NPC_TEMPLATES.size()!=80 && Manager.NPC_TEMPLATES.size()!=81)
            throw new IllegalStateException("Thiếu dữ liệu NPC gốc");
        NpcTemplate npc=new NpcTemplate(); npc.id=80; npc.name="Sứ Giả Tri Ân";
        NpcTemplate source=Manager.NPC_TEMPLATES.get(13); npc.head=source.head; npc.body=source.body; npc.leg=source.leg;
        if (Manager.NPC_TEMPLATES.size()==80) Manager.NPC_TEMPLATES.add(npc); else Manager.NPC_TEMPLATES.set(80,npc);
        ready=true;
    }
    private static void material(int id,int source,String name) {
        ItemTemplate original=Manager.ITEM_TEMPLATES.get(source);
        put(id,original,name,(byte)27);
    }
    private static void costume(int id,int source,String name) {
        ItemTemplate original=Manager.ITEM_TEMPLATES.get(source);
        put(id,original,name,(byte)5);
        CaiTrang skin=Manager.getCaiTrangByItemId(source);
        if (skin==null) throw new IllegalStateException("Thiếu hình cải trang nguồn "+source);
        Manager.CAI_TRANGS.removeIf(c->c.tempId==id);
        Manager.CAI_TRANGS.add(new CaiTrang(id,skin.id.clone()));
    }
    private static void put(int id,ItemTemplate source,String name,byte type) {
        ItemTemplate old=Manager.ITEM_TEMPLATES.get(id);
        if (!old.name.startsWith("Ô dự phòng ") && !name.equals(old.name))
            throw new IllegalStateException("Trùng ID sự kiện "+id+": "+old.name);
        ItemTemplate t=new ItemTemplate(); t.id=(short)id; t.type=type; t.gender=3;
        t.name=name; t.description="Tri Ân Sư Phụ tháng 11/2026. Gặp Sứ Giả Tri Ân ở đảo Kamê.";
        t.iconID=source.iconID; t.part=source.part; t.level=source.level;
        Manager.ITEM_TEMPLATES.set(id,t);
    }
    public static Item item(int id,int count) {
        Item item=ItemService.gI().createNewItem((short)id,count);
        item.itemOptions.add(new ItemOption(30,0)); item.itemOptions.add(new ItemOption(74,0));
        return item;
    }
    public static Item costume(int model,int days,boolean top) {
        if (model<0 || model>=5) throw new IllegalArgumentException("Cải trang không hợp lệ");
        Item item=item((top?NovemberRules.TOP_COSTUME:NovemberRules.COSTUME)+model,1);
        for (int[] option:NovemberRules.OPTIONS[model]) item.itemOptions.add(new ItemOption(option[0],option[1]));
        if (days>0) item.itemOptions.add(new ItemOption(93,days));
        return item;
    }
}

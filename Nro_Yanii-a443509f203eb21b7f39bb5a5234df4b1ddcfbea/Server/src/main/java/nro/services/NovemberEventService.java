package nro.services;

import java.sql.*;
import java.time.*;
import java.util.*;
import nro.consts.ConstMob;
import nro.event.november.*;
import nro.jdbc.DBService;
import nro.jdbc.daos.PlayerDAO;
import nro.models.boss.Boss;
import nro.models.item.Item;
import nro.models.item.ItemOption;
import nro.models.map.ItemMap;
import nro.models.mob.BigBoss;
import nro.models.mob.Mob;
import nro.models.player.Pet;
import nro.models.player.Player;
import nro.utils.Util;

/** Persistent November event. Reward inventory and event balances commit together. */
public final class NovemberEventService {
    @FunctionalInterface public interface Connections { Connection open() throws Exception; }
    @FunctionalInterface private interface Action { String run(NovemberState state,Connection con) throws Exception; }
    private static final NovemberEventService INSTANCE=new NovemberEventService(
            ()->DBService.gI().getConnection(),Clock.system(NovemberRules.ZONE));
    private final Connections connections;
    private final Clock clock;
    private volatile boolean initialized;
    private long lastError;
    private final java.util.concurrent.ConcurrentHashMap<Long,String> mobMilestone=new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Long,String> bossMilestone=new java.util.concurrent.ConcurrentHashMap<>();
    public NovemberEventService(Connections connections,Clock clock) {
        this.connections=connections; this.clock=clock;
    }
    public static NovemberEventService gI() { return INSTANCE; }
    public Clock clock() { return clock; }
    public synchronized void initialize() throws Exception {
        if (initialized) return;
        try (Connection c=connections.open(); Statement st=c.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS november2026_players (player_id BIGINT PRIMARY KEY,"
                    +"player_name VARCHAR(100) NOT NULL,score BIGINT NOT NULL DEFAULT 0,"
                    +"reached_at BIGINT NOT NULL DEFAULT 0,data LONGTEXT NOT NULL) ENGINE=InnoDB");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS november2026_meta (id INT PRIMARY KEY,settled INT NOT NULL) ENGINE=InnoDB");
            st.executeUpdate("INSERT IGNORE INTO november2026_meta(id,settled) VALUES(1,0)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS november2026_winners (player_id BIGINT PRIMARY KEY,"
                    +"rank_no INT NOT NULL,score BIGINT NOT NULL,player_name VARCHAR(100) NOT NULL) ENGINE=InnoDB");
        }
        initialized=true;
    }
    public NovemberRules.Phase phase() { return NovemberRules.phase(clock.instant()); }
    private void active() { if (phase()!=NovemberRules.Phase.ACTIVE) throw new IllegalArgumentException("Chỉ cày và tính điểm từ 01 đến hết 30/11/2026."); }
    private void exchange() {
        if (phase()!=NovemberRules.Phase.ACTIVE && phase()!=NovemberRules.Phase.EXCHANGE)
            throw new IllegalArgumentException("Đổi quà từ 01/11 đến hết 03/12/2026.");
    }
    private LocalDate today() { return LocalDate.now(clock.withZone(NovemberRules.ZONE)); }
    private Player owner(Player p) { return p!=null && p.isPet ? ((Pet)p).master : p; }

    public void addMobDrops(Player attacker,Mob mob,List<ItemMap> drops,int x,int y) {
        Player p=owner(attacker);
        if (!NovemberCatalog.ready || !initialized || phase()!=NovemberRules.Phase.ACTIVE
                || p==null || p.isBoss || p.isMiniPet || mob==null || mob.zone==null
                || mob instanceof BigBoss || mob.tempId==ConstMob.MOC_NHAN) return;
        recordKill(p,false);
        if (Util.isTrue(NovemberRules.MATERIAL_RATE,100)) drops.add(drop(mob.zone,NovemberRules.PEN,1,x,y,p.id));
        if (Util.isTrue(NovemberRules.MATERIAL_RATE,100)) drops.add(drop(mob.zone,NovemberRules.PAPER,1,x+12,y,p.id));
    }
    public void onBossDefeated(Boss boss,Player attacker) {
        Player p=owner(attacker);
        if (!NovemberCatalog.ready || !initialized || phase()!=NovemberRules.Phase.ACTIVE
                || p==null || p.isBoss || p.isMiniPet || boss.zone==null) return;
        recordKill(p,true);
        if (Util.isTrue(NovemberRules.BADGE_RATE,100)) {
            int x=boss.location.x, y=boss.zone.map.yPhysicInTop(x,boss.location.y-24);
            ItemMap item=drop(boss.zone,NovemberRules.BADGE,Util.nextInt(1,3),x,y,p.id);
            Service.getInstance().dropItemMap(boss.zone,item);
        }
    }
    private ItemMap drop(nro.models.map.Zone zone,int id,int count,int x,int y,long playerId) {
        ItemMap item=new ItemMap(zone,id,count,x,y,playerId);
        item.options.add(new ItemOption(30,0)); item.options.add(new ItemOption(74,0)); return item;
    }
    private void recordKill(Player p,boolean boss) {
        String date=today().toString();
        java.util.concurrent.ConcurrentHashMap<Long,String> completed=boss?bossMilestone:mobMilestone;
        if(date.equals(completed.get(p.id)))return;
        String result=transaction(p,false,(s,c)->{
            active();
            if (boss) s.bosses=Math.min(3,s.bosses+1); else s.kills=Math.min(500,s.kills+1);
            return (boss?s.bosses>=3:s.kills>=500)?"done":"";
        });
        if(result.equals("done"))completed.put(p.id,date);
        else if (!result.isEmpty()) error("Không lưu được tiến độ hạ quái/boss: "+result);
    }

    public String craftCards(Player p,int count) {
        return transaction(p,true,(s,c)->{ active(); NovemberRules.checkQuantity(count);
            take(p,NovemberRules.PEN,10*count); take(p,NovemberRules.PAPER,5*count);
            give(p,NovemberCatalog.item(NovemberRules.CARD,count)); return "Đã ghép "+count+" Thiệp tri ân.";
        });
    }
    public String donate(Player p,int count) {
        return transaction(p,true,(s,c)->{ active(); NovemberRules.checkQuantity(count);
            take(p,NovemberRules.CARD,count); s.coins=Math.addExact(s.coins,10L*count);
            s.score(10L*count,clock.millis()); s.gifts=(int)Math.min(5L,(long)s.gifts+count);
            return "Đã tặng "+count+" thiệp: +"+(10L*count)+" xu và điểm tri ân.";
        });
    }
    public String craftBoxes(Player p,int count) {
        return transaction(p,true,(s,c)->{ active(); NovemberRules.checkQuantity(count);
            take(p,NovemberRules.CARD,3*count); take(p,NovemberRules.BADGE,2*count);
            give(p,NovemberCatalog.item(NovemberRules.BOX,count));
            return "Đã ghép "+count+" hộp. Mở mỗi hộp tự ghép được 30 điểm, không nhận xu.";
        });
    }
    public String openBox(Player p,boolean daily) {
        return transaction(p,true,(s,c)->{ exchange();
            // After collection closes all score is frozen, including crafted boxes.
            if (!daily) active();
            take(p,daily?NovemberRules.DAILY_BOX:NovemberRules.BOX,1);
            int tier=NovemberRules.rewardTier(Util.nextInt(10000));
            Item reward;
            if (tier==0) reward=bound(Util.nextInt(381,384),3);
            else if (tier==1) reward=bound(Util.nextInt(220,224),5);
            else if (tier==2) reward=bound(Util.nextInt(1066,1070),5);
            else reward=NovemberCatalog.costume(Util.nextInt(0,4),tier==3?3:tier==4?7:0,false);
            give(p,reward);
            if (!daily) s.score(30,clock.millis());
            return "Nhận "+reward.quantity+" "+reward.template.name+(daily?" (hộp thưởng không cộng điểm)":" và 30 điểm tri ân.");
        });
    }
    public String buyCostume(Player p,int model,int duration) {
        return transaction(p,true,(s,c)->{ exchange(); int price=NovemberRules.price(duration);
            if (s.coins<price) throw new IllegalArgumentException("Cần "+price+" xu, hiện có "+s.coins+" xu.");
            Item item=NovemberCatalog.costume(model,NovemberRules.DAYS[duration],false);
            give(p,item); s.coins-=price;
            return "Đã đổi "+item.template.name+" - "+(duration==3?"vĩnh viễn":NovemberRules.DAYS[duration]+" ngày")+".";
        });
    }
    public String buySupport(Player p) {
        return transaction(p,true,(s,c)->{ exchange();
            if (s.coins<30) throw new IllegalArgumentException("Cần 30 xu.");
            give(p,bound(381,3)); give(p,bound(382,3)); give(p,bound(383,3)); s.coins-=30;
            return "Nhận 3 Cuồng nộ, 3 Bổ huyết và 3 Bổ khí.";
        });
    }
    public String claimDaily(Player p,int task) {
        return transaction(p,true,(s,c)->{ active();
            if (task<0 || task>2) throw new IllegalArgumentException("Nhiệm vụ không hợp lệ");
            int bit=1<<task;
            if ((s.claims&bit)!=0) throw new IllegalArgumentException("Hôm nay đã nhận nhiệm vụ này.");
            if (task==0) {
                if (s.kills<500) throw new IllegalArgumentException("Chưa hạ đủ 500 quái.");
                give(p,NovemberCatalog.item(NovemberRules.PEN,5)); give(p,NovemberCatalog.item(NovemberRules.PAPER,3));
            } else if (task==1) {
                if (s.bosses<3) throw new IllegalArgumentException("Chưa hạ đủ 3 boss.");
                give(p,NovemberCatalog.item(NovemberRules.BADGE,2));
            } else {
                if (s.gifts<5) throw new IllegalArgumentException("Chưa tặng đủ 5 thiệp.");
                give(p,NovemberCatalog.item(NovemberRules.DAILY_BOX,1));
            }
            s.claims|=bit; return "Đã nhận thưởng nhiệm vụ ngày.";
        });
    }
    public String claimParticipation(Player p) {
        return transaction(p,true,(s,c)->{ exchange();
            if (s.score<500) throw new IllegalArgumentException("Cần đạt 500 điểm tri ân.");
            if (s.participation) throw new IllegalArgumentException("Đã nhận quà tham gia.");
            give(p,NovemberCatalog.item(NovemberRules.DAILY_BOX,3)); s.participation=true;
            return "Nhận 3 hộp thưởng tham gia (không cộng điểm khi mở).";
        });
    }

    public String claimTop(Player p,int model) {
        try { settle(); } catch (Exception e) { error(e.toString()); return "Chưa thể chốt bảng top, hãy thử lại."; }
        return transaction(p,true,(s,c)->{
            if (clock.instant().isBefore(NovemberRules.END)) throw new IllegalArgumentException("Trao thưởng từ 01/12/2026.");
            if (s.topClaimed) throw new IllegalArgumentException("Đã nhận thưởng top tháng.");
            int rank;
            try (PreparedStatement st=c.prepareStatement("SELECT rank_no FROM november2026_winners WHERE player_id=?")) {
                st.setLong(1,p.id); try (ResultSet r=st.executeQuery()) {
                    if (!r.next()) throw new IllegalArgumentException("Bạn không nằm trong Top 30."); rank=r.getInt(1);
                }
            }
            int days=rank<=3?0:rank<=10?30:7;
            give(p,NovemberCatalog.costume(model,days,rank==1));
            if (rank<=3) {
                Item title=NovemberCatalog.item(NovemberRules.TITLE,1);
                if (rank!=1) title.itemOptions.add(new ItemOption(93,30)); give(p,title);
            } else { give(p,bound(381,3)); give(p,bound(220,5)); }
            s.topClaimed=true; return "Đã nhận phần thưởng Top "+rank+". Cảm ơn bạn đã tham gia!";
        });
    }
    public synchronized void settle() throws Exception {
        if (clock.instant().isBefore(NovemberRules.END)) return;
        initialize();
        try (Connection c=connections.open()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement st=c.prepareStatement("SELECT settled FROM november2026_meta WHERE id=1 FOR UPDATE");
                     ResultSet r=st.executeQuery()) { r.next(); if (r.getInt(1)!=0) { c.rollback(); return; } }
                try (PreparedStatement st=c.prepareStatement("SELECT player_id,player_name,score FROM november2026_players WHERE score>0 ORDER BY score DESC,reached_at ASC,player_id ASC LIMIT 30");
                     ResultSet r=st.executeQuery(); PreparedStatement ins=c.prepareStatement("INSERT INTO november2026_winners(player_id,rank_no,score,player_name) VALUES(?,?,?,?)")) {
                    int rank=0;
                    while (r.next()) {
                        ins.setLong(1,r.getLong(1)); ins.setInt(2,++rank); ins.setLong(3,r.getLong(3)); ins.setString(4,r.getString(2)); ins.executeUpdate();
                    }
                }
                try (Statement st=c.createStatement()) { st.executeUpdate("UPDATE november2026_meta SET settled=1 WHERE id=1"); }
                c.commit();
            } catch (Exception e) { c.rollback(); throw e; }
        }
    }
    public String status(Player p) {
        return transaction(p,false,(s,c)->"|1|TRI ÂN SƯ PHỤ - THÁNG 11/2026\n|0|"
                +"Cày và tính điểm: 01–30/11. Đổi quà đến hết 03/12.\n"
                +"Trạng thái: "+switch(phase()) {case BEFORE->"Chưa bắt đầu";case ACTIVE->"Đang diễn ra";case EXCHANGE->"Đã khóa điểm, còn đổi quà";case CLOSED->"Đã kết thúc";}
                +"\nXu: "+s.coins+" | Tổng điểm: "+s.score
                +"\nTúi: "+count(p,2400)+" Bút; "+count(p,2401)+" Thiệp trắng; "+count(p,2402)+" Huy hiệu"
                +"\nThiệp tri ân: "+count(p,2403)+" | Hộp tự ghép: "+count(p,2404)+" | Hộp thưởng: "+count(p,2405)
                +"\nHôm nay: "+s.kills+"/500 quái; "+s.bosses+"/3 boss; "+s.gifts+"/5 thiệp."
                +"\nĐã nhận nhiệm vụ: "+((s.claims&1)!=0?"Quái ":"")+((s.claims&2)!=0?"Boss ":"")+((s.claims&4)!=0?"Tặng thiệp":""));
    }
    public String leaderboard(Player p) {
        try {
            settle(); initialize();
            StringBuilder text=new StringBuilder("|1|TOP ĐỆ TỬ HIẾU HỌC\n|0|");
            try (Connection c=connections.open(); PreparedStatement st=c.prepareStatement(
                    clock.instant().isBefore(NovemberRules.END)
                    ?"SELECT player_id,player_name,score FROM november2026_players WHERE score>0 ORDER BY score DESC,reached_at ASC,player_id ASC LIMIT 30"
                    :"SELECT player_id,player_name,score FROM november2026_winners ORDER BY rank_no"); ResultSet r=st.executeQuery()) {
                int rank=0; boolean found=false;
                while (r.next()) { rank++; text.append(rank).append(". ").append(r.getString(2)).append(" — ").append(r.getLong(3)).append(" điểm\n"); if(r.getLong(1)==p.id)found=true; }
                if (rank==0) text.append("Chưa có người chơi ghi điểm.\n");
                if (!found) text.append("Bạn chưa nằm trong Top 30.\n");
            }
            try(Connection c=connections.open();PreparedStatement st=c.prepareStatement(
                    "SELECT 1+(SELECT COUNT(*) FROM november2026_players rival WHERE rival.score>mine.score OR (rival.score=mine.score AND (rival.reached_at<mine.reached_at OR (rival.reached_at=mine.reached_at AND rival.player_id<mine.player_id)))) AS rank_position,mine.score FROM november2026_players mine WHERE mine.player_id=? AND mine.score>0")) {
                st.setLong(1,p.id);try(ResultSet r=st.executeQuery()){if(r.next())text.append("Bạn: hạng ").append(r.getLong(1)).append(" — ").append(r.getLong(2)).append(" điểm.\n");}
            }
            return text.append("Đồng điểm: ưu tiên người đạt mốc trước, sau đó ID nhân vật.\nTop tháng nhận quà tại NPC từ 01/12.").toString();
        } catch (Exception e) { error(e.toString()); return "Không tải được bảng top. Hãy thử lại sau."; }
    }
    public static String guide() {
        return "Bút và Thiệp trắng: mỗi loại 4%/quái, quay độc lập. Boss: 30% rơi 1–3 Huy hiệu.\n"
                +"10 Bút + 5 Thiệp trắng = 1 Thiệp tri ân. Tặng thiệp: +10 xu và +10 điểm.\n"
                +"3 Thiệp + 2 Huy hiệu = 1 hộp tự ghép. Mở hộp: +30 điểm, không có xu.\n"
                +"Hộp nhiệm vụ không cộng điểm. Quà hộp: hỗ trợ 40%, đá 30%, mảnh 20%, cải trang 3 ngày 7%, 7 ngày 2,5%, vĩnh viễn 0,5%.\n"
                +"Cải trang: 3 ngày/50 xu, 7 ngày/150, 30 ngày/500, vĩnh viễn/1.500 xu.\n"
                +"Nhiệm vụ ngày: 500 quái; 3 boss; tặng 5 thiệp. Mỗi mốc nhận một lần/ngày theo giờ Việt Nam.\n"
                +"30/11 hết cày và cộng điểm; đổi quà đến hết 03/12. Hộp tự ghép phải mở trước khi khóa điểm.\n"
                +"Top 1: cải trang Vinh Danh vĩnh viễn + danh hiệu lưu niệm; Top 2–3: cải trang vĩnh viễn + danh hiệu 30 ngày; Top 4–10: cải trang 30 ngày; Top 11–30: cải trang 7 ngày.\n"
                +"Đạt 500 điểm nhận 3 hộp thưởng tham gia. Nguyên liệu, cải trang không giao dịch.";
    }

    private String transaction(Player p,boolean inventory,Action action) {
        if (p==null || p.isBoss || p.isPet || p.isMiniPet) return "Nhân vật không hợp lệ.";
        boolean saving=false;
        if (inventory) {
            if(p.isUseMaBaoVe)return "Hãy tắt bảo vệ trước khi đổi vật phẩm sự kiện.";
            nro.services.func.TransactionService.gI().cancelTrade(p);
            saving=PlayerDAO.tryAcquireSaving(p); if(!saving)return "Nhân vật đang được lưu, hãy thử lại.";
        }
        try {
            synchronized(p.inventory) {
                initialize();
                List<Item> backup=new ArrayList<>();
                if (inventory) for(Item item:p.inventory.itemsBag) backup.add(item==null?ItemService.gI().createItemNull():ItemService.gI().copyItem(item));
                try (Connection c=connections.open()) {
                    c.setAutoCommit(false);
                    try {
                        try (PreparedStatement st=c.prepareStatement("INSERT IGNORE INTO november2026_players(player_id,player_name,score,reached_at,data) VALUES(?,?,0,0,?)")) {
                            st.setLong(1,p.id); st.setString(2,p.name==null?"":p.name); st.setString(3,new NovemberState().json()); st.executeUpdate();
                        }
                        NovemberState s;
                        try (PreparedStatement st=c.prepareStatement("SELECT data FROM november2026_players WHERE player_id=? FOR UPDATE")) {
                            st.setLong(1,p.id); try(ResultSet r=st.executeQuery()) { if(!r.next())throw new SQLException("Missing event state"); s=NovemberState.parse(r.getString(1)); }
                        }
                        s.day(today()); long previousScore=s.score;
                        String result=action.run(s,c);
                        // Serialize scored transactions against settlement, then recheck the cutoff.
                        if(s.score!=previousScore) {
                            try(PreparedStatement st=c.prepareStatement("SELECT settled FROM november2026_meta WHERE id=1 FOR UPDATE");ResultSet r=st.executeQuery()) {
                                r.next(); if(r.getInt(1)!=0)throw new IllegalArgumentException("Bảng điểm đã khóa.");
                            }
                            active();
                        }
                        try (PreparedStatement st=c.prepareStatement("UPDATE november2026_players SET player_name=?,score=?,reached_at=?,data=? WHERE player_id=?")) {
                            st.setString(1,p.name==null?"":p.name); st.setLong(2,s.score); st.setLong(3,s.reachedAt); st.setString(4,s.json()); st.setLong(5,p.id); st.executeUpdate();
                        }
                        if (inventory) try (PreparedStatement st=c.prepareStatement("UPDATE player SET items_bag=? WHERE id=?")) {
                            st.setString(1,InventoryService.gI().itemsBagToString(p)); st.setLong(2,p.id);
                            if(st.executeUpdate()!=1)throw new SQLException("Could not persist inventory");
                        }
                        c.commit(); return result;
                    } catch (Exception e) {
                        c.rollback(); if(inventory) {p.inventory.itemsBag.clear(); p.inventory.itemsBag.addAll(backup);}
                        if(e instanceof IllegalArgumentException)return e.getMessage();
                        error(e.toString()); return "Không thể lưu giao dịch sự kiện; vật phẩm và xu đã được hoàn lại.";
                    }
                }
            }
        } catch (Exception e) { error(e.toString()); return "Không kết nối được dữ liệu sự kiện. Hãy thử lại sau."; }
        finally { if(saving) PlayerDAO.releaseSaving(p); }
    }
    public static int count(Player p,int id) {
        long count=0; for(Item i:p.inventory.itemsBag)if(i!=null && i.isNotNullItem() && i.template.id==id)count+=i.quantity;
        return (int)Math.min(Integer.MAX_VALUE,count);
    }
    private void take(Player p,int id,int quantity) {
        if (count(p,id)<quantity) throw new IllegalArgumentException("Thiếu "+ItemService.gI().getTemplate(id).name+" (cần "+quantity+").");
        int left=quantity;
        for(Item item:p.inventory.itemsBag)if(left>0 && item!=null && item.isNotNullItem() && item.template.id==id) {
            int n=Math.min(left,item.quantity); InventoryService.gI().subQuantityItemsBag(p,item,n); left-=n;
        }
    }
    private void give(Player p,Item item) {
        if(!InventoryService.gI().addItemBag(p,item,99999)) throw new IllegalArgumentException("Hành trang không đủ chỗ trống; giao dịch đã được hoàn lại.");
    }
    private Item bound(int id,int amount) {
        Item item=ItemService.gI().createNewItem((short)id,amount); item.itemOptions.add(new ItemOption(30,0)); return item;
    }
    private synchronized void error(String message) {
        if(System.currentTimeMillis()-lastError>10000) {System.err.println("[TriAn2026] "+message); lastError=System.currentTimeMillis();}
    }
}

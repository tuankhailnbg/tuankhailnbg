package nro.services;

import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import nro.event.november.*;
import nro.models.item.*;
import nro.models.npc.NpcTemplate;
import nro.models.player.Player;
import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.map.Zone;
import nro.models.map.ItemMap;
import nro.models.mob.Mob;
import nro.server.Manager;
import org.junit.*;
import static org.junit.Assert.*;

public class NovemberEventTest {
    private static List<ItemTemplate> oldItems;
    private static List<ItemOptionTemplate> oldOptions;
    private static List<NpcTemplate> oldNpcs;
    private static List<CaiTrang> oldSkins;
    private String url;
    private MutableClock clock;
    private NovemberEventService event;
    private Player player;

    @BeforeClass public static void catalog() {
        oldItems=new ArrayList<>(Manager.ITEM_TEMPLATES); oldOptions=new ArrayList<>(Manager.ITEM_OPTION_TEMPLATES);
        oldNpcs=new ArrayList<>(Manager.NPC_TEMPLATES); oldSkins=new ArrayList<>(Manager.CAI_TRANGS);
        Manager.ITEM_TEMPLATES.clear(); Manager.ITEM_OPTION_TEMPLATES.clear(); Manager.NPC_TEMPLATES.clear(); Manager.CAI_TRANGS.clear();
        for(int i=0;i<2400;i++) {
            ItemTemplate t=new ItemTemplate();t.id=(short)i;t.type=27;t.name="Test "+i;t.description="";t.gender=3;t.part=-1;
            if(i>=220 && i<=224)t.type=14;
            if(i>=381 && i<=384)t.type=29;
            Manager.ITEM_TEMPLATES.add(t);
        }
        for(int id:new int[]{1304,1305,1306,1276,2129,2142,904,985,1270,2045}) {
            Manager.ITEM_TEMPLATES.get(id).type=5;
            Manager.CAI_TRANGS.add(new CaiTrang(id,id,id+1,id+2,-1));
        }
        for(int i=0;i<=250;i++){ItemOptionTemplate t=new ItemOptionTemplate();t.id=i;t.name="Option "+i;Manager.ITEM_OPTION_TEMPLATES.add(t);}
        for(int i=0;i<80;i++){NpcTemplate n=new NpcTemplate();n.id=i;n.name="Npc "+i;Manager.NPC_TEMPLATES.add(n);}
        NovemberCatalog.ready=false; NovemberCatalog.install();
    }
    @AfterClass public static void restore() {
        Manager.ITEM_TEMPLATES.clear();Manager.ITEM_TEMPLATES.addAll(oldItems);
        Manager.ITEM_OPTION_TEMPLATES.clear();Manager.ITEM_OPTION_TEMPLATES.addAll(oldOptions);
        Manager.NPC_TEMPLATES.clear();Manager.NPC_TEMPLATES.addAll(oldNpcs);
        Manager.CAI_TRANGS.clear();Manager.CAI_TRANGS.addAll(oldSkins);NovemberCatalog.ready=false;
    }
    @Before public void database() throws Exception {
        url="jdbc:h2:mem:"+UUID.randomUUID()+";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        clock=new MutableClock(Instant.parse("2026-11-15T08:00:00Z"));
        event=new NovemberEventService(()->DriverManager.getConnection(url),clock);event.initialize();
        try(Connection c=connection();Statement st=c.createStatement()) {st.execute("CREATE TABLE player(id BIGINT PRIMARY KEY,items_bag LONGTEXT)");}
        player=createPlayer(1);
    }
    private Connection connection() throws SQLException {return DriverManager.getConnection(url);}
    private Player createPlayer(long id) throws Exception {
        Player p=new Player();p.id=id;p.name="Tester "+id;
        for(int i=0;i<30;i++)p.inventory.itemsBag.add(ItemService.gI().createItemNull());
        try(Connection c=connection();PreparedStatement st=c.prepareStatement("INSERT INTO player(id,items_bag) VALUES(?,'[]')")){st.setLong(1,id);st.executeUpdate();}
        return p;
    }
    private void add(Player p,int id,int count){assertTrue(InventoryService.gI().addItemBag(p,NovemberCatalog.item(id,count),99999));}
    private NovemberState state(Player p) throws Exception {
        event.status(p);
        try(Connection c=connection();PreparedStatement st=c.prepareStatement("SELECT data FROM november2026_players WHERE player_id=?")) {
            st.setLong(1,p.id);try(ResultSet r=st.executeQuery()){assertTrue(r.next());return NovemberState.parse(r.getString(1));}
        }
    }
    private void seedState(Player p,NovemberState s) throws Exception {
        event.status(p);
        try(Connection c=connection();PreparedStatement st=c.prepareStatement("UPDATE november2026_players SET data=?,score=?,reached_at=? WHERE player_id=?")) {
            st.setString(1,s.json());st.setLong(2,s.score);st.setLong(3,s.reachedAt);st.setLong(4,p.id);st.executeUpdate();
        }
    }
    @Test public void eventBoundariesUseVietnameseTime() {
        assertEquals(NovemberRules.Phase.BEFORE,NovemberRules.phase(Instant.parse("2026-10-31T16:59:59Z")));
        assertEquals(NovemberRules.Phase.ACTIVE,NovemberRules.phase(Instant.parse("2026-10-31T17:00:00Z")));
        assertEquals(NovemberRules.Phase.EXCHANGE,NovemberRules.phase(Instant.parse("2026-11-30T17:00:00Z")));
        assertEquals(NovemberRules.Phase.CLOSED,NovemberRules.phase(Instant.parse("2026-12-03T17:00:00Z")));
    }
    @Test public void boxDistributionAndCombatCapsMatchApprovedRules() {
        int[] counts=new int[6];for(int r=0;r<10000;r++)counts[NovemberRules.rewardTier(r)]++;
        assertArrayEquals(new int[]{4000,3000,2000,700,250,50},counts);
        assertEquals(4,NovemberRules.MATERIAL_RATE);assertEquals(30,NovemberRules.BADGE_RATE);
        for(int model=0;model<4;model++)for(int[] option:NovemberRules.OPTIONS[model])assertTrue(option[1]<=32);
        assertEquals(1500,NovemberRules.price(3));assertEquals(0,NovemberRules.DAYS[3]);
        assertEquals(25000,NovemberRules.goldWithBonus(10000,150));
        assertEquals(Integer.MAX_VALUE,NovemberRules.goldWithBonus(Integer.MAX_VALUE,150));
    }
    @Test public void craftDonateBuyAndRestartPersistAllBalances() throws Exception {
        add(player,2400,1500);add(player,2401,750);
        assertTrue(event.craftCards(player,150).startsWith("Đã ghép"));
        assertTrue(event.donate(player,150).startsWith("Đã tặng"));
        assertEquals(1500,state(player).coins);assertEquals(1500,state(player).score);
        assertTrue(event.buyCostume(player,0,3).startsWith("Đã đổi"));
        assertEquals(0,state(player).coins);assertEquals(1500,state(player).score);
        Item costume=player.inventory.itemsBag.stream().filter(i->i.isNotNullItem()&&i.template.id==2410).findFirst().orElseThrow();
        assertFalse(costume.itemOptions.stream().anyMatch(o->o.optionTemplate.id==93));
        NovemberEventService restarted=new NovemberEventService(()->connection(),clock);
        assertTrue(restarted.status(player).contains("Tổng điểm: 1500"));
    }
    @Test public void incompleteRecipeAndInvalidQuantityNeverConsumeMaterials() throws Exception {
        add(player,2400,10);
        assertTrue(event.craftCards(player,1).startsWith("Thiếu"));
        assertEquals(10,NovemberEventService.count(player,2400));
        for(int quantity:new int[]{-1,0,1001,Integer.MAX_VALUE})event.craftCards(player,quantity);
        assertEquals(10,NovemberEventService.count(player,2400));assertEquals(0,state(player).score);
    }
    @Test public void fullInventoryRefundsCoinsAndDatabaseFailureRefundsMaterials() throws Exception {
        NovemberState s=new NovemberState();s.coins=1500;seedState(player,s);
        for(int i=0;i<30;i++)player.inventory.itemsBag.set(i,NovemberCatalog.item(220,99999));
        assertTrue(event.buyCostume(player,0,3).contains("không đủ chỗ"));
        assertEquals(1500,state(player).coins);
        for(int i=0;i<30;i++)player.inventory.itemsBag.set(i,ItemService.gI().createItemNull());
        add(player,2400,10);add(player,2401,5);
        try(Connection c=connection();Statement st=c.createStatement()){st.executeUpdate("DELETE FROM player WHERE id=1");}
        assertTrue(event.craftCards(player,1).contains("hoàn lại"));
        assertEquals(10,NovemberEventService.count(player,2400));assertEquals(5,NovemberEventService.count(player,2401));
        assertEquals(0,NovemberEventService.count(player,2403));
    }
    @Test public void dailyBoxesAndParticipationCannotMintScoreOrBeClaimedTwice() throws Exception {
        NovemberState s=new NovemberState();s.day(LocalDate.now(clock));s.gifts=5;s.score=500;
        seedState(player,s);
        assertTrue(event.claimDaily(player,2).startsWith("Đã nhận"));
        assertTrue(event.claimDaily(player,2).contains("đã nhận"));
        assertEquals(1,NovemberEventService.count(player,2405));
        assertTrue(event.openBox(player,true).startsWith("Nhận"));
        assertEquals(500,state(player).score);
        assertTrue(event.claimParticipation(player).startsWith("Nhận"));
        assertTrue(event.claimParticipation(player).contains("Đã nhận"));
        assertEquals(3,NovemberEventService.count(player,2405));
        clock.instant=clock.instant.plus(Duration.ofDays(1));assertEquals(0,state(player).gifts);assertEquals(0,state(player).claims);
    }
    @Test public void craftedBoxesScoreExactlyOnceAndCannotBeOpenedAfterCutoff() throws Exception {
        add(player,2403,3);add(player,2402,2);
        assertTrue(event.craftBoxes(player,1).startsWith("Đã ghép"));assertEquals(0,state(player).score);
        assertTrue(event.openBox(player,false).startsWith("Nhận"));assertEquals(30,state(player).score);
        event.openBox(player,false);assertEquals(30,state(player).score);
        add(player,2404,1);clock.instant=NovemberRules.END;
        assertTrue(event.openBox(player,false).contains("Chỉ cày"));assertEquals(1,NovemberEventService.count(player,2404));
        assertEquals(30,state(player).score);
    }
    @Test public void monthEndFreezesRanksAndTopClaimsAreIdempotent() throws Exception {
        Player second=createPlayer(2);
        add(player,2403,10);add(second,2403,10);
        event.donate(player,10);clock.instant=clock.instant.plusSeconds(1);event.donate(second,10);
        assertTrue(event.leaderboard(player).contains("Bạn: hạng 1"));
        assertTrue(event.claimTop(player,0).contains("Trao thưởng"));
        clock.instant=NovemberRules.END;event.settle();event.settle();
        try(Connection c=connection();Statement st=c.createStatement();ResultSet r=st.executeQuery("SELECT rank_no FROM november2026_winners WHERE player_id=1")){assertTrue(r.next());assertEquals(1,r.getInt(1));}
        assertTrue(event.claimTop(player,0).startsWith("Đã nhận"));
        assertTrue(event.claimTop(player,0).contains("Đã nhận thưởng top"));
        assertEquals(1,NovemberEventService.count(player,2415));assertEquals(1,NovemberEventService.count(player,2420));
        add(second,2403,10);event.donate(second,10);assertEquals(100,state(second).score);
        assertTrue(event.buyCostume(second,0,0).startsWith("Đã đổi"));
        clock.instant=NovemberRules.CLOSE;assertTrue(event.buyCostume(second,0,0).contains("Đổi quà từ"));
    }
    @Test public void simultaneousDonationsNeverSpendTheSameCardTwice() throws Exception {
        add(player,2403,1);ExecutorService pool=Executors.newFixedThreadPool(2);
        Future<String> a=pool.submit(()->event.donate(player,1));Future<String> b=pool.submit(()->event.donate(player,1));
        a.get(5,TimeUnit.SECONDS);b.get(5,TimeUnit.SECONDS);pool.shutdown();
        assertEquals(10,state(player).score);assertEquals(10,state(player).coins);
    }
    @Test public void costumeExpiryAndTrainingPenaltyOptionsAreDistinct() {
        Item item=NovemberCatalog.costume(4,7,false);
        assertTrue(item.itemOptions.stream().anyMatch(o->o.optionTemplate.id==251&&o.param==50));
        assertTrue(item.itemOptions.stream().anyMatch(o->o.optionTemplate.id==100&&o.param==150));
        assertTrue(item.itemOptions.stream().anyMatch(o->o.optionTemplate.id==93&&o.param==7));
        assertFalse(item.itemOptions.stream().anyMatch(o->o.optionTemplate.id==156));
        assertEquals(500,NovemberRules.penalize(1000,50));
    }
    @Test public void actualMobAndBossDropsHaveIndependentRatesAndPersistDailyProgress() throws Exception {
        java.lang.reflect.Field f=nro.utils.Util.class.getDeclaredField("rand");f.setAccessible(true);((Random)f.get(null)).setSeed(20261101);
        Zone zone=zone();Mob mob=new Mob();mob.tempId=1;mob.zone=zone;
        int pens=0,papers=0;boolean both=false;
        for(int i=0;i<5000;i++) {
            List<ItemMap> drops=new ArrayList<>();event.addMobDrops(player,mob,drops,100,150);
            boolean pen=false,paper=false;
            for(ItemMap item:drops){assertEquals(player.id,item.playerId);assertEquals(1,item.quantity);
                if(item.itemTemplate.id==2400){pens++;pen=true;}else if(item.itemTemplate.id==2401){papers++;paper=true;}else fail("Unknown material");
                zone.removeItemMap(item);
            }
            both|=pen&&paper;
        }
        assertTrue(pens>120&&pens<280);assertTrue(papers>120&&papers<280);assertTrue(both);assertEquals(500,state(player).kills);
        TestBoss boss=new TestBoss();boss.zone=zone;int badges=0;
        for(int i=0;i<1000;i++) {
            event.onBossDefeated(boss,player);
            List<ItemMap> drops=zone.getItemMapsForPlayer(player);
            for(ItemMap item:drops){assertEquals(2402,item.itemTemplate.id);assertTrue(item.quantity>=1&&item.quantity<=3);badges++;zone.removeItemMap(item);}
        }
        assertTrue(badges>230&&badges<370);assertEquals(3,state(player).bosses);
        clock.instant=NovemberRules.END;List<ItemMap> closed=new ArrayList<>();event.addMobDrops(player,mob,closed,100,150);assertTrue(closed.isEmpty());
    }
    @Test public void equippingTrainingCostumeReallyHalvesHpKiAndDamageAndUnequippingRestoresThem() {
        Player p=new Player();p.isBoss=true;
        for(int i=0;i<13;i++)p.inventory.itemsBody.add(ItemService.gI().createItemNull());
        p.nPoint.hpg=1000;p.nPoint.mpg=2000;p.nPoint.dameg=1000;
        p.nPoint.calPoint();int hp=p.nPoint.hpMax,ki=p.nPoint.mpMax,damage=p.nPoint.dame;
        p.nPoint.hp=hp;p.nPoint.mp=ki;
        p.inventory.itemsBody.set(5,NovemberCatalog.costume(4,0,false));p.nPoint.calPoint();
        assertEquals(hp/2,p.nPoint.hpMax);assertEquals(ki/2,p.nPoint.mpMax);assertEquals(damage/2,p.nPoint.dame);
        assertEquals(150,p.nPoint.tlGold);assertTrue(p.nPoint.hp<=p.nPoint.hpMax);assertTrue(p.nPoint.mp<=p.nPoint.mpMax);
        p.inventory.itemsBody.set(5,ItemService.gI().createItemNull());p.nPoint.calPoint();
        assertEquals(hp,p.nPoint.hpMax);assertEquals(ki,p.nPoint.mpMax);assertEquals(damage,p.nPoint.dame);assertEquals(0,p.nPoint.tlGold);
    }
    @Test public void optionalMigrationIsRepeatableAndPreservesSourceArtwork() throws Exception {
        try(Connection c=connection();Statement st=c.createStatement()) {
            st.execute("CREATE TABLE item_template(id INT PRIMARY KEY,TYPE INT,gender INT,NAME VARCHAR(255),description TEXT,icon_id INT,part INT,is_up_to_up INT,power_require BIGINT)");
            st.execute("CREATE TABLE cai_trang(id_temp INT PRIMARY KEY,head INT,body INT,leg INT,bag INT)");
            st.execute("CREATE TABLE item_option_template(id INT PRIMARY KEY,NAME VARCHAR(255),TYPE INT)");
            st.execute("CREATE TABLE npc_template(id INT PRIMARY KEY,NAME VARCHAR(255),head INT,body INT,leg INT)");
            try(PreparedStatement ins=c.prepareStatement("INSERT INTO item_template VALUES(?,?,?,?,?,?,?,?,?)")) {
                for(int id:new int[]{1282,399,649,1191,2149,1304,1305,1306,1276,2129,2142,904,985,1270,2045,1293}) {
                    ins.setInt(1,id);ins.setInt(2,27);ins.setInt(3,3);ins.setString(4,"Source "+id);ins.setString(5,"");
                    ins.setInt(6,id);ins.setInt(7,-1);ins.setInt(8,0);ins.setLong(9,0);ins.executeUpdate();
                }
            }
            for(CaiTrang skin:Manager.CAI_TRANGS)if(skin.tempId<2400)st.execute("INSERT INTO cai_trang VALUES("+skin.tempId+","+skin.id[0]+","+skin.id[1]+","+skin.id[2]+",-1)");
            st.execute("INSERT INTO npc_template VALUES(13,'Quy Lao',18,19,20)");
            java.nio.file.Path sql=java.nio.file.Path.of("../database/migrations/20261030_november_tri_an.sql");
            for(int run=0;run<2;run++)try(java.io.Reader reader=java.nio.file.Files.newBufferedReader(sql,java.nio.charset.StandardCharsets.UTF_8)){org.h2.tools.RunScript.execute(c,reader);}
            try(ResultSet r=st.executeQuery("SELECT COUNT(*) FROM item_template WHERE id>=2400")){r.next();assertEquals(17,r.getInt(1));}
            try(ResultSet r=st.executeQuery("SELECT COUNT(*) FROM cai_trang WHERE id_temp>=2410")){r.next();assertEquals(10,r.getInt(1));}
            try(ResultSet r=st.executeQuery("SELECT head FROM cai_trang WHERE id_temp=2410")){r.next();assertEquals(1304,r.getInt(1));}
            try(ResultSet r=st.executeQuery("SELECT NAME FROM npc_template WHERE id=80")){r.next();assertEquals("Sứ Giả Tri Ân",r.getString(1));}
        }
    }
    @Test public void existingSchemaBootstrapsWithoutCreatePrivilege() throws Exception {
        NovemberEventService restricted=new NovemberEventService(()->{
            Connection connection=connection();
            return (Connection)java.lang.reflect.Proxy.newProxyInstance(Connection.class.getClassLoader(),new Class<?>[]{Connection.class},(proxy,method,args)->{
                try {
                    Object value=method.invoke(connection,args);
                    if(method.getName().equals("createStatement")) {
                        Statement statement=(Statement)value;
                        return java.lang.reflect.Proxy.newProxyInstance(Statement.class.getClassLoader(),new Class<?>[]{Statement.class},(p,m,a)->{
                            if(a!=null&&a.length>0&&a[0] instanceof String sql&&sql.startsWith("CREATE "))throw new SQLException("CREATE denied","42000");
                            try{return m.invoke(statement,a);}catch(java.lang.reflect.InvocationTargetException e){throw e.getCause();}
                        });
                    }
                    return value;
                } catch(java.lang.reflect.InvocationTargetException e){throw e.getCause();}
            });
        },clock);
        restricted.initialize();
        assertTrue(restricted.status(player).contains("TRI ÂN SƯ PHỤ"));
    }
    private Zone zone() {
        nro.models.map.Map m=new nro.models.map.Map(2,"Test",(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
                new int[2][2],new int[2],0,false,10,new ArrayList<>(),new ArrayList<>()) {
            @Override public int yPhysicInTop(int x,int y){return 150;}
        };
        return new Zone(m,0,10);
    }
    private static class TestBoss extends Boss {
        TestBoss(){super(-999999,BossData.KUKU);}
        @Override public void init(){nPoint.hpMax=100;nPoint.hp=100;}
        @Override public void rewards(Player p){}
        @Override public void initTalk(){}
        @Override public void idle(){}
        @Override public void checkPlayerDie(Player p){}
    }
    private static class MutableClock extends Clock {
        Instant instant;MutableClock(Instant instant){this.instant=instant;}
        @Override public ZoneId getZone(){return NovemberRules.ZONE;}
        @Override public Clock withZone(ZoneId zone){return this;}
        @Override public Instant instant(){return instant;}
    }
}

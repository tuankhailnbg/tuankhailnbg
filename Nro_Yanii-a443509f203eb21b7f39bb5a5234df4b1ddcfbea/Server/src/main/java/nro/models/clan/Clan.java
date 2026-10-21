package nro.models.clan;

import lombok.Getter;
import lombok.Setter;
import nro.jdbc.DBService;
import nro.models.map.Map;
import nro.models.map.Zone;
import nro.models.map.dungeon.SnakeRoad;
import nro.models.map.phoban.BanDoKhoBau;
import nro.models.mob.Mob;
import nro.services.ClanService;
import nro.models.map.phoban.DoanhTrai;

import java.util.ArrayList;
import java.util.List;

import nro.models.player.Player;
import nro.server.Client;
import nro.server.Manager;
import nro.services.MapService;
import nro.services.Service;
import nro.server.io.Message;
import nro.utils.Log;
import nro.utils.Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import nro.models.map.phoban.KhiGas;
//import nro.models.map.Yanii.TreasureMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * @author 💖 Nro Yanii 💖
 * 
 */
public class Clan {

    public static int NEXT_ID = 0;

    public int clanMessageId = 0;
    private final List<ClanMessage> clanMessages;

    public static final byte LEADER = 0;
    public static final byte DEPUTY = 1;
    public static final byte MEMBER = 2;

    public int id;

    public int imgId;

    public String name;

    public String slogan;

    public int createTime;

    public long powerPoint;

    public byte maxMember;

    public int level;

    public int idLeader;
    public String nameLeader;
    public int headLeader;
    public int bodyLeader;
    public int legLeader;

    /**
     * Tổng capsule bang
     */
    public int clanPoint;

    public boolean active;

    public final List<ClanMember> members;
    public final List<Player> membersInGame;

    /**
     * Đã đi doanh trại trong ngày
     */
    public boolean haveGoneDoanhTrai;
    public DoanhTrai doanhTrai;
    public Player playerOpenDoanhTrai;
    public long timeOpenDoanhTrai;

    public SnakeRoad snakeRoad;

    public BanDoKhoBau banDoKhoBau;
    public Player playerOpenBanDoKhoBau;
    public long timeOpenBanDoKhoBau;
    public long thoiGianHoanThanhBDKB;
    public int levelDoneBanDoKhoBau;

    public KhiGas khiGas;
    public Player playerOpenKhiGas;
    public long timeOpenKhiGas;
    public long thoiGianHoanThanhKG;
    public int levelDoneKhiGas;

    public boolean isLeader;
    @Setter
    @Getter
    private Buff buff;
    @Getter
    private Zone clanArea;

    public Clan() {
        this.id = NEXT_ID++;
        this.name = "";
        this.slogan = "";
        this.maxMember = 15;
        this.createTime = (int) (System.currentTimeMillis() / 1000);
        this.members = new ArrayList<>();
        this.membersInGame = new ArrayList<>();
        this.clanMessages = new ArrayList<>();
        initialize();
    }

    private void initialize() {
        Map map = MapService.gI().getMapById(153);
        this.clanArea = new Zone(map, 0, 50);
        Zone z = map.zones.get(0);
        if (z != null) {
            for (Mob m : z.mobs) {
                Mob mob = new Mob(m);
                mob.zone = clanArea;
                clanArea.addMob(mob);
            }
        }
    }

    public ClanMember getLeader() {
        for (ClanMember cm : members) {
            if (cm.role == LEADER) {
                return cm;
            }
        }
        ClanMember cm = new ClanMember();
        cm.name = "Bang chủ";
        return cm;
    }

    public byte getRole(Player player) {
        for (ClanMember cm : members) {
            if (cm.id == player.id) {
                return cm.role;
            }
        }
        return -1;
    }

    public boolean isLeader(Player player) {
        for (ClanMember cm : members) {
            if (cm.id == player.id && cm.role == LEADER) {
                return true;
            }
        }
        return false;
    }

    public boolean isDeputy(Player player) {
        for (ClanMember cm : members) {
            if (cm.id == player.id && cm.role == DEPUTY) {
                return true;
            }
        }
        return false;
    }

    public void addSMTNClan(Player plOri, long param) {
        for (Player pl : this.membersInGame) {
            if (!plOri.equals(pl) && plOri.zone.equals(pl.zone)) {
                Service.getInstance().addSMTN(pl, (byte) 1, param, false);
            }
        }
    }

    public void sendMessageClan(ClanMessage cmg) {
        Message msg;
        try {
            msg = new Message(-51);
            msg.writer().writeByte(cmg.type);
            msg.writer().writeInt(cmg.id);
            msg.writer().writeInt(cmg.playerId);
            if (cmg.type == 2) {
                msg.writer().writeUTF(cmg.playerName + " (" + Util.numberToMoney(cmg.playerPower) + ")");
            } else {
                msg.writer().writeUTF(cmg.playerName);
            }
            msg.writer().writeByte(cmg.role);
            msg.writer().writeInt(cmg.time);
            if (cmg.type == 0) {
                msg.writer().writeUTF(cmg.text);
                msg.writer().writeByte(cmg.color);
            } else if (cmg.type == 1) {
                msg.writer().writeByte(cmg.receiveDonate);
                msg.writer().writeByte(cmg.maxDonate);
                msg.writer().writeByte(cmg.isNewMessage);
            }
            for (Player pl : this.membersInGame) {
                pl.sendMessage(msg);
            }
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void addClanMessage(ClanMessage cmg) {
        this.clanMessages.add(0, cmg);
    }

    public ClanMessage getClanMessage(int clanMessageId) {
        for (ClanMessage cmg : this.clanMessages) {
            if (cmg.id == clanMessageId) {
                return cmg;
            }
        }
        return null;
    }

    public List<ClanMessage> getCurrClanMessages() {
        List<ClanMessage> list = new ArrayList();
        if (this.clanMessages.size() <= 20) {
            list.addAll(this.clanMessages);
        } else {
            for (int i = 0; i < 20; i++) {
                list.add(this.clanMessages.get(i));
            }
        }
        return list;
    }

    public void sendRemoveClanForAllMember() {
        for (Player pl : this.membersInGame) {
            if (pl != null) {
                ClanService.gI().sendRemoveClan(pl);
            }
        }
    }

    public void sendMyClanForAllMember() {
        for (Player pl : this.membersInGame) {
            if (pl != null) {
                ClanService.gI().sendMyClan(pl);
            }
        }
    }

    public void sendRemoveForAllMember() {
        for (Player pl : this.membersInGame) {
            if (pl != null) {
                Service.getInstance().sendThongBao(pl, "Bang Hội của bạn đã bị giải tán.");
            }
        }
    }

    public void sendFlagBagForAllMember() {
        for (Player pl : this.membersInGame) {
            if (pl != null) {
                Service.getInstance().sendFlagBag(pl);
            }
        }
    }

    public void addMemberOnline(Player player) {
        this.membersInGame.add(player);
    }

    public void removeMemberOnline(ClanMember cm, Player player) {
        if (player != null) {
            this.membersInGame.remove(player);
        }
        if (cm != null) {
            for (int i = this.membersInGame.size() - 1; i >= 0; i--) {
                if (this.membersInGame.get(i).id == cm.id) {
                    this.membersInGame.remove(i);
                    break;
                }
            }
        }
    }

    public Player getPlayerOnline(int playerId) {
        for (Player player : this.membersInGame) {
            if (player.id == playerId) {
                return player;
            }
        }
        return null;
    }

    // load db danh sách member
    public void addClanMember(ClanMember cm) {
        this.members.add(cm);
    }

    // thêm vào khi player tạo mới clan or mới vào clan
    public void addClanMember(Player player, byte role) {
        ClanMember cm = new ClanMember(player, this, role);
        this.members.add(cm);
        player.clanMember = cm;
    }

    // xóa khi member rời clan or bị kích
    public void removeClanMember(ClanMember cm) {
        this.members.remove(cm);
    }

    public byte getCurrMembers() {
        return (byte) this.members.size();
    }

    public List<ClanMember> getMembers() {
        return this.members;
    }

    public ClanMember getClanMember(int memberId) {
        for (ClanMember cm : members) {
            if (cm.id == memberId) {
                return cm;
            }
        }
        return null;
    }

    public void reloadClanMember() {
        for (ClanMember cm : this.members) {
            Player pl = Client.gI().getPlayer(cm.id);
            if (pl != null) {
                cm.powerPoint = pl.nPoint.power;
            }
        }
    }

    public void insert() {
        JSONArray dataArray = new JSONArray();
        for (ClanMember cm : this.members) {
            JSONObject dataObject = new JSONObject();
            dataObject.put("id", cm.id);
            dataObject.put("name", cm.name);
            dataObject.put("head", cm.head);
            dataObject.put("body", cm.body);
            dataObject.put("leg", cm.leg);
            dataObject.put("role", cm.role);
            dataObject.put("donate", cm.donate);
            dataObject.put("receive_donate", cm.receiveDonate);
            dataObject.put("member_point", cm.memberPoint);
            dataObject.put("clan_point", cm.clanPoint);
            dataObject.put("join_time", cm.joinTime);
            dataObject.put("ask_pea_time", cm.timeAskPea);
            dataObject.put("power", cm.powerPoint);
            dataArray.add(dataObject);
        }
        String member = dataArray.toJSONString();

        String topBanDoKhoBau = "[" + levelDoneBanDoKhoBau + "," + thoiGianHoanThanhBDKB + "]";

        String thongTinLeader = "[" + getLeader().id + "," + getLeader().name + "," + getLeader().head + ","
                + getLeader().body + "," + getLeader().leg + "]";

        PreparedStatement ps = null;
        try (Connection con = DBService.gI().getConnectionForClan();) {
            ps = con.prepareStatement("insert into clan_sv" + Manager.SERVER
                    + "(id, name, slogan, img_id, power_point, max_member, clan_point, level, members, thanhTichBDKB, thongTinLeader) "
                    + "values (?,?,?,?,?,?,?,?,?,?,?)");
            ps.setInt(1, this.id);
            ps.setString(2, this.name);
            ps.setString(3, this.slogan);
            ps.setInt(4, this.imgId);
            ps.setLong(5, this.powerPoint);
            ps.setByte(6, this.maxMember);
            ps.setInt(7, this.clanPoint);
            ps.setInt(8, this.level);
            ps.setString(9, member);
            ps.setString(10, topBanDoKhoBau);
            ps.setString(11, thongTinLeader);
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            Log.error(Clan.class, e, "Có lỗi khi insert clan vào db");
        }
    }

    public void update() {
        JSONArray dataArray = new JSONArray();
        JSONObject dataObject = new JSONObject();
        for (ClanMember cm : this.members) {
            dataObject.put("id", cm.id);
            dataObject.put("name", cm.name);
            dataObject.put("head", cm.head);
            dataObject.put("body", cm.body);
            dataObject.put("leg", cm.leg);
            dataObject.put("role", cm.role);
            dataObject.put("donate", cm.donate);
            dataObject.put("receive_donate", cm.receiveDonate);
            dataObject.put("member_point", cm.memberPoint);
            dataObject.put("clan_point", cm.clanPoint);
            dataObject.put("join_time", cm.joinTime);
            dataObject.put("ask_pea_time", cm.timeAskPea);
            dataArray.add(dataObject.toJSONString());
            dataObject.clear();
        }
        String member = dataArray.toJSONString();

        String topBanDoKhoBau = "[" + levelDoneBanDoKhoBau + "," + thoiGianHoanThanhBDKB + "]";

        String thongTinLeader = "[" + getLeader().id + "," + getLeader().name + "," + getLeader().head + ","
                + getLeader().body + "," + getLeader().leg + "]";

        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = DBService.gI().getConnectionForClan();
        } catch (Exception e) {
        }
        try {
            ps = con.prepareStatement("update clan_sv" + Manager.SERVER
                    + " set slogan = ?, img_id = ?, power_point = ?, max_member = ?, clan_point = ?, "
                    + "level = ?, members = ?, thanhTichBDKB = ?, thongTinLeader where id = ? limit 1");
            ps.setString(1, this.slogan);
            ps.setInt(2, this.imgId);
            ps.setLong(3, this.powerPoint);
            ps.setByte(4, this.maxMember);
            ps.setInt(5, this.clanPoint);
            ps.setInt(6, this.level);
            ps.setString(7, member);
            ps.setString(8, topBanDoKhoBau);
            ps.setString(9, thongTinLeader);
            ps.setInt(10, this.id);
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            Log.error(Clan.class, e, "Có lỗi khi insert clan vào db");
        } finally {
            try {
                ps.close();
            } catch (Exception e) {
            }
            DBService.gI().release(con);
        }
    }

    public void updatethanhTichBDKB(int clanId) {

        JSONArray dataArray = new JSONArray();

        String topBanDoKhoBau = "[" + levelDoneBanDoKhoBau + "," + thoiGianHoanThanhBDKB + "]";

        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = DBService.gI().getConnectionForClan();
        } catch (Exception e) {
        }
        try {
            ps = con.prepareStatement("update clan_sv" + Manager.SERVER
                    + " set thanhTichBDKB = ? where id = ? limit 1");
            ps.setString(1, topBanDoKhoBau);
            ps.setInt(2, clanId);
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            Log.error(Clan.class, e, "ERROR KHI UPDATE THÀNH TÍCH BANG");
        } finally {
            try {
                ps.close();
            } catch (Exception e) {
            }
            DBService.gI().release(con);
        }
    }

    public void updatethanhTichBDKBForLeader() {

        JSONArray dataArray = new JSONArray();

        String topBanDoKhoBau = "[" + this.name + "," + this.levelDoneBanDoKhoBau + "," + thoiGianHoanThanhBDKB + ","
                + System.currentTimeMillis() + "]";

        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = DBService.gI().getConnectionForClan();
        } catch (Exception e) {
        }
        try {
            ps = con.prepareStatement("update player set thanhTichBang = ? where id = ? limit 1");
            ps.setString(1, topBanDoKhoBau);
            ps.setInt(2, this.getLeader().id);
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            Log.error(Clan.class, e, "ERROR KHI UPDATE THÀNH TÍCH BANG");
        } finally {
            try {
                ps.close();
            } catch (Exception e) {
            }
            DBService.gI().release(con);
        }
    }

    public void updateThongTinLeader(int clanId) {

        JSONArray dataArray = new JSONArray();

        String thongTinLeader = "[" + getLeader().id + "," + getLeader().name + "," + getLeader().head + ","
                + getLeader().body + "," + getLeader().leg + "]";

        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = DBService.gI().getConnectionForClan();
        } catch (Exception e) {
        }
        try {
            ps = con.prepareStatement("update clan_sv" + Manager.SERVER
                    + " set thongTinLeader = ? where id = ? limit 1");
            ps.setString(1, thongTinLeader);
            ps.setInt(2, clanId);
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            Log.error(Clan.class, e, "ERROR KHI UPDATE THÔNG TIN LEADER");
        } finally {
            try {
                ps.close();
            } catch (Exception e) {
            }
            DBService.gI().release(con);
        }
    }

}

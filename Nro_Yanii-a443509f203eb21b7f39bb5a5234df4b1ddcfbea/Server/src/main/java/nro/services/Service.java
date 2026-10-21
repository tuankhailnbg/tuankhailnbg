package nro.services;

import java.io.DataOutputStream;
import nro.consts.Cmd;
import nro.consts.ConstNpc;
import nro.consts.ConstPlayer;
import nro.data.DataGame;
import nro.jdbc.daos.AccountDAO;
import nro.jdbc.daos.ShopDAO;
import nro.manager.TopPowerManager;
import nro.models.Part;
import nro.models.PartManager;
import nro.models.item.Item;
import nro.models.item.ItemOption;
import nro.models.map.ItemMap;
import nro.models.map.Zone;
import nro.models.map.dungeon.zones.ZDungeon;
import nro.models.mob.Mob;
import nro.models.player.Pet;
import nro.models.player.Player;
import nro.models.skill.Skill;
import nro.power.Caption;
import nro.power.CaptionManager;
import nro.server.Client;
import nro.server.io.Message;
import nro.server.io.Session;
import nro.services.func.Input;
import nro.utils.FileIO;
import nro.utils.Log;
import nro.utils.TimeUtil;
import nro.utils.Util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import nro.consts.ConstTask;
import nro.dialog.ConfirmDialog;
import nro.jdbc.DBService;

//import nro.manager.TopBangBDKBManager;
import nro.manager.TopKillWhisManager;
import nro.manager.TopManager;

import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.models.boss.BossManager;
import static nro.models.boss.BossManager.BOSSES_IN_GAME;
import nro.models.boss.broly.Broly;
import nro.models.boss.mapoffline.Boss_Tau77;
import nro.models.npc.specialnpc.MabuEgg;
import nro.models.player.Inventory;
import nro.server.Maintenance;
import nro.server.Manager;
import nro.server.ServerManager;
import nro.utils.SkillUtil;

public class Service {

    private static Service instance;

    public static Service getInstance() {
        if (instance == null) {
            instance = new Service();
        }
        return instance;
    }

    public static Service gI() {
        if (instance == null) {
            instance = new Service();
        }
        return instance;
    }

    public void sendMessAllPlayer(Message msg) {
        msg.transformData();
        PlayerService.gI().sendMessageAllPlayer(msg);
    }

    public void sendMessAllPlayerIgnoreMe(Player player, Message msg) {
        msg.transformData();
        PlayerService.gI().sendMessageIgnore(player, msg);
    }

    public void sendMessAllPlayerInMap(Zone zone, Message msg) {
        msg.transformData();
        if (zone != null) {
            List<Player> players = zone.getPlayers();
            synchronized (players) {
                for (Player pl : players) {
                    if (pl != null) {
                        pl.sendMessage(msg);
                    }
                }
            }
            msg.cleanup();
        }
    }

    public void removeTitle1(Player player) {
        Message me;
        try {
            me = new Message(-128);
            me.writer().writeByte(2);
            me.writer().writeInt((int) player.id);
            player.getSession().sendMessage(me);
            this.sendMessAllPlayerInMap(player, me);
            me.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendTitleRv1(Player player, Player p2, int part) {
        Message me;
        try {
            me = new Message(-128);
            me.writer().writeByte(0);
            me.writer().writeInt((int) player.id);
            if (player.partDanhHieu >= 60) {
                me.writer().writeShort(part);
            }
            me.writer().writeShort(part);
            me.writer().writeByte(1);
            me.writer().writeByte(-1);
            me.writer().writeShort(50);
            me.writer().writeByte(-1);
            me.writer().writeByte(-1);
            p2.sendMessage(me);
            me.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeTitle(Player player) {
        Message me;
        try {
            me = new Message(-128);
            me.writer().writeByte(2);
            me.writer().writeInt((int) player.id);
            player.getSession().sendMessage(me);
            this.sendMessAllPlayerInMap(player, me);
            me.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeFoot1(Player player) {
        Message me;
        try {
            me = new Message(-128);
            me.writer().writeByte(1);
            me.writer().writeInt((int) player.id);
            player.getSession().sendMessage(me);
            this.sendMessAllPlayerInMap(player, me);
            me.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendTitle1(Player player, int part) {
        Message me;
        try {
            me = new Message(-128);
            me.writer().writeByte(0);
            me.writer().writeInt((int) player.id);
            if (player.titleitem) {
                me.writer().writeShort(part);
            }
            me.writer().writeShort(part);
            me.writer().writeByte(1);
            me.writer().writeByte(-1);
            me.writer().writeShort(50);
            me.writer().writeByte(-1);
            me.writer().writeByte(-1);
            this.sendMessAllPlayerInMap(player, me);
            me.cleanup();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendTitle(Player player, int id) {
        Message me;
        try {
            me = new Message(-128);
            me.writer().writeByte(0);
            me.writer().writeInt((int) player.id);
            switch (id) {
            }
            me.writer().writeByte(1);
            me.writer().writeByte(-1);
            me.writer().writeShort(50);
            me.writer().writeByte(-1);
            me.writer().writeByte(-1);
            this.sendMessAllPlayerInMap(player, me);
            me.cleanup();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showListBoss(Player pl) {
        Message msg = null;
        try {
            List<Boss> bosses = BossManager.BOSSES_IN_GAME.stream()
                    .filter(boss -> boss != null && boss.name != null)
                    .toList();

            msg = new Message(-91);
            msg.writer().writeByte(bosses.size());

            for (Boss boss : bosses) {
                String bossInfo = "Boss: " + boss.name;
                String location;

                if (boss.zone != null && boss.zone.map != null) {
                    bossInfo += " (HP: " + Util.numberToMoney(boss.nPoint.hp) + ")";
                    location = boss.zone.map.mapName + " (" + boss.zone.map.mapId + ") khu vực: " + boss.zone.zoneId;
                } else {
                    location = "Chưa xuất hiện";
                }
                msg.writer().writeUTF(bossInfo);
                msg.writer().writeUTF(location);
            }
            pl.sendMessage(msg);
        } catch (Exception e) {
            Log.error(Service.class, e); // Hoặc in ra lỗi nếu chưa dùng logger
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    private void showListBoss1(Player player) {
        final byte OPCODE_SHOW_BOSS_LIST = -96;
        Message msg = null;

        try {
            msg = new Message(OPCODE_SHOW_BOSS_LIST);
            msg.writer().writeByte(0);
            msg.writer().writeUTF("Boss (SL: " + BossManager.BOSSES_IN_GAME.size() + ")");

            List<Boss> bosses = BossManager.BOSSES_IN_GAME.stream()
                    .filter(b -> b != null && !BossFactory.isYar((byte) b.id))
                    .toList();

            msg.writer().writeByte(bosses.size());

            for (Boss boss : bosses) {
                msg.writer().writeInt((int) boss.id);
                msg.writer().writeInt((int) boss.id);
                msg.writer().writeShort(boss.getHead());

                if (player.isVersionAbove(220)) {
                    Part part = PartManager.getInstance().find(boss.getHead());
                    msg.writer().writeShort(part != null ? part.getIcon(0) : -1);
                }
                msg.writer().writeShort(boss.getBody());
                msg.writer().writeShort(boss.getLeg());
                msg.writer().writeUTF(boss.name);

                if (boss.zone != null && boss.zone.map != null) {
                    msg.writer().writeUTF("Sống");
                    String mapInfo = boss.zone.map.mapName + " (" + boss.zone.map.mapId + ")";
                    if (!player.getSession().actived) {
                        msg.writer().writeUTF(mapInfo);
                    } else {
                        msg.writer().writeUTF(mapInfo + " khu " + boss.zone.zoneId);
                    }
                } else {
                    msg.writer().writeUTF("Chết");
                    msg.writer().writeUTF("Chết rồi");
                }
            }
            player.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void sendMessAllPlayerInMap(Player player, Message msg) {
        if (player == null || player.zone == null) {
            return; // Tránh lỗi NullPointerException
        }

        msg.transformData();

        if (player.zone.map.isMapOffline) {
            if (player.isPet) {
                ((Pet) player).master.sendMessage(msg);
            } else {
                List<Player> players = player.zone.getPlayers();
                synchronized (players) {
                    for (Player pl : players) {
                        if (pl != null) {
                            pl.sendMessage(msg);
                        }
                    }
                }
                msg.cleanup();
            }
        } else {
            List<Player> players = player.zone.getPlayers();
            synchronized (players) {
                for (Player pl : players) {
                    if (pl != null) {
                        pl.sendMessage(msg);
                    }
                }
            }
            msg.cleanup();
        }
    }

    public void sendMessAnotherNotMeInMap(Player player, Message msg) {
        if (player.zone != null) {
            List<Player> players = player.zone.getPlayers();
            synchronized (players) {
                for (Player pl : players) {
                    if (pl != null && !pl.equals(player)) {
                        pl.sendMessage(msg);
                    }
                }
            }

            msg.cleanup();
        }
    }

    public void Send_Info_NV(Player pl) {
        Message msg;
        try {
            msg = Service.getInstance().messageSubCommand((byte) 14);// Cập nhật máu
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeInt(pl.nPoint.hp);
            msg.writer().writeByte(0);// Hiệu ứng Ăn Đậu
            msg.writer().writeInt(pl.nPoint.hpMax);
            sendMessAnotherNotMeInMap(pl, msg);
            msg.cleanup();
        } catch (Exception e) {

        }
    }

    public void sendInfoPlayerEatPea(Player pl) {
        Message msg;
        try {
            msg = Service.getInstance().messageSubCommand((byte) 14);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeInt(pl.nPoint.hp);
            msg.writer().writeByte(1);
            msg.writer().writeInt(pl.nPoint.hpMax);
            sendMessAnotherNotMeInMap(pl, msg);
            msg.cleanup();
        } catch (Exception e) {

        }
    }

    public void loginDe(Session session, short second) {
        Message msg;
        try {
            msg = new Message(122);
            msg.writer().writeShort(second);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void resetPoint(Player player, int x, int y) {
        Message msg;
        try {
            player.location.x = x;
            player.location.y = y;
            msg = new Message(46);
            msg.writer().writeShort(x);
            msg.writer().writeShort(y);
            player.sendMessage(msg);
            msg.cleanup();

        } catch (Exception e) {
        }
    }

    // public void SendImgSkill9(Player player) {
    // Message msg;
    // try {
    // msg = new Message(62);
    // msg.writer().writeShort(24);
    // msg.writer().writeByte(1);
    // msg.writer().writeByte(3);
    // player.sendMessage(msg);
    // msg.cleanup();
    //
    // } catch (Exception e) {
    // }
    // }
    public void SendImgSkill9(short SkillId, int IdAnhSKill) {
        Message msg = new Message(62);
        DataOutputStream ds = msg.writer();
        try {
            ds.writeShort(SkillId);
            ds.writeByte(1);
            ds.writeByte(IdAnhSKill);
            ds.flush();
            Service.getInstance().sendMessAllPlayer(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    // public void SendImgSkill9_TD(short SkillId) {
    // Message msg = new Message(62);
    // DataOutputStream ds = msg.writer();
    // try {
    // ds.writeShort(SkillId);
    // ds.writeByte(1);
    // ds.writeByte(3);
    // System.out.println("OK");
    // ds.flush();
    // Service.getInstance().sendMessAllPlayer(msg);
    // msg.cleanup();
    // } catch (Exception e) {
    // }
    // }
    public void regisAccount(Session session, Message _msg) {
        try {
            _msg.reader().readUTF();
            _msg.reader().readUTF();
            _msg.reader().readUTF();
            _msg.reader().readUTF();
            _msg.reader().readUTF();
            _msg.reader().readUTF();
            _msg.reader().readUTF();
            String user = _msg.reader().readUTF();
            ;
            String pass = _msg.reader().readUTF();
            ;
            if (!(user.length() >= 4 && user.length() <= 18)) {
                sendThongBaoOK(session, "Tài khoản phải có độ dài 4-18 ký tự");
                return;
            }
            if (!(pass.length() >= 5 && pass.length() <= 18)) {
                sendThongBaoOK(session, "Mật khẩu phải có độ dài 5-18 ký tự");
                return;
            }
            Connection connection = null;
            PreparedStatement preparedStatement = null;
            ResultSet resultSet = null;
            try {
                connection = DBService.gI().getConnectionForGame();
                String sql = "SELECT * FROM `account` WHERE username = ?";
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, user);
                resultSet = preparedStatement.executeQuery();
                if (resultSet.first()) {
                    sendThongBaoOK(session, "Tài khoản đã tồn tại");
                } else {
                    Connection con = DBService.gI().getConnectionForGame();
                    PreparedStatement ps = con
                            .prepareStatement("INSERT INTO account (username, password) VALUES (?, ?)");
                    ps.setString(1, user);
                    ps.setString(2, pass);
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void login2(Session session, String user) {
        Message msg;
        try {
            msg = new Message(62);
            DataOutputStream ds = msg.writer();
            ds.writeUTF(user);
            ds.writeByte(1);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            System.err.println("Error Login 2");
        }
    }

    public void callTau77(Player player) {
        if (TaskService.gI().getIdTask(player) > ConstTask.TASK_9_1
                && TaskService.gI().getIdTask(player) < ConstTask.TASK_10_2
                && BossManager.gI().getBossTau77ByPlayer(player) == null) {
            try {
                Boss_Tau77 dt = new Boss_Tau77(Util.createIdDuongTank((int) player.id), BossData.TAU_7_7, player.zone,
                        -1, -1, (int) player.id);
            } catch (Exception ex) {

            }
        }
    }

    public void clearMap(Player player) {
        Message msg;
        try {
            msg = new Message(-22);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void send_HieuUng_ThanhCong(Player player, int iconsuccess) {
        Message msg;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(7);
            msg.writer().writeShort(iconsuccess);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    int test = 0;
    /// MENU ADMIN

    public void chat(Player player, String text) {
        if (player.getSession() != null && player.isAdmin()) {
            if (text.equals("logskill")) {
                Service.getInstance().sendThongBao(player, player.playerSkill.skillSelect.coolDown + "");
                return;
            }
            if (text.startsWith("getsl")) {
                String[] parts = text.split(" ");
                if (parts.length >= 3) {
                    short id = Short.parseShort(parts[1]);
                    int quantity = Integer.parseInt(parts[2]);
                    Item item = ItemService.gI().createNewItem(id, quantity);
                    InventoryService.gI().addItemBag(player, item, quantity);
                    InventoryService.gI().sendItemBags(player);
                    Service.getInstance().sendThongBao(player,
                            "Bạn nhận được " + item.template.name + " số lượng: " + quantity);
                    return;
                } else {
                    Service.getInstance().sendThongBao(player, "Lỗi");
                    return;
                }
            }
            if (text.equals("thread")) {
                Service.gI().sendThongBao(player, "Số lượng thread hiện tại: " + Thread.activeCount() + "");
                return;
            }
            if (text.equals("client")) {
                Client.gI().show(player);
                return;
            }
            if (text.equals("ls")) {
                try (Connection con = DBService.gI().getConnectionForGame();) {
                    Manager.SHOPS = ShopDAO.getShops(con);
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
            if (text.equals("baotri_")) {
                int giay = Integer.getInteger(text.replaceAll("baotri_", ""));
                try {
                    Maintenance.gI().start(giay * 60);
                } catch (Exception e) {
                }
                return;
            } else if (text.startsWith("danhhieu ")) {
                int sodanhhieu = Integer.parseInt(text.replace("danhhieu ", ""));
                if (player.lastTimeTitle1 == 0 && player.IdDanhHieu_1 != sodanhhieu) {
                    if (player.lastTimeTitle1 == 0) {
                        player.lastTimeTitle1 += System.currentTimeMillis() + (1000 * 60 * 60 * 24);
                    }
                    player.isTitleUse = true;
                    player.IdDanhHieu_1 = sodanhhieu;
                    Service.getInstance().point(player);
                    Service.getInstance().sendMoney(player);
                    return;
                } else if (player.lastTimeTitle1 != 0 && player.lastTimeTitle2 == 0 && player.IdDanhHieu_1 != sodanhhieu
                        && player.IdDanhHieu_2 != sodanhhieu) {
                    if (player.lastTimeTitle2 == 0) {
                        player.lastTimeTitle2 += System.currentTimeMillis() + (1000 * 60 * 60 * 24);
                    }
                    player.isTitleUse2 = true;
                    player.IdDanhHieu_2 = sodanhhieu;
                    Service.getInstance().point(player);
                    Service.getInstance().sendMoney(player);
                    return;
                } else if (player.lastTimeTitle1 != 0 && player.lastTimeTitle2 != 0 && player.lastTimeTitle3 == 0
                        && player.IdDanhHieu_1 != sodanhhieu && player.IdDanhHieu_2 != sodanhhieu
                        && player.IdDanhHieu_3 != sodanhhieu) {
                    if (player.lastTimeTitle3 == 0) {
                        player.lastTimeTitle3 += System.currentTimeMillis() + (1000 * 60 * 60 * 24);
                    }
                    player.isTitleUse3 = true;
                    player.IdDanhHieu_3 = sodanhhieu;
                    Service.getInstance().point(player);
                    Service.getInstance().sendMoney(player);
                    return;
                } else if (player.lastTimeTitle1 != 0 && player.lastTimeTitle2 != 0 && player.lastTimeTitle3 != 0
                        && player.lastTimeTitle4 == 0 && player.IdDanhHieu_1 != sodanhhieu
                        && player.IdDanhHieu_2 != sodanhhieu && player.IdDanhHieu_3 != sodanhhieu
                        && player.IdDanhHieu_4 != sodanhhieu) {

                    if (player.lastTimeTitle4 == 0) {
                        player.lastTimeTitle4 += System.currentTimeMillis() + (1000 * 60 * 60 * 24);
                    }
                    player.IdDanhHieu_4 = sodanhhieu;
                    player.isTitleUse4 = true;
                    Service.getInstance().point(player);
                    Service.getInstance().sendMoney(player);
                    return;
                } else if (player.lastTimeTitle1 != 0 && player.lastTimeTitle2 != 0 && player.lastTimeTitle3 != 0
                        && player.lastTimeTitle4 != 0 && player.lastTimeTitle5 == 0 && player.IdDanhHieu_1 != sodanhhieu
                        && player.IdDanhHieu_2 != sodanhhieu && player.IdDanhHieu_3 != sodanhhieu
                        && player.IdDanhHieu_4 != sodanhhieu && player.IdDanhHieu_5 != sodanhhieu) {
                    if (player.lastTimeTitle5 == 0) {
                        player.lastTimeTitle5 += System.currentTimeMillis() + (1000 * 60 * 60 * 24);
                    }
                    player.IdDanhHieu_5 = sodanhhieu;
                    player.isTitleUse5 = true;
                    Service.getInstance().point(player);
                    Service.getInstance().sendMoney(player);
                    return;
                }
                return;
            }
            if (text.equals("skill9ne")) {
                StringBuffer sb = new StringBuffer();
                int skillID = player.gender == 0 ? 24
                        : player.gender == 1 ? 26 : 25;
                Skill newSkill = SkillUtil.createSkill(skillID, 1);
                sb.append("|2|Ta sẽ dạy ngươi tuyệt kỹ ")
                        .append(newSkill.template.name).append("\n")
                        .append("Bí ki tuyệt kỹ ").append("/9999\n")
                        .append("Giá vàng : 500.000.000 \n")
                        .append("Giá hồng ngọc: 200");
                ConfirmDialog confirmDialog = new ConfirmDialog(sb.toString(),
                        () -> {
                            Inventory inv = player.inventory;
                            SkillUtil.setSkill(player, newSkill);
                            try {
                                Message msg = Service.getInstance()
                                        .messageSubCommand((byte) 23);
                                msg.writer().writeShort(newSkill.skillId);
                                player.sendMessage(msg);
                                msg.cleanup();
                            } catch (IOException e) {
                            }
                        });
                confirmDialog.show(player);
            }

            if (text.equals("hskill")) {
                Skill skill;
                for (int i = 0; i < player.playerSkill.skills.size(); i++) {
                    skill = player.playerSkill.skills.get(i);
                    skill.lastTimeUseThisSkill = System.currentTimeMillis() - (long) skill.coolDown;
                }
                Service.getInstance().sendTimeSkill(player);
            }
            if (text.equals("test_thu")) {
                Message msg;
                try {
                    msg = new Message(29);
                    msg.writer().writeByte(player.zone.map.zones.size());
                    for (Zone zone : player.zone.map.zones) {
                        msg.writer().writeByte(zone.zoneId);
                        int numPlayers = zone.getNumOfPlayers();
                        msg.writer().writeByte((numPlayers < 5 ? 0 : (numPlayers < 8 ? 1 : 2)));
                        msg.writer().writeByte(numPlayers);
                        msg.writer().writeByte(zone.maxPlayer);
                        msg.writer().writeByte(1);
                        msg.writer().writeUTF("test03");
                        msg.writer().writeInt(1);
                        msg.writer().writeUTF("test04");
                        msg.writer().writeInt(2);
                    }
                    sendMessAllPlayer(msg);
                    msg.cleanup();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            if (text.equals("a")) {
                showListBoss(player);
                return;
            }
            if (text.equals("get_mabuegg111")) {
                MabuEgg.createMabuEgg(player);
                Service.getInstance().sendThongBao(player, "Đã get thành công một quả trứng mabư");
                return;
            }
            if (text.equals("done_bdkb")) {
                player.clan.banDoKhoBau.doneBDKBSom = true;
                return;
            }
            if (text.equals("battu123")) {
                player.isBatTu = true;
                Service.getInstance().sendThongBao(player, "Bất tử");
                return;
            }
            if (text.startsWith("dmgoc")) {
                try {
                    int dameg = Integer.parseInt(text.replaceAll("dmgoc", ""));
                    player.nPoint.dameg = dameg;
                    this.point(player);
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (text.startsWith("hpgoc")) {
                try {
                    int hpg = Integer.parseInt(text.replaceAll("hpgoc", ""));
                    player.nPoint.hpg = hpg;
                    this.point(player);
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (text.startsWith("mpg")) {
                try {
                    int mpg = Integer.parseInt(text.replaceAll("mpg", ""));
                    player.nPoint.mpg = mpg;
                    this.point(player);
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (text.startsWith("defg")) {
                try {
                    int defg = Integer.parseInt(text.replaceAll("defg", ""));
                    player.nPoint.defg = defg;
                    this.point(player);
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (text.startsWith("crg")) {
                try {
                    int critg = Integer.parseInt(text.replaceAll("crg", ""));
                    player.nPoint.critg = critg;
                    this.point(player);
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //// MENU ADMIN
            if (text.equals("admin")) {
                NpcService.gI().createMenuConMeo(player, ConstNpc.MENU_ADMIN, 17997,
                        "|7|-----Nro Yanii-----\n"
                                + "|1|Số người chơi đang online: " + Client.gI().getPlayers().size() + "\n"
                                + "|8|Current thread server: " + Thread.activeCount() + "\n"
                                + "|6|Time start server: " + ServerManager.timeStart,
                        "Ngọc rồng", "Đệ tử", "Bảo trì", "Tìm kiếm\nngười chơi", "Đóng");
                return;
            }
            if (text.equals("quanct")) {
                NpcService.gI().createMenuConMeo(player, ConstNpc.MENU_BUFF_DO, 17997,
                        "|7|-----Nro Yanii-----\n"
                                + "|1|Số người chơi đang online: " + Client.gI().getPlayers().size(),
                        "Set\nThần Linh", "Tặng Ngọc", "Tặng SKH", "Đóng");
                return;
            }
            if (text.equals("nro")) {
                for (int i = 14; i <= 20; i++) {
                    Item item = ItemService.gI().createNewItem((short) i);
                    InventoryService.gI().addItemBag(player, item, 0);
                }
                InventoryService.gI().sendItemBags(player);
                return;
            } else if (text.equals("nrobang")) {
                for (int i = 925; i <= 931; i++) {
                    Item item = ItemService.gI().createNewItem((short) i);
                    InventoryService.gI().addItemBag(player, item, 0);
                }
                InventoryService.gI().sendItemBags(player);
                return;
            } else if (text.equals("toado")) {
                Service.getInstance().sendThongBao(player, player.location.x + " - " + player.location.y);
                return;
            } else if (text.equals("buff")) {
                Input.gI().createFormAddItem(player);
                return;
            } else if (text.equals("quanct1")) {
                Input.gI().createFormSenditem1(player);
                return;
            } else if (text.startsWith("upp")) {
                try {
                    long power = Long.parseLong(text.replaceAll("upp", ""));
                    addSMTN(player.pet, (byte) 2, power, false);
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (text.equals("nnv") && player.playerTask.taskMain.id <= 31) {
                TaskService.gI().sendNextTaskMain(player);
                return;
            } else if (text.equals("bnv") && player.playerTask.taskMain.id <= 32) {
                TaskService.gI().sendBackTaskMain(player);
                return;
            } else if (text.startsWith("up")) {
                try {
                    long power = Long.parseLong(text.replaceAll("up", ""));
                    addSMTN(player, (byte) 2, power, false);
                    return;
                } catch (Exception e) {
                }
            } else if (text.startsWith("m")) {
                try {
                    int mapId = Integer.parseInt(text.replace("m", ""));
                    Zone zone = MapService.gI().getZoneJoinByMapIdAndZoneId(player, mapId, 0);
                    if (zone != null) {
                        player.location.x = 500;
                        player.location.y = zone.map.yPhysicInTop(500, 100);
                        MapService.gI().goToMap(player, zone);
                        Service.getInstance().clearMap(player);
                        zone.mapInfo(player);
                        player.zone.loadAnotherToMe(player);
                        player.zone.load_Me_To_Another(player);
                    }
                    return;
                } catch (Exception e) {

                    Service.gI().sendThongBaoOK(player, "Cú pháp: m_MapId ( _ là dấu cách )");
                }
            }
        }
        if (text.startsWith("ten con la ")) {
            PetService.gI().changeNamePet(player, text.replaceAll("ten con la ", ""));
        }
        if (text.startsWith("boss")) {
            showListBoss1(player);
        }
        if (text.equals("fixlag")) {
            Service.getInstance().player(player);
            Service.getInstance().Send_Caitrang(player);
        }
        if (text.equals("nnvquanct") && player.playerTask.taskMain.id <= 31) {
            TaskService.gI().sendNextTaskMain(player);
            return;
        }
        if (player.pet != null) {
            if (text.equals("di theo") || text.equals("follow")) {
                if (!player.pet.isBoss) {
                    player.pet.changeStatus(Pet.FOLLOW);
                }

            } else if (text.equals("bao ve") || text.equals("protect")) {
                if (!player.pet.isBoss) {
                    player.pet.changeStatus(Pet.PROTECT);
                }

            } else if (text.equals("tan cong") || text.equals("attack")) {
                if (!player.pet.isBoss) {
                    player.pet.changeStatus(Pet.ATTACK);
                }

            } else if (text.equals("ve nha") || text.equals("go home")) {
                if (!player.pet.isBoss) {
                    player.pet.changeStatus(Pet.GOHOME);
                }
            } else if (text.equals("bien hinh")) {
                if (!player.pet.isBoss) {
                    player.pet.transform();

                }
            }
        }

        if (text.length() > 100) {
            text = text.substring(0, 100);
        }

        chatMap(player, text);
    }

    public void chatMap(Player player, String text) {
        Message msg;
        try {
            msg = new Message(44);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeUTF(text);
            sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
            Log.error(Service.class, e);
        }
    }

    public void ServerMessageVip(String text) {
        Message msg;
        try {
            msg = new Message(24);
            msg.writer().writeByte(4);
            msg.writer().writeUTF(text);
            Service.getInstance().sendMessAllPlayer(msg);
            msg.cleanup();
        } catch (Exception e) {
            Log.error(Service.class, e);
        }
    }

    public void SendDanhHieu_Teamobi(Player player) {
        Message msg;
        try {
            msg = new Message(24);
            msg.writer().writeByte(2);
            msg.writer().writeInt(0);
            msg.writer().writeByte(0);
            Service.getInstance().sendMessAllPlayer(msg);
            msg.cleanup();
        } catch (Exception e) {
            Log.error(Service.class, e);
        }
    }

    public void sendTimeWaitLogin(Session session, short second) {
        Message msg = null;
        try {
            msg = new Message(122);
            msg.writer().writeShort(second);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void chatJustForMe(Player me, Player plChat, String text) {
        Message msg;
        try {
            msg = new Message(44);
            msg.writer().writeInt((int) plChat.id);
            msg.writer().writeUTF(text);
            me.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void point(Player player) {
        player.nPoint.calPoint();
        Send_Info_NV(player);
        if (!player.isPet && !player.isBoss) {
            Message msg;
            try {
                msg = new Message(-42);
                msg.writer().writeInt(player.nPoint.hpg);
                msg.writer().writeInt(player.nPoint.mpg);
                msg.writer().writeInt(player.nPoint.dameg);
                msg.writer().writeInt(player.nPoint.hpMax);// hp full
                msg.writer().writeInt(player.nPoint.mpMax);// mp full
                msg.writer().writeInt(player.nPoint.hp);// hp
                msg.writer().writeInt(player.nPoint.mp);// mp
                msg.writer().writeByte(player.nPoint.speed);// speed
                msg.writer().writeByte(20);
                msg.writer().writeByte(20);
                msg.writer().writeByte(1);
                msg.writer().writeInt(player.nPoint.dame);// dam base
                msg.writer().writeInt(player.nPoint.def);// def full
                msg.writer().writeByte(player.nPoint.crit);// crit full
                msg.writer().writeLong(player.nPoint.tiemNang);
                msg.writer().writeShort(100);
                msg.writer().writeShort(player.nPoint.defg);
                msg.writer().writeByte(player.nPoint.critg);
                player.sendMessage(msg);
                msg.cleanup();
            } catch (Exception e) {
                Log.error(Service.class, e);
            }
        }
    }

    public void player(Player pl) {
        if (pl == null) {
            return;
        }
        Message msg;
        try {
            msg = messageSubCommand((byte) 0);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeByte(pl.playerTask.taskMain.id);
            msg.writer().writeByte(pl.gender);
            msg.writer().writeShort(pl.head);
            msg.writer().writeUTF(pl.name);
            msg.writer().writeByte(0); // cPK
            msg.writer().writeByte(pl.typePk);
            msg.writer().writeLong(pl.nPoint.power);
            msg.writer().writeShort(0);
            msg.writer().writeShort(0);
            msg.writer().writeByte(pl.gender);
            // --------skill---------

            ArrayList<Skill> skills = (ArrayList<Skill>) pl.playerSkill.skills;

            msg.writer().writeByte(pl.playerSkill.getSizeSkill());

            for (Skill skill : skills) {
                if (skill.skillId != -1) {
                    msg.writer().writeShort(skill.skillId);
                }
            }

            // ---vang---luong--luongKhoa
            long gold = pl.inventory.getGoldDisplay();
            if (pl.isVersionAbove(214)) {
                msg.writer().writeLong(gold);
            } else {
                msg.writer().writeInt((int) gold);
            }
            msg.writer().writeInt(pl.inventory.ruby);
            msg.writer().writeInt(pl.inventory.gem);

            // --------itemBody---------
            ArrayList<Item> itemsBody = (ArrayList<Item>) pl.inventory.itemsBody;
            msg.writer().writeByte(itemsBody.size());
            for (Item item : itemsBody) {
                if (!item.isNotNullItem()) {
                    msg.writer().writeShort(-1);
                } else {
                    msg.writer().writeShort(item.template.id);
                    msg.writer().writeInt(item.quantity);
                    msg.writer().writeUTF(item.getInfo());
                    msg.writer().writeUTF(item.getContent());
                    List<ItemOption> itemOptions = item.getDisplayOptions();
                    msg.writer().writeByte(itemOptions.size());
                    for (ItemOption itemOption : itemOptions) {
                        msg.writer().writeByte(itemOption.optionTemplate.id);
                        msg.writer().writeShort(itemOption.param);
                    }
                }

            }

            // --------itemBag---------
            ArrayList<Item> itemsBag = (ArrayList<Item>) pl.inventory.itemsBag;
            msg.writer().writeByte(itemsBag.size());
            for (int i = 0; i < itemsBag.size(); i++) {
                Item item = itemsBag.get(i);
                if (!item.isNotNullItem()) {
                    msg.writer().writeShort(-1);
                } else {
                    msg.writer().writeShort(item.template.id);
                    msg.writer().writeInt(item.quantity);
                    msg.writer().writeUTF(item.getInfo());
                    msg.writer().writeUTF(item.getContent());
                    List<ItemOption> itemOptions = item.getDisplayOptions();
                    msg.writer().writeByte(itemOptions.size());
                    for (ItemOption itemOption : itemOptions) {
                        msg.writer().writeByte(itemOption.optionTemplate.id);
                        msg.writer().writeShort(itemOption.param);
                    }
                }

            }

            // --------itemBox---------
            ArrayList<Item> itemsBox = (ArrayList<Item>) pl.inventory.itemsBox;
            msg.writer().writeByte(itemsBox.size());
            for (int i = 0; i < itemsBox.size(); i++) {
                Item item = itemsBox.get(i);
                if (!item.isNotNullItem()) {
                    msg.writer().writeShort(-1);
                } else {
                    msg.writer().writeShort(item.template.id);
                    msg.writer().writeInt(item.quantity);
                    msg.writer().writeUTF(item.getInfo());
                    msg.writer().writeUTF(item.getContent());
                    List<ItemOption> itemOptions = item.getDisplayOptions();
                    msg.writer().writeByte(itemOptions.size());
                    for (ItemOption itemOption : itemOptions) {
                        msg.writer().writeByte(itemOption.optionTemplate.id);
                        msg.writer().writeShort(itemOption.param);
                    }
                }
            }
            // -----------------
            DataGame.sendHeadAvatar(msg);
            // -----------------
            msg.writer().writeShort(514); // char info id - con chim thông báo
            msg.writer().writeShort(515); // char info id
            msg.writer().writeShort(537); // char info id
            msg.writer().writeByte(pl.fusion.typeFusion != ConstPlayer.NON_FUSION ? 1 : 0); // nhập thể
            // msg.writer().writeInt(1632811835); //deltatime
            msg.writer().writeInt(333); // deltatime
            msg.writer().writeByte(pl.isNewMember ? 1 : 0); // is new member

            // if (pl.isAdmin()) {
            msg.writer().writeShort(pl.getAura()); // idauraeff
            msg.writer().writeByte(pl.getEffFront());
            // }
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Log.error(Service.class, e);
        }
    }

    public Message messageNotLogin(byte command) throws IOException {
        Message ms = new Message(-29);
        ms.writer().writeByte(command);
        return ms;
    }

    public Message messageNotMap(byte command) throws IOException {
        Message ms = new Message(-28);
        ms.writer().writeByte(command);
        return ms;
    }

    public Message messageSubCommand(byte command) throws IOException {
        Message ms = new Message(-30);
        ms.writer().writeByte(command);
        return ms;
    }

    public void addSMTN(Player player, byte type, long param, boolean isOri) {
        if (player.isPet) {
            if (player.nPoint.power > player.nPoint.getPowerLimit()) {
                return;
            }
            player.nPoint.powerUp(param);
            player.nPoint.tiemNangUp(param);
            Player master = ((Pet) player).master;

            param = master.nPoint.calSubTNSM(param);
            if (master.nPoint.power < master.nPoint.getPowerLimit()) {
                master.nPoint.powerUp(param);
            }
            master.nPoint.tiemNangUp(param);
            addSMTN(master, type, param, true);
        } else {
            if (player.nPoint.power > player.nPoint.getPowerLimit()) {
                return;
            }
            switch (type) {
                case 1:
                    player.nPoint.tiemNangUp(param);
                    break;
                case 2:
                    player.nPoint.powerUp(param);
                    player.nPoint.tiemNangUp(param);
                    break;
                default:
                    player.nPoint.powerUp(param);
                    break;
            }
            PlayerService.gI().sendTNSM(player, type, param);
            if (isOri) {
                if (player.clan != null) {
                    player.clan.addSMTNClan(player, param);
                }
            }
        }
    }

    // public void congTiemNang(Player pl, byte type, int tiemnang) {
    // Message msg;
    // try {
    // msg = new Message(-3);
    // msg.writer().writeByte(type);// 0 là cộng sm, 1 cộng tn, 2 là cộng cả 2
    // msg.writer().writeInt(tiemnang);// số tn cần cộng
    // if (!pl.isPet) {
    // pl.sendMessage(msg);
    // } else {
    // ((Pet) pl).master.nPoint.powerUp(tiemnang);
    // ((Pet) pl).master.nPoint.tiemNangUp(tiemnang);
    // ((Pet) pl).master.sendMessage(msg);
    // }
    // msg.cleanup();
    // switch (type) {
    // case 1:
    // pl.nPoint.tiemNangUp(tiemnang);
    // break;
    // case 2:
    // pl.nPoint.powerUp(tiemnang);
    // pl.nPoint.tiemNangUp(tiemnang);
    // break;
    // default:
    // pl.nPoint.powerUp(tiemnang);
    // break;
    // }
    // } catch (Exception e) {
    //
    // }
    // }
    public String get_HanhTinh(int hanhtinh) {
        switch (hanhtinh) {
            case 0:
                return "Trái Đất";
            case 1:
                return "Namếc";
            case 2:
                return "Xayda";
            default:
                return "";
        }
    }

    public String getCurrStrLevel(Player pl) {
        long sucmanh = pl.nPoint.power;
        if (sucmanh < 3000) {
            return "Tân thủ";
        } else if (sucmanh < 15000) {
            return "Tập sự sơ cấp";
        } else if (sucmanh < 40000) {
            return "Tập sự trung cấp";
        } else if (sucmanh < 90000) {
            return "Tập sự cao cấp";
        } else if (sucmanh < 170000) {
            return "Tân binh";
        } else if (sucmanh < 340000) {
            return "Chiến binh";
        } else if (sucmanh < 700000) {
            return "Chiến binh cao cấp";
        } else if (sucmanh < 1500000) {
            return "Vệ binh";
        } else if (sucmanh < 15000000) {
            return "Vệ binh hoàng gia";
        } else if (sucmanh < 150000000) {
            return "Siêu " + get_HanhTinh(pl.gender) + " cấp 1";
        } else if (sucmanh < 1500000000) {
            return "Siêu " + get_HanhTinh(pl.gender) + " cấp 2";
        } else if (sucmanh < 5000000000L) {
            return "Siêu " + get_HanhTinh(pl.gender) + " cấp 3";
        } else if (sucmanh < 10000000000L) {
            return "Siêu " + get_HanhTinh(pl.gender) + " cấp 4";
        } else if (sucmanh < 40000000000L) {
            return "Thần " + get_HanhTinh(pl.gender) + " cấp 1";
        } else if (sucmanh < 50010000000L) {
            return "Thần " + get_HanhTinh(pl.gender) + " cấp 2";
        } else if (sucmanh < 60010000000L) {
            return "Thần " + get_HanhTinh(pl.gender) + " cấp 3";
        } else if (sucmanh < 70010000000L) {
            return "Giới Vương Thần cấp 1";
        } else if (sucmanh < 80010000000L) {
            return "Giới Vương Thần cấp 2";
        } else if (sucmanh < 100010000000L) {
            return "Giới Vương Thần cấp 3";
        } else if (sucmanh < 110010000000L) {
            return "Thần hủy diệt cấp 1";
        } else if (sucmanh < 120010000000L) {
            return "Thần hủy diệt cấp 2";
        } else if (sucmanh < 130010000000L) {
            return "Thần hủy diệt cấp 3";
        } else if (sucmanh < 140010000000L) {
            return "Thần hủy diệt cấp 4";
        } else if (sucmanh < 150010000000L) {
            return "Thần hủy diệt cấp 5";
        } else if (sucmanh < 160010000000L) {
            return "Thần hủy diệt cấp 6";
        } else if (sucmanh < 170010000000L) {
            return "Thần Huỷ Diệt cấp 7";
        }
        return "Thần Huỷ Diệt cấp 2";
    }

    public void hsChar(Player pl, int hp, int mp) {
        Message msg;
        try {
            pl.setJustRevivaled();
            pl.nPoint.setHp(hp);
            pl.nPoint.setMp(mp);
            if (!pl.isPet) {
                msg = new Message(-16);
                pl.sendMessage(msg);
                msg.cleanup();
                PlayerService.gI().sendInfoHpMpMoney(pl);
            }

            msg = messageSubCommand((byte) 15);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeInt(hp);
            msg.writer().writeInt(mp);
            msg.writer().writeShort(pl.location.x);
            msg.writer().writeShort(pl.location.y);
            sendMessAllPlayerInMap(pl, msg);
            msg.cleanup();

            Send_Info_NV(pl);
            PlayerService.gI().sendInfoHpMp(pl);
        } catch (Exception e) {
            Log.error(Service.class, e);
        }
    }

    public void charDie(Player pl) {
        Message msg;
        try {
            if (!pl.isPet) {
                msg = new Message(-17);
                msg.writer().writeByte((int) pl.id);
                msg.writer().writeShort(pl.location.x);
                msg.writer().writeShort(pl.location.y);
                pl.sendMessage(msg);
                msg.cleanup();
            } else {
                ((Pet) pl).lastTimeDie = System.currentTimeMillis();
            }

            msg = new Message(-8);
            msg.writer().writeShort((int) pl.id);
            msg.writer().writeByte(0); // cpk
            msg.writer().writeShort(pl.location.x);
            msg.writer().writeShort(pl.location.y);
            sendMessAnotherNotMeInMap(pl, msg);
            msg.cleanup();
            Send_Info_NV(pl);
        } catch (Exception e) {
            Log.error(Service.class, e);
        }
    }

    public void attackMob(Player pl, int mobId) {
        if (pl != null && pl.zone != null) {
            for (Mob mob : pl.zone.mobs) {
                if (mob.id == mobId) {
                    SkillService.gI().useSkill(pl, null, mob, null);
                    break;
                }
            }
        }
    }

    public void Send_Caitrang(Player player) {
        if (player != null) {
            Message msg;
            try {
                msg = new Message(-90);
                msg.writer().writeByte(1);// check type
                msg.writer().writeInt((int) player.id); // id player
                short head = player.getHead();
                short body = player.getBody();
                short leg = player.getLeg();

                msg.writer().writeShort(head);// set head
                msg.writer().writeShort(body);// setbody
                msg.writer().writeShort(leg);// set leg
                msg.writer().writeByte(player.effectSkill.isMonkey ? 1 : 0);// set khỉ
                sendMessAllPlayerInMap(player, msg);
                msg.cleanup();
            } catch (Exception e) {
                Log.error(Service.class, e);
            }
        }
    }

    public void setNotMonkey(Player player) {
        Message msg;
        try {
            msg = new Message(-90);
            msg.writer().writeByte(-1);
            msg.writer().writeInt((int) player.id);
            Service.getInstance().sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
            Log.error(Service.class, e);
        }
    }

    public void sendFlagBag(Player pl) {
        Message msg;
        try {
            msg = new Message(-64);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeByte(pl.getFlagBag());
            sendMessAllPlayerInMap(pl, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void Transport(Player pl) {
        Message msg = null;
        try {
            msg = new Message(-105);
            msg.writer().writeShort(pl.maxTime);
            msg.writer().writeByte(pl.type);
            pl.sendMessage(msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }

    public void sendThongBaoOK(Player pl, String text) {
        if (pl.isPet) {
            return;
        }
        Message msg;
        try {
            msg = new Message(-26);
            msg.writer().writeUTF(text);
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Log.error(Service.class, e);
        }
    }

    public void minigame_taixiu(Player player) {
        {
            Service.getInstance().sendThongBao(player, "Chức năng đang được cập nhật!");
        }
    }

    public void sendThongBaoOK(Session session, String text) {
        Message msg;
        try {
            msg = new Message(-26);
            msg.writer().writeUTF(text);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void sendThongBaoAllPlayer(String thongBao) {
        Message msg;
        try {
            msg = new Message(-25);
            msg.writer().writeUTF(thongBao);
            this.sendMessAllPlayer(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void sendBigMessage(Player player, int iconId, String text) {
        try {
            Message msg;
            msg = new Message(-70);
            msg.writer().writeShort(iconId);
            msg.writer().writeUTF(text);
            msg.writer().writeByte(0);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendThongBaoFromAdmin(Player player, String text) {
        sendBigMessage(player, 1139, text);
    }

    public void sendBigMessAllPlayer(int iconId, String text) {
        try {
            Message msg;
            msg = new Message(-70);
            msg.writer().writeShort(iconId);
            msg.writer().writeUTF(text);
            msg.writer().writeByte(0);
            this.sendMessAllPlayer(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public static void UpdateMocNap(Player pl, int amount) {
        try (Connection con = DBService.gI().getConnectionForSaveData();
                PreparedStatement ps = con.prepareStatement("UPDATE account SET mocnap = ? WHERE id = ?")) {
            ps.setInt(1, amount);
            ps.setInt(2, pl.getSession().userId);
            ps.executeUpdate();
        } catch (SQLException e) {

        }
    }

    public static boolean CheckMocNap(Player pl, int amount) {
        try (Connection con = DBService.gI().getConnectionForSaveData();
                PreparedStatement ps = con.prepareStatement("SELECT mocnap FROM account WHERE id = ?")) {
            ps.setInt(1, pl.getSession().userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int currentMocNap = rs.getInt("mocnap");
                if (currentMocNap >= amount) {
                    return true;
                }
            }
        } catch (SQLException e) {
        }
        return false;
    }

    public void sendThongBao(Player pl, String thongBao) {
        Message msg;
        try {
            msg = new Message(-25);
            msg.writer().writeUTF(thongBao);
            pl.sendMessage(msg);
            msg.cleanup();

        } catch (Exception e) {
        }
    }

    public void sendLogout(Player player) {
        for (int i = 5; i >= 0; i--) {
            Service.getInstance().sendThongBao(player, "Tự động đăng xuất sau " + i + " giây");
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
        }
        Client.gI().kickSession(player.getSession()); // Đăng xuất người chơi
    }

    public void sendMoney(Player pl) {
        Message msg;
        try {
            msg = new Message(6);
            long gold = pl.inventory.getGoldDisplay();
            if (pl.isVersionAbove(214)) {
                msg.writer().writeLong(gold);
            } else {
                msg.writer().writeInt((int) gold);
            }
            msg.writer().writeInt(pl.inventory.gem);
            msg.writer().writeInt(pl.inventory.ruby);
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {

        }
    }

    public void showYourNumber(Player player, String Number, String result, String finish, int type) {
        Message msg = null;
        try {
            msg = new Message(-126);
            msg.writer().writeByte(type); // 1 = RESET GAME | 0 = SHOW CON SỐ CỦA PLAYER
            if (type == 0) {
                msg.writer().writeUTF(Number);
            } else if (type == 1) {
                msg.writer().writeByte(type);
                msg.writer().writeUTF(result); //
                msg.writer().writeUTF(finish);
            }
            player.sendMessage(msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }

    public void sendToAntherMePickItem(Player player, int itemMapId) {
        Message msg;
        try {
            msg = new Message(-19);
            msg.writer().writeShort(itemMapId);
            msg.writer().writeInt((int) player.id);
            // sendMessAllPlayerIgnoreMe(player, msg);
            sendMessAnotherNotMeInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {

        }
    }

    public boolean isItemMoney(int type) {
        return type == 9 || type == 10 || type == 34;
    }

    public void useSkillNotFocus(Player player, Message m) {
        try {
            byte status = m.reader().readByte();
            switch (status) {
                case 0 -> {
                    SkillService.gI().useSkillThaiDuong(player);
                }
                case 1 -> {
                    SkillService.gI().useSkillTTNL(player);
                }
                case 4 -> {
                    SkillService.gI().sendPlayerPrepareSkill(player, player.gender == 0 ? 4000 : 3000);
                }
                case 6 -> {
                    SkillService.gI().useSkillBienKhi(player);
                }
                case 7 -> {
                    if (!SkillService.gI().canUseSkillWithCooldown(player)) {
                        return;
                    }
                    SkillService.gI().useSkillTuSat(player);
                    Zone zone = player.zone;
                    Util.setTimeout(() -> {
                        if (player.zone == zone) {
                            SkillService.gI().handleTuSat(player);
                        }
                    }, 2000, "Thread skill tu sat");
                    break;
                }
                case 8 -> {
                    if (player.gender == 1) {
                        SkillService.gI().useSkillDeTrung(player);
                    }
                }
                case 9 -> {
                    SkillService.gI().useSkillKhien(player);
                }
                case 10 -> {
                    SkillService.gI().useSkillHuytSao(player);
                }
                case 20 -> {
                    // System.out.println(status);
                    byte SkillID = m.reader().readByte();
                    short xPlayer = m.reader().readShort();
                    short yPlayer = m.reader().readShort();
                    byte dir = m.reader().readByte();
                    short x = m.reader().readShort();
                    short y = m.reader().readShort();
                    SkillService.gI().useSKillNotFocus(player, SkillID, xPlayer, yPlayer, dir, x, y);
                }
            }
        } catch (IOException e) {
            Log.error(Service.class, e);
        }
    }

    public void chatGlobal(Player pl, String text) {
        if (pl.inventory.getGem() >= 5) {
            if (pl.isAdmin() || Util.canDoWithTime(pl.lastTimeChatGlobal, 180000)) {
                if (pl.isAdmin() || pl.nPoint.power > 2000000000) {
                    pl.inventory.subGem(5);
                    sendMoney(pl);
                    pl.lastTimeChatGlobal = System.currentTimeMillis();
                    Message msg;
                    try {
                        msg = new Message(92);
                        msg.writer().writeUTF(pl.name);
                        msg.writer().writeUTF("|5|" + text);
                        msg.writer().writeInt((int) pl.id);
                        msg.writer().writeShort(pl.getHead());
                        msg.writer().writeShort(pl.getBody());
                        msg.writer().writeShort(pl.getFlagBag()); // bag
                        msg.writer().writeShort(pl.getLeg());
                        msg.writer().writeByte(0);
                        sendMessAllPlayer(msg);
                        msg.cleanup();
                    } catch (Exception e) {
                    }
                } else {
                    sendThongBao(pl, "Sức mạnh phải ít nhất 2tỷ mới có thể chat thế giới");
                }
            } else {
                sendThongBao(pl, "Không thể chat thế giới lúc này, vui lòng đợi "
                        + TimeUtil.getTimeLeft(pl.lastTimeChatGlobal, 120));
            }
        } else {
            sendThongBao(pl, "Không đủ ngọc chat thế giới");
        }
    }

    private int tiLeXanhDo = 3;

    public int xanhToDo(int n) {
        return n * tiLeXanhDo;
    }

    public int doToXanh(int n) {
        return (int) n / tiLeXanhDo;
    }

    public static final int[] flagTempId = { 363, 364, 365, 366, 367, 368, 369, 370, 371, 519, 520, 747 };
    public static final int[] flagIconId = { 2761, 2330, 2323, 2327, 2326, 2324, 2329, 2328, 2331, 4386, 4385, 2325 };

    public void openFlagUI(Player pl) {
        Message msg;
        try {
            msg = new Message(-103);
            msg.writer().writeByte(0);
            msg.writer().writeByte(flagTempId.length);
            for (int i = 0; i < flagTempId.length; i++) {
                msg.writer().writeShort(flagTempId[i]);
                msg.writer().writeByte(1);
                switch (flagTempId[i]) {
                    case 363:
                        msg.writer().writeByte(73);
                        msg.writer().writeShort(0);
                        break;
                    case 371:
                        msg.writer().writeByte(88);
                        msg.writer().writeShort(10);
                        break;
                    default:
                        msg.writer().writeByte(88);
                        msg.writer().writeShort(5);
                        break;
                }
            }
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void changeFlag(Player pl, int index) {
        Message msg;
        try {
            pl.cFlag = (byte) index;
            msg = new Message(-103);
            msg.writer().writeByte(1);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeByte(index);
            Service.getInstance().sendMessAllPlayerInMap(pl, msg);
            msg.cleanup();

            msg = new Message(-103);
            msg.writer().writeByte(2);
            msg.writer().writeByte(index);
            msg.writer().writeShort(flagIconId[index]);
            Service.getInstance().sendMessAllPlayerInMap(pl, msg);
            msg.cleanup();

            if (pl.pet != null) {
                pl.pet.cFlag = (byte) index;
                msg = new Message(-103);
                msg.writer().writeByte(1);
                msg.writer().writeInt((int) pl.pet.id);
                // System.out.println("pl.pet.id: " + pl.pet.id);
                msg.writer().writeByte(index);
                Service.getInstance().sendMessAllPlayerInMap(pl.pet, msg);
                msg.cleanup();

                msg = new Message(-103);
                msg.writer().writeByte(2);
                msg.writer().writeByte(index);
                msg.writer().writeShort(flagIconId[index]);
                Service.getInstance().sendMessAllPlayerInMap(pl.pet, msg);
                msg.cleanup();
            }
            pl.lastTimeChangeFlag = System.currentTimeMillis();
        } catch (Exception e) {
            Log.error(Service.class, e);
        }
    }

    public void sendFlagPlayerToMe(Player me, Player pl) {
        Message msg;
        try {
            msg = new Message(-103);
            msg.writer().writeByte(2);
            msg.writer().writeByte(pl.cFlag);
            msg.writer().writeShort(flagIconId[pl.cFlag]);
            me.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void chooseFlag(Player pl, int index) {
        if (Util.canDoWithTime(pl.lastTimeChangeFlag, 60000)) {
            if (!MapService.gI().isMapBlackBallWar(pl.zone.map.mapId)
                    && !MapService.gI().isMapMabuWar(pl.zone.map.mapId) && !pl.isHoldBlackBall) {
                changeFlag(pl, index);
            } else {
                sendThongBao(pl, "Không thể đổi cờ ở khu vực này");
            }
        } else {
            sendThongBao(pl, "Không thể đổi cờ lúc này! Vui lòng đợi " + TimeUtil.getTimeLeft(pl.lastTimeChangeFlag, 60)
                    + " nữa!");
        }
    }

    public void attackPlayer(Player pl, int idPlAnPem) {
        SkillService.gI().useSkill(pl, pl.zone.getPlayerInMap(idPlAnPem), null, null);
    }

    public void openZoneUI(Player pl) {
        if (!pl.isAdmin()
                && (pl.zone == null || pl.zone.map.isMapOffline || MapService.gI().isMapOfflineNe(pl.zone.map.mapId))) {
            sendThongBaoOK(pl, "Không thể đổi khu vực trong map này");
            return;
        }
        int mapid = pl.zone.map.mapId;
        if (!pl.isAdmin() && (MapService.gI().isMapDoanhTrai(mapid) || MapService.gI().isMapBanDoKhoBau(mapid)
                || MapService.gI().isMapKhiGas(mapid) || mapid == 120 || MapService.gI().isMapVS(mapid) || mapid == 126
                || pl.zone instanceof ZDungeon)) {
            sendThongBaoOK(pl, "Không thể đổi khu vực trong map này");
            return;
        }
        Message msg;
        try {
            msg = new Message(29);
            msg.writer().writeByte(pl.zone.map.zones.size());
            for (Zone zone : pl.zone.map.zones) {
                msg.writer().writeByte(zone.zoneId);
                int numPlayers = zone.getNumOfPlayers();
                msg.writer().writeByte((numPlayers < 5 ? 0 : (numPlayers < 8 ? 1 : 2)));
                msg.writer().writeByte(numPlayers);
                msg.writer().writeByte(zone.maxPlayer);
                msg.writer().writeByte(0);
            }
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void UpdateCoolDown(Player pl) {
        Message msg;
        try {
            msg = new Message(-94);
            for (Skill skill : pl.playerSkill.skills) {
                skill.coolDown = 0;
                msg.writer().writeShort(skill.skillId);
                int leftTime = (int) (skill.lastTimeUseThisSkill + skill.coolDown - System.currentTimeMillis());
                if (leftTime < 0) {
                    leftTime = 0;
                }
                msg.writer().writeInt(leftTime);
            }
            pl.sendMessage(msg);
            pl.nPoint.setMp(pl.nPoint.mpMax);
            PlayerService.gI().sendInfoHpMpMoney(pl);
            msg.cleanup();

        } catch (Exception e) {
        }
    }

    public void sendTimeSkill(Player pl) {
        Message msg;
        try {
            msg = new Message(-94);
            for (Skill skill : pl.playerSkill.skills) {
                msg.writer().writeShort(skill.skillId);
                int timeLeft = (int) (skill.lastTimeUseThisSkill + skill.coolDown - System.currentTimeMillis());
                if (timeLeft < 0) {
                    timeLeft = 0;
                }
                msg.writer().writeInt(timeLeft);
            }
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void dropItemMap(Zone zone, ItemMap item) {
        final int PLAYER_ID_FOR_ALL = -2;

        Message msg = null;
        try {
            msg = new Message(68);
            msg.writer().writeShort(item.itemMapId);
            msg.writer().writeShort(item.itemTemplate.id);
            msg.writer().writeShort(item.x);
            msg.writer().writeShort(item.y);
            msg.writer().writeInt((int) item.playerId);

            if (item.playerId == PLAYER_ID_FOR_ALL) {
                msg.writer().writeShort(item.range);
            }

            sendMessAllPlayerInMap(zone, msg);
        } catch (Exception e) {
            e.printStackTrace(); // In lỗi để biết lý do
        } finally {
            if (msg != null) {
                msg.cleanup(); // Dọn dẹp tài nguyên
            }
        }
    }

    public void dropItemMapForMe(Player player, ItemMap item) {
        final int PLAYER_ID_FOR_ALL = -2;

        Message msg = null;
        try {
            msg = new Message(68);
            msg.writer().writeShort(item.itemMapId);
            msg.writer().writeShort(item.itemTemplate.id);
            msg.writer().writeShort(item.x);
            msg.writer().writeShort(item.y);
            msg.writer().writeInt((int) item.playerId);

            if (item.playerId == PLAYER_ID_FOR_ALL) {
                msg.writer().writeShort(item.range);
            }

            player.sendMessage(msg);
        } catch (Exception e) {
            Log.error(Service.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void showInfoPet(Player pl) {
        if (pl != null && pl.pet != null) {
            Message msg;
            try {
                msg = new Message(-107);
                msg.writer().writeByte(2);
                msg.writer().writeShort(pl.pet.getAvatar());
                msg.writer().writeByte(pl.pet.inventory.itemsBody.size());

                for (Item item : pl.pet.inventory.itemsBody) {
                    if (!item.isNotNullItem()) {
                        msg.writer().writeShort(-1);
                    } else {
                        msg.writer().writeShort(item.template.id);
                        msg.writer().writeInt(item.quantity);
                        msg.writer().writeUTF(item.getInfo());
                        msg.writer().writeUTF(item.getContent());

                        List<ItemOption> itemOptions = item.getDisplayOptions();
                        int countOption = itemOptions.size();
                        msg.writer().writeByte(countOption);
                        for (ItemOption iop : itemOptions) {
                            msg.writer().writeByte(iop.optionTemplate.id);
                            msg.writer().writeShort(iop.param);
                        }
                    }
                }

                if (pl.getSession() != null && pl.getSession().version == 239) {
                    msg.writer().writeInt(pl.pet.nPoint.hpg);
                    msg.writer().writeInt(pl.pet.nPoint.mpg);
                    msg.writer().writeInt(pl.pet.nPoint.dameg);
                    msg.writer().writeInt(pl.pet.nPoint.defg);
                    msg.writer().writeInt(pl.pet.nPoint.critg);
                }
                msg.writer().writeInt(pl.pet.nPoint.hp); // hp
                msg.writer().writeInt(pl.pet.nPoint.hpMax); // hpfull
                msg.writer().writeInt(pl.pet.nPoint.mp); // mp
                msg.writer().writeInt(pl.pet.nPoint.mpMax); // mpfull
                msg.writer().writeInt(pl.pet.nPoint.dame); // damefull
                msg.writer().writeUTF(pl.pet.name); // name
                msg.writer().writeUTF(getCurrStrLevel(pl.pet)); // curr level
                msg.writer().writeLong(pl.pet.nPoint.power); // power
                msg.writer().writeLong(pl.pet.nPoint.tiemNang); // tiềm năng
                msg.writer().writeByte(pl.pet.getStatus()); // status
                msg.writer().writeShort(pl.pet.nPoint.stamina); // stamina
                msg.writer().writeShort(pl.pet.nPoint.maxStamina); // stamina full
                msg.writer().writeByte(pl.pet.nPoint.crit); // crit
                msg.writer().writeShort(pl.pet.nPoint.def); // def
                int sizeSkill = pl.pet.playerSkill.skills.size();
                msg.writer().writeByte(4); // counnt pet skill
                for (int i = 0; i < pl.pet.playerSkill.skills.size(); i++) {
                    if (pl.pet.playerSkill.skills.get(i).skillId != -1) {
                        msg.writer().writeShort(pl.pet.playerSkill.skills.get(i).skillId);
                    } else {
                        switch (i) {
                            case 1:
                                msg.writer().writeShort(-1);
                                msg.writer().writeUTF("Cần 150 Tr sức mạnh để mở");
                                break;
                            case 2:
                                msg.writer().writeShort(-1);
                                msg.writer().writeUTF("Cần 1,5 Tỉ sức mạnh để mở");
                                break;
                            case 3:
                                msg.writer().writeShort(-1);
                                msg.writer().writeUTF("Cần 20 Tỉ sức mạnh để mở");
                                break;
                            default:
                                msg.writer().writeShort(-1);
                                msg.writer().writeUTF("Cần đạt sức mạnh 60tỷ\nđể mở");
                                break;
                        }
                    }
                }
                pl.sendMessage(msg);
                msg.cleanup();

            } catch (Exception e) {
                Log.error(Service.class, e);
            }
        }
    }

    // public void sendItemTime(Player pl, int itemId, int time) {
    // Message msg;
    // try {
    // msg = new Message(-106);
    // msg.writer().writeShort(itemId);
    // msg.writer().writeShort(time);
    // pl.sendMessage(msg);
    // } catch (Exception e) {
    // }
    // }
    // public void removeItemTime(Player pl, int itemTime) {
    // sendItemTime(pl, itemTime, 0);
    // }
    public void sendSpeedPlayer(Player pl, int speed) {
        Message msg;
        try {
            msg = Service.getInstance().messageSubCommand((byte) 8);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeByte(speed != -1 ? speed : pl.nPoint.speed);
            pl.sendMessage(msg);
            // Service.getInstance().sendMessAllPlayerInMap(pl.map, msg);
            msg.cleanup();
        } catch (Exception e) {
            Log.error(Service.class, e);
        }
    }

    public void setPos(Player player, int x, int y) {
        player.location.x = x;
        player.location.y = y;
        Message msg;
        try {
            msg = new Message(123);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeShort(x);
            msg.writer().writeShort(y);
            msg.writer().writeByte(1);
            sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void getPlayerMenu(Player player, int playerId) {
        Message msg;
        try {
            msg = new Message(-79);
            Player pl = player.zone.getPlayerInMap(playerId);
            if (pl != null) {
                msg.writer().writeInt(playerId);
                msg.writer().writeLong(pl.nPoint.power);
                msg.writer().writeUTF(Service.getInstance().getCurrStrLevel(pl));
                player.sendMessage(msg);
            }
            msg.cleanup();
            if (player.isAdmin()) {
                SubMenuService.gI().showMenuForAdmin(player);
            }
        } catch (Exception e) {
            Log.error(Service.class, e);
        }
    }

    public void subMenuPlayer(Player player) {
        Message msg;
        try {
            msg = messageSubCommand((byte) 63);
            msg.writer().writeByte(1);
            msg.writer().writeUTF("String 1");
            msg.writer().writeUTF("String 2");
            msg.writer().writeShort(550);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void hideWaitDialog(Player pl) {
        Message msg;
        try {
            msg = new Message(-99);
            msg.writer().writeByte(-1);
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void chatPrivate(Player plChat, Player plReceive, String text) {
        Message msg;
        try {
            msg = new Message(Cmd.CHAT_THEGIOI_SERVER);
            msg.writer().writeUTF(plChat.name);
            msg.writer().writeUTF("|5|" + text);
            msg.writer().writeInt((int) plChat.id);
            msg.writer().writeShort(plChat.getHead());
            Part part = PartManager.getInstance().find(plChat.getHead());
            msg.writer().writeShort(part.getIcon(0));
            msg.writer().writeShort(plChat.getBody());
            msg.writer().writeShort(plChat.getFlagBag()); // bag
            msg.writer().writeShort(plChat.getLeg());
            msg.writer().writeByte(1);
            plChat.sendMessage(msg);
            plReceive.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void changePassword(Player player, String oldPass, String newPass, String rePass) {
        if (player.getSession().pp.equals(oldPass)) {
            if (newPass.length() >= 6) {
                if (newPass.equals(rePass)) {
                    player.getSession().pp = newPass;
                    AccountDAO.updateAccount(player.getSession());
                    Service.getInstance().sendThongBao(player, "Đổi mật khẩu thành công!");
                } else {
                    Service.getInstance().sendThongBao(player, "Mật khẩu nhập lại không đúng!");
                }
            } else {
                Service.getInstance().sendThongBao(player, "Mật khẩu ít nhất 6 ký tự!");
            }
        } else {
            Service.getInstance().sendThongBao(player, "Mật khẩu cũ không đúng!");
        }
    }

    public void switchToCreateChar(Session session) {
        Message msg;
        try {
            msg = new Message(2);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void sendCaption(Session session, byte gender) {
        Message msg;
        try {
            List<Caption> captions = CaptionManager.getInstance().getCaptions();
            msg = new Message(-41);
            msg.writer().writeByte(captions.size());
            for (Caption caption : captions) {
                msg.writer().writeUTF(caption.getCaption(gender));
            }
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void sendHavePet(Player player) {
        Message msg;
        try {
            msg = new Message(-107);
            msg.writer().writeByte(player.pet == null ? 0 : 1);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void sendWaitToLogin(Session session, int secondsWait) {
        Message msg;
        try {
            msg = new Message(122);
            msg.writer().writeShort(secondsWait);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Log.error(Service.class, e);
        }
    }

    public void sendMessage(Session session, int cmd, String path) {
        Message msg;
        try {
            msg = new Message(cmd);
            msg.writer().write(FileIO.readFile(path));
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void sendTopRank(Player pl) {
        Message msg;
        try {
            msg = new Message(Cmd.THELUC);
            msg.writer().writeInt(1);
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void createItemMap(Player player, int tempId) {
        ItemMap itemMap = new ItemMap(player.zone, tempId, 1, player.location.x, player.location.y, player.id);
        dropItemMap(player.zone, itemMap);
    }

    public void sendNangDong(Player player) {
        Message msg;
        try {
            msg = new Message(-97);
            msg.writer().writeInt(100);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void sendPowerInfo(Player pl, String info, short point) {
        Message m = null;
        try {
            m = new Message(-115);
            m.writer().writeUTF(info);
            m.writer().writeShort(point);
            m.writer().writeShort(20);
            m.writer().writeShort(10);
            m.writer().flush();
            if (pl != null && pl.getSession() != null) {
                pl.sendMessage(m);
            }
            m.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (m != null) {
                m.cleanup();
            }
        }
    }

    public void setMabuHold(Player pl, byte type) {
        Message m = null;
        try {
            m = new Message(52);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (m != null) {
                m.cleanup();
            }
        }
    }

    public void sendPercentMabuEgg(Player player, byte percent) {
        try {
            Message msg = new Message(-117);
            msg.writer().writeByte(percent);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void sendPlayerInfo(Player player) {
        try {
            Message msg = messageSubCommand((byte) 7);
            try {
                msg.writer().writeInt((int) player.id);
            } catch (IOException e) {

                // Xử lý ngoại lệ khi ghi dữ liệu vào writer
            }
            if (player.clan != null) {
                msg.writer().writeInt(player.clan.id);
            } else {
                msg.writer().writeInt(-1);
            }
            int level = CaptionManager.getInstance().getLevel(player);
            msg.writer().writeByte(level);
            msg.writer().writeBoolean(player.isInvisible);
            msg.writer().writeByte(player.typePk);
            msg.writer().writeByte(player.gender);
            msg.writer().writeByte(player.gender);
            msg.writer().writeShort(player.getHead());
            msg.writer().writeUTF(player.name);
            msg.writer().writeInt(player.nPoint.hp);
            msg.writer().writeInt(player.nPoint.hpMax);
            msg.writer().writeShort(player.getBody());
            msg.writer().writeShort(player.getLeg());
            msg.writer().writeByte(player.getFlagBag());
            msg.writer().writeByte(-1);
            msg.writer().writeShort(player.location.x);
            msg.writer().writeShort(player.location.y);
            msg.writer().writeShort(0);
            msg.writer().writeShort(0);
            msg.writer().writeByte(0);
            sendMessAllPlayer(msg);
            msg.cleanup();
        } catch (Exception e) {

        }
    }

    public void getCurrLevel(Player pl) {

    }

    public int getWidthHeightImgPetFollow(int id) {
        if (id == 15067) {
            return 65;
        }
        return 75;
    }

    public void showTopTask(Player player) {
        List<Player> list = TopManager.getInstance().getListTask();
        Message msg = new Message(Cmd.TOP);
        try {
            msg.writer().writeByte(0);
            msg.writer().writeUTF("Top N.Vu");
            msg.writer().writeByte(list.size());
            for (int i = 0; i < list.size(); i++) {
                Player pl = list.get(i);
                msg.writer().writeInt(i + 1);
                msg.writer().writeInt((int) pl.id);
                msg.writer().writeShort(pl.getHead());
                if (player.isVersionAbove(220)) {
                    Part part = PartManager.getInstance().find(pl.getHead());
                    msg.writer().writeShort(part.getIcon(0));
                }
                msg.writer().writeShort(pl.getBody());
                msg.writer().writeShort(pl.getLeg());
                msg.writer().writeUTF(pl.name);

                msg.writer().writeUTF(Client.gI().getPlayer(pl.id) != null ? "Online" : "");
                msg.writer().writeUTF("NV: " + pl.topTask);
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
            // e.printStackTrace();
        }
    }

    public void showTopVnd(Player player) {
        List<Player> list = TopManager.getInstance().getListVnd();
        Message msg = new Message(Cmd.TOP);
        try {
            msg.writer().writeByte(0);
            msg.writer().writeUTF("Top VND");
            msg.writer().writeByte(list.size());
            for (int i = 0; i < list.size(); i++) {
                Player pl = list.get(i);
                msg.writer().writeInt(i + 1);
                msg.writer().writeInt((int) pl.id);
                msg.writer().writeShort(pl.getHead());
                if (player.isVersionAbove(220)) {
                    Part part = PartManager.getInstance().find(pl.getHead());
                    msg.writer().writeShort(part.getIcon(0));
                }
                msg.writer().writeShort(pl.getBody());
                msg.writer().writeShort(pl.getLeg());
                msg.writer().writeUTF(pl.name);

                msg.writer().writeUTF(Client.gI().getPlayer(pl.id) != null ? "Online" : "");
                msg.writer().writeUTF(pl.topVnd + " VND");
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
            // e.printStackTrace();
        }
    }

    public void showTopNangDong(Player player) {
        List<Player> list = TopManager.getInstance().getListNangDong();
        Message msg = new Message(Cmd.TOP);
        try {
            msg.writer().writeByte(0);
            msg.writer().writeUTF("Top Năng Động");
            msg.writer().writeByte(list.size());
            for (int i = 0; i < list.size(); i++) {
                Player pl = list.get(i);
                msg.writer().writeInt(i + 1);
                msg.writer().writeInt((int) pl.id);
                msg.writer().writeShort(pl.getHead());
                if (player.isVersionAbove(220)) {
                    Part part = PartManager.getInstance().find(pl.getHead());
                    msg.writer().writeShort(part.getIcon(0));
                }
                msg.writer().writeShort(pl.getBody());
                msg.writer().writeShort(pl.getLeg());
                msg.writer().writeUTF(pl.name);

                msg.writer().writeUTF(Client.gI().getPlayer(pl.id) != null ? "Online" : "");
                msg.writer().writeUTF(pl.topNangDong + " Điểm");
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {

        }
    }

    public void showTopPower(Player player) {
        TopPowerManager.getInstance().load();
        List<Player> list = TopPowerManager.getInstance().getList();
        Message msg = new Message(Cmd.TOP);
        try {
            msg.writer().writeByte(0);
            msg.writer().writeUTF("TOP 100 Sức Mạnh");
            msg.writer().writeByte(list.size());
            for (int i = 0; i < list.size(); i++) {
                Player pl = list.get(i);
                msg.writer().writeInt(i + 1);
                msg.writer().writeInt((int) pl.id);
                msg.writer().writeShort(pl.getHead());
                Part part = PartManager.getInstance().find(pl.getHead());
                msg.writer().writeShort(part.getIcon(0));
                msg.writer().writeShort(pl.getBody());
                msg.writer().writeShort(pl.getLeg());
                msg.writer().writeUTF(pl.name);
                // Thêm thời gian vào chuỗi Sức Mạnh
                String inputDateString = pl.lastimelogin.toString();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                try {
                    Date inputDate = dateFormat.parse(inputDateString);
                    Date currentDate = new Date();
                    long timeDifferenceInMillis = currentDate.getTime() - inputDate.getTime();
                    long giây = timeDifferenceInMillis / 1000;
                    msg.writer().writeUTF(Util.convertSecondsToTime2(giây));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                msg.writer().writeUTF("...");
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {

        }
    }

    public void showToplevelWhis(Player player) {
        TopKillWhisManager.getInstance().load();
        List<Player> list = TopKillWhisManager.getInstance().getList();
        Message msg = new Message(Cmd.TOP);
        try {
            msg.writer().writeByte(0);
            msg.writer().writeUTF("TOP 100");
            msg.writer().writeByte(list.size());
            for (int i = 0; i < list.size(); i++) {
                Player pl = list.get(i);
                msg.writer().writeInt(i + 1);
                msg.writer().writeInt((int) pl.id);
                msg.writer().writeShort(pl.getHead());
                Part part = PartManager.getInstance().find(pl.getHead());
                msg.writer().writeShort(part.getIcon(0));
                msg.writer().writeShort(pl.getBody());
                msg.writer().writeShort(pl.getLeg());
                msg.writer().writeUTF(pl.name);

                msg.writer().writeUTF("LV: " + pl.levelKillWhisDone + " với "
                        + Util.convertMillisecondsToSeconds(pl.timeKillWhis) + " giây");

                String inputDateString = pl.lastimelogin.toString();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                try {
                    Date inputDate = dateFormat.parse(inputDateString);
                    Date currentDate = new Date();
                    long timeDifferenceInMillis = currentDate.getTime() - inputDate.getTime();
                    long giây = timeDifferenceInMillis / 1000;
                    msg.writer().writeUTF(Util.convertSecondsToTime2(giây));
                } catch (ParseException e) {

                }
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void bossUseSkillNotFocus(Player player, Skill skill) {
        switch (skill.template.id) {
            case Skill.TAI_TAO_NANG_LUONG ->
                SkillService.gI().useSkillTTNL(player);
            case Skill.THAI_DUONG_HA_SAN ->
                SkillService.gI().useSkillThaiDuong(player);
            case Skill.BIEN_KHI ->
                SkillService.gI().useSkillBienKhi(player);
            case Skill.KHIEN_NANG_LUONG ->
                SkillService.gI().useSkillKhien(player);
            case Skill.TU_SAT -> {
                SkillService.gI().useSkillTuSat(player);
                Util.setTimeout(() -> {
                    SkillService.gI().handleTuSat(player);
                }, 2000, "tu sat");
            }
        }
    }

    public void moveFast(Broly broly, int x, int y, long id) {
        try {
            Message m = new Message(58);
            m.writer().writeInt((int) broly.id);
            m.writer().writeShort(x);
            m.writer().writeShort(y);
            m.writer().writeInt((int) id);
            sendMessAllPlayerInMap(broly.zone, m);
            m.cleanup();
        } catch (IOException e) {
        }
    }
}

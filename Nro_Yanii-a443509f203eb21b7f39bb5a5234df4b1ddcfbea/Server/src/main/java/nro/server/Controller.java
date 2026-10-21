package nro.server;

import nro.consts.*;
import nro.data.DataGame;
import nro.data.ItemData;
import nro.jdbc.DBService;
import nro.models.kigui.KiGuiShop;
import nro.models.map.war.BlackBallWar;
import nro.models.npc.NpcManager;
import nro.models.player.Player;
import nro.models.skill.PlayerSkill;
import nro.noti.NotiManager;
import nro.resources.Resources;
import nro.server.io.Message;
import nro.server.io.Session;
import nro.services.*;
import nro.services.func.*;
import nro.utils.Log;
import nro.utils.Util;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import nro.models.boss.Boss;
import nro.models.boss.BossManager;
import nro.models.skill.Skill;

public class Controller {

    private static Controller instance;

    public static Controller getInstance() {
        if (instance == null) {
            instance = new Controller();
        }
        return instance;
    }

    private static final Logger logger = Logger.getLogger(Controller.class);

    public void onMessage(Session _session, Message _msg) {
        long st = System.currentTimeMillis();
        try {
            Player player = _session.player;
            byte cmd = _msg.command;
            if (Manager.debug) {
                System.out.println("CMD receive: " + cmd);
            }
            switch (cmd) {
                case Cmd.KIGUI:
                    KiGuiShop.getInstance().handler(player, _msg);
                    break;
                case Cmd.ACHIEVEMENT:
                    TaskService.gI().rewardAchivement(player, _msg.reader().readByte());
                    break;
                case Cmd.RADA_CARD:
                    RadaService.getInstance().controller(player, _msg);
                    break;
                case -127:
                    if (player != null) {
                        LuckyRoundService.gI().readOpenBall(player, _msg);
                    }
                    break;
                case -125:
                    if (player != null) {
                        Input.gI().doInput(player, _msg);
                    }
                    break;
                case 112:
                    if (player != null) {
                        IntrinsicService.gI().showMenu(player);
                    }
                    break;
                case -34:
                    if (player != null) {
                        switch (_msg.reader().readByte()) {
                            case 1:
                                player.magicTree.openMenuTree();
                                break;
                            case 2:
                                player.magicTree.loadMagicTree();
                                break;
                        }
                    }
                    break;
                case -99:
                    if (player != null) {
                        FriendAndEnemyService.gI().controllerEnemy(player, _msg);
                    }
                    break;
                case 18:
                    if (player != null) {
                        FriendAndEnemyService.gI().goToPlayerWithYardrat(player, _msg);
                    }
                    break;
                case -72:
                    if (player != null) {
                        FriendAndEnemyService.gI().chatPrivate(player, _msg);
                    }
                    break;
                case -80:
                    if (player != null) {
                        FriendAndEnemyService.gI().controllerFriend(player, _msg);
                    }
                    break;
                case -59:
                    if (player != null) {
                        PVPServcice.gI().controller(player, _msg);
                    }
                    break;
                case -86:
                    if (player != null) {
                        TransactionService.gI().controller(player, _msg);
                    }
                    break;
                case -107:

                    if (player != null) {
                        Service.getInstance().showInfoPet(player);
                    }
                    break;
                case -108:
                    if (player != null && player.pet != null) {
                        player.pet.changeStatus(_msg.reader().readByte());
                    }
                    break;
                case 6: //buy item
                    if (player != null) {
                        byte typeBuy = _msg.reader().readByte();
                        int tempId = _msg.reader().readShort();
                        int quantity = 0;
                        try {
                            quantity = _msg.reader().readShort();
                        } catch (Exception e) {
                        }
                        ShopService.gI().buyItem(player, typeBuy, tempId);
                    }
                    break;
                case 7: //sell item
                    if (player != null) {
                        int action = _msg.reader().readByte();
                        int where = _msg.reader().readByte();
                        int index = _msg.reader().readShort();
                        if (action == 0) {
                            ShopService.gI().showConfirmSellItem(player, where, index - 4);
                        } else {
                            ShopService.gI().sellItem(player, where, index);
                        }
                    }
                    break;
                case 29:
                    if (player != null) {
                        Service.getInstance().openZoneUI(player);
                    }
                    break;
                case 21:
                    if (player != null) {
                        int zoneId = _msg.reader().readByte();
                        ChangeMapService.gI().changeZone(player, zoneId);
                    }
                    break;
                case -71:
                    if (player != null) {
                        ChatGlobalService.gI().chat(player, _msg.reader().readUTF());
                    }
                    break;
                case -79:
                    if (player != null) {
                        Service.getInstance().getPlayerMenu(player, _msg.reader().readInt());
                    }
                    break;
                case -113:
                    if (player != null) {
                        PlayerSkill playerSkill = player.playerSkill;
                        int len = _msg.reader().available();
                        for (int i = 0; i < len; i++) {
                            byte b = _msg.reader().readByte();
                            playerSkill.skillShortCut[i] = b;
                        }
                        playerSkill.sendSkillShortCut();
                    }
                    break;
                case 42:
                    Service.getInstance().regisAccount(_session, _msg);
                    break;
                case -101:
//                    try {
//                    Connection con = DBService.gI().getConnectionForGame();
//                    PreparedStatement ps = con.prepareStatement("SELECT id FROM account ORDER BY id DESC LIMIT 1");
//                    ResultSet rs = ps.executeQuery();
//                    if (rs.next()) {
//                        int lastId = rs.getInt("id") + 1;
//                        String user = "userAo" + lastId;
//                        String pass = "passAo" + lastId;
                    login2(_session, _msg);
//                        _session.login(user, pass);
//                        System.out.println("Dang Ky Thanh Cong");
//                    } else {
//                    }
//                } catch (SQLException e) {
//                    Log.error(AccountDAO.class, e);
//                }

                    break;
                case -103:
                    if (player != null) {
                        byte act = _msg.reader().readByte();
                        if (act == 0) {
                            Service.getInstance().openFlagUI(player);
                        } else if (act == 1) {
                            Service.getInstance().chooseFlag(player, _msg.reader().readByte());
                        } else {
                        }
                    }
                    break;
                case Cmd.LOCK_INVENTORY:
                    int mabaove = _msg.reader().readInt();
                    if (player != null) {
                        if (player.MaBaoVe != 0) {
                            if (mabaove == player.getSession().MaBaoVe) {
                                if (!player.isUseMaBaoVe) {
                                    NpcService.gI().createMenuConMeo(player, 25100305, -1, "Tài khoản không được bảo vệ\nBạn muốn bật chức năng bảo vệ tài khoản?", "Đồng ý", "Từ chối");
                                    break;
                                } else {
                                    NpcService.gI().createMenuConMeo(player, 25100304, -1, "Tài khoản đang được bảo vệ\bBạn có muốn tắt bảo vệ không?", "Đồng ý", "Từ chối");
                                    break;
                                }
                            } else {
                                Service.getInstance().sendThongBao(player, "Mật khẩu không đúng. Vui lòng kiểm tra lại");
                                break;
                            }
                        } else {
                            NpcService.gI().createMenuConMeo(player, 25100303, -1, "Bạn chưa từng kích hoạt chức năng mã bảo vệ để kích hoạt bạn cần có 30K vàng, mật khẩu của bạn là:" + mabaove, "Đồng ý", "Từ chối");
                            player.MaBaoVe_TamThoi = mabaove;
                            break;
                        }
                    }
                case -7:
                    if (player != null) {
                        int toX = player.location.x;
                        int toY = player.location.y;
                        try {
                            byte b = _msg.reader().readByte();
                            toX = _msg.reader().readShort();
                            toY = _msg.reader().readShort();
                        } catch (Exception e) {
                        }
                        player.playerTask.achivements.get(ConstAchive.KHINH_CONG_THANH_THAO).count++;
                        PlayerService.gI().playerMove(player, toX, toY);
                    }
                    break;
                case Cmd.GET_IMAGE_SOURCE:
                    Resources.getInstance().downloadResources(_session, _msg);
                    break;
                case -81:
                    if (player != null) {
                        _msg.reader().readByte();
                        int[] indexItem = new int[_msg.reader().readByte()];
                        for (int i = 0; i < indexItem.length; i++) {
                            indexItem[i] = _msg.reader().readByte();
                        }
                        try {
                            CombineServiceNew.gI().showInfoCombine(player, indexItem);
                        } catch (Exception e) {
                            ///VMN
                        }

                    }
                    break;
                case -87:
                    DataGame.updateData(_session);
                    break;
                case Cmd.FINISH_UPDATE:
                    _session.finishUpdate();
                    break;
                case Cmd.REQUEST_ICON:
                    int id = _msg.reader().readInt();
                    Resources.getInstance().downloadIconData(_session, id);
                    break;
                case Cmd.GET_IMG_BY_NAME:
                    Resources.getInstance().sendImageByName(_session, _msg.reader().readUTF());
                    break;
                case -66:
                    int effId = _msg.reader().readShort();
                    int idT = effId;
                    if (effId == 25) {
                        idT = 50;
                    }
                    Resources.effData(_session, effId, idT);
                    break;
                case -62:
                    if (player != null) {
                        FlagBagService.gI().sendIconFlagChoose(player, _msg.reader().readByte());
                    }
                    break;
                case -63:
                    if (player != null) {
                        FlagBagService.gI().sendIconEffectFlag(player, _msg.reader().readByte());
                    }
                    break;
                case Cmd.BACKGROUND_TEMPLATE:
                    int bgId = _msg.reader().readShort();
                    Resources.getInstance().downloadBGTemplate(_session, bgId);
                    break;
                case 22:
                    if (player != null) {
                        _msg.reader().readByte();
                        NpcManager.getNpc(ConstNpc.DAU_THAN).confirmMenu(player, _msg.reader().readByte());
                    }
                    break;
                case -33:
                case -23:
                    if (player != null) {
                        try {
                            player.zone.changeMapWaypoint(player);
                        } catch (Exception e) {

                        }

                        Service.getInstance().hideWaitDialog(player);
                    }
                    break;
                case -45:
                    if (player != null) {
                        Service.getInstance().useSkillNotFocus(player, _msg);
                    }
                    break;
                case -46:
                    if (player != null) {
                        ClanService.gI().getClan(player, _msg);
                    }
                    break;
                case -51:
                    if (player != null) {
                        ClanService.gI().clanMessage(player, _msg);
                    }
                    break;
                case -54:
                    if (player != null) {
                        ClanService.gI().clanDonate(player, _msg);
//                        Service.getInstance().sendThongBao(player, "Can not invoke clan donate");
                    }
                    break;
                case -49:
                    if (player != null) {
                        ClanService.gI().joinClan(player, _msg);
                    }
                    break;
                case -50:
                    if (player != null) {
                        ClanService.gI().sendListMemberClan(player, _msg.reader().readInt());
                    }
                    break;
                case -56:
                    if (player != null) {
                        ClanService.gI().clanRemote(player, _msg);
                    }
                    break;
                case -47:
                    if (player != null) {
                        ClanService.gI().sendListClan(player, _msg.reader().readUTF());
                    }
                    break;
                case -55:
                    if (player != null) {
                        ClanService.gI().showMenuLeaveClan(player);
                    }
                    break;
                case -57:
                    if (player != null) {
                        ClanService.gI().clanInvite(player, _msg);
                    }
                    break;
                case -40:
                    UseItem.gI().getItem(_session, _msg);
                    break;
                case -41:
                    Service.getInstance().sendCaption(_session, _msg.reader().readByte());
                    break;
                case -43:
                    if (player != null) {
                        UseItem.gI().doItem(player, _msg);
                    }
                    break;
                case -105: // Nhẫn thời không
                    if (player.type == 0 && player.maxTime == 30) {
                        ChangeMapService.gI().changeMap(player, 102, 0, 100, 336);
                    } else if (player.type == 1 && player.maxTime == 5) {
                        ChangeMapService.gI().changeMap(player, 197, 0, -1, 5);
                    } else if (player.type == 2 && player.maxTime == 5) {
                        ChangeMapService.gI().changeMap(player, 170, 0, 1560, 336);
                    } else if (player.type == 2 && player.maxTime == 11) {
                        ChangeMapService.gI().changeMapBySpaceShip(player, 198, 0, 35);
                    } else if (player.type == 2 && player.maxTime == 12) {
                        ChangeMapService.gI().changeMapBySpaceShip(player, 201, 0, 35);
                    } else if (player.type == 2 && player.maxTime == 34) { // đến tương lai
                        ChangeMapService.gI().changeMapBySpaceShip(player, 102, -1, Util.nextInt(60, 200));
                        player.isGotoFuture = false;
                    } else if (player.type == 2 && player.maxTime == 13) {
                        ChangeMapService.gI().changeMapBySpaceShip(player, 205, -1, 193);
                    } else if (player.type == 3 && player.maxTime == 50) { // đến bản đồ kho báu
                        ChangeMapService.gI().changeMap(player, 135, -1, 35, 276);
                    } else if (player.type == 3 && player.maxTime == 51) { // đến bản đồ kho báu
                        if (MapService.gI().isMapKhiGas(player.zone.map.mapId)) {
                            ChangeMapService.gI().changeMap(player, player.location.goMap, -1, player.location.goX, player.location.goY);
                        }
                    } else if (player.type == 3 && player.maxTime == 52) {
                        ChangeMapService.gI().changeMap(player, 149, -1, 156, 228);
                    }
                    break;
                case -91:
                    if (player != null) {
                        switch (player.iDMark.getTypeChangeMap()) {
                            case ConstMap.CHANGE_CAPSULE:
                                UseItem.gI().choseMapCapsule(player, _msg.reader().readByte());
                                break;
                            case ConstMap.CHANGE_BLACK_BALL:
                                BlackBallWar.gI().changeMap(player, _msg.reader().readByte());
                                break;
                            default:
                                Boss boss = BossManager.BOSSES_IN_GAME.get(_msg.reader().readByte());
                                if (boss.zone != null) {
                                    ChangeMapService.gI().changeMap(player, boss.zone.map.mapId, boss.zone.zoneId, boss.location.x, boss.location.y);
                                    break;
                                } else {
                                    Service.getInstance().sendThongBao(player, "Boss này chưa xuất hiện hoặc đang chết");
                                    Service.getInstance().sendMoney(player);
                                    break;
                                }
                        }
                    }
                    break;
                case -39:
                    if (player != null) {
                        //finishLoadMap
                        ChangeMapService.gI().finishLoadMap(player);
                        if (player.zone.map.mapId == (21 + player.gender)) {
                            if (player.mabuEgg != null) {
                                player.mabuEgg.sendMabuEgg();
                            }
//                            Logger.log(Logger.PURPLE, "done load map nhà!\n");
                        }
                        EffectMapService.gI().sendEffEvent(player);
                    }
                    break;
                case 11:
                    byte modId = _msg.reader().readByte();
                    DataGame.requestMobTemplate(_session, modId);
                    break;
                case 44:
                    if (player != null) {
                        String text = _msg.reader().readUTF();
                        Service.getInstance().chat(player, text);
                    }
                    break;
                case 32:
    try {
                    if (player != null) {
                        int npcId = _msg.reader().readShort();
                        int select = _msg.reader().readByte();
                        MenuController.getInstance().doSelectMenu(player, npcId, select);
                    }
                } catch (Exception e) {
                    ///VMN
                }
                break;

                ///MINI GAME
                case 33:
                    if (player != null) {
                        int npcId = _msg.reader().readShort();
                        if (npcId != 54) {
                            MenuController.getInstance().openMenuNPC(_session, npcId, player);
                        } else {
                            Service.getInstance().minigame_taixiu(player);
                        }
                    }
                    break;
                case 34:
                    if (player != null) {
                        int selectSkill = _msg.reader().readShort();
                        SkillService.gI().selectSkill(player, selectSkill);
                        if (selectSkill == Skill.BIEN_HINH || selectSkill == Skill.HUYT_SAO) {
                            SkillService.gI().useSkill(player, null, null, _msg);
                        }
                    }
                    break;
                case 54:
                    if (player != null) {
                        Service.getInstance().attackMob(player, (int) (_msg.reader().readByte()));
                    }
                    break;
                case -60:
                    if (player != null) {
                        int playerId = _msg.reader().readInt();
                        Service.getInstance().attackPlayer(player, playerId);
                    }
                    break;
                case -27:
                    _session.sendSessionKey();
                    break;
                case -111:
                    System.out.println("send image version");
                    DataGame.sendDataImageVersion(_session);
                    break;
                case -20:
                    if (player != null && !player.isDie()) {
                        int itemMapId = _msg.reader().readShort();
                        ItemMapService.gI().pickItem(player, itemMapId);
                    }
                    break;
                case -28:
                    messageNotMap(_session, _msg);
                    break;
                case -118:
                    int pId = _msg.reader().readInt();
                    if (pId != -1 && player.id != pId) {
//                        SieuHangService.gI().startChallenge(player, pId);
                        Service.getInstance().sendThongBao(player, "Không thể thực hiện");
                    }
                    break;
                case -29:
                    messageNotLogin(_session, _msg);
                    break;
                case -30:
                    messageSubCommand(_session, _msg);
                    break;
                case -15: // về nhà
                    if (player != null) {
                        player.isGoHome = true;
                        ChangeMapService.gI().changeMapBySpaceShip(player, player.gender + 21, 0, -1);
                        player.isGoHome = false;
                    }
                    break;
                case -16: // hồi sinh
                    if (player != null) {
                        PlayerService.gI().hoiSinh(player);
                    }
                default:
//                    Util.log("CMD: " + cmd);
                    break;
            }
            if (_session.logCheck) {
//                System.out.println("Time do controller (" + cmd + "): " + (System.currentTimeMillis() - st) + " ms");
            }
        } catch (Exception e) {
            logger.error("Err controller message command: " + _msg.command, e);
//            Log.logException(Controller.class, e);
//            Log.warning("Lỗi controller message command: " + _msg.command);
        }
    }

//    public void messageNotLogin(Session session, Message msg) {
//        if (msg != null) {
//            try {
//                byte cmd = msg.reader().readByte();
//                switch (cmd) {
//                    case 0: {
//                        // LOCK KEY 2
//                        String userName = msg.reader().readUTF();
//                        String passWork = msg.reader().readUTF();
//                        String version = msg.reader().readUTF();
//                        Byte type = msg.reader().readByte();
//                        if (!Manager.activeKey) {
//                            session.login(userName, passWork);
//                            return;
//                        }
//                        if (msg.reader().available() >= 1) {
//                            String key2 = msg.reader().readUTF();
//                            if (key2.equals(Manager.KEY_SERVER_2)) {
//                                session.login(userName, passWork);
//                            } else {
//                                thongBaoLockKey(session);
//                            }
//                        } else {
//                            thongBaoLockKey(session);
//                        }
//                        // END LOCK KEY 2
//                    }
//                    break;
//
//                    case 2: {
//                        int typeClient = (msg.reader().readByte());//client_type
//                        byte zoomLevel = msg.reader().readByte();//zoom_level
//                        msg.reader().readBoolean();//is_gprs
//                        msg.reader().readInt();//width
//                        msg.reader().readInt();//height
//                        msg.reader().readBoolean();//is_qwerty
//                        msg.reader().readBoolean();//is_touch
//                        String platform = msg.reader().readUTF();
//                        String[] arrPlatform = platform.split("\\|");
//                        int version2 = Integer.parseInt(arrPlatform[1].replaceAll("\\.", ""));
//                        if (Manager.activeKey) {
//                            if (arrPlatform.length >= 3) {
//                                String key = String.valueOf(arrPlatform[2].replaceAll("\\.", ""));
////                                System.out.println("key: " + key);
//                                if (!key.isEmpty() && key.equals(KEY_SERVER)) {
//                                    session.isLock = false;
//                                } else {
//                                    session.isLock = true;
//                                }
//                            } else {
//                                session.isLock = true;
//                            }
//                        } else {
//                            session.isLock = false;
//                        }
//                        session.setClientType(session, msg, typeClient, zoomLevel, version2);
//                    }
//                    break;
//                    default:
//                        break;
//                }
//            } catch (IOException e) {
//                Log.error(Controller.class, e);
//            }
//        }
//    }
    public void messageNotLogin(Session session, Message msg) {
        if (msg != null) {
            try {
                byte cmd = msg.reader().readByte();
                switch (cmd) {
                    case 0 -> {
                        session.login(msg.reader().readUTF(), msg.reader().readUTF());
                    }
                    // Message m77 = new Message(-77);
                    // m77.writer().ử
                    case 2 ->
                        session.setClientType(msg);
                    default -> {
                    }
                }
            } catch (IOException e) {
                Log.error(Controller.class, e);
            }
        }
    }

    public void thongBaoLockKey(Session session) {
        Service.getInstance().sendThongBaoOK(session, "Vui lòng tải phiên bản chính thúc tại\b");
        return;
    }

    public void messageNotMap(Session _session, Message _msg) {
        if (_msg != null) {
            try {
                Player player = _session.player;
                byte cmd = _msg.reader().readByte();
//                System.out.println("CMD receive -28 / " + cmd);
                switch (cmd) {
                    case 2:
                        createChar(_session, _msg);
                        break;
                    case 6:
                        DataGame.createMap(_session);
                        break;
                    case 7:
                        DataGame.updateSkill(_session);
                        break;
                    case 8:
                        ItemData.updateItem(_session);
                        break;
                    case 10:
                        DataGame.sendMapTemp(_session, _msg.reader().readUnsignedByte());
                        break;
                    case 13:
                        //client ok
                        if (player != null) {
                            Service.getInstance().player(player);
                            Service.getInstance().Send_Caitrang(player);
                            player.zone.loadAnotherToMe(player);

                            // -64 my flag bag
                            Service.getInstance().sendFlagBag(player);

                            // -113 skill shortcut
                            player.playerSkill.sendSkillShortCut();
                            // item time
                            ItemTimeService.gI().sendAllItemTime(player);

                            // send current task
                            TaskService.gI().sendInfoCurrentTask(player);
                        }
                        break;
                    default:
                        break;
                }
            } catch (IOException e) {
                Log.error(Controller.class, e);
            }
        }
    }

    public void messageSubCommand(Session _session, Message _msg) {
        if (_msg != null) {
            try {
                Player player = _session.player;
                byte command = _msg.reader().readByte();
                switch (command) {
                    case 16:
                        byte type = _msg.reader().readByte();
                        short point = _msg.reader().readShort();
                        if (player != null && player.nPoint != null) {
                            player.nPoint.increasePoint(type, point);
                        }
                        break;
                    case 64:
                        int playerId = _msg.reader().readInt();
                        int menuId = _msg.reader().readShort();
                        SubMenuService.gI().controller(player, playerId, menuId);
                        break;
                    default:
                        break;
                }
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }
    }

    public void createChar(Session session, Message msg) {
        if (!Maintenance.isRuning) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            boolean created = false;
            try ( Connection con = DBService.gI().getConnectionCreatPlayer();) {
                String name = msg.reader().readUTF();
                int gender = msg.reader().readByte();
                int hair = msg.reader().readByte();
                if (name.length() <= 10 && name.length() >= 5) {
                    ps = con.prepareStatement("select * from player where name = ? or account_id = ?");
                    ps.setString(1, name);
                    ps.setInt(2, session.userId);
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        Service.getInstance().sendThongBaoOK(session, "Tên nhân vật đã tồn tại");
                    } else {
                        if (Util.haveSpecialCharacter(name)) {
                            Service.getInstance().sendThongBaoOK(session, "Tên nhân vật không được chứa ký tự đặc biệt");
                        } else {
                            boolean isNotIgnoreName = true;
                            for (String n : ConstIgnoreName.IGNORE_NAME) {
                                if (name.equals(n)) {
                                    Service.getInstance().sendThongBaoOK(session, "Tên nhân vật đã tồn tại");
                                    isNotIgnoreName = false;
                                    break;
                                }
                            }
                            if (isNotIgnoreName) {
                                created = PlayerService.gI().createPlayer(con, session.userId, name.toLowerCase(), gender, hair);
                            }
                        }
                    }
                } else {
                    Service.getInstance().sendThongBaoOK(session, "Tên nhân vật tối thiểu 5 kí tự và tối đa 10 ký tự");
                }
            } catch (Exception e) {
                Log.error(Controller.class, e);
            } finally {
                try {
                    if (rs != null) {
                        rs.close();
                    }
                    if (ps != null) {
                        ps.close();
                    }
                } catch (SQLException ex) {
                }
            }
            if (created) {
                session.finishUpdate();
            }
        }
    }

//    public void login2(Session session, String username, String password) {
//        ResultSet resultSet = null;
//        try {
//            Connection con = DBService.gI().getConnectionForGame();
//            PreparedStatement ps = con.prepareStatement("INSERT INTO account (username, password) VALUES (?, ?)");
//            ps.setString(1, username);
//            ps.setString(2, password);
//            ps.executeUpdate();
//        } catch (SQLException e) {
//            Log.error(AccountDAO.class, e);
//        }
//    }
    /* public void login2(Session session, Message msg) {
        try {
            Message message;
            try {
                message = new Message(42);
                message.writer().writeByte(0);
                session.sendMessage(message);
                message.cleanup();
            } catch (Exception e) {
            }
        } catch (Exception e) {
        }
    }*/
    public void login2(Session session, Message msg) {
        //  Service.gI().switchToRegisterScr(session);
        Service.gI().sendThongBaoOK(session, "Vui lòng đăng ký tài khoản tại www.nronotbuff.com!");
    }

    public void sendInfo(Session session) {
        Player player = session.player;

        DataGame.sendDataItemBG(session);
        // -82 set tile map
        DataGame.sendTileSetInfo(session);

        // 112 my info intrinsic
        IntrinsicService.gI().sendInfoIntrinsic(player);

        // -42 my point
        Service.getInstance().point(player);

        // 40 task
        TaskService.gI().sendTaskMain(player);

        // -22 reset all
        Service.getInstance().clearMap(player);

        // -53 my clan
        ClanService.gI().sendMyClan(player);

        // -69 max statima
        PlayerService.gI().sendMaxStamina(player);

        // -68 cur statima
        PlayerService.gI().sendCurrentStamina(player);

        // -97 năng động
        // -107 have pet
        Service.getInstance().sendHavePet(player);

        // -119 top rank
        Service.getInstance().sendTopRank(player);

        // -50 thông tin bảng thông báo
        // -24 join map - map info
        player.zone.load_Me_To_Another(player);
        player.zone.mapInfo(player);

        // -70 thông báo bigmessage
        //check activation set
        player.setClothes.setup();
        if (player.pet != null) {
            player.pet.setClothes.setup();
        }
        if (player.pet != null) {
            player.pet.joinMapMaster();
        }

//        Boss yajiro = BossManager.gI().getBossById(Util.createIdDuongTank((int) player.id) + 300000);
//        if (yajiro == null) {
//            try {
//                Boss_Yanjiro dt = new Boss_Yanjiro(Util.createIdDuongTank((int) player.id), BossData.YANJIRO, player.zone, -1, -1, player.id);
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }
//
//        Boss mrpopo = BossManager.gI().getBossById(Util.createIdDuongTank((int) player.id) + 200000);
//        if (mrpopo == null) {
//            try {
//                Boss_MrPôPô dt = new Boss_MrPôPô(Util.createIdDuongTank((int) player.id), BossData.MR_POPO, player.zone, -1, -1, player.id);
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }
        //last time use skill
        Service.getInstance().sendTimeSkill(player);

        if (TaskService.gI().getIdTask(player) == ConstTask.TASK_0_0) {
            Service.getInstance().sendThongBao(player, "Nhiệm vụ của bạn là\nHãy di chuyển nhân vật");
            String npcSay = "Chào mừng " + player.name + " đến với thế giới " + Manager.SERVER_NAME + "\n";
            if (player.gender == 0) {
                npcSay += "Mình là Puaru sẽ đồng hành cũng bạn trên thế giới này\n";
            } else if (player.gender == 1) {
                npcSay += "Mình là Piano sẽ đồng hành cũng bạn trên thế giới này\n";
            } else {
                npcSay += "Mình là Icarus sẽ đồng hành cũng bạn trên thế giới này\n";
            }
            npcSay += "Để di chuyển, hãy chạm 1 lần vào nơi muốn đến";
            NpcService.gI().createTutorial(player, -1, npcSay);
        } else {
            NotiManager.getInstance().sendAlert(player);
            NotiManager.getInstance().sendDanhQuaiNhanNgoc(player);
//            if (player.isAdmin()) {
//                Service.gI().ServerMessageVip("Thông báo này chỉ là thông báo thử nghiệm của Client Version 2.3.7");
//            }
        }

//        if (player.inventory.itemsBag != null) {
//            for (int i = 0; i < player.inventory.itemsBag.size() - 1; i++) {
//                if (player.inventory.itemsBag.get(i).template.id == 738) {
//                    InventoryService.gI().removeItemBag(player, i);
//                }
//            }
//        }
//        for (int i = 0; i < player.inventory.itemsBody.size() - 1; i++) {
//            if (player.inventory.itemsBody.get(i).template.id == 738) {
//                InventoryService.gI().removeItemBody(player, i);
//            }
//        }
//
//        for (int i = 0; i < player.inventory.itemsBox.size() - 1; i++) {
//            if (player.inventory.itemsBox.get(i).template.id == 738) {
//                InventoryService.gI().removeItemBox(player, i);
//            }
//        }
        if (player.inventory.itemsBody.get(12).isNotNullItem()) {
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    Service.getInstance().sendTitle1(player, (short) player.inventory.itemsBody.get(12).template.part);
                } catch (Exception e) {
                }
            }).start();
        }
        player.soDuVND = player.getSession().vndBar;

        player.soThoiVang = player.getSession().goldBar;

        player.thanhVien = player.getSession().actived;

        NotiManager.getInstance().sendNoti(player);

        KiGuiShop.getInstance().sendExpirationNotification(player);

        Util.setTimeout(() -> PlayerService.gI().sendPetFollow(player), 500, "pet folloer");

        player.timeFixInventory = System.currentTimeMillis() + 500;

//        Service.getInstance().sendThongBaoXamLon(player);
    }
}

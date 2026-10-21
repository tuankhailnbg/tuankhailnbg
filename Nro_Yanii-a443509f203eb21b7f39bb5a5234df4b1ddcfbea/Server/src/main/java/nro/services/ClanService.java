package nro.services;

import nro.consts.Cmd;
import nro.consts.ConstAchive;
import nro.consts.ConstNpc;
import nro.jdbc.DBService;
import nro.models.item.FlagBag;
import nro.models.Part;
import nro.models.PartManager;
import nro.models.clan.Clan;
import nro.models.clan.ClanMember;
import nro.models.clan.ClanMessage;
import nro.models.item.Item;
import nro.models.player.Player;
import nro.server.Client;
import nro.server.Manager;
import nro.server.io.Message;
import nro.utils.Log;
import nro.utils.Util;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import nro.services.func.ChangeMapService;

/**
 *
 * @author 💖 Nro Yanii 💖
 * 
 *
 */
public class ClanService {

    // get clan
    private static final byte REQUEST_FLAGS_CHOOSE_CREATE_CLAN = 1;
    private static final byte ACCEPT_CREATE_CLAN = 2;
    private static final byte REQUEST_FLAGS_CHOOSE_CHANGE_CLAN = 3;
    private static final byte ACCEPT_CHANGE_INFO_CLAN = 4;

    // clan message
    private static final byte CHAT = 0;
    private static final byte ASK_FOR_PEA = 1;
    private static final byte ASK_FOR_JOIN_CLAN = 2;

    // join clan
    private static final byte ACCEPT_ASK_JOIN_CLAN = 0;
    private static final byte CANCEL_ASK_JOIN_CLAN = 1;

    // clan remote
    private static final byte KICK_OUT = -1;
    private static final byte CAT_CHUC = 2;
    private static final byte PHONG_PHO = 1;
    private static final byte PHONG_PC = 0;

    // clan invite
    private static final byte SEND_INVITE_CLAN = 0;
    private static final byte ACCEPT_JOIN_CLAN = 1;

    private static ClanService i;

    private ClanService() {
    }

    public static ClanService gI() {
        if (i == null) {
            i = new ClanService();
        }
        return i;
    }

    public Clan getClanById(int id) throws Exception {
        return getClanById(0, Manager.getNumClan(), id);
    }

    private Clan getClanById(int l, int r, int id) throws Exception {
        if (l <= r) {
            int m = (l + r) / 2;
            Clan clan = null;
            if (isNullClan(m)) {
                return null;
            }
            clan = Manager.CLANS.get(m);
            if (clan.id == id) {
                return clan;
            } else if (clan.id > id) {
                r = m - 1;
            } else {
                l = m + 1;
            }
            return getClanById(l, r, id);
        } else {
            throw new Exception("Không tìm thấy clan id: " + id);
        }
    }

    public static boolean isNullClan(int clanid) {
        return Manager.CLANS.get(clanid) == null;
    }

    public List<Clan> getClans(String name) {
        List<Clan> listClan = new ArrayList();
        if (Manager.CLANS.size() <= 20) {
            for (Clan clan : Manager.CLANS) {
                if (clan.name.contains(name)) {
                    listClan.add(clan);
                }
            }
        } else {
            int n = Util.nextInt(0, Manager.CLANS.size() - 20);
            for (int i = n; i < Manager.CLANS.size(); i++) {
                Clan clan = Manager.CLANS.get(i);
                if (clan.name.contains(name)) {
                    listClan.add(clan);
                }
                if (listClan.size() >= 20) {
                    break;
                }
            }
        }

        // for (Clan clan : Manager.CLANS) {
        // if (clan.name.contains(name)) {
        // listClan.add(clan);
        // }
        // if (listClan.size() >= 20) {
        // break;
        // }
        // }
        return listClan;
    }

    public void getClan(Player player, Message msg) {
        try {
            byte action = msg.reader().readByte();
            switch (action) {
                case REQUEST_FLAGS_CHOOSE_CREATE_CLAN:
                    FlagBagService.gI().sendListFlagClan(player);
                    break;
                case ACCEPT_CREATE_CLAN:
                    byte imgId = msg.reader().readByte();
                    String name = msg.reader().readUTF();
                    createClan(player, imgId, name);
                    break;
                case REQUEST_FLAGS_CHOOSE_CHANGE_CLAN:
                    FlagBagService.gI().sendListFlagClan(player);
                    break;
                case ACCEPT_CHANGE_INFO_CLAN:
                    imgId = msg.reader().readByte();
                    String slogan = msg.reader().readUTF();
                    changeInfoClan(player, imgId, slogan);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public void clanMessage(Player player, Message msg) {
        try {
            byte type = msg.reader().readByte();
            switch (type) {
                case CHAT:
                    chat(player, msg.reader().readUTF());
                    break;
                case ASK_FOR_PEA:
                    askForPea(player);
                    break;
                case ASK_FOR_JOIN_CLAN:
                    askForJoinClan(player, msg.reader().readInt());
                    break;
            }
        } catch (Exception e) {
        }

    }

    // cho đậu
    public void clanDonate(Player plGive, Message msg) {
        Clan clan = plGive.clan;
        if (clan != null) {
            try {
                ClanMessage cmg = clan.getClanMessage(msg.reader().readInt());
                if (cmg != null) {
                    if (cmg.receiveDonate < cmg.maxDonate) {
                        Player plReceive = clan.getPlayerOnline(cmg.playerId);
                        if (plReceive != null) {
                            Item pea = InventoryService.gI().getPeaBox(plGive);
                            if (pea != null) {
                                InventoryService.gI().subQuantityItem(plGive.inventory.itemsBox, pea, 1);
                                Item peaCopy = ItemService.gI().createNewItem(pea.template.id);
                                peaCopy.itemOptions = pea.itemOptions;
                                InventoryService.gI().addItemBag(plReceive, peaCopy, 0);
                                InventoryService.gI().sendItemBags(plReceive);
                                Service.getInstance().sendThongBao(plReceive,
                                        plGive.name + " đã cho bạn " + peaCopy.template.name);
                                plGive.playerTask.achivements.get(ConstAchive.HO_TRO_DONG_DOI).count++;
                                cmg.receiveDonate++;
                                clan.sendMessageClan(cmg);
                            } else {
                                Service.getInstance().sendThongBao(plGive, "Không tìm thấy đậu trong rương");
                            }
                        } else {
                            Service.getInstance().sendThongBao(plGive, "Người chơi hiện không online");
                        }
                    }
                }
            } catch (Exception e) {
            }
        }

    }

    public void joinClan(Player player, Message msg) {
        try {
            int clanMessageId = msg.reader().readInt();
            byte action = msg.reader().readByte();
            switch (action) {
                case ACCEPT_ASK_JOIN_CLAN:
                    acceptAskJoinClan(player, clanMessageId);
                    break;
                case CANCEL_ASK_JOIN_CLAN:
                    cancelAskJoinClan(player, clanMessageId);
                    break;
            }
        } catch (Exception e) {
        }

    }

    public void clanRemote(Player player, Message msg) {
        try {
            int playerId = msg.reader().readInt();
            byte role = msg.reader().readByte();
            switch (role) {
                case CAT_CHUC:
                    catChuc(player, playerId);
                    break;
                case KICK_OUT:
                    kickOut(player, playerId);
                    break;
                case PHONG_PHO:
                    phongPho(player, playerId);
                    break;
                case PHONG_PC:
                    showMenuNhuongPc(player, playerId);
                    break;
            }

        } catch (Exception e) {
        }
    }

    public void clanInvite(Player player, Message msg) {
        try {
            byte action = msg.reader().readByte();
            switch (action) {
                case SEND_INVITE_CLAN:
                    sendInviteClan(player, msg.reader().readInt());
                    break;
                case ACCEPT_JOIN_CLAN:
                    acceptJoinClan(player, msg.reader().readInt());
                    break;
            }
        } catch (Exception e) {
        }

    }

    // --------------------------------------------------------------------------
    /**
     * Mời vào bang
     */
    private void sendInviteClan(Player player, int playerId) {
        Player pl = Client.gI().getPlayer(playerId);
        if (pl != null && player.clan != null) {
            Message msg;
            try {
                msg = new Message(-57);
                player.isChangeMap = false;
                msg.writer().writeUTF(player.name + " mời bạn vào bang " + player.clan.name);
                msg.writer().writeInt(player.clan.id);
                msg.writer().writeInt(758435); // code
                pl.sendMessage(msg);
                msg.cleanup();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Đồng ý mời vào bang
     */
    private void acceptJoinClan(Player player, int clanId) {
        try {
            if (!player.isChangeMap) {
                if (player.playerTask.taskMain.id < 10) {
                    Service.getInstance().sendThongBao(player, "Vui lòng làm đến nhiệm vụ bang hội");
                    return;
                }
                if (player.clan == null) {
                    Clan clan = getClanById(clanId);
                    if (clan != null && clan.getCurrMembers() < clan.maxMember) {
                        clan.addClanMember(player, Clan.MEMBER);
                        clan.addMemberOnline(player);
                        player.clan = clan;

                        clan.sendMyClanForAllMember();
                        this.sendClanId(player);
                        Service.getInstance().sendFlagBag(player);

                        ItemTimeService.gI().sendTextDoanhTrai(player);
                        checkDoneTaskJoinClan(clan);
                    } else {
                        Service.getInstance().sendThongBao(player, "Bang hội đã đủ người");
                    }
                } else {
                    Service.getInstance().sendThongBao(player, "Không thể thực hiện");
                }
            } else {
                ChangeMapService.gI().changeMapInYard(player, 46, -1, -1);
            }
        } catch (Exception ex) {
            Service.getInstance().sendThongBao(player, ex.getMessage());
        }
    }

    /**
     * Chấp nhận xin vào bang
     */
    private void acceptAskJoinClan(Player player, int clanMessageId) {
        Clan clan = player.clan;
        if (clan != null && clan.isLeader(player)) {
            ClanMessage cmg = clan.getClanMessage(clanMessageId);
            boolean existInClan = false;
            for (ClanMember cm : clan.members) {
                if (cm.id == cmg.playerId) {
                    existInClan = true;
                    break;
                }
            }
            if (cmg != null && !existInClan) {
                Player pl = Client.gI().getPlayer(cmg.playerId);
                cmg.type = 0;
                cmg.role = Clan.LEADER;
                cmg.playerId = (int) player.id;
                cmg.playerName = player.name;
                cmg.isNewMessage = 0;
                cmg.color = ClanMessage.RED;
                if (pl != null) {
                    if (pl.clan == null) {
                        if (clan.getCurrMembers() < clan.maxMember) {
                            clan.addClanMember(pl, Clan.MEMBER);
                            clan.addMemberOnline(pl);
                            pl.clan = player.clan;

                            cmg.text = "Chấp nhận " + pl.name + " vào bang.";

                            this.sendClanId(pl);
                            Service.getInstance().sendFlagBag(pl);
                            ItemTimeService.gI().sendTextDoanhTrai(pl);
                            Service.getInstance().sendThongBao(pl, "Bạn vừa được nhận vào bang " + clan.name);
                            checkDoneTaskJoinClan(clan);
                        } else {
                            cmg.text = "Bang hội đã đủ người";
                        }
                    } else {
                        cmg.text = "Người chơi đã vào bang khác";
                    }
                } else {
                    cmg.text = "Người chơi đang offline";
                }
                clan.sendMyClanForAllMember();
            } else {
                Service.getInstance().sendThongBao(player, "Không thể thực hiện");
            }
        }
    }

    /**
     * Từ chối xin vào bang
     */
    private void cancelAskJoinClan(Player player, int clanMessageId) {
        Clan clan = player.clan;
        if (clan != null && clan.isLeader(player)) {
            ClanMessage cmg = clan.getClanMessage(clanMessageId);
            if (cmg != null) {
                Player newMember = Client.gI().getPlayer(cmg.playerId);
                cmg.type = 0;
                cmg.role = Clan.LEADER;
                cmg.playerId = (int) player.id;
                cmg.playerName = player.name;
                cmg.isNewMessage = 0;
                cmg.color = ClanMessage.RED;
                cmg.text = "Từ chối " + cmg.playerName + " vào bang";
                if (newMember != null) {
                    Service.getInstance().sendThongBao(newMember, "Bang hội " + clan.name + " đã từ chối bạn vào bang");
                }
                clan.sendMyClanForAllMember();
            }
        }
    }

    /**
     * Xin đậu
     */
    private void askForPea(Player player) {
        Clan clan = player.clan;
        if (clan != null) {
            ClanMember cm = clan.getClanMember((int) player.id);
            if (cm != null) {
                if ((cm.timeAskPea + 1000 * 60 * 5) < System.currentTimeMillis()) {
                    cm.timeAskPea = System.currentTimeMillis();
                    ClanMessage cmg = new ClanMessage(clan);
                    cmg.type = 1;
                    cmg.playerId = cm.id;
                    cmg.playerName = cm.name;
                    cmg.role = cm.role;
                    cmg.receiveDonate = 0;
                    cmg.maxDonate = 5;
                    clan.addClanMessage(cmg);
                    clan.sendMessageClan(cmg);
                } else {
                    Service.getInstance().sendThongBao(player, "Bạn chỉ có thể xin đậu 5 phút 1 lần.");
                }
            }
        }
    }

    /**
     * Xin vào bang
     */
    private void askForJoinClan(Player player, int clanId) {
        try {
            if (player.clan == null) {
                Clan clan = getClanById(clanId);
                if (clan != null) {
                    boolean isMeInClan = false;
                    for (ClanMember cm : clan.members) {
                        if (cm.id == player.id) {
                            isMeInClan = true;
                            break;
                        }
                    }
                    if (!isMeInClan) {
                        if (clan.getCurrMembers() < clan.maxMember) {
                            boolean asked = false;
                            for (ClanMessage c : clan.getCurrClanMessages()) {
                                if (c.type == 2 && c.playerId == (int) player.id
                                        && c.role == -1) {
                                    asked = true;
                                    break;
                                }
                            }
                            if (!asked) {
                                ClanMessage cmg = new ClanMessage(clan);
                                cmg.type = 2;
                                cmg.playerId = (int) player.id;
                                cmg.playerName = player.name;
                                cmg.playerPower = player.nPoint.power;
                                cmg.role = -1;
                                clan.addClanMessage(cmg);
                                clan.sendMessageClan(cmg);
                            }
                        } else {
                            Service.getInstance().sendThongBao(player, "Bang hội đã đủ người");
                        }
                    } else {
                        Service.getInstance().sendThongBao(player, "Không thể thực hiện");
                    }
                }
            } else {
                Service.getInstance().sendThongBao(player, "Không thể thực hiện");
            }
        } catch (Exception ex) {
            Service.getInstance().sendThongBao(player, ex.getMessage());
        }
    }

    /**
     * Đổi thông tin clan (cờ, khẩu hiệu)
     */
    private void changeInfoClan(Player player, byte imgId, String slogan) {
        if (!slogan.equals("")) {
            changeSlogan(player, slogan);
        } else {
            changeFlag(player, imgId);
        }
    }

    /**
     * Tạo clan mới
     */
    private void createClan(Player player, byte imgId, String name) {
        if (player.clan == null) {
            if (name.length() > 30) {
                Service.getInstance().sendThongBao(player, "Tên bang hội không được quá 30 ký tự");
                return;
            }
            FlagBag flagBag = FlagBagService.gI().getFlagBag(imgId);
            if (flagBag != null) {
                if (flagBag.gold > 0) {
                    if (player.inventory.gold >= flagBag.gold) {
                        player.inventory.gold -= flagBag.gold;
                    } else {
                        Service.getInstance().sendThongBao(player, "Bạn không đủ vàng, còn thiếu "
                                + Util.numberToMoney(flagBag.gold - player.inventory.gold) + " vàng");
                        return;
                    }
                }
                if (flagBag.gem > 0) {
                    if (player.inventory.getGem() >= flagBag.gem) {
                        player.inventory.subGem(flagBag.gem);
                    } else {
                        Service.getInstance().sendThongBao(player, "Bạn không đủ ngọc, còn thiếu "
                                + (flagBag.gem - player.inventory.getGem()) + " ngọc");
                        return;
                    }
                }
                PlayerService.gI().sendInfoHpMpMoney(player);

                Clan clan = new Clan();
                clan.imgId = imgId;
                clan.name = name;
                Manager.addClan(clan);

                player.clan = clan;
                clan.addClanMember(player, Clan.LEADER);
                clan.addMemberOnline(player);
                clan.insert();

                Service.getInstance().sendFlagBag(player);
                sendMyClan(player);
            }
        }
    }

    // danh sách clan
    public void sendListClan(Player player, String name) {
        Message msg;
        try {
            List<Clan> clans = getClans(name);
            msg = new Message(-47);
            msg.writer().writeByte(clans.size());
            for (Clan clan : clans) {
                msg.writer().writeInt(clan.id);
                msg.writer().writeUTF(clan.name);
                msg.writer().writeUTF(clan.slogan);
                msg.writer().writeByte(clan.imgId);
                msg.writer().writeUTF(String.valueOf(clan.powerPoint));
                msg.writer().writeUTF(clan.getLeader().name);
                msg.writer().writeByte(clan.getCurrMembers());
                msg.writer().writeByte(clan.maxMember);
                msg.writer().writeInt(clan.createTime);
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {

        }
    }

    public void sendListMemberClan(Player player, int clanId) {
        try {
            Clan clan = getClanById(clanId);
            if (clan != null) {
                clan.reloadClanMember();
                Message msg;
                try {
                    msg = new Message(Cmd.CLAN_MEMBER);
                    msg.writer().writeByte(clan.getCurrMembers());
                    for (ClanMember cm : clan.getMembers()) {
                        msg.writer().writeInt((int) cm.id);
                        msg.writer().writeShort(cm.head);
                        if (player.isVersionAbove(220)) {
                            Part part = PartManager.getInstance().find(cm.head);
                            msg.writer().writeShort(part.getIcon(0));
                        }
                        msg.writer().writeShort(cm.leg);
                        msg.writer().writeShort(cm.body);
                        msg.writer().writeUTF(cm.name);
                        msg.writer().writeByte(cm.role);
                        msg.writer().writeUTF(Util.numberToMoney(cm.powerPoint));
                        msg.writer().writeInt(cm.donate);
                        msg.writer().writeInt(cm.receiveDonate);
                        msg.writer().writeInt(cm.clanPoint);
                        msg.writer().writeInt(cm.joinTime);
                    }
                    player.sendMessage(msg);
                    msg.cleanup();
                } catch (Exception e) {
                    Service.getInstance().sendThongBao(player, e.getMessage());
                }
            }
        } catch (Exception ex) {
            Service.getInstance().sendThongBao(player, ex.getMessage());

        }
    }

    public void sendRemoveClan(Player player) {
        Message msg;
        try {
            player.clan = null;
            player.clanMember = null;
            ClanService.gI().sendMyClan(player);
            ClanService.gI().sendClanId(player);
            Service.getInstance().sendFlagBag(player);
            Service.getInstance().sendThongBao(player, "Bạn đã giải tán bang thành công");
            ItemTimeService.gI().removeTextDoanhTrai(player);

            msg = new Message(Cmd.CLAN_INFO);
            msg.writer().writeInt(-1);
        } catch (Exception e) {
            Log.error(ClanService.class, e, "Lỗi send my clan " + player.clan.name + " - " + player.clan.id);
        }
    }

    public void sendMyClan(Player player) {
        Message msg = new Message(Cmd.CLAN_INFO);
        Clan clan = player.clan;
        try {
            if (clan == null) {
                msg.writer().writeInt(-1);
            } else {
                msg.writer().writeInt(clan.id);
                msg.writer().writeUTF(clan.name);
                msg.writer().writeUTF(clan.slogan);
                msg.writer().writeByte(clan.imgId);
                msg.writer().writeUTF(String.valueOf(clan.powerPoint));
                msg.writer().writeUTF(clan.getLeader().name);
                msg.writer().writeByte(clan.getCurrMembers());
                msg.writer().writeByte(clan.maxMember);
                msg.writer().writeByte(clan.getRole(player));
                msg.writer().writeInt(clan.clanPoint);
                msg.writer().writeByte(clan.level);
                for (ClanMember cm : clan.getMembers()) {
                    msg.writer().writeInt(cm.id);
                    msg.writer().writeShort(cm.head);
                    if (player.isVersionAbove(220)) {
                        Part part = PartManager.getInstance().find(cm.head);
                        msg.writer().writeShort(part.getIcon(0));
                    }
                    msg.writer().writeShort(cm.leg);
                    msg.writer().writeShort(cm.body);
                    msg.writer().writeUTF(cm.name);
                    msg.writer().writeByte(cm.role);
                    msg.writer().writeUTF(Util.numberToMoney(cm.powerPoint));
                    msg.writer().writeInt(cm.donate);
                    msg.writer().writeInt(cm.receiveDonate);
                    msg.writer().writeInt(cm.clanPoint);
                    msg.writer().writeInt(cm.memberPoint);
                    msg.writer().writeInt(cm.joinTime);
                }
                List<ClanMessage> clanMessages = clan.getCurrClanMessages();
                msg.writer().writeByte(clanMessages.size());
                for (ClanMessage cmg : clanMessages) {
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
                }
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Log.error(ClanService.class, e, "Lỗi send my clan " + clan.name + " - " + clan.id);
        }
    }

    public void sendClanId(Player player) {
        Message msg;
        try {
            msg = new Message(-61);
            msg.writer().writeInt((int) player.id);
            if (player.clan == null) {
                msg.writer().writeInt(-1);
            } else {
                msg.writer().writeInt(player.clan.id);
            }
            Service.getInstance().sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void showMenuLeaveClan(Player player) {
        NpcService.gI().createMenuConMeo(player, ConstNpc.CONFIRM_LEAVE_CLAN,
                -1, "Bạn có chắc chắn rời bang hội không?", "Đồng ý", "Từ chối");

    }

    public void showMenuNhuongPc(Player player, int playerId) {
        if (player.clan.isLeader(player)) {
            ClanMember cm = player.clan.getClanMember(playerId);
            if (cm != null) {
                NpcService.gI().createMenuConMeo(player, ConstNpc.CONFIRM_NHUONG_PC, -1,
                        "Bạn có đồng ý nhường chức bang chủ cho " + cm.name + "?", new String[] { "Đồng ý", "Từ chối" },
                        playerId);
            }
        }
    }

    public void changeSlogan(Player player, String slogan) {
        if (slogan.length() > 250) {
            slogan = slogan.substring(0, 250);
        }
        Clan clan = player.clan;
        if (clan != null && clan.isLeader(player)) {
            clan.slogan = slogan;
            clan.sendMyClanForAllMember();
        }
    }

    public void changeFlag(Player player, int imgId) {
        Clan clan = player.clan;
        if (clan != null && clan.isLeader(player) && imgId != clan.imgId) {
            // sub money
            FlagBag flagBag = FlagBagService.gI().getFlagBag(imgId);

            if (flagBag != null) {
                if (flagBag.gold > 0) {
                    if (player.inventory.gold >= flagBag.gold) {
                        player.inventory.gold -= flagBag.gold;
                    } else {
                        Service.getInstance().sendThongBao(player, "Bạn không đủ vàng, còn thiếu "
                                + Util.numberToMoney(flagBag.gold - player.inventory.gold) + " vàng");
                        return;
                    }
                }
                if (flagBag.gem > 0) {
                    if (player.inventory.getGem() >= flagBag.gem) {
                        player.inventory.subGem(flagBag.gem);
                    } else {
                        Service.getInstance().sendThongBao(player, "Bạn không đủ ngọc, còn thiếu "
                                + (flagBag.gem - player.inventory.getGem()) + " ngọc");
                        return;
                    }
                }
                PlayerService.gI().sendInfoHpMpMoney(player);
                player.clan.imgId = imgId;
                // ClanDAO.saveClan(player.clan);
                clan.sendFlagBagForAllMember();

            }
        }
    }

    // Rời khỏi bang
    public void RemoveClanAll(Player player) {
        Clan clan = player.clan;
        if (clan != null) {
            ClanMember cm = clan.getClanMember((int) player.id);
            if (cm != null) {
                if (!clan.isLeader(player)) {
                    Service.getInstance().sendThongBao(player, "Phải là bang chủ mới có thể giải tán");
                    return;
                }
                clan.removeClanMember(cm);
                clan.removeMemberOnline(cm, null);
                cm.clan = null;
                cm = null;

                player.clan = null;
                player.clanMember = null;
                ClanService.gI().sendMyClan(player);
                ClanService.gI().sendClanId(player);
                Service.getInstance().sendFlagBag(player);
                Service.getInstance().sendThongBao(player, "Bạn đã giải tán bang thành công");
                ItemTimeService.gI().removeTextDoanhTrai(player);
                clan.name = null;
                clan.slogan = null;
                clan.sendRemoveClanForAllMember();
                removeClan(clan.id);
            }
        }
    }

    // Rời khỏi bang
    public void leaveClan(Player player) {
        Clan clan = player.clan;
        if (clan != null) {
            ClanMember cm = clan.getClanMember((int) player.id);
            if (cm != null) {
                if (clan.isLeader(player)) {
                    Service.getInstance().sendThongBao(player, "Phải nhường chức bang chủ trước khi rời");
                    return;
                }
                ClanMessage cmg = new ClanMessage(clan);
                cmg.type = 0;
                cmg.role = clan.getRole(player);
                cmg.color = ClanMessage.BLACK;
                cmg.playerId = (int) player.id;
                cmg.playerName = player.name;
                cmg.text = player.name + " đã rời khỏi bang.";
                cmg.color = ClanMessage.RED;

                clan.removeClanMember(cm);
                clan.removeMemberOnline(cm, null);
                cm.clan = null;
                cm = null;
                player.clan = null;
                player.clanMember = null;
                ClanService.gI().sendMyClan(player);
                ClanService.gI().sendClanId(player);
                Service.getInstance().sendFlagBag(player);
                Service.getInstance().sendThongBao(player, "Bạn đã rời khỏi bang");
                ItemTimeService.gI().removeTextDoanhTrai(player);

                clan.sendMyClanForAllMember();
                clan.addClanMessage(cmg);
                clan.sendMessageClan(cmg);
            }
        }
    }

    // Cắt chức
    public void catChuc(Player player, int memberId) {
        Clan clan = player.clan;
        if (clan != null) {
            if (player.clan.isLeader(player)) {
                ClanMember cm = clan.getClanMember(memberId);
                if (cm != null) {
                    ClanMember leader = clan.getLeader();
                    ClanMessage cmg = new ClanMessage(clan);
                    cmg.type = 0;
                    cmg.role = leader.role;
                    cmg.playerId = leader.id;
                    cmg.playerName = leader.name;
                    cmg.text = "Cắt chức phó bang của " + cm.name;
                    cmg.color = ClanMessage.RED;

                    cm.role = Clan.MEMBER;
                    clan.sendMyClanForAllMember();

                    clan.addClanMessage(cmg);
                    clan.sendMessageClan(cmg);
                }
            }
        }
    }

    // Đuổi khỏi bang
    public void kickOut(Player player, int memberId) {
        Clan clan = player.clan;
        ClanMember cm = clan.getClanMember(memberId);
        if (clan != null && cm != null
                && (clan.isLeader(player) || clan.isDeputy(player) && cm.role == Clan.MEMBER)) {
            Player plKicked = clan.getPlayerOnline(memberId);
            ClanMember cmKick = clan.getClanMember((int) player.id);
            ClanMessage cmg = new ClanMessage(clan);
            cmg.type = 0;
            cmg.role = cmKick.role;
            cmg.playerId = cmKick.id;
            cmg.playerName = cmKick.name;
            cmg.text = "Đuổi " + cm.name + " ra khỏi bang.";
            cmg.color = ClanMessage.RED;

            clan.removeClanMember(cm);
            clan.removeMemberOnline(cm, null);
            cm.clan = null;
            cm = null;
            if (plKicked != null) {
                plKicked.clan = null;
                plKicked.clanMember = null;
                ClanService.gI().sendMyClan(plKicked);
                ClanService.gI().sendClanId(plKicked);
                Service.getInstance().sendFlagBag(plKicked);
                Service.getInstance().sendThongBao(plKicked, "Bạn đã bị đuổi khỏi bang");
                ItemTimeService.gI().removeTextDoanhTrai(plKicked);
            } else {
                removeClanPlayer(memberId);
            }
            clan.sendMyClanForAllMember();

            clan.addClanMessage(cmg);
            clan.sendMessageClan(cmg);
        }
    }

    private void removeClan(int clId) {
        PreparedStatement ps = null;
        PreparedStatement ps2 = null;
        try (Connection con = DBService.gI().getConnectionForClan();) {
            ps = con.prepareStatement("DELETE FROM clan_sv"
                    + Manager.SERVER + " where id = " + clId);
            ps.executeUpdate();
            ps.close();

            ps2 = con.prepareStatement("update player set clan_id_sv"
                    + Manager.SERVER + " = -1 where clan_id_sv" + Manager.SERVER + " = " + clId);
            ps2.executeUpdate();
            ps2.close();
        } catch (Exception ex) {
            removeClan(clId);
            return;
        } finally {
            try {
                ps.close();
                ps2.close();
            } catch (Exception e) {
            }
        }
    }

    private void removeClanPlayer(int plId) {
        PreparedStatement ps = null;
        try (Connection con = DBService.gI().getConnectionForClan();) {
            ps = con.prepareStatement("update player set clan_id_sv"
                    + Manager.SERVER + " = -1 where id = " + plId);
            ps.executeUpdate();
            ps.close();
        } catch (Exception ex) {
            removeClanPlayer(plId);
            return;
        } finally {
            try {
                ps.close();
            } catch (Exception e) {
            }
        }
    }

    // Phong phó bang
    public void phongPho(Player player, int memberId) {
        Clan clan = player.clan;
        if (clan != null && (clan.isLeader(player) || clan.isDeputy(player))) {
            ClanMember cm1 = clan.getClanMember(memberId);
            if (cm1 != null && cm1.role == Clan.MEMBER) {
                ClanMember cm2 = clan.getClanMember((int) player.id);
                ClanMessage cmg = new ClanMessage(clan);
                cmg.type = 0;
                cmg.role = cm2.role;
                cmg.playerId = cm2.id;
                cmg.playerName = cm2.name;
                cmg.text = "Phong phó bang cho " + cm1.name;
                cmg.color = ClanMessage.RED;

                cm1.role = Clan.DEPUTY;
                clan.sendMyClanForAllMember();

                clan.addClanMessage(cmg);
                clan.sendMessageClan(cmg);

            } else {
                Service.getInstance().sendThongBao(player, "Không thể thực hiện");
            }
        }
    }

    // Phong chủ bang
    public void phongPc(Player player, int memberId) {
        Clan clan = player.clan;
        if (clan != null && clan.isLeader(player)) {
            ClanMember leader = clan.getLeader();
            ClanMember cm = clan.getClanMember(memberId);
            if (cm != null) {
                ClanMessage cmg = new ClanMessage(clan);
                cmg.type = 0;
                cmg.role = leader.role;
                cmg.playerId = leader.id;
                cmg.playerName = leader.name;
                cmg.text = "Nhường chức bang chủ cho " + cm.name;
                cmg.color = ClanMessage.RED;

                leader.role = Clan.MEMBER;
                cm.role = Clan.LEADER;
                // ClanDAO.updateClanMember(leader);
                // ClanDAO.updateClanMember(cm);
                clan.sendMyClanForAllMember();

                clan.addClanMessage(cmg);
                clan.sendMessageClan(cmg);

            }
        }
    }

    public void chat(Player player, String text) {
        Clan clan = player.clan;
        if (clan != null) {
            ClanMember cm = clan.getClanMember((int) player.id);
            if (cm != null) {
                ClanMessage cmg = new ClanMessage(clan);
                cmg.type = 0;
                cmg.playerId = cm.id;
                cmg.playerName = cm.name;
                cmg.role = cm.role;
                cmg.text = text;
                cmg.color = 0;

                clan.addClanMessage(cmg);
                clan.sendMessageClan(cmg);
            }
        }
    }

    private void checkDoneTaskJoinClan(Clan clan) {
        if (clan.getMembers().size() >= 3) { // CHECK SỐ THÀNH VIÊN TRONG BANG
            for (Player player : clan.membersInGame) {
                TaskService.gI().checkDoneTaskJoinClan(player);
            }
        }
    }

    public void close() {
        PreparedStatement ps = null;

        try {
            Connection con = DBService.gI().getConnectionForClan();
            ps = con.prepareStatement("update clan_sv" + Manager.SERVER
                    + " set slogan = ?, img_id = ?, power_point = ?, max_member = ?, clan_point = ?, "
                    + "level = ?, members = ? where id = ? limit 1");
            for (Clan clan : Manager.CLANS) {
                JSONArray dataArray = new JSONArray();
                JSONObject dataObject = new JSONObject();
                for (ClanMember cm : clan.members) {
                    dataObject.put("id", cm.id);
                    dataObject.put("power", cm.powerPoint);
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

                ps.setString(1, clan.slogan);
                ps.setInt(2, clan.imgId);
                ps.setLong(3, clan.powerPoint);
                ps.setByte(4, clan.maxMember);
                ps.setInt(5, clan.clanPoint);
                ps.setInt(6, clan.level);
                ps.setString(7, member);
                ps.setInt(8, clan.id);
                ps.addBatch();
                Log.log("SAVE CLAN: " + clan.name + " (" + clan.id + ")");
            }
            ps.executeBatch();
            ps.close();
        } catch (Exception e) {
            Log.error(Clan.class, e, "Có lỗi khi update clan vào db");
        } finally {
            try {
                ps.close();
            } catch (Exception e) {
            }
        }
    }

}

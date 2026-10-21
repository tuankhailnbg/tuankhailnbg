package nro.models.npc;

import nro.consts.ConstNpc;
import nro.models.map.Map;
import nro.models.map.Zone;
import nro.models.player.Player;
import nro.server.Manager;
import nro.server.io.Message;
import nro.services.MapService;
import nro.services.NpcService;
import nro.services.Service;
import nro.services.func.ShopService;
import nro.utils.Log;
import nro.utils.Util;

/**
 *
 * @author 💖 Nro Yanii 💖
 * 
 *
 */
public abstract class Npc implements IAtionNpc {

    public int mapId;
    public Map map;

    public int status;

    public int cx;

    public int cy;

    public int tempId;

    public int avartar;

    public BaseMenu baseMenu;

    public long lastTimeChat;

    protected Npc(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        this.map = MapService.gI().getMapById(mapId);
        this.mapId = mapId;
        this.status = 15;
        this.cx = cx;
        this.cy = cy;
        this.tempId = tempId;
        this.avartar = avartar;
        Manager.NPCS.add(this);
    }

    public void initBaseMenu(String text) {
        text = text.substring(1);
        String[] data = text.split("\\|");
        baseMenu = new BaseMenu();
        baseMenu.npcId = tempId;
        baseMenu.npcSay = data[0].replaceAll("<>", "\n");
        baseMenu.menuSelect = new String[data.length - 1];
        for (int i = 0; i < baseMenu.menuSelect.length; i++) {
            baseMenu.menuSelect[i] = data[i + 1].replaceAll("<>", "\n");
        }
    }

    public void createOtherMenu(Player player, int indexMenu, String npcSay, String... menuSelect) {
        Message msg;
        try {
            player.iDMark.setIndexMenu(indexMenu);
            msg = new Message(32);
            msg.writer().writeShort(tempId);
            msg.writer().writeUTF(npcSay);
            msg.writer().writeByte(menuSelect.length);
            for (String menu : menuSelect) {
                msg.writer().writeUTF(menu);
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openShopWithGender(Player player, int shopId, int order) {
        ShopService.gI().openShopNormal(player, this, shopId, order, player.gender);
    }

    public void openShopWithGender1(Player player, int shopId, int order) {
        ShopService.gI().openShopSpecial(player, this, shopId, order, player.gender);
    }

    public void hide_npc(Player player, int idnpc, int action) {
        Message msg;
        try {
            msg = new Message(-73);
            msg.writer().writeByte(idnpc);
            msg.writer().writeByte(action);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception ex) {

        }
    }

    public void remove_player(Zone zone, int idplayer) {
        Message msg;
        try {
            msg = new Message(-6);
            msg.writer().writeInt(idplayer);
            Service.getInstance().sendMessAllPlayerInMap(zone, msg);
            msg.cleanup();
        } catch (Exception ex) {

        }
    }

    // public void send_npc_attack_me(Player player,Npc npc, int HP, int MP) {
    // Message msg;
    // try {
    // msg = new Message(-11);
    // msg.writer().writeByte(this);
    // msg.writer().writeInt(HP);
    // msg.writer().writeInt(MP);
    // player.sendMessage(msg);
    // msg.cleanup();
    // } catch (Exception ex) {
    //
    // }
    // }
    public void createOtherMenu(Player player, int indexMenu, String npcSay, String[] menuSelect, Object object) {
        NpcFactory.PLAYERID_OBJECT.put(player.id, object);
        Message msg;
        try {
            player.iDMark.setIndexMenu(indexMenu);
            msg = new Message(32);
            msg.writer().writeShort(tempId);
            msg.writer().writeUTF(npcSay);
            msg.writer().writeByte(menuSelect.length);
            for (String menu : menuSelect) {
                msg.writer().writeUTF(menu);
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            player.iDMark.setIndexMenu(ConstNpc.BASE_MENU);
            try {
                if (baseMenu != null) {
                    baseMenu.openMenu(player);
                } else {
                    Message msg;
                    msg = new Message(32);
                    msg.writer().writeShort(tempId);
                    if (this.mapId == 5 || this.mapId == 13 || this.mapId == 20) {
                        NpcService.gI().createTutorial(player, this.avartar,
                                "Con hãy về hành tinh của mình mà thể hiện");
                    } else {
                        NpcService.gI().createTutorial(player, this.avartar,
                                "Chức năng đang được phát triển [" + this.tempId + "][" + this.mapId + "]");
                    }
                    msg.writer().writeByte(1);
                    player.sendMessage(msg);
                    msg.cleanup();
                }
            } catch (Exception e) {
                Log.error(Npc.class, e);
            }
        }
    }

    public void npcChat(Player player, String text) {
        Message msg;
        try {
            msg = new Message(124);
            msg.writer().writeShort(tempId);
            msg.writer().writeUTF(text);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Log.error(Service.class, e);
        }
    }

    public void npcChat(Zone zone, String text) {
        Message msg;
        try {
            msg = new Message(124);
            msg.writer().writeShort(tempId);
            msg.writer().writeUTF(text);
            Service.getInstance().sendMessAllPlayerInMap(zone, msg);
            msg.cleanup();
        } catch (Exception e) {
            Log.error(Service.class, e);
        }
    }

    public void npcChat(String text) {
        Message msg;
        try {
            msg = new Message(124);
            msg.writer().writeShort(tempId);
            msg.writer().writeUTF(text);
            for (Zone zone : map.zones) {
                Service.getInstance().sendMessAllPlayerInMap(zone, msg);
            }
            msg.cleanup();
        } catch (Exception e) {
            Log.error(Service.class, e);
        }
    }

    public void openShop(Player player, int shopId, int order) {
        ShopService.gI().openShopNormal(player, this, shopId, order, -1);
    }

    public boolean canOpenNpc(Player player) {
        if (this.tempId == ConstNpc.DAU_THAN) {
            if (player.zone.map.mapId == 21
                    || player.zone.map.mapId == 22
                    || player.zone.map.mapId == 23) {
                return true;
            } else {
                Service.getInstance().hideWaitDialog(player);
                Service.getInstance().sendThongBao(player, "Không thể thực hiện");
                return false;
            }
        }
        if (player.zone.map.mapId == this.mapId
                && Util.getDistance(this.cx, this.cy, player.location.x, player.location.y) <= 60) {
            player.iDMark.setNpcChose(this);
            return true;
        } else {
            Service.getInstance().hideWaitDialog(player);
            Service.getInstance().sendThongBao(player, "Không thể thực hiện khi đứng quá xa");
            return false;
        }
    }

}

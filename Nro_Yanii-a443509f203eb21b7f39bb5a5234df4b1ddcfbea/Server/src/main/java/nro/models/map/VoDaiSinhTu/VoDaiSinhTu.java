package nro.models.map.VoDaiSinhTu;

import lombok.Getter;
import lombok.Setter;
import nro.consts.ConstItem;
import nro.consts.ConstMap;
import nro.consts.ConstPlayer;
import nro.consts.ConstRewardLimit;
import nro.event.Event;
import nro.event.SummerEvent;
import nro.models.boss.Boss;
import nro.models.boss.VoDaiSinhTu.BongBang;
import nro.models.boss.VoDaiSinhTu.Dracula;
import nro.models.boss.VoDaiSinhTu.ThoDauBac;
import nro.models.boss.VoDaiSinhTu.VoHinh;
import nro.models.boss.VoDaiSinhTu.VuaQuySatan;
import nro.models.boss.dhvt.ChaPa;
import nro.models.boss.dhvt.ChanXu;
import nro.models.boss.dhvt.JackyChun;
import nro.models.boss.dhvt.LiuLiu;
import nro.models.boss.dhvt.ODo;
import nro.models.boss.dhvt.PonPut;
import nro.models.boss.dhvt.SoiHecQuyn;
import nro.models.boss.dhvt.TauPayPay;
import nro.models.boss.dhvt.ThienXinHang;
import nro.models.boss.dhvt.Xinbato;
import nro.models.boss.dhvt.Yamcha;
import nro.models.item.Item;
import nro.models.map.DaiHoiVoThuat.DHVT23Manager;
import nro.models.map.DaiHoiVoThuat.DHVT23Service;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.skill.Skill;
import nro.server.io.Message;
import nro.services.EffectSkillService;
import nro.services.InventoryService;
import nro.services.ItemService;
import nro.services.ItemTimeService;
import nro.services.PlayerService;
import nro.services.Service;
import nro.services.func.ChangeMapService;
import nro.utils.Log;
import nro.utils.Util;

/**
 *
 * @author louis
 */
public class VoDaiSinhTu {

    @Setter
    @Getter
    private Player player;
    @Setter
    private Boss boss;
    @Setter
    private Player npc;

    @Setter
    private int time;
    private int round;
    @Setter
    private int timeWait;

    public void update() {
        if (time > 0) {
            time--;
            if (!player.isDie() && player != null && player.zone != null) {
                if (boss.isDie()) {
                    round++;
                    boss.leaveMap();
                    toTheNextRound();
                }
            } else if (player.zone != null) {
                if (player.zone.map.mapId != 112) {
                    endChallenge();
                }
            } else {
                endChallenge();
            }
        } else {
            timeOut();
        }
        if (timeWait > 0) {
            switch (timeWait) {
                case 5:
                    ready();
                    break;
                case 3:
                    Service.gI().chat(boss, "Sẵn sàng chưa?");
                    break;
                case 2:
                    Service.gI().chat(player, "OK");
                    break;
            }
            timeWait--;
        }
    }

    public void ready() {
        Util.setTimeout(() -> {
            DHVT23Service.gI().sendTypePK(player, boss);
            PlayerService.gI().changeAndSendTypePK(this.player, ConstPlayer.PK_PVP);
            boss.setStatus((byte) 3);
        }, 5000,"vo dai sinh tu");
    }

    public void toTheNextRound() {
        PlayerService.gI().changeAndSendTypePK(player, ConstPlayer.NON_PK);
        Boss boss = null;
        switch (round) {
            case 0:
                boss = new Dracula(player);
                break;
            case 1:
                this.npcChat(player, "Tốt lắm, tiếp tục đấm vô mồm nó nào", player.zone.map.npcs.get(0));
                boss = new VoHinh(player);
                break;
            case 2:
                this.npcChat(player, "Tốt lắm, tiếp tục đấm vô mồm nó nào", player.zone.map.npcs.get(0));
                boss = new BongBang(player);
                break;
            case 3:
                this.npcChat(player, "Tốt lắm, tiếp tục đấm vô mồm nó nào", player.zone.map.npcs.get(0));
                boss = new VuaQuySatan(player);
                break;
            case 4:
                this.npcChat(player, "Tốt lắm, tiếp tục đấm vô mồm nó nào", player.zone.map.npcs.get(0));
                boss = new ThoDauBac(player);
                break;
            default:
                this.npcChat(player, "Đây là phần thưởng của con", player.zone.map.npcs.get(0));
                champion();
                return;
        }
        PlayerService.gI().setPos(player, 390, 336, 0);
        setTimeWait(5);
        setBoss(boss);
        PlayerService.gI().setPos(boss, 435, 264, 0);
        setTime(185);
        resetSkill();
    }

    public void npcChat(Player player, String text, Npc npc) {
        Message msg;
        try {
            msg = new Message(124);
            msg.writer().writeShort(npc.tempId);
            msg.writer().writeUTF(text);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Log.error(Service.class, e);
        }
    }

    private void resetSkill() {
        for (Skill skill : player.playerSkill.skills) {
            skill.lastTimeUseThisSkill = 0;
        }
        Service.getInstance().sendTimeSkill(player);
    }

    private void timeOut() {
        Service.getInstance().sendThongBao(player, "Bạn bị xử thua vì hết thời gian");
        endChallenge();
    }

    private void champion() {
        player.DoneVoDaiBaHatMit = 1;
        endChallenge();
    }

    public void leave() {
        setTime(0);
        EffectSkillService.gI().removeStun(player);
        Service.getInstance().sendThongBao(player, "Bạn bị xử thua vì rời khỏi võ đài");
        endChallenge();
    }

    private void reward() {
        if (player.levelWoodChest < round) {
            player.levelWoodChest = round;
        }
    }

    public void endChallenge() {
        reward();
        PlayerService.gI().hoiSinh(player);
        PlayerService.gI().changeAndSendTypePK(player, ConstPlayer.NON_PK);
        if (boss != null) {
            boss.leaveMap();
        }
        PlayerService.gI().setPos(player, 178, 408, 0);
        VoDaiSinhTuManager.gI().remove(this);
    }
}

package nro.models.map.DaiHoiVoThuat;

import lombok.Getter;
import lombok.Setter;
import nro.consts.ConstItem;
import nro.consts.ConstMap;
import nro.consts.ConstPlayer;
import nro.consts.ConstRewardLimit;
import nro.event.Event;
import nro.event.SummerEvent;
import nro.models.boss.Boss;
import nro.models.boss.dhvt.*;
import nro.models.item.Item;
import nro.models.player.Player;
import nro.models.player.Referee;
import nro.models.skill.PlayerSkill;
import nro.models.skill.Skill;
import nro.services.*;
import nro.services.func.ChangeMapService;
import nro.utils.Util;

/**
 * @author outcast c-cute hột me 😳
 */
public class DHVT23 {

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
                    player.levelWoodChest++;
                    boss.leaveMap();
                    toTheNextRound();
                }
                if (player.location.y > 264 && time > 10) {
                    leave();
                }
            } else {
                endChallenge();
            }
        } else {
            timeOut();
        }
        if (timeWait > 0) {
            switch (timeWait) {
                case 10:
                    PlayerService.gI().setPos(player, 335, 264, 0);
                    PlayerService.gI().playerMove(npc, 385, 264);
                    Service.getInstance().chat(npc, "Trận đấu giữa " + player.name + " VS " + boss.name + " sắp diễn ra");
                    ready();
                    break;
                case 8:
                    Service.getInstance().chat(npc, "Xin quý vị khán giả cho 1 tràng pháo tay để cổ vũ cho 2 đối thủ nào");
                    break;
                case 4:
                    Service.getInstance().chat(npc, "Mọi người ngồi sau hãy ổn định chỗ ngồi,trận đấu sẽ bắt đầu sau 3 giây nữa");
                    break;
                case 2:
                    Service.getInstance().chat(npc, "Trận đấu bắt đầu");
                    PlayerService.gI().playerMove(npc, 385, 360);
                    break;
                case 1:
                    Service.getInstance().chat(player, "OK");
                    Service.getInstance().chat(boss, "OK");
                    break;
            }
            timeWait--;
        }
    }

    public void ready() {
        EffectSkillService.gI().startStun(boss, System.currentTimeMillis(), 10000);
        EffectSkillService.gI().startStun(player, System.currentTimeMillis(), 10000);
        ItemTimeService.gI().sendItemTime(player, 3779, 10000 / 1000);
//        Service.getInstance().releaseCooldownSkill(this.player);
        Util.setTimeout(() -> {
            DHVT23Service.gI().sendTypePK(player, boss);
            PlayerService.gI().changeAndSendTypePK(this.player, ConstPlayer.PK_PVP);
            boss.setStatus((byte) 3);
        }, 10000,"dhvt23");
    }

    public void toTheNextRound() {
        PlayerService.gI().changeAndSendTypePK(player, ConstPlayer.NON_PK);
        Boss boss = null;
        switch (player.levelWoodChest) {
            case 0:
                boss = new SoiHecQuyn(player);
                break;
            case 1:
                boss = new ODo(player);
                break;
            case 2:
                boss = new Xinbato(player);
                break;
            case 3:
                boss = new ChaPa(player);
                break;
            case 4:
                boss = new PonPut(player);
                break;
            case 5:
                boss = new ChanXu(player);
                break;
            case 6:
                boss = new TauPayPay(player);
                break;
            case 7:
                boss = new Yamcha(player);
                break;
            case 8:
                boss = new JackyChun(player);
                break;
            case 9:
                boss = new ThienXinHang(player);
                break;
            case 10:
                boss = new LiuLiu(player);
                break;
            default:
                champion();
                return;
        }
//        if (round > 0 && round < 11) {
//            boss.joinMap();
//        }
        PlayerService.gI().setPos(player, 335, 264, 0);
        setTimeWait(11);
        setBoss(boss);
        setTime(185);
        resetSkill();
    }

    private void resetSkill() {
        Skill skill;
        for (int i = 0; i < player.playerSkill.skills.size(); i++) {
            skill = player.playerSkill.skills.get(i);
            skill.lastTimeUseThisSkill = System.currentTimeMillis() - (long) skill.coolDown;
        }
        Service.getInstance().sendTimeSkill(player);
    }

    private void timeOut() {
        Service.getInstance().sendThongBao(player, "Bạn bị xử thua vì hết thời gian");
        endChallenge();
    }

    private void champion() {
        Service.getInstance().sendThongBao(player, "Chúc mừng " + player.name + " vừa đoạt giải vô địch");
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
        if (round > 5 && Event.isEvent() && Event.getInstance() instanceof SummerEvent) {
            byte[] rwLimit = player.getRewardLimit();
            if (rwLimit[ConstRewardLimit.QUE_DOT] < 10) {
                rwLimit[ConstRewardLimit.QUE_DOT]++;
                Item item = ItemService.gI().createNewItem((short) ConstItem.QUE_DOT, 1);
                InventoryService.gI().addItemBag(player, item, 99);
            }
        }
        reward();
        PlayerService.gI().hoiSinh(player);
        PlayerService.gI().changeAndSendTypePK(player, ConstPlayer.NON_PK);
        if (player != null && player.zone != null && player.zone.map.mapId == ConstMap.DAI_HOI_VO_THUAT_129) {
            Util.setTimeout(() -> {
                ChangeMapService.gI().changeMapNonSpaceship(player, ConstMap.DAI_HOI_VO_THUAT_129, player.location.x, 360);
            }, 500,"len san dau");
        }
        if (boss != null) {
            boss.leaveMap();
        }
        DHVT23Manager.gI().remove(this);
    }
}

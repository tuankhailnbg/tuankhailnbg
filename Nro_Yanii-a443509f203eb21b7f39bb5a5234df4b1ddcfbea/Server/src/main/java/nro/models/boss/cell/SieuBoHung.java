package nro.models.boss.cell;

import nro.consts.ConstItem;
import nro.models.boss.*;
import nro.models.item.ItemOption;
import nro.models.map.ItemMap;
import nro.models.player.Player;
import nro.server.Manager;
import nro.services.RewardService;
import nro.services.Service;
import nro.services.TaskService;
import nro.utils.Util;

/**
 *
 * @author 💖 Nro Yanii 💖
 *
 *
 */
public class SieuBoHung extends FutureBoss {

    public static boolean tuSat;

    public SieuBoHung() {
        super(BossFactory.SIEU_BO_HUNG, BossData.SIEU_BO_HUNG);
    }

    @Override
    public void joinMap() {
        super.joinMap();
        BossFactory.createBoss(BossFactory.XEN_CON_6).zone = this.zone;
    }

    @Override
    public void leaveMap() {
        super.leaveMap();
        this.changeToIdle();
    }

    @Override
    public void idle() {
    }

    @Override
    public void rewards(Player pl) {
        if (pl != null) {
            int[] tempIds1 = new int[] { 555, 556, 563, 557, 558, 559, 567 };
            int[] tempIds2 = new int[] { 565, 560, 562, 564, 566, 561 };
            int[] tempIds3 = new int[] { 561 };
            int tempId = -1;
            int rand = Util.nextInt(1, 100);

            if (rand <= 2) {
                tempId = tempIds3[Util.nextInt(0, tempIds3.length - 1)];
            } else if (rand <= 10) {
                tempId = tempIds1[Util.nextInt(0, tempIds1.length - 1)];
            } else if (rand <= 15) {
                tempId = tempIds2[Util.nextInt(0, tempIds2.length - 1)];
            }

            if (tempId != -1) {
                ItemMap itemMap = new ItemMap(this.zone, tempId, 1,
                        pl.location.x, this.zone.map.yPhysicInTop(pl.location.x, pl.location.y - 24), pl.id);
                RewardService.gI().initBaseOptionClothes(itemMap.itemTemplate.id, itemMap.itemTemplate.type,
                        itemMap.options);
                Service.getInstance().dropItemMap(this.zone, itemMap);
            }
        }
        TaskService.gI().checkDoneTaskKillBoss(pl, this);
        generalRewards(pl);
    }

    @Override
    public void checkPlayerDie(Player pl) {

    }

    @Override
    public void initTalk() {
        this.textTalkBefore = new String[] {};
        this.textTalkMidle = new String[] { "Kame Kame Haaaaa!!", "Mi khá đấy nhưng so với ta chỉ là hạng tôm tép",
                "Tất cả nhào vô hết đi", "Cứ chưởng tiếp đi. haha", "Các ngươi yếu thế này sao hạ được ta đây. haha",
                "Khi công pháo!!", "Cho mi biết sự lợi hại của ta" };
        this.textTalkAfter = new String[] { "Các ngươi được lắm", "Hãy đợi đấy thời gian tới ta sẽ quay lại.." };
    }

}

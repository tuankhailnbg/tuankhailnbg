package nro.models.boss.NguHanhSon;

import nro.consts.ConstItem;
import nro.consts.ConstRatio;
import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.models.item.ItemOption;
import nro.models.map.ItemMap;
import nro.models.player.Player;
import nro.server.Manager;
import nro.services.RewardService;
import nro.services.Service;
import nro.services.SkillService;
import nro.utils.Log;
import nro.utils.SkillUtil;
import nro.utils.Util;

/**
 * @author outcast c-cute hột me 😳
 */
public class NgoKhong extends Boss {

    public NgoKhong() {
        super(BossFactory.NGO_KHONG, BossData.NGO_KHONG);
    }

    @Override
    protected boolean useSpecialSkill() {
        return false;
    }


    public int injured(Player plAtt, int damage, boolean piercing, boolean isMobAttack) {
        damage = 200000;
        return super.injured(plAtt, damage, piercing, isMobAttack);
    }


    @Override
    public void attack() {
        try {
            Player pl = getPlayerAttack();
            if (pl != null) {
                this.playerSkill.skillSelect = this.getSkillAttack();
                if (Util.getDistance(this, pl) <= this.getRangeCanAttackWithSkillSelect()) {
                    if (Util.isTrue(15, ConstRatio.PER100) && SkillUtil.isUseSkillChuong(this)) {
                        goToXY(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 80)),
                                Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0, 50), false);
                    }
                    SkillService.gI().useSkill(this, pl, null, null);
                    checkPlayerDie(pl);
                } else {
                    goToPlayer(pl, false);
                }
            }
        } catch (Exception ex) {
            Log.error(NgoKhong.class, ex);
        }
    }

    @Override
    public void idle() {
    }

    @Override
    public void rewards(Player pl) {
        ItemMap itemMap = null;
        int x = this.location.x;
        int y = this.zone.map.yPhysicInTop(x, this.location.y - 24);
        if (Util.isTrue(1, 40)) {
            int[] set1 = {562, 564, 566, 561};
            itemMap = new ItemMap(this.zone, set1[Util.nextInt(0, set1.length - 1)], 1, x, y, pl.id);
            RewardService.gI().initBaseOptionClothes(itemMap.itemTemplate.id, itemMap.itemTemplate.type, itemMap.options);
        } else if (Util.isTrue(1, 20)) {
            int[] set2 = {555, 556, 563, 557, 558, 565, 559, 567, 560};
            itemMap = new ItemMap(this.zone, set2[Util.nextInt(0, set2.length - 1)], 1, x, y, pl.id);
            RewardService.gI().initBaseOptionClothes(itemMap.itemTemplate.id, itemMap.itemTemplate.type, itemMap.options);
        } else if (Util.isTrue(1, 10)) {
            int[] set3= {547,548};
            itemMap = new ItemMap(this.zone, set3[Util.nextInt(0, set3.length - 1)], 1, x, y, pl.id);

            itemMap.options.add(new ItemOption(77, Util.nextInt(2, 40)));
            itemMap.options.add(new ItemOption(103, Util.nextInt(20, 40)));
            itemMap.options.add(new ItemOption(50, Util.nextInt(25, 35)));
            itemMap.options.add(new ItemOption(199, 0));

            /// check thời hạn cải trang
            if (Util.isTrue(1, 20)) {
                itemMap.options.add(new ItemOption(73, 0));
            } else {
                itemMap.options.add(new ItemOption(93, Util.nextInt(1, 30)));
            }
            RewardService.gI().initBaseOptionClothes(itemMap.itemTemplate.id, itemMap.itemTemplate.type, itemMap.options);

        } else if (Util.isTrue(1, 5)) {
            itemMap = new ItemMap(this.zone, 15, 1, x, y, pl.id);
        } else if (Util.isTrue(1, 2)) {
            itemMap = new ItemMap(this.zone, 16, 1, x, y, pl.id);
        }
        if (Manager.EVENT_SEVER == 3) {
            if (pl.nPoint.wearingNoelHat && Util.isTrue(1,30)) {
                itemMap = new ItemMap(this.zone, 926, 1, x, y, pl.id);
                itemMap.options.add(new ItemOption(93,70));
            }
        }
        if (Manager.EVENT_SEVER == 4 && itemMap == null) {
            itemMap = new ItemMap(this.zone, ConstItem.LIST_ITEM_NLSK_TET_2023[Util.nextInt(0, ConstItem.LIST_ITEM_NLSK_TET_2023.length - 1)], 1, x, y, pl.id);
            itemMap.options.add(new ItemOption(74, 0));
        }
        if (itemMap != null) {
            Service.getInstance().dropItemMap(zone, itemMap);
        }
    }

    @Override
    public void checkPlayerDie(Player pl) {

    }

    @Override
    public void initTalk() {
        this.textTalkBefore = new String[]{};
        this.textTalkMidle = new String[]{"Yêu quái! Chạy đi đâu?!", "Mi khá đấy nhưng so với Lão Tôn chỉ là tép riu",
                "Tất cả nhào vô hết đi", "Lão Tôn là Tề thiên đại thánh 500 năm trước từng đại náo thiên cung.", "Các ngươi yếu thế này sao hạ được Lão Tôn đây. haha",
                "Lão Tôn ta đến đây!!!", "Yêu quái ăn một gậy của lão Tôn ta!"};
        this.textTalkAfter = new String[]{"Các ngươi được lắm", "Hãy đợi đấy thời gian tới Lão Tôn sẽ quay lại.."};
    }
}

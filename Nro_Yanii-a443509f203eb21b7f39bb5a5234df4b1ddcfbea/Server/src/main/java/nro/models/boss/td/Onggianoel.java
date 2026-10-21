package nro.models.boss.td;

import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.models.map.ItemMap;
import nro.models.player.Player;
import nro.services.PlayerService;
import nro.services.Service;
import nro.utils.Util;

public class Onggianoel extends Boss {

    int dir = 1;
    int xMove = 60;

    public Onggianoel() {
        super(BossFactory.ONG_GIA_NOEL, BossData.ONG_GIA_NOEL);

    }

    @Override
    protected boolean useSpecialSkill() {
        return false;
    }

    @Override
    public void rewards(Player pl) {

    }

    @Override
    public int injured(Player plAtt, int damage, boolean piercing, boolean isMobAttack) {
        return 0;
    }

    @Override
    public void attack() {
         
     
        lastTimeAttack = System.currentTimeMillis();
        if (isDie()) {
            super.leaveMap();
        }

        try {
            int ground = this.zone.map.yPhysicInTop(this.location.x, 200);
            this.goToXY(xMove, ground - 100, false);
            xMove += (dir * 20);
            if (xMove > this.zone.map.mapWidth - 60) {
                xMove = this.zone.map.mapWidth - 60;
                dir = -1;
            } else if (xMove < 60) {
                xMove = 60;
                dir = 1;
            }
            if (Util.isTrue(2, 100)) {

                int vatpham = 2149;
                int itemGroundY = this.zone.map.yPhysicInTop(this.location.x, 200);
                ItemMap itemMap = new ItemMap(this.zone, vatpham, 1, this.location.x, itemGroundY, -1);
                Service.getInstance().dropItemMap(this.zone, itemMap);
            }
        } catch (Exception ex) {

        }
    }

    @Override
    protected void goToXY(int x, int y, boolean isTeleport) {
        PlayerService.gI().playerMove(this, x, y);
    }

    @Override
    public void checkPlayerDie(Player pl) {
    }

    @Override
    public void initTalk() {
        this.textTalkBefore = new String[]{};
        this.textTalkMidle = new String[]{"|-1|Giáng sinh an lành", "|-1|He he", "|-1|NroNotBuff mãi đỉnh"};
        this.textTalkAfter = new String[]{};
    }

    @Override
    public void joinMap() {
        super.joinMap();
        this.dir = 1;
        this.xMove = 60;
    }

    @Override
    public short getMount() {
        return 30_006;
    }
}

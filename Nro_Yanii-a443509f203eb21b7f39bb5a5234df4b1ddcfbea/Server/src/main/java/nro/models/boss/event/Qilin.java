package nro.models.boss.event;

import nro.lib.RandomCollection;
import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.models.player.Player;
import nro.services.Service;
import nro.services.func.ChangeMapService;
import nro.utils.Util;

public class Qilin extends EscortedBoss {

    private int mapID;
    private long lastTimeChangeMap;

    public Qilin(int mapID) {
        super(BossFactory.QILIN, BossData.QILIN);
        this.mapID = mapID;
    }

    @Override
    public void attack() {
        if (!canAttackWithTime(400)) {
            return;
        }
        super.attack();
        if (zone.map.mapId != mapID || escort != null) {
            if (Util.canDoWithTime(lastTimeChangeMap, 60000)) {
                stopEscorting();
            }
        }
    }

    @Override
    public void setEscort(Player escort) {
        super.setEscort(escort);
        lastTimeChangeMap = System.currentTimeMillis();
    }

    protected boolean useSpecialSkill() {
        return false;
    }

    @Override
    public int injured(Player plAtt, int damage, boolean piercing, boolean isMobAttack) {
        try {
            damage = 50000;
            return super.injured(plAtt, damage, piercing, isMobAttack);
        } finally {
            boolean result = false;
            if (nPoint.hp > nPoint.hpMax / 10) {
                RandomCollection<Boolean> rd = new RandomCollection<>();
                rd.add(nPoint.hpMax - nPoint.hp, true);
                rd.add(nPoint.hp, false);
                result = rd.next();
            } else {
                result = true;
            }
            if (result) {
                Service.getInstance().chat(plAtt, "Đi thôi lân con");
                setEscort(plAtt);
            }
        }
    }

    @Override
    public void rewards(Player pl) {

    }

    @Override
    public void idle() {

    }

    @Override
    public void checkPlayerDie(Player pl) {

    }

    @Override
    public void initTalk() {
        textTalkMidle = new String[]{"tùng tùng tùng", "tùng xèn tùng xèn"};
    }

    @Override
    public void leaveMap() {
        super.leaveMap();
        setJustRest();
    }

    @Override
    public void joinMap() {
        this.zone = getMapCanJoin(mapID);
        int x = Util.nextInt(50, this.zone.map.mapWidth - 50);
        ChangeMapService.gI().changeMap(this, this.zone, x, this.zone.map.yPhysicInTop(x, 0));
    }

    protected void notifyPlayeKill(Player player) {

    }

    @Override
    public void joinMapEscort() {
        super.joinMapEscort();
        lastTimeChangeMap = System.currentTimeMillis();
    }
}

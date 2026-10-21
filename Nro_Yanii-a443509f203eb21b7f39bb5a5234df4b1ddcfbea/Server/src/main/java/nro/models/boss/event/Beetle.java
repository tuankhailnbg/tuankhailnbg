package nro.models.boss.event;

import nro.consts.ConstItem;
import nro.models.boss.BossData;
import nro.models.boss.BossManager;
import nro.models.map.ItemMap;
import nro.models.player.Player;
import nro.services.Service;
import nro.services.func.ChangeMapService;

/**
 * @author outcast c-cute hột me 😳
 */
public class Beetle extends EscortedBoss {
    public Beetle(int id, BossData data, Player owner) {
        super(id, data);
        setEscort(owner);
    }

    @Override
    public int injured(Player plAtt, int damage, boolean piercing, boolean isMobAttack) {
        damage = 46;
        return super.injured(plAtt, damage, piercing, isMobAttack);
    }

    @Override
    protected boolean useSpecialSkill() {
        return false;
    }

    @Override
    public void rewards(Player pl) {
        ItemMap itemMap = new ItemMap(this.zone, ConstItem.NGAI_DEM, 1, this.location.x, this.zone.map.yPhysicInTop(this.location.x, 100), -1);
        Service.getInstance().dropItemMap(this.zone, itemMap);
    }

    @Override
    public void initTalk() {

    }

    @Override
    public void idle() {

    }

    @Override
    public void setEscort(Player escort) {
        super.setEscort(escort);
    }

    @Override
    public void checkPlayerDie(Player pl) {

    }

    @Override
    protected void notifyPlayeKill(Player player) {

    }

    @Override
    public void joinMap() {
        if (escort != null && escort.zone != null) {
            ChangeMapService.gI().changeMapBySpaceShip(this, escort.zone, ChangeMapService.NON_SPACE_SHIP);
        }
    }

    @Override
    public void joinMapEscort() {

    }

    @Override
    public void die() {
        leaveMap();
    }

    @Override
    public void leaveMap() {
        super.leaveMap();
        BossManager.gI().removeBoss(this);
    }
}

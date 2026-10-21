package nro.models.boss;

import nro.models.player.Player;
import nro.server.io.Message;

public abstract class FutureBoss extends Boss {

    public FutureBoss(int id, BossData data) {
        super(id, data);
    }

    @Override
    public int injured(Player plAtt, int damage, boolean piercing, boolean isMobAttack) {
        if (plAtt != null) {
            damage = (damage / 100) * 80;
            return super.injured(plAtt, damage, piercing, isMobAttack);
        } else {
            return 0;
        }
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
}

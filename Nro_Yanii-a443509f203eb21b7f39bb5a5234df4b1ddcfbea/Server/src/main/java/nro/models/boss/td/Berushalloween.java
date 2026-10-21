package nro.models.boss.td;

import java.sql.Connection;
import java.sql.PreparedStatement;
import nro.jdbc.DBService;
import nro.jdbc.daos.PlayerDAO;
import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.models.boss.FutureBoss;
import nro.models.item.Item;
import nro.models.map.ItemMap;
import nro.models.player.Player;
import nro.services.InventoryService;
import nro.services.ItemTimeService;
import nro.services.Service;
import nro.services.func.ChangeMapService;
import nro.utils.Log;
import nro.utils.Util;

public class Berushalloween extends FutureBoss {

    public Berushalloween() {
        super(BossFactory.BERUSHALLOWEEN, BossData.BERUSHALLOWEEN);
        super.isBoss = true;
    }

    @Override
    public int injured(Player plAtt, int damage, boolean piercing, boolean isMobAttack) {
        if (plAtt == null) {
            return 0;
        }
        if (!this.isDie()) {
            damage = 1;
            this.nPoint.subHP(damage);
            if (isDie()) {
                rewards(plAtt);
                notifyPlayeKill(plAtt);
                die();
            }
            return damage;
        } else {
            return 0;
        }
    }

    @Override
    public void rewards(Player pl) {
        // Cộng điểm vào SQL
        try (
            Connection con = DBService.gI().getConnectionForSaveData(); ///
            PreparedStatement ps = con.prepareStatement("UPDATE account SET TopBossHalloween = TopBossHalloween + 1 WHERE id = ?")) {
            ps.setInt(1, pl.getSession().userId); // ID của người chơi
            ps.executeUpdate();
            Service.getInstance().sendThongBao(pl, "Bạn nhận được 1 điểm giết boss Halloween ");
        } catch (Exception e) {
            Log.error(PlayerDAO.class, e, "Lỗi update top Halloween cho người chơi " + pl.name);
        }
        /// Rơi vật phẩm
        if (Util.isTrue(1, 1)) {
            int soluong = Util.nextInt(5, 10);
            for (int i = 0; i < soluong; i++) {
                int vatpham = 900;
                int randomvitrix = Util.nextInt(5, 8);
                int xPosition = pl.location.x + (i * randomvitrix);
                int yPosition = this.zone.map.yPhysicInTop(pl.location.x, pl.location.y - 24);

                ItemMap itemMap = new ItemMap(this.zone, vatpham, 1, xPosition, yPosition, -1);
                Service.getInstance().dropItemMap(this.zone, itemMap);
            }
        }
    }
    // BiNgo
    @Override
    public void checkPlayerDie(Player pl) {
        if (pl.effectSkill.isBiNgo) {
            return;
        }
        Item ct739 = InventoryService.gI().findItem(pl.inventory.itemsBody, 739);
        if (ct739 == null || !ct739.isNotNullItem()) {
            // Bi ngo
            pl.effectSkill.isBiNgo = true;
            pl.effectSkill.lastBiNgo = System.currentTimeMillis();
            Service.gI().Send_Caitrang(pl);
            ItemTimeService.gI().sendAllItemTime(pl);
        }
    }
    // BiNgo

    @Override
    public void initTalk() {
        this.textTalkMidle = new String[]{"Hóa bí ngô!!"};
        this.textTalkAfter = new String[]{"Các ngươi chờ đấy, ta sẽ quay lại sau"};
    }

    @Override
    public void leaveMap() {
        ChangeMapService.gI().spaceShipArrive(this, (byte) 2, ChangeMapService.TENNIS_SPACE_SHIP);
       // this.setJustRestToFuture();
        super.leaveMap();
    }
}

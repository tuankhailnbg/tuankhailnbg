package nro.models.boss.nappa;

import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.models.boss.FutureBoss;
import nro.models.map.ItemMap;
import nro.models.player.Player;
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
public class Rambo extends FutureBoss {

    public Rambo() {
        super(BossFactory.RAMBO, BossData.RAMBO);
    }

    @Override
    protected boolean useSpecialSkill() {
        return false;
    }

    @Override
    public void rewards(Player pl) {
//        // Kiểm tra xem có thực hiện phần thưởng hay không (ví dụ: 10% xảy ra)
//        if (Util.isTrue(1, 1000)) { // Thay đổi số 100 thành 10 nếu muốn 10% xảy ra
//            // Số lượng thỏi vàng từ 1 đến 5 (ngẫu nhiên)
//            int numberOfGoldBars = Util.nextInt(1, 5);
//            // Tạo itemMap cho từng thỏi vàng
//            for (int i = 0; i < numberOfGoldBars; i++) {
//                // ID của thỏi vàng, 457 là ID mẫu (có thể thay đổi nếu ID thỏi vàng khác)
//                int goldItemId = 457;
//                // Tạo một itemMap với thông số ngẫu nhiên
//                // Cộng thêm một giá trị ngẫu nhiên trong khoảng từ 2 đến 3 cho vị trí x
//                int xOffset = Util.nextInt(5, 8); // Giá trị ngẫu nhiên từ 2 đến 3
//                // Tính toán vị trí x của thỏi vàng để chúng rơi cách nhau
//                int xPosition = pl.location.x + (i * xOffset);
//                ItemMap itemMap = new ItemMap(
//                        this.zone,
//                        goldItemId,
//                        1, // Số lượng là 1 thỏi vàng
//                        xPosition,
//                        this.zone.map.yPhysicInTop(pl.location.x, pl.location.y - 24),
//                        pl.id);
//                // Thả itemMap xuống vị trí của người chơi trong zone
//                Service.getInstance().dropItemMap(this.zone, itemMap);
//            }
//        }
        // Kiểm tra và hoàn thành nhiệm vụ khi giết boss
        TaskService.gI().checkDoneTaskKillBoss(pl, this);
        // Thực hiện phần thưởng chung
    }

    @Override
    public void idle() {

    }

    @Override
    public void checkPlayerDie(Player pl) {

    }

    @Override
    public void initTalk() {

    }

}

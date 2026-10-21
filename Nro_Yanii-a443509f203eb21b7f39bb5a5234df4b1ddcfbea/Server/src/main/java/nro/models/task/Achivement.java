package nro.models.task;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nro.consts.ConstAchive;
import nro.models.player.Player;

/**
 * @author outcast c-cute hột me 😳
 */
@Setter
@Getter
@NoArgsConstructor
public class Achivement {

    private int id;

    private String name;

    private String detail;

    private int money;

    public int count;

    private int maxCount;

    private boolean isFinish;

    private boolean isReceive;

    public boolean isDone(Player player) {
        if (this.id == ConstAchive.GIA_NHAP_THAN_CAP || this.id == ConstAchive.SUC_MANH_GIOI_VUONG_THAN) {
            this.count = (int) player.nPoint.power;
        }
        if (this.id == ConstAchive.NONG_DAN_CHAM_CHI) {
            this.count = player.magicTree.level;
        }
        return this.count >= this.maxCount;
    }

    public boolean isDone(int divisor) {
        return this.count / divisor >= this.maxCount / divisor;
    }

    public String getCount(Player player) {
        if (this.id == ConstAchive.GIA_NHAP_THAN_CAP) {
            if ((player.nPoint.power) >= 1000000000) {
                return player.nPoint.power / 1000000000 + " Tỉ";
            } else if ((player.nPoint.power) >= 1000000) {
                return player.nPoint.power / 1000000 + " Tr";
            } else if ((player.nPoint.power) >= 1000) {
                return player.nPoint.power / 1000 + " k";
            }
        } else if (this.id == ConstAchive.SUC_MANH_GIOI_VUONG_THAN) {
            if ((player.nPoint.power) >= 1000000000) {
                return player.nPoint.power / 1000000000 + " Tỉ";
            } else if ((player.nPoint.power) >= 1000000) {
                return player.nPoint.power / 1000000 + " Tr";
            } else if ((player.nPoint.power) >= 1000) {
                return player.nPoint.power / 1000 + " k";
            }
        } else if (this.id == ConstAchive.KHINH_CONG_THANH_THAO) {
            if (this.count > 1000) {
                return this.count / 1000 + " k";
            } else {
                return this.count + "";
            }
        }
        if (this.id == ConstAchive.NONG_DAN_CHAM_CHI) {
            this.count = player.magicTree.level;
        }
        return this.count + "";
    }

    public String getMaxCount() {
        if (this.id == ConstAchive.GIA_NHAP_THAN_CAP) {
            return this.maxCount / 1000 + "";
        } else if (this.id == ConstAchive.SUC_MANH_GIOI_VUONG_THAN) {
            return this.maxCount / 1000000 + ",5";
        } else if (this.id == ConstAchive.KHINH_CONG_THANH_THAO) {
            return this.maxCount / 1000 + " k";
        }
        return this.maxCount + "";
    }
}

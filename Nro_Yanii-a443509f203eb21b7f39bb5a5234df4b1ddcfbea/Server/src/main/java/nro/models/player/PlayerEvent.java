package nro.models.player;

import lombok.Getter;
import lombok.Setter;

/**
 * @author outcast c-cute hột me 😳
 */
@Setter
@Getter
public class PlayerEvent {
    private boolean isUseQuanHoa;
    private boolean isUseBonTam;
    private int diemTichLuy;
    private int mocNapDaNhan;
    private int eventPoint;
    private Player player;
    private boolean cookingChungCake;
    private int timeCookChungCake;
    private boolean cookingTetCake;
    private int timeCookTetCake;
    private boolean receivedLuckyMoney;
    
    public int luotNhanNgocMienPhi = 1;
    public int luotNhanBuaMienPhi = 1;
    
    public PlayerEvent(Player player) {
        this.player = player;
    }

    public void setTimeCookChungCake(int sec) {
        timeCookChungCake += sec;
    }

    public void setTimeCookTetCake(int sec) {
        timeCookTetCake += sec;
    }

    public void addEventPoint(int num) {
        eventPoint += num;
    }

    public void subEventPoint(int num) {
        eventPoint -= num;
    }

    public void update() {
        if (timeCookChungCake > 0) {
            timeCookChungCake--;
        }
        if (timeCookTetCake > 0) {
            timeCookTetCake--;
        }
    }

}

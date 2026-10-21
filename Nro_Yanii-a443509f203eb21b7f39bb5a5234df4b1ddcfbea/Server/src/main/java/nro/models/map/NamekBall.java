package nro.models.map;

import lombok.Getter;
import lombok.Setter;
import nro.services.ItemMapService;
import nro.services.Service;

/**
 * @author outcast c-cute hột me 😳
 */

@Getter
@Setter
public class NamekBall extends ItemMap {

    private boolean isHolding;
    private boolean isCleaning;
    private boolean isStone;
    private long cleaningTime;
    private int index;
    private String holderName;
    public NamekBall(Zone zone, int tempId, int quantity, int x, int y, long playerId) {
        super(zone, tempId, quantity, x, y, playerId);
        setHolderName("");
    }

    @Override
    public void update() {
        if (isCleaning && cleaningTime > 0) {
            cleaningTime--;
        }
    }

    public void setZone(Zone newZone) {
        this.zone.removeItemMap(this);
        this.zone = newZone;
        this.zone.addItem(this);
    }

    @Override
    public void reAppearItem() {
        if (isHolding) {
            ItemMapService.gI().sendItemMapDisappear(this);
        } else {
            Service.getInstance().dropItemMap(this.zone, this);
        }
    }

}

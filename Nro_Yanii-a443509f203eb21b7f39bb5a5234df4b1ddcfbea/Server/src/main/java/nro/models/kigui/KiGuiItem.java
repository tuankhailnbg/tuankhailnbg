package nro.models.kigui;

import nro.models.item.Item;
import lombok.Getter;
import lombok.Setter;

/**
 * @author outcast c-cute hột me 😳
 */
@Setter
@Getter
public class KiGuiItem extends Item {
    private int consignID;
    private long consignorID;
    private int priceGold;
    private int priceGem;
    private byte tab;
    private int sold;
    private boolean upTop;
    private long timeConsign;
    private String consignName = "";
    private boolean isSuKien;
}

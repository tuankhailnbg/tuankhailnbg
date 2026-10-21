package nro.resources.entity;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

/**
 *
 * @author 💖 Nro Yanii 💖
 */
@Getter
public class Frame {

    @SerializedName("sprite_id")
    private int spriteID;
    private int dx;
    private int dy;

}

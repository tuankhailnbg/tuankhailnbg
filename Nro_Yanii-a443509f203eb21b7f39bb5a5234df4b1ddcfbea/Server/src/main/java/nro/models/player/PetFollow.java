package nro.models.player;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class PetFollow {

    private int id;

    private int iconID;

    private int width;

    private int height;

    private byte nFrame;
}

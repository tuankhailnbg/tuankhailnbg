package nro.models.item;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class MinipetTemplate {
    private int id;
    private short head;
    private short body;
    private short leg;
}

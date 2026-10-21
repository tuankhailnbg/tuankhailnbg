
package nro.models;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author bkt
 */
@Getter
@Setter
public class Part {

    private short id;

    private byte type;

    private int[][] partData;

    public int getIcon(int index) {
        if (partData != null && index >= 0 && index < partData.length) {
            return partData[index][0]; 
        } else {
            return -1; 
        }
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nro.models.item;

/**
 *
 * @author Kitak
 */
public class CaiTrang {

    public int tempId;
    /**
     * head, body, leg, bag
     */
    public int[] id;

    public CaiTrang(int tempId, int... id) {
        this.tempId = tempId;
        this.id = id;
    }

    public int[] getID() {
        return id;
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nro.models.item;

import java.util.Arrays;

/**
 *
 * @author Kitak
 */
public class ItemReward {

    public int[] mapId;
    public int tempId;
    public int ratio;
    public int typeRatio;
    public boolean forAllGender;

    @Override
    public String toString() {
        return "ItemReward{" + "mapId=" + Arrays.toString(mapId) + ", tempId=" + tempId + ", ratio=" + ratio + ", typeRatio=" + typeRatio + ", forAllGender=" + forAllGender + '}';
    }
}

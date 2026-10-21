/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nro.card;

import nro.models.item.ItemOption;
import java.util.ArrayList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 *
 * @author 💖 Nro Yanii 💖
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardTemplate {

    private int id;
    private int itemID;
    private byte rank;
    private byte maxAmount;
    private byte type;
    private int icon;
    private String name;
    private String info;
    private short mobID;
    private short head;
    private short body;
    private short leg;
    private short bag;
    private short aura;
    private ArrayList<ItemOption> options;
}

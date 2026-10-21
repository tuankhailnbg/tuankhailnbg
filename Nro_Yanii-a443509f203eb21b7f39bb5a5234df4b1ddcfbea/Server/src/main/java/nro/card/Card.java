/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nro.card;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author 💖 Nro Yanii 💖
 */
@Getter
@Setter
public class Card {

    @SerializedName("id")
    private int id;
    @SerializedName("amount")
    private int amount;
    @SerializedName("level")
    private int level;
    @SerializedName("use")
    private boolean isUse;
    private transient CardTemplate cardTemplate;

    public void addAmount(int amount) {
        this.amount += amount;
        if (this.amount >= cardTemplate.getMaxAmount()) {
            levelUp();
        }
    }

    private void levelUp() {
        this.amount = 0;
        this.level++;
    }

    public void setTemplate() {
        cardTemplate = CardManager.getInstance().find(id);
    }
}

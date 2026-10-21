/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nro.models.mob;

import nro.models.player.Player;

/**
 *
 * @author 💖 Nro Yanii 💖
 */
public interface IBigBoss {

    public void attack(Player player);

    public void move(int x, int y);

}

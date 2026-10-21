/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nro.models;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @fix by bkt
 */
@Getter
@Setter
public class PartImage {

    private short icon;
    private byte dx;
    private byte dy;

    public PartImage(short icon, byte dx, byte dy) {
        this.icon = icon;
        this.dx = dx;
        this.dy = dy;
    }
}

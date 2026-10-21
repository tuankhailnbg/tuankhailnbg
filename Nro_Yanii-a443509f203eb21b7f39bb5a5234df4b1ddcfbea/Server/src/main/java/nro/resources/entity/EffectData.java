/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nro.resources.entity;

import nro.server.io.Message;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;

/**
 *
 * @author 💖 Nro Yanii 💖
 */
@Getter
public class EffectData {

    private int id;
    private byte type;
    private Sprite[] sprites;
    private Frame[][] frames;
    private short[] animations;

    public byte[] getData(int version) {
        byte[] data = null;
        try {
            Message ms = new Message();
            DataOutputStream ds = ms.writer();
            ds.writeByte(sprites.length);
            for (Sprite sprite : sprites) {
                ds.writeByte(sprite.getId());
                if (type == 0 || type == 1 || version < 220) {
                    ds.writeByte(sprite.getX());
                    ds.writeByte(sprite.getY());
                } else {
                    ds.writeShort(sprite.getX());
                    ds.writeShort(sprite.getY());
                }
                ds.writeByte(sprite.getW());
                ds.writeByte(sprite.getH());
            }
            ds.writeShort(frames.length);
            for (Frame[] a : frames) {
                ds.writeByte(a.length);
                for (Frame frame : a) {
                    ds.writeShort(frame.getDx());
                    ds.writeShort(frame.getDy());
                    ds.writeByte(frame.getSpriteID());
                }
            }
            ds.writeShort(animations.length);
            for (short a : animations) {
                ds.writeShort(a);
            }
            ds.flush();
            data = ms.getData();
            ms.cleanup();
        } catch (IOException ex) {
            Logger.getLogger(MobData.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }

}

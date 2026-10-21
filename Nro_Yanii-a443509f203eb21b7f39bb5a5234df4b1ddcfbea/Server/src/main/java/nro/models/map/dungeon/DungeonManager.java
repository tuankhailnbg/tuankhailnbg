/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nro.models.map.dungeon;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author 💖 Nro Yanii 💖
 */
public class DungeonManager implements Runnable {

    private final List<Dungeon> list = new ArrayList<>();
    private boolean running;
    private int increasement;

    public DungeonManager() {
        start();
    }

    public int generateID() {
        return increasement++;
    }

    public void addDungeon(Dungeon dungeon) {
        synchronized (list) {
            dungeon.setId(generateID());
            list.add(dungeon);
        }
    }

    public void removeDungeon(Dungeon dungeon) {
        synchronized (list) {
            list.remove(dungeon);
        }
    }

    public Dungeon find(int id) {
        synchronized (list) {
            for (Dungeon dungeon : list) {
                if (dungeon.id == id) {
                    return dungeon;
                }
            }
        }
        return null;
    }

    public void update() {
        synchronized (list) {
            List<Dungeon> r = new ArrayList<>();
            for (Dungeon dungeon : list) {
                try {
                    dungeon.update();
                } catch (Exception e) {
                }
                if (dungeon.isClosed()) {
                    r.add(dungeon);
                }
            }
            list.removeAll(r);
        }
    }

    public void start() {
        running = true;
    }

    public void shutdown() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            long now = System.currentTimeMillis();
            update();
            long now2 = System.currentTimeMillis();
            if (now2 - now < 1000) {
                try {
                    Thread.sleep(1000 - (now2 - now));
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}

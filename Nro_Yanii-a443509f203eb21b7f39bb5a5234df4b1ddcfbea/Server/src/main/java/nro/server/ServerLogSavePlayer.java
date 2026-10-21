package nro.server;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author 💖 Nro Yanii 💖
 * 
 *
 */
public class ServerLogSavePlayer implements Runnable {

    private static ServerLogSavePlayer i;

    private ServerLogSavePlayer() {
        this.list = new ArrayList<>();
        try {
            this.bw = new BufferedWriter(
                    new FileWriter("log/log_save_player" + System.currentTimeMillis() + ".sql", true));
            new Thread(this, "Log file save player").start();
        } catch (IOException ex) {
            Logger.getLogger(ServerLogSavePlayer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static ServerLogSavePlayer gI() {
        if (i == null) {
            i = new ServerLogSavePlayer();
        }
        return i;
    }

    private List<String> list;
    private BufferedWriter bw;

    @Override
    public void run() {
        while (true) {
            while (!this.list.isEmpty()) {
                String text = this.list.remove(0);
                try {
                    bw.write(text.substring(text.indexOf(":") + 2, text.length()) + "\n");
                    bw.flush();
                } catch (IOException ex) {
                }

            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
            }
        }
    }

    public void add(String text) {
        list.add(text);
    }

}

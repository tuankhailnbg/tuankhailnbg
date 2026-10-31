// Decompiled with: VMN
package server;

import db.DbManager;
import io.Session;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    private static final Server instance = new Server();
    private ServerSocket listen;
    private Config config = new Config("server.ini");
    private ServerManager manager = new ServerManager();
    private ServerService service = new ServerService(this.manager);
    private boolean running;

    public static Server getInstance() {
        return instance;
    }

    public void start() {
        activeCommandLine();
        DbManager.getInstance().start();
        this.running = true;
        try {
            // Game and Login are on the same VPS: do not expose port 3105.
            // Override only when intentionally using a separate protected Login host.
            String bindAddress = System.getProperty("nro.login.bindAddress", "127.0.0.1");
            this.listen = new ServerSocket(this.config.getListen(), 64, InetAddress.getByName(bindAddress));
            System.out.println("Login listening at " + bindAddress + ":" + this.config.getListen());
            int i = 0;
            while (this.running) {
                Session session = new Session(this.listen.accept(), i++);
                System.out.println("client " + session.sessionName + " connected!");
            }
        } catch (IOException ex) {
            if (this.running) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void shutdown() {
        this.running = false;
        try {
            if (this.listen != null) {
                this.listen.close();
            }
        } catch (IOException iOException) {
        }
        DbManager.getInstance().shutdown();
    }

    private void activeCommandLine() {
        new Thread(() -> {
            Scanner sc = new Scanner(System.in);
            while (true) {
                String line = sc.nextLine();
                if (line.equals("baotri")) {
                    shutdown();
                    break;
                }

            }
            sc.close();
        }, "Active line").start();
    }

    public static void main(String[] args) {
        instance.start();
    }

    public Config getConfig() {
        return this.config;
    }

    public ServerManager getManager() {
        return this.manager;
    }

    public ServerService getService() {
        return this.service;
    }
}

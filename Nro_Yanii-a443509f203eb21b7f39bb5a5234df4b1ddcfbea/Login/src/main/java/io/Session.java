// Decompiled with: VMN
package io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import model.User;
import model.UserManager;
import server.Server;

public class Session {

    public String sessionName;
    private int serverID;
    private Controller controller;
    private Service service;
    public int id;
    private final byte[] key = "vmn".getBytes();
    public Socket sc;
    public DataInputStream dis;
    public DataOutputStream dos;
    public boolean connected;
    public boolean isLoginSuccess;
    private byte curR;
    private byte curW;
    private Sender sender;
    private Thread collectorThread;
    protected Thread sendThread;
    public boolean sendKeyComplete;
    public boolean isClosed;

    public Session(Socket sc, int id) throws IOException {
        this.sc = sc;
        this.id = id;
        this.sessionName = sc.getRemoteSocketAddress().toString();
        this.sc.setKeepAlive(true);
        this.connected = true;
        this.dis = new DataInputStream(sc.getInputStream());
        this.dos = new DataOutputStream(sc.getOutputStream());
        this.controller = new Controller(this);
        this.service = new Service(this);
        this.sender = new Sender();
        this.sendThread = new Thread(this.sender);
        this.sendThread.setName("sender id: " + this.id);
        this.collectorThread = new Thread(new MessageCollector());
        this.collectorThread.setName("reader id: " + this.id);
        this.collectorThread.start();
        Server.getInstance().getManager().add(this);
    }

    public boolean isConnected() {
        return this.connected;
    }

    public void sendMessage(Message message) {
        if (this.connected) {
            this.sender.addMessage(message);
        }
    }

    private void doSendMessage(Message m) {
        try {
            byte[] data = m.getData();
            byte value = m.command;
            int num = data.length;
            byte b = value = num > Short.MAX_VALUE ? (byte) -32 : (byte) value;
            if (this.sendKeyComplete) {
                b = this.writeKey(value);
            }
            this.dos.writeByte(b);
            if (value == -32) {
                if (this.sendKeyComplete) {
                    this.dos.writeByte(this.writeKey(m.command));
                } else {
                    this.dos.writeByte(m.command);
                }
                byte byte2 = this.writeKey((byte) (num >> 24));
                this.dos.writeByte(byte2);
                byte byte3 = this.writeKey((byte) (num >> 16));
                this.dos.writeByte(byte3);
                byte byte4 = this.writeKey((byte) (num >> 8));
                this.dos.writeByte(byte4);
                byte byte5 = this.writeKey((byte) (num & 0xFF));
                this.dos.writeByte(byte5);
            } else if (this.sendKeyComplete) {
                byte byte6 = this.writeKey((byte) (num >> 8));
                this.dos.writeByte(byte6);
                byte byte7 = this.writeKey((byte) (num & 0xFF));
                this.dos.writeByte(byte7);
            } else {
                this.dos.writeByte(num & 0xFF00);
                this.dos.writeByte(num & 0xFF);
            }
            if (this.sendKeyComplete) {
                for (int i = 0; i < num; ++i) {
                    data[i] = this.writeKey(data[i]);
                }
            }
            this.dos.write(data);
            this.dos.flush();
        } catch (Exception exception) {
            // empty catch block
        }
    }

    public void setServer(Message ms) {
        try {
            this.serverID = ms.reader().readInt();
            System.out.println("set server: " + this.serverID);
            System.out.println("remove all user");
            UserManager.getInstance().removeAllUserWithServerID(this.serverID);
            int size = ms.reader().readInt();
            for (int i = 0; i < size; ++i) {
                int clientID = ms.reader().readInt();
                int userID = ms.reader().readInt();
                String username = ms.reader().readUTF();
                String password = ms.reader().readUTF();
                User user = new User(username, password, this.serverID, clientID, this);
                user.setUserID(userID);
                UserManager.getInstance().add(user);
                System.out.println("add user: " + username);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public byte readKey(byte b) {
        byte b2 = this.curR;
        this.curR = (byte) (b2 + 1);
        byte result = (byte) (this.key[b2] & 0xFF ^ b & 0xFF);
        if (this.curR >= this.key.length) {
            this.curR = (byte) (this.curR % this.key.length);
        }
        return result;
    }

    public byte writeKey(byte b) {
        byte b2 = this.curW;
        this.curW = (byte) (b2 + 1);
        byte result = (byte) (this.key[b2] & 0xFF ^ b & 0xFF);
        if (this.curW >= this.key.length) {
            this.curW = (byte) (this.curW % this.key.length);
        }
        return result;
    }

    public void close() {
        try {
            Server.getInstance().getManager().remove(this);
            this.cleanNetwork();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cleanNetwork() {
        try {
            this.curR = 0;
            this.curW = 0;
            this.connected = false;
            this.isLoginSuccess = false;
            if (this.sc != null) {
                this.sc.close();
                this.sc = null;
            }
            if (this.dos != null) {
                this.dos.close();
                this.dos = null;
            }
            if (this.dis != null) {
                this.dis.close();
                this.dis = null;
            }
            if (this.sendThread != null) {
                this.sendThread.interrupt();
                this.sendThread = null;
            }
            if (this.collectorThread != null) {
                this.collectorThread.interrupt();
                this.collectorThread = null;
            }
            this.controller = null;
            this.service = null;
            System.gc();
        } catch (IOException iOException) {
            // empty catch block
        }
    }

    public void sendKey() throws Exception {
        if (!this.sendKeyComplete) {
            Message ms = new Message(-27);
            DataOutputStream ds = ms.writer();
            ds.writeByte(this.key.length);
            ds.writeByte(this.key[0]);
            for (int i = 1; i < this.key.length; ++i) {
                ds.writeByte(this.key[i] ^ this.key[i - 1]);
            }
            ds.flush();
            this.doSendMessage(ms);
            this.sendKeyComplete = true;
            this.sendThread.start();
        }
    }

    public void sendTimeWaitLogin(short second) {
        Message msg = null;
        try {
            msg = new Message(122);
            msg.writer().writeShort(second);
            this.doSendMessage(msg);
            msg.writer().flush();
        } catch (Exception e) {
            System.out.println("io.Session.sendTimeWaitLogin()");
        }
    }

    public void disconnect() {
        try {
            if (this.sc != null) {
                this.sc.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeMessage() {
        try {
            if (this.isClosed) {
                return;
            }
            this.isClosed = true;
            if (this.controller != null) {
                this.controller.onDisconnected();
            }
            this.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getServerID() {
        return this.serverID;
    }

    public Service getService() {
        return this.service;
    }

    private class Sender
            implements Runnable {

        private final ArrayList<Message> sendingMessage = new ArrayList();

        public void addMessage(Message message) {
            this.sendingMessage.add(message);
        }

        @Override
        public void run() {
            while (Session.this.connected) {
                if (Session.this.sendKeyComplete) {
                    while (this.sendingMessage != null && this.sendingMessage.size() > 0) {
                        try {
                            Message m = this.sendingMessage.get(0);
                            if (m != null) {
                                Session.this.doSendMessage(m);
                            }
                            this.sendingMessage.remove(0);
                        } catch (Exception e) {
                            Session.this.disconnect();
                        }
                    }
                }
                try {
                    Thread.sleep(10L);
                } catch (InterruptedException interruptedException) {
                }
            }
        }
    }

    class MessageCollector
            implements Runnable {

        MessageCollector() {
        }

        /*
         * Enabled aggressive block sorting
         * Enabled unnecessary exception pruning
         * Enabled aggressive exception aggregation
         */
        @Override
        public void run() {
            try {
                Message message;
                while (Session.this.connected && (message = this.readMessage()) != null) {
                    try {
                        if (!Session.this.sendKeyComplete) {
                            Session.this.sendKey();
                            continue;
                        }
                        if (Session.this.isClosed) {
                            continue;
                        }
                        Session.this.controller.process(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception exception) {
                // empty catch block
            }
            Session.this.closeMessage();
        }

        private Message readMessage() throws Exception {
            try {
                int size;
                byte cmd = Session.this.dis.readByte();
                if (Session.this.sendKeyComplete) {
                    cmd = Session.this.readKey(cmd);
                }
                if (Session.this.sendKeyComplete) {
                    byte b1 = Session.this.dis.readByte();
                    byte b2 = Session.this.dis.readByte();
                    size = (Session.this.readKey(b1) & 0xFF) << 8 | Session.this.readKey(b2) & 0xFF;
                } else {
                    size = Session.this.dis.readUnsignedShort();
                }
                byte[] data = new byte[size];
                int len = 0;
                int byteRead = 0;
                while (len != -1 && byteRead < size) {
                    len = Session.this.dis.read(data, byteRead, size - byteRead);
                    if (len <= 0) {
                        continue;
                    }
                    byteRead += len;
                }
                if (Session.this.sendKeyComplete) {
                    for (int i = 0; i < data.length; ++i) {
                        data[i] = Session.this.readKey(data[i]);
                    }
                }
                Message msg = new Message(cmd, data);
                return msg;
            } catch (EOFException e) {
                return null;
            }
        }
    }
}

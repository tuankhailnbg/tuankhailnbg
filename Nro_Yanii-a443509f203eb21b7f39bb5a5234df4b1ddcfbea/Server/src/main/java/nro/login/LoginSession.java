/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nro.login;

import nro.server.io.Message;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Vector;
import lombok.Getter;

/**
 *
 * @author 💖 Nro Yanii 💖
 */
public class LoginSession {

    @Getter
    private boolean connected;
    private LoginController controller;
    @Getter
    private LoginService service;
    public boolean isStopSend = false;
    private DataOutputStream dos;
    public DataInputStream dis;
    public Socket sc;
    public boolean connecting;
    private final Sender sender = new Sender();
    public Thread initThread;
    public Thread collectorThread;
    public int sendByteCount;
    public int recvByteCount;
    boolean getKeyComplete;
    public byte[] key = null;
    private byte curR, curW;
    long timeConnected;
    public String strRecvByteCount = "";
    public boolean isCancel;
    private Vector sendingMessage;
    private String host;
    private int port;

    public LoginSession() {
        this.controller = new LoginController(this);
        this.service = new LoginService(this);
    }

    public void connect(String host, int port) {
        if (connected || connecting) {
            return;
        } else {
            getKeyComplete = false;
            sc = null;
            initThread = new Thread(new NetworkInit(host, port));
            initThread.start();
        }
    }

    public void reconnect() {
        System.out.println("ket noi lai!");
        connect(host, port);
    }

    class NetworkInit implements Runnable {

        NetworkInit(String h, int p) {
            host = h;
            port = p;
        }

        public void run() {
            isCancel = false;
            new Thread(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(20000);
                    } catch (InterruptedException e) {
                    }
                    if (connecting) {
                        try {
                            sc.close();
                        } catch (Exception e) {
                        }
                        isCancel = true;
                        connecting = false;
                        connected = false;
                        controller.onConnectionFail();
                    }
                }
            }).start();
            connecting = true;
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            connected = true;
            try {
                doConnect(host, port);
                controller.onConnectOK();
            } catch (Exception ex) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
                if (isCancel) {
                    return;
                }
                if (controller != null) {
                    close();
                    controller.onConnectionFail();
                }
            }
        }

        public void doConnect(String host, int port) throws Exception {
            sc = new Socket(host, port);
            dos = new DataOutputStream(sc.getOutputStream());
            dis = new DataInputStream(sc.getInputStream());
            new Thread(sender).start();
            collectorThread = new Thread(new MessageCollector());
            collectorThread.start();
            timeConnected = System.currentTimeMillis();
            doSendMessage(new Message(-27));
            connecting = false;
        }
    }

    public void sendMessage(Message message) {
        sender.AddMessage(message);
    }

    private synchronized void doSendMessage(Message m) throws IOException {
        byte[] data = m.getData();
        try {
            if (getKeyComplete) {
                byte b = (writeKey(m.command));
                dos.writeByte(b);
            } else {
                dos.writeByte(m.command);
            }
            // System.out.println("cmd send ---> "+m.command);
            if (data != null) {
                int size = data.length;
                if (m.command == -31) {
                    dos.writeShort(size);
                } else if (getKeyComplete) {
                    int byte1 = writeKey((byte) (size >> 8));
                    dos.writeByte(byte1);
                    int byte2 = writeKey((byte) (size & 0xFF));
                    dos.writeByte(byte2);
                } else {
                    dos.writeShort(size);
                }
                if (getKeyComplete) {
                    for (int i = 0; i < data.length; i++) {
                        data[i] = writeKey(data[i]);
                    }
                }
                dos.write(data);
                sendByteCount += (5 + data.length);
            } else {
                dos.writeShort(0);
                sendByteCount += 5;
            }
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte readKey(byte b) {
        byte i = (byte) ((key[curR++] & 0xff) ^ (b & 0xff));
        if (curR >= key.length) {
            curR %= key.length;
        }
        return i;
    }

    private byte writeKey(byte b) {
        byte i = (byte) ((key[curW++] & 0xff) ^ (b & 0xff));
        if (curW >= key.length) {
            curW %= key.length;
        }
        return i;
    }

    private class Sender implements Runnable {

        public Sender() {
            sendingMessage = new Vector();
        }

        public void AddMessage(Message message) {
            sendingMessage.addElement(message);
        }

        public void run() {
            while (connected) {
                try {
                    if (getKeyComplete) {
                        while (sendingMessage.size() > 0) {
                            Message m = (Message) sendingMessage.elementAt(0);
                            sendingMessage.removeElementAt(0);
                            doSendMessage(m);
                        }
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class MessageCollector implements Runnable {

        public void run() {
            Message message;
            try {
                while (isConnected()) {
                    message = readMessage();
                    if (message != null) {
                        try {
                            if (message.command == -27) {
                                getKey(message);
                            } else {
                                controller.process(message);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        break;
                    }
                }
            } catch (Exception ex) {
            }
            if (connected) {
                if (controller != null) {
                    if (System.currentTimeMillis() - timeConnected > 500) {
                        controller.onDisconnected();
                    } else {
                        controller.onConnectionFail();
                    }
                }
                if (sc != null) {
                    cleanNetwork();
                }
            }
        }

        private void getKey(Message message) throws IOException {
            byte keySize = message.reader().readByte();
            key = new byte[keySize];
            for (int i = 0; i < keySize; i++) {
                key[i] = message.reader().readByte();
            }
            for (int i = 0; i < key.length - 1; i++) {
                key[i + 1] ^= key[i];
            }
            getKeyComplete = true;
        }

        private Message readMessage() throws Exception {

            // read message command
            byte cmd = dis.readByte();
            if (getKeyComplete) {
                cmd = readKey(cmd);
            }
            // read size of data
            int size;

            if (cmd == -32) {
                cmd = dis.readByte();
                if (getKeyComplete) {
                    cmd = readKey(cmd);
                }
                byte b1 = readKey(dis.readByte());
                byte b2 = readKey(dis.readByte());
                byte b3 = readKey(dis.readByte());
                byte b4 = readKey(dis.readByte());
                size = ((b1 & 0xff) << 24) | ((b2 & 0xff) << 16)
                        | ((b3 & 0xff) << 8) | (b4 & 0xff);
            } else if (getKeyComplete) {
                byte b1 = dis.readByte();
                byte b2 = dis.readByte();
                size = (readKey(b1) & 0xff) << 8 | readKey(b2) & 0xff;
            } else {
                size = dis.readUnsignedShort();
            }
            byte data[] = new byte[size];
            int len = 0;
            int byteRead = 0;
            while (len != -1 && byteRead < size) {
                len = dis.read(data, byteRead, size - byteRead);
                if (len > 0) {
                    byteRead += len;
                    recvByteCount += (5 + byteRead);
                    int Kb = (recvByteCount + sendByteCount);
                    strRecvByteCount = Kb / 1024 + "." + Kb % 1024 / 102 + "Kb";
                }
            }
            if (getKeyComplete) {
                for (int i = 0; i < data.length; i++) {
                    data[i] = readKey(data[i]);
                }
            }
            Message msg = new Message(cmd, data);
            return msg;
        }
    }

    public void close() {
        cleanNetwork();
    }

    private void cleanNetwork() {
        key = null;
        curR = 0;
        curW = 0;
        try {
            connected = false;
            connecting = false;
            if (sc != null) {
                sc.close();
                sc = null;
            }
            if (dos != null) {
                dos.close();
                dos = null;
            }
            if (dis != null) {
                dis.close();
                dis = null;
            }
            collectorThread = null;
            System.gc();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

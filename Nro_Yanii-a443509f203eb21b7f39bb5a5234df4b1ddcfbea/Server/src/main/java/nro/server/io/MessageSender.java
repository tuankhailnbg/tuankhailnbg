package nro.server.io;

import static nro.server.io.Session.KEYS;
import nro.utils.Log;
import nro.server.Manager;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author 💖 Nro Yanii 💖
 * 
 *
 */
public class MessageSender implements Runnable {

    private Session session;
    private final Queue<Message> sendingMessage;
    DataOutputStream dos;

    public int getNumMessage() {
        return this.sendingMessage.size();
    }

    public MessageSender(Session session, Socket socket) {
        sendingMessage = new ConcurrentLinkedQueue<>();
        try {
            this.session = session;
            this.dos = new DataOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            Log.error(MessageSender.class, e);
        }
    }

    public void addMessage(Message message) {
        try {
            sendingMessage.add(message);
        } catch (Exception e) {
        }
    }

    @Override
    public void run() {
        Message message;
        while (session != null && session.connected) {
            try {
                while ((message = sendingMessage.poll()) != null) {
                    doSendMessage(message);
                }
            } catch (Exception e) {
            }
            try {
                Thread.sleep(10);
            } catch (Exception e) {
            }
        }
    }

    public synchronized void doSendMessage(Message msg) {
        try {
            byte[] data = msg.getData();
            boolean largeFrame = msg.command == -32 || msg.command == -66 || msg.command == -74
                    || msg.command == 11 || msg.command == -67 || msg.command == -87 || msg.command == 66
                    || msg.command == 12;
            int maxSize = largeFrame ? 0xFFFFFF : 0xFFFF;
            if (data != null && data.length > maxSize) {
                // Validate before writing even the command byte. A wrapped length
                // corrupts every subsequent packet on the connection.
                Log.error("Refused oversized packet: command=" + msg.command + ", bytes=" + data.length);
                return;
            }
            if (session.connected) {
                byte b = writeKey(msg.command);
                dos.writeByte(b);
            } else {
                dos.writeByte(msg.command);
            }
            if (data != null) {
                int size = data.length;
                if (largeFrame) {
                    byte b = writeKey((byte) (size));
                    dos.writeByte(b - 128);
                    byte b2 = writeKey((byte) (size >> 8));
                    dos.writeByte(b2 - 128);
                    byte b3 = writeKey((byte) (size >> 16));
                    dos.writeByte(b3 - 128);
                } else if (session.connected) {
                    int byte1 = writeKey((byte) (size >> 8));
                    dos.writeByte(byte1);
                    int byte2 = writeKey((byte) (size & 255));
                    dos.writeByte(byte2);
                } else {
                    dos.writeShort(size);
                }
                if (session.connected) {
                    for (int i = 0; i < data.length; i++) {
                        data[i] = writeKey(data[i]);
                    }
                }
                dos.write(data);
            } else {
                dos.writeShort(0);
            }
            dos.flush();
            session.traceGameplay(false, msg.command, data == null ? 0 : data.length);
            msg.cleanup();
        } catch (Exception e) {
            Log.error(MessageSender.class, e, "Lỗi gửi gói tin cmd=" + msg.command);
        }
    }

    private byte writeKey(byte b) {
        byte i = (byte) ((Session.KEYS[session.curW++] & 255) ^ (b & 255));
        if (session.curW >= Session.KEYS.length) {
            session.curW %= Session.KEYS.length;
        }
        return i;
    }

    public void sendSessionKey() {
        Message msg = new Message(-27);
        try {
            msg.writer().writeByte(KEYS.length);
            msg.writer().writeByte(KEYS[0]);
            for (int i = 1; i < KEYS.length; i++) {
                msg.writer().writeByte(KEYS[i] ^ KEYS[i - 1]);
            }
            msg.writer().writeUTF(Manager.advertisedGameHost);
            msg.writer().writeInt(14445);
            msg.writer().writeBoolean(false);
            doSendMessage(msg);
            msg.cleanup();
            session.connected = true;
            session.sendThread.start();
        } catch (Exception e) {
        }
    }

    void close() throws IOException {
        if (this.dos != null) {
            this.dos.close();
        }
        this.dos = null;
        this.session = null;
        if (this.sendingMessage != null) {
            this.sendingMessage.clear();
        }
    }
}

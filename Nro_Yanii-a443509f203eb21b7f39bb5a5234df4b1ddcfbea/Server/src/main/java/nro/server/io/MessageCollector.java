package nro.server.io;

import nro.consts.Cmd;
import nro.server.Client;
import nro.utils.Log;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.EOFException;
import java.net.Socket;

/**
 *
 * @author 💖 Nro Yanii 💖
 * 
 *
 */
public class MessageCollector implements Runnable {

    private DataInputStream dis;
    private Session session;
    private byte lastCommand;
    private int pendingPayloadBytes;

    public MessageCollector(Session session, Socket socket) {
        try {
            this.session = session;
            this.dis = new DataInputStream(socket.getInputStream());
        } catch (Exception e) {
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                Message msg = readMessage();
                session.lastTimeReadMessage = System.currentTimeMillis();
                if (!session.connected && msg.command != Cmd.GET_SESSION_ID) {
                    break;
                }
                try {
                    session.controller.onMessage(session, msg);
                } catch (Exception e) {
                    session.reportPacketError(msg.command, e);
                }

                msg.cleanup();
            }
        } catch (EOFException ex) {
            if (pendingPayloadBytes > 0) {
                Log.warning("[NET] session=" + session.id + " truncated cmd=" + lastCommand
                        + " expectedBytes=" + pendingPayloadBytes);
            }
        } catch (Exception ex) {
            session.reportPacketError(lastCommand, ex);
        }
        Client.gI().kickSession(session);
    }

    private Message readMessage() throws Exception {
        long st = System.currentTimeMillis();
        byte cmd = dis.readByte();
        if (session.connected) {
            cmd = readKey(cmd);
        }
        lastCommand = cmd;
        int size;
        if (session.connected) {
            byte b1 = dis.readByte();
            byte b2 = dis.readByte();
            size = (readKey(b1) & 255) << 8 | readKey(b2) & 255;
        } else {
            size = dis.readUnsignedShort();
        }
        if (size > 1024) {
            throw new IOException("Inbound frame too large: cmd=" + cmd + ", bytes=" + size);
        }
        byte data[] = new byte[size];
        pendingPayloadBytes = size;
        dis.readFully(data);
        pendingPayloadBytes = 0;
        if (session.connected) {
            for (int i = 0; i < data.length; i++) {
                data[i] = readKey(data[i]);
            }
        }
        if (session.logCheck) {
            System.out.println("Time read message: " + (System.currentTimeMillis() - st) + " ms");
        }
        return new Message(cmd, data);
    }

    private byte readKey(byte b) {
        byte i = (byte) ((Session.KEYS[session.curR++] & 255) ^ (b & 255));
        if (session.curR >= Session.KEYS.length) {
            session.curR %= Session.KEYS.length;
        }
        return i;
    }

    void close() throws IOException {
        if (this.dis != null) {
            this.dis.close();
        }
        this.dis = null;
        this.session = null;
    }
}

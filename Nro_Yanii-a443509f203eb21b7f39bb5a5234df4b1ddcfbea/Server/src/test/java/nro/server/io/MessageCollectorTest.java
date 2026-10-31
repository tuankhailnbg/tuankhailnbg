package nro.server.io;

import java.io.*;
import java.lang.reflect.*;
import java.net.Socket;
import org.junit.Test;
import static org.junit.Assert.*;

public class MessageCollectorTest {
    private Message read(byte[] bytes) throws Exception {
        InputStream stream = new ByteArrayInputStream(bytes) {
            @Override public synchronized int read(byte[] data, int offset, int length) {
                return super.read(data, offset, Math.min(2, length));
            }
        };
        Session session = new Session();
        session.connected = true;
        MessageCollector collector = new MessageCollector(session, new Socket() {
            @Override public InputStream getInputStream() { return stream; }
        });
        Method method = MessageCollector.class.getDeclaredMethod("readMessage");
        method.setAccessible(true);
        try {
            return (Message) method.invoke(collector);
        } catch (InvocationTargetException error) {
            throw (Exception) error.getCause();
        }
    }

    @Test public void completeFragmentedNpcActionIsDecoded() throws Exception {
        Message message = read(new byte[]{32, 0, 3, 0, 80, 6});
        assertEquals(32, message.command);
        assertEquals(80, message.reader().readShort());
        assertEquals(6, message.reader().readByte());
        assertEquals(0, message.reader().available());
    }

    @Test(expected = EOFException.class)
    public void truncatedActionCannotBeDispatchedWithZeroFilledBytes() throws Exception {
        read(new byte[]{32, 0, 3, 0, 80});
    }

    @Test(expected = IOException.class)
    public void oversizedIncomingActionIsRejected() throws Exception {
        read(new byte[]{32, 4, 1});
    }
}

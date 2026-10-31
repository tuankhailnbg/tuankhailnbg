package nro.server.io;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class MessageSenderTest {
    private MessageSender sender(ByteArrayOutputStream wire) throws Exception {
        Session session=new Session(); session.connected=true;
        return new MessageSender(session,new Socket(){
            @Override public OutputStream getOutputStream(){return wire;}
        });
    }
    @Test public void oversizedNormalPacketCannotCorruptFollowingFrame() throws Exception {
        ByteArrayOutputStream wire=new ByteArrayOutputStream(); MessageSender sender=sender(wire);
        Message tooBig=new Message(-28); tooBig.writer().write(new byte[180564]);
        sender.doSendMessage(tooBig); assertEquals(0,wire.size());
        Message valid=new Message(-28); valid.writer().writeInt(123456); sender.doSendMessage(valid);
        DataInputStream in=new DataInputStream(new ByteArrayInputStream(wire.toByteArray()));
        assertEquals(-28,in.readByte()); assertEquals(4,in.readUnsignedShort());
        assertEquals(123456,in.readInt()); assertEquals(0,in.available());
    }
    @Test public void simultaneousWritersKeepCompleteFramesTogether() throws Exception {
        ByteArrayOutputStream wire=new ByteArrayOutputStream(){
            @Override public synchronized void write(int b){super.write(b);Thread.yield();}
        };
        MessageSender sender=sender(wire); ExecutorService pool=Executors.newFixedThreadPool(8);
        try {
            List<Future<?>> jobs=new ArrayList<>();
            for(int id=0;id<200;id++) {
                final int value=id;
                jobs.add(pool.submit(()->{
                    try {
                        Message m=new Message(-28);m.writer().writeInt(value);m.writer().write(new byte[100]);
                        sender.doSendMessage(m);
                    }catch(IOException e){throw new UncheckedIOException(e);}
                }));
            }
            for(Future<?> job:jobs)job.get(10,TimeUnit.SECONDS);
        } finally {pool.shutdownNow();}
        DataInputStream in=new DataInputStream(new ByteArrayInputStream(wire.toByteArray()));
        Set<Integer> received=new HashSet<>();
        for(int i=0;i<200;i++) {
            assertEquals(-28,in.readByte()); assertEquals(104,in.readUnsignedShort());
            assertTrue(received.add(in.readInt()));byte[] payload=new byte[100];in.readFully(payload);
            assertArrayEquals(new byte[100],payload);
        }
        assertEquals(200,received.size()); assertEquals(0,in.available());
    }
    @Test public void largeCommandKeepsItsExistingThreeByteLength() throws Exception {
        ByteArrayOutputStream wire=new ByteArrayOutputStream();MessageSender sender=sender(wire);
        Message m=new Message(-32);m.writer().write(new byte[70000]);sender.doSendMessage(m);
        byte[] data=wire.toByteArray();assertEquals(70004,data.length);assertEquals(-32,data[0]);
        int length=((data[1]+128)&255)|(((data[2]+128)&255)<<8)|(((data[3]+128)&255)<<16);
        assertEquals(70000,length);
    }

    @Test public void concurrentQueueProducersDoNotLoseLoginPackets() throws Exception {
        ByteArrayOutputStream wire=new ByteArrayOutputStream();
        Session session=new Session();session.connected=true;
        MessageSender sender=new MessageSender(session,new Socket(){
            @Override public OutputStream getOutputStream(){return wire;}
        });
        Thread consumer=new Thread(sender);consumer.start();
        ExecutorService pool=Executors.newFixedThreadPool(8);
        try {
            List<Future<?>> jobs=new ArrayList<>();
            for(int id=0;id<2000;id++) {
                final int value=id;
                jobs.add(pool.submit(()->{
                    try {
                        Message message=new Message(-28);message.writer().writeInt(value);
                        sender.addMessage(message);
                    } catch(IOException e){throw new UncheckedIOException(e);}
                }));
            }
            for(Future<?> job:jobs)job.get(10,TimeUnit.SECONDS);
            long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(10);
            while(sender.getNumMessage()>0 && System.nanoTime()<deadline) Thread.sleep(5);
        } finally {
            pool.shutdownNow();session.connected=false;consumer.join(10000);
        }
        DataInputStream in=new DataInputStream(new ByteArrayInputStream(wire.toByteArray()));
        Set<Integer> received=new HashSet<>();
        for(int i=0;i<2000;i++) {
            assertEquals(-28,in.readByte());assertEquals(4,in.readUnsignedShort());
            assertTrue(received.add(in.readInt()));
        }
        assertEquals(2000,received.size());assertEquals(0,in.available());
    }
}

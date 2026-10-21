package nro;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ServerBroadcaster {
    public static void startBroadcasting(int gamePort) {
        Thread broadcastThread = new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.setBroadcast(true);
                // Thông điệp gửi đi để điện thoại nhận diện
                String message = "NRO_SERVER_IP:" + gamePort;
                byte[] buffer = message.getBytes();

                while (true) {
                    // Phát broadcast ra toàn bộ mạng LAN qua cổng 8888
                    DatagramPacket packet = new DatagramPacket(
                        buffer, buffer.length, 
                        InetAddress.getByName("255.255.255.255"), 8888
                    );
                    socket.send(packet);
                    Thread.sleep(3000); // Cứ 3 giây phát một lần
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        broadcastThread.setDaemon(true);
        broadcastThread.start();
    }
}
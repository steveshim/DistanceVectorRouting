package run;


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class test {
    public static void main(String[] args) throws Exception{
        DatagramSocket s = new DatagramSocket();
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        InetAddress ip = InetAddress.getByName("192.168.10.105");
        String message = "Hello";
        buffer = message.getBytes();

        DatagramPacket out = new DatagramPacket(buffer, buffer.length, ip, 6500);
        s.send(out);
    }
}

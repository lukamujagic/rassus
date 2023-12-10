package hr.fer.tel.rassus.lab2.udp;

import hr.fer.tel.rassus.lab2.data.DataManager;
import hr.fer.tel.rassus.lab2.network.EmulatedSystemClock;
import hr.fer.tel.rassus.lab2.network.SimpleSimulatedDatagramSocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UDPServer {
    private final DatagramSocket socket;
    private final EmulatedSystemClock clock;
    private final DataManager dataManager;

    private static final int AVERAGE_DELAY_MILLIS = 1000;
    private static final double PACKET_LOSS = 0.2;

    public UDPServer(EmulatedSystemClock esc, DataManager dm, int port){
        this.clock = esc;
        this.dataManager = dm;
        try {
            this.socket = new SimpleSimulatedDatagramSocket(port, PACKET_LOSS, AVERAGE_DELAY_MILLIS);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Successfully started UDP server on port " + port);
    }

    public void shutdown(){
        socket.close();
        System.out.println("UDP server stopped.");
    }

    public void listen() {
        byte[] receivedBuffer = new byte[256];
        byte[] sendBuffer;
        String receivedString;

        DatagramPacket packetReceive = new DatagramPacket(receivedBuffer, receivedBuffer.length);

        try {
            socket.receive(packetReceive);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        receivedString = new String(packetReceive.getData(), packetReceive.getOffset(), packetReceive.getLength());
        System.out.println("Received data: " + receivedString);
        dataManager.addReceivedData(receivedString);

        String[] tmpReceived = receivedString.split(",");

        sendBuffer = ("ACK," + tmpReceived[2]).getBytes();

        DatagramPacket packetSend = new DatagramPacket(
                sendBuffer, sendBuffer.length, packetReceive.getAddress(), packetReceive.getPort());

        try {
            socket.send(packetSend);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

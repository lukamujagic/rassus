package hr.fer.tel.rassus.lab2.udp;

import hr.fer.tel.rassus.lab2.data.CsvReader;
import hr.fer.tel.rassus.lab2.data.Reading;
import hr.fer.tel.rassus.lab2.network.EmulatedSystemClock;
import hr.fer.tel.rassus.lab2.network.SimpleSimulatedDatagramSocket;

import java.io.IOException;
import java.net.*;

public class UDPClient {
    private static final int AVERAGE_DELAY_MILLIS = 1000;
    private static final double PACKET_LOSS = 0.2;

    private final DatagramSocket socket;
    private final EmulatedSystemClock clock;
    private final CsvReader csvReader;

    private int packetId = 0;


    public UDPClient(CsvReader reader, EmulatedSystemClock esc){
        this.csvReader = reader;
        this.clock = esc;

        try {
            this.socket = new SimpleSimulatedDatagramSocket(PACKET_LOSS, AVERAGE_DELAY_MILLIS);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public void shutdown(){
        socket.close();
        System.out.println("UDP client stopped. Sent " + (packetId+1) + " packets.");
    }

    public void send(String address, Integer port){
        Reading reading = csvReader.getNewReading();
        long currTime = reading.timestamp();
        Integer no2Value = reading.valueNO2();

        byte[] receivedBuffer = new byte[256];
        byte[] sendBuffer;
        String receivedString;
        System.out.println("Current time: " + currTime);
        String sendString = currTime + "," + no2Value + "," + packetId;

        boolean receivedACK = false;
        DatagramPacket packetReceive = new DatagramPacket(receivedBuffer, receivedBuffer.length);

        InetAddress inetAddress = null;

        try {
            inetAddress = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        while(!receivedACK) {
            sendBuffer = sendString.getBytes();
            DatagramPacket packetSend = new DatagramPacket(sendBuffer, sendBuffer.length, inetAddress, port);
            try {
                socket.send(packetSend);
                System.out.println("UDPClient sending packet #" + packetId + " sent to " + address + ":" + port + " " + sendString);

                socket.receive(packetReceive);
                receivedString = new String(packetReceive.getData(), packetReceive.getOffset(), packetReceive.getLength());
                String[] tmpReceived = receivedString.split(",");

                if(tmpReceived[0].equals("ACK")){
                    try{
                        int idACK = Integer.parseInt(tmpReceived[1]);
                        if(idACK == packetId){
                            System.out.println("Received confirmation for packet #" + packetId);
                            receivedACK = true;
                            ++packetId;
                        }
                    } catch (NumberFormatException ignored){

                    }
                }

            } catch (SocketTimeoutException e){
                System.err.println("ACK not received for #" + packetId + ", sending again.");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}

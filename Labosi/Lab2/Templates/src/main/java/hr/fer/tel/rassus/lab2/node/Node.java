package hr.fer.tel.rassus.lab2.node;

import hr.fer.tel.rassus.lab2.data.CsvReader;
import hr.fer.tel.rassus.lab2.data.DataManager;
import hr.fer.tel.rassus.lab2.network.EmulatedSystemClock;
import hr.fer.tel.rassus.lab2.udp.UDPClient;
import hr.fer.tel.rassus.lab2.udp.UDPServer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.random.RandomGenerator;

public class Node {

    private static final String TOPIC_REGISTER = "Register";
    private static final String TOPIC_COMMAND = "Command";
    private static final String CSV_FILENAME = "readings.csv";
    private static final int MINIMUM_REGISTERED_NODES = 5;
    private static final long MAX_WAIT_TIME_MS = 3000;
    private static final List<NodeInfo> registeredNodes = new ArrayList<>();

    private static volatile boolean stopMessage = false;

    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);
        //TODO: change to user input for ID and UDP port
        System.out.println("Enter Node ID: ");
        final String id = scanner.nextLine();

        System.out.println("Enter UDP port number: ");
        final int port = scanner.nextInt();

        scanner.close();

        final String address = "localhost";
        final int kafkaPort = 9092;

        EmulatedSystemClock clock = new EmulatedSystemClock();
        DataManager dataManager = new DataManager();
        CsvReader csvReader = new CsvReader(CSV_FILENAME, dataManager, clock);

        Consumer<String, String> consumer = createConsumer(id, address, kafkaPort);
        consumer.subscribe(Arrays.asList(TOPIC_COMMAND, TOPIC_REGISTER));

        //Postavke producera
        Producer<String, String> producer = createProducer(address, kafkaPort);

        System.out.println("Waiting for command message to arrive...");

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

            records.forEach(record -> {
                if (record.topic().equals(TOPIC_COMMAND)) {
                    handleCommandMessage(record.value(), producer, consumer, id, address, port);
                }
                else if (record.topic().equals(TOPIC_REGISTER)){
                    handleRegisterMessage(record.value());
                }
            });

            if(registeredNodes.size() >= MINIMUM_REGISTERED_NODES) {
                System.out.println("Received minimum number of registrations, waiting for additional...");
                long startTime = System.currentTimeMillis();

                while(System.currentTimeMillis() - startTime < MAX_WAIT_TIME_MS){
                    records = consumer.poll(Duration.ofMillis(1000));

                    records.forEach(record -> {
                        if (record.topic().equals(TOPIC_REGISTER)) {
                            handleRegisterMessage(record.value());
                        }
                        System.out.println("Additional Registration added.");
                    });
                }

                System.out.println("Total registered nodes: " + registeredNodes.size());
                break;
            }
        }

        //Jedan scheduled thread za kalkulacije s primljenim podacima
        //Dva threada - jedan za UDP slanje, jedan za UDP listen
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        long startTime = System.currentTimeMillis();

        //Kalkulacije
        scheduledExecutorService.scheduleAtFixedRate(() -> {
                try{
                    Map<Long, Integer> accumulatedData = dataManager.accumulateData();
                    System.out.println("Data: " + accumulatedData);
                    System.out.println("Average: " + dataManager.calculateAverage(accumulatedData));

                    if (Thread.interrupted()){
                        System.out.println("Kalkulacije interrupted...");
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            },
                5, 5, TimeUnit.SECONDS);

        //UDP listen
        executorService.submit(() -> {
            try{
                UDPServer udpServer = new UDPServer(clock, dataManager, port);

                while(!stopMessage){
                    udpServer.listen();
                }
                udpServer.shutdown();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });

        //UDP send
        executorService.submit(() -> {
            try{
                UDPClient udpClient = new UDPClient(csvReader, clock);

                while(!stopMessage){
                    registeredNodes.forEach(nodeInfo -> {
                        //Ne saljemo sami sebi
                        if(stopMessage) return;
                        if(!Objects.equals(nodeInfo.id, id)){
                            udpClient.send(nodeInfo.address, nodeInfo.port);
                        }
                    });
                }
                udpClient.shutdown();
            } catch (Throwable e) {
                e.printStackTrace();
            }

        });



        //Pracenje Command topica za Stop naredbu
        while(true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

            records.forEach(record -> {
                if (record.topic().equals(TOPIC_COMMAND)) {
                    handleCommandMessage(record.value(), producer, consumer, id, address, port);

                    if(stopMessage) {
                        System.out.println("Stopping...");
                        producer.flush();
                        producer.close();
                        consumer.commitSync();
                        consumer.unsubscribe();
                        consumer.close();
                        scheduledExecutorService.shutdown();
                        executorService.shutdown();

                        try {
                            if(scheduledExecutorService.awaitTermination(10, TimeUnit.SECONDS)){
                                System.out.println("Kalkulacije uspjesno prekinute.");
                            }
                            else {
                                System.out.println("Force shutdown kalkulacije.");
                                scheduledExecutorService.shutdownNow();
                            }
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                        try {
                            if(executorService.awaitTermination(10, TimeUnit.SECONDS)){
                                System.out.println("UDP komunikacija uspjesno prekinuta.");
                            }
                            else {
                                System.out.println("Force shutdown UDP komunikacija.");
                                executorService.shutdownNow();
                            }
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                        System.out.println("Stopped.");

                        System.out.println("Final results:");
                        System.out.println("Data: " + dataManager.getAccumulatedData());
                        System.out.println("Size: " + dataManager.getAccumulatedData().size());
                        System.out.println("Average: " + dataManager.calculateAccumulatedAverage());


                        //TODO: mozda hook umjesto ovoga?
                        System.exit(0);
                    }
                }
            });
        }
    }

    private static Consumer<String, String> createConsumer(String id, String address, int port) {
        Properties consumerProperties = new Properties();
        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, address + ":" + port);
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, id);
        consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        return new KafkaConsumer<>(consumerProperties);
    }

    private static Producer<String, String> createProducer(String address, int port) {
        Properties producerProperties = new Properties();
        producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, address + ":" + port);
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        return new KafkaProducer<>(producerProperties);
    }

    private static void handleCommandMessage(String command, Producer<String, String> producer, Consumer<String,
            String> consumer, String id, String address, int port){
        System.out.println("Received command: " + command);
        if(command.equals("Start")){
            sendRegistrationMessage(producer, id, address, port);
            //blabla
        }
        else if(command.equals("Stop")){
            stopMessage = true;
        }
    }

    private static void handleRegisterMessage(String registerMessage){
        System.out.println("Received register: " + registerMessage);
        try {
            JSONObject jsonObject = new JSONObject(registerMessage);
            String id = jsonObject.getString("id");
            String address = jsonObject.getString("address");
            Integer port = jsonObject.getInt("port");

            registeredNodes.add(new NodeInfo(id, address, port));

            System.out.println("Received registration: ID=" + id + ", address=" + address + ", port=" + port);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void sendRegistrationMessage(Producer<String, String> producer, String id, String address, int port) {
        try {
            JSONObject registrationJson = new JSONObject();
            registrationJson.put("id", id);
            registrationJson.put("address", address);
            registrationJson.put("port", port);

            System.out.println("Registering: " + registrationJson);

            producer.send(new ProducerRecord<>(TOPIC_REGISTER, registrationJson.toString()));
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    record NodeInfo(String id, String address, Integer port) {}

    /*
    static final class NodeInfo {
        private final String id;
        private final String address;
        private final Integer port;

        public NodeInfo(String id, String address, Integer port){
            this.id = id;
            this.address = address;
            this.port = port;
        }
        public String getId() {
            return id;
        }

        public String getAddress() {
            return address;
        }

        public Integer getPort() {
            return port;
        }
    }
     */
}

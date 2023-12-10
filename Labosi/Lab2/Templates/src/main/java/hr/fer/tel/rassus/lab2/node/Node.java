package hr.fer.tel.rassus.lab2.node;

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

public class Node {

    private static final String TOPIC_REGISTER = "Register";
    private static final String TOPIC_COMMAND = "Command";
    private static final int MINIMUM_REGISTERED_NODES = 3;
    private static final long MAX_WAIT_TIME_MS = 3000;
    private static final List<NodeInfo> registeredNodes = new ArrayList<>();

    public static void main(String[] args){
        //TODO: change to user input for ID and UDP port
        final String id = UUID.randomUUID().toString();
        final String address = "localhost";
        final String port = "9092";

        Consumer<String, String> consumer = createConsumer(id, address, port);
        consumer.subscribe(Arrays.asList(TOPIC_COMMAND, TOPIC_REGISTER));

        //Postavke producera
        Producer<String, String> producer = createProducer(address, port);

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
            }
        }
    }

    private static Consumer<String, String> createConsumer(String id, String address, String port) {
        Properties consumerProperties = new Properties();
        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, address + ":" + port);
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, id);
        consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        return new KafkaConsumer<>(consumerProperties);
    }

    private static Producer<String, String> createProducer(String address, String port) {
        Properties producerProperties = new Properties();
        producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, address + ":" + port);
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        return new KafkaProducer<>(producerProperties);
    }

    private static void handleCommandMessage(String command, Producer<String, String> producer, Consumer<String,
            String> consumer, String id, String address, String port){
        System.out.println("Received command: " + command);
        if(command.equals("Start")){
            sendRegistrationMessage(producer, id, address, port);
            //blabla
        }
        else if(command.equals("Stop")){
            System.out.println("Stopping...");
            producer.flush();
            producer.close();
            consumer.commitSync();
            consumer.unsubscribe();
            consumer.close();
            System.out.println("Stopped.");

            //TODO: mozda hook umjesto ovoga?
            System.exit(0);
        }
    }

    private static void handleRegisterMessage(String registerMessage){
        System.out.println("Received register: " + registerMessage);
        try {
            JSONObject jsonObject = new JSONObject(registerMessage);
            String id = jsonObject.getString("id");
            String address = jsonObject.getString("address");
            String port = jsonObject.getString("port");

            registeredNodes.add(new NodeInfo(id, address, port));

            System.out.println("Received registration: ID=" + id + ", address=" + address + ", port=" + port);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void sendRegistrationMessage(Producer<String, String> producer, String id, String address, String port) {
        try {
            JSONObject registrationJson = new JSONObject();
            registrationJson.put("id", id);
            registrationJson.put("address", address);
            registrationJson.put("port", port);

            producer.send(new ProducerRecord<>(TOPIC_REGISTER, registrationJson.toString()));
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    record NodeInfo(String id, String address, String port) {}

    /*
    static final class NodeInfo {
        private final String id;
        private final String address;
        private final String port;

        public NodeInfo(String id, String address, String port){
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

        public String getPort() {
            return port;
        }
    }
     */
}

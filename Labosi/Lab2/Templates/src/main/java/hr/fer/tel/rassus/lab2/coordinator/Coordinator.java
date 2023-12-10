package hr.fer.tel.rassus.lab2.coordinator;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;

public class Coordinator {

    private final static String TOPIC_REGISTER = "Register";
    private final static String TOPIC_COMMAND = "Command";

    public static void main(String[] args) throws IOException {
        /*
        //Postavke consumera
        Properties consumerProperties = new Properties();
        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
        consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        Consumer<String, String> consumer = new org.apache.kafka.clients.consumer.KafkaConsumer<>(consumerProperties);
        consumer.subscribe(Collections.singleton(TOPIC_REGISTER));
         */

        //Postavke producera
        Properties producerProperties = new Properties();
        producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        Producer<String, String> producer = new org.apache.kafka.clients.producer.KafkaProducer<>(producerProperties);

        //Pocetak rada, ceka na slanje Start poruke
        System.out.println("Press Enter to send the \"Start\" message...");
        System.in.read();

        ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_COMMAND, "Start");

        producer.send(record);
        producer.flush();

        //TODO MAKNI
        /*
        System.out.println("Press Enter to send the \"Register\" message...");
        System.in.read();
        JSONObject registrationJson = new JSONObject();
        registrationJson.put("id", "1");
        registrationJson.put("address", "localhost");
        registrationJson.put("port", "6969");

        producer.send(new ProducerRecord<>(TOPIC_REGISTER, registrationJson.toString()));
        */

        //Ceka na slanje Stop poruke
        System.out.println("Press Enter to send the \"Stop\" message...");
        System.in.read();



        record = new ProducerRecord<>(TOPIC_COMMAND, "Stop");

        producer.send(record);
        producer.flush();

        //TODO: hendlanje register poruka

        //Gasenje programa
        System.out.println("Successful \"Stop\", exiting program...");

        producer.close();
        /*
        consumer.commitSync();
        consumer.unsubscribe();
        consumer.close();

         */

        System.out.println("Consumer and producer cleaned-up and closed. Exiting...");
    }
}

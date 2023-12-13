package hr.fer.tel.rassus.lab2.coordinator;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.IOException;
import java.util.Properties;

public class Coordinator {

    private final static String TOPIC_REGISTER = "Register";
    private final static String TOPIC_COMMAND = "Command";

    public static void main(String[] args) throws IOException {
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

        //Ceka na slanje Stop poruke
        System.out.println("Press Enter to send the \"Stop\" message...");
        System.in.read();

        record = new ProducerRecord<>(TOPIC_COMMAND, "Stop");
        producer.send(record);
        producer.flush();

        //Gasenje programa
        System.out.println("Successful \"Stop\", exiting program...");
        producer.close();

        System.out.println("Consumer and producer cleaned-up and closed. Exiting...");
    }
}

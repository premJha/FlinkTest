package kafka.jsonCP;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
//import org.apache.kafka.connect.json.JsonSerializer;


import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class ProducerUser {
    public static void main(String[] args) {
        String topic="quickstart-events";
        Properties properties = new Properties();
        properties.put("bootstrap.servers","localhost:9092");
        properties.put("key.serializer","org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer","org.apache.kafka.common.serialization.StringSerializer");
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);
        Random random = new Random();

        for(int i = 0; i < 15; i++)
            try {

                User user=   new User(Integer.toString(i),Integer.toString(i),random.nextInt(100));
                ObjectMapper objectMapper = new ObjectMapper();
                String sendMessage=objectMapper.writeValueAsString(user);
                System.out.println("Sending:"+sendMessage);
                RecordMetadata recordMetadata = producer.send(new ProducerRecord<String, String >(topic,
                        Integer.toString(i), sendMessage)).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

        System.out.println("Message sent successfully");
        producer.close();

    }
}


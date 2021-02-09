package flnk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kafka.jsonCP.User;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;

import java.util.Properties;

public class ReadingJsonKafka {

    public static void main(String[] args) throws Exception {

        System.out.println("WindowWordCount Kafka");

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        Properties properties = new Properties();
        //properties.setProperty("bootstrap.servers", "localhost:9092");

        //String jaasTemplate = "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"%s\" password=\"%s\";";
        properties.setProperty("bootstrap.servers", "localhost:9092");
        properties.setProperty("group.id", "test");
        ObjectMapper objectMapper = new ObjectMapper();
        DataStream<String> stream = env
                .addSource(new FlinkKafkaConsumer<>("quickstart-events", new SimpleStringSchema(), properties));

        SingleOutputStreamOperator<User> agedPersons = stream.map(new MapFunction<String, User>() {
            public User map(String value) {
                User user = null;
                try {
                    user = objectMapper.readValue(value, User.class);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                return user;
            }
        }).filter(u -> u.age > 80);


        System.out.println("Done processins");
        agedPersons.print();
        //dataStream.addSink(new FlinkKafkaConsumer<String>("quickstart-events", new SimpleStringSchema(), properties));

        env.execute("Test Reading json");
    }



    public static class Splitter implements FlatMapFunction<String, Tuple2<String, Integer>> {
        @Override
        public void flatMap(String sentence, Collector<Tuple2<String, Integer>> out) throws Exception {
            for (String word: sentence.split(" ")) {
                out.collect(new Tuple2<String, Integer>(word, 1));
            }
        }
    }

}

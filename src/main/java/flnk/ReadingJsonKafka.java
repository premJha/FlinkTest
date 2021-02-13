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
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SaslConfigs;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Properties;

import kafka.collectd.Metric;
import kafka.collectd.RawMetric;

public class ReadingJsonKafka {

    public static void main(String[] args) throws Exception {

        System.out.println("WindowWordCount Kafka");

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        String username = null;
        String password = null;
        for(int i = 0;i < args.length; i++) {
            switch(args[i]){
                case "-u":
                    i++;
                    username = args[i];
                    break;
                case "-p":
                    i++;
                    password = args[i];
                    break;
                default:
                    throw new IllegalArgumentException("Unknown argument: " + args[i]);
            }
        }
        String jaasTemplate = "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"" + username + "\" password=\"" + password + "\";";

        Properties properties = new Properties();

        properties.setProperty("bootstrap.servers", "flink.albeadoprism.com:9093");
        properties.setProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        properties.setProperty(SaslConfigs.SASL_MECHANISM, "PLAIN");
        properties.setProperty(SaslConfigs.SASL_JAAS_CONFIG, jaasTemplate);

        // properties.setProperty("group.id", "test");
        ObjectMapper objectMapper = new ObjectMapper();
        
        FlinkKafkaConsumer<String> cloudKafka = new FlinkKafkaConsumer<>("collectd-metrics", new SimpleStringSchema(), properties);
        cloudKafka.setStartFromEarliest();
        
        DataStream<String> stream = env.addSource(cloudKafka);

        SingleOutputStreamOperator<Metric> metrics = stream.map(new MapFunction<String, List<RawMetric>>() {
            private static final long serialVersionUID = 1L;

            public List<RawMetric> map(String value) {
                List<RawMetric> metric = null;
                try {
                    metric = objectMapper.readValue(value, new TypeReference<List<RawMetric>>(){});
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                return metric;
            }
        })
        .flatMap(new SplitMetric())
        .filter(m -> !m.plugin.equals("irq"));

        System.out.println("Done processing");
        metrics.print();

        env.execute("Test Reading json");
    }

    public static class SplitMetric implements FlatMapFunction<List<RawMetric>, Metric> {
        @Override
        public void flatMap(List<RawMetric> metrics, Collector<Metric> out ) throws Exception {
            for(RawMetric m : metrics) {
                for(int i=0;i < m.dsnames.length;i++) {
                    out.collect(new Metric(m, i));
                }
            }
        }
    }
}

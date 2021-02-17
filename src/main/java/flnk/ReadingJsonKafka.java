package flnk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kafka.jsonCP.User;

import org.apache.commons.cli.*;
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

import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;

import kafka.collectd.Metric;
import kafka.collectd.RawMetric;

import static java.lang.System.out;

public class ReadingJsonKafka {

    private String user;
    private String password;
    private String KAFKA_BOOTSTRAP_SERVERS;
    private String KAFKA_TOPIC;

    private static void printUsage(final Options options) {
        final HelpFormatter formatter = new HelpFormatter();
        final String syntax = "Main";
        out.println("\n=====");
        out.println("USAGE");
        out.println("=====");
        final PrintWriter pw = new PrintWriter(out);
        formatter.printUsage(pw, 80, syntax, options);
        pw.flush();
    }


    public void getCommandLineParameters(String[] args) {
        String USER = "u";

        String PASSWORD = "p";
        String KAFKA_BOOTSTRAP_SERVERS_ARG = "k";
        String KAFKA_TOPIC_ARG = "t";
        Option u = Option.builder("u").required().desc("User name Needed").longOpt(USER).hasArg().argName("USER").build();
        Option p = Option.builder("p").required().desc("Password name Needed").longOpt(PASSWORD).hasArg().argName("PASSWORD").build();
        Option k = Option.builder("k").required().desc("Kafka bootstrap servers are needed").longOpt(PASSWORD).hasArg().argName("KAFKA_BOOTSTRAP_SERVERS").build();
        Option t = Option.builder("t").required().desc("Kafka bootstrap servers are needed").longOpt(PASSWORD).hasArg().argName("KAFKA_TOPIC").build();

        Options options = new Options();

        options.addOption(u);

        options.addOption(p);
        options.addOption(k);
        options.addOption(t);
        CommandLineParser defaultParser = new DefaultParser();
        try {
            CommandLine commandLine = defaultParser.parse(options, args);
            user = commandLine.getOptionValue(USER);
            password = commandLine.getOptionValue(PASSWORD);

            KAFKA_BOOTSTRAP_SERVERS = commandLine.getOptionValue(KAFKA_BOOTSTRAP_SERVERS_ARG);
            KAFKA_TOPIC = commandLine.getOptionValue(KAFKA_TOPIC_ARG);
            out.println(user + ":" + password + ":" + KAFKA_BOOTSTRAP_SERVERS + ":" + KAFKA_TOPIC);

        } catch (ParseException e) {
            printUsage(options);
            System.exit(1);
        }

    }

    public DataStream<String> addKafkaSource(StreamExecutionEnvironment env, String[] args) {

        String jaasTemplate = "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"" + user + "\" password=\"" + password + "\";";
        Properties properties = new Properties();

        properties.setProperty("bootstrap.servers", KAFKA_BOOTSTRAP_SERVERS);
        properties.setProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        properties.setProperty(SaslConfigs.SASL_MECHANISM, "PLAIN");
        properties.setProperty(SaslConfigs.SASL_JAAS_CONFIG, jaasTemplate);
        FlinkKafkaConsumer<String> cloudKafka = new FlinkKafkaConsumer<>(KAFKA_TOPIC, new SimpleStringSchema(), properties);
        cloudKafka.setStartFromEarliest();

        DataStream<String> stream = env.addSource(cloudKafka);
        return stream;
    }

    public static void main(String[] args) throws Exception {

        ReadingJsonKafka readingJsonKafka1 = new ReadingJsonKafka();
        readingJsonKafka1.getCommandLineParameters(args);

        System.out.println("Basic  Kafka Events Processing in Flink");
        ObjectMapper objectMapper = new ObjectMapper();
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<String> stream = readingJsonKafka1.addKafkaSource(env, args);


        SingleOutputStreamOperator<Metric> metrics = stream.map(new MapFunction<String, List<RawMetric>>() {
            private static final long serialVersionUID = 1L;

            public List<RawMetric> map(String value) {
                List<RawMetric> metric = null;
                try {
                    metric = objectMapper.readValue(value, new TypeReference<List<RawMetric>>() {
                    });
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                return metric;
            }
        })
                .flatMap(new ReadingJsonKafka.SplitMetric())
                .filter(m -> !m.plugin.equals("irq"));

        System.out.println("Done processing");
        metrics.print();

        env.execute("Test Reading json");
    }

    public static class SplitMetric implements FlatMapFunction<List<RawMetric>, Metric> {
        @Override
        public void flatMap(List<RawMetric> metrics, Collector<Metric> out) throws Exception {
            for (RawMetric m : metrics) {
                for (int i = 0; i < m.dsnames.length; i++) {
                    out.collect(new Metric(m, i));
                }
            }
        }
    }
}

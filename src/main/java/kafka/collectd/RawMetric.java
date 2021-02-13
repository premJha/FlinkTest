package kafka.collectd;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class RawMetric {

    public static void main(String[] args) {
        String test_m = "{\"values\":[166.301197037],\"dstypes\":[\"derive\"],\"dsnames\":[\"value\"],\"time\":1612573852.331,\"interval\":10.000,\"host\":\"ip-172-30-1-122.us-west-2.compute.internal\",\"plugin\":\"irq\",\"plugin_instance\":\"\",\"type\":\"irq\",\"type_instance\":\"60\"}";
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            RawMetric m1 = objectMapper.readValue(test_m, RawMetric.class);
            System.out.println(m1);

            System.out.println("-------------------------------");
            List<RawMetric> marr1 = objectMapper.readValue("[" + test_m + "]", new TypeReference<List<RawMetric>>(){});
            System.out.println(marr1.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public double[] values;
    public String[] dstypes;
    public String[] dsnames;
    public long time;
    public int interval;
    public String host;
    public String plugin;
    public String plugin_instance;
    public String type;
    public String type_instance;

    public RawMetric() {}
    public RawMetric(double[] values, String[] dstypes, String[] dsnames, long time, int interval, 
                    String host, String plugin, String plugin_instance, String type, String type_instance) {

        this.values = values;
        this.dstypes = dstypes;
        this.dsnames = dsnames;
        this.time = time;
        this.interval = interval;
        this.host = host;
        this.plugin = plugin;
        this.plugin_instance = plugin_instance;
        this.type = type;
        this.type_instance = type_instance;
    }

    @Override
    public String toString() {
        return "Metric{" +
            "values=" + Arrays.toString(this.values) + 
            ", dstypes=" + Arrays.toString(this.dstypes) +
            ", dsnames=" + Arrays.toString(this.dsnames) +
            ", time=" + String.valueOf(this.time) +
            ", interval=" + String.valueOf(this.interval) +
            ", host=\'" + this.host + "\'" +
            ", plugin=\'" + this.plugin + "\'" +
            ", plugin_instance=\'" + this.plugin_instance + "\'" +
            ", type=\'" + this.type + "\'" +
            ", type_instance=\'" + this.type_instance + "\'";
    }
}

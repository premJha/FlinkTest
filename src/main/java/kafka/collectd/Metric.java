package kafka.collectd;

public class Metric {
    public double value;
    public String dstype;
    public long time;
    public int interval;
    public String host;
    public String plugin;
    public String plugin_instance;
    public String type;
    public String type_instance;

    public Metric() {}    

    public Metric(RawMetric rawMetric, int valueIndex) {
        if(valueIndex > rawMetric.values.length - 1) {
            throw new IllegalArgumentException("Index out of range: " + String.valueOf(valueIndex));
        }
        this.value = rawMetric.values[valueIndex];
        this.dstype = rawMetric.dstypes[valueIndex];
        this.time = rawMetric.time;
        this.interval = rawMetric.interval;
        this.host = rawMetric.host;
        this.plugin = rawMetric.plugin;
        this.plugin_instance = rawMetric.plugin_instance;
        this.type = rawMetric.type;

        String dsname = rawMetric.dsnames[valueIndex];
        if(dsname.equals("value")){
            this.type_instance = rawMetric.type_instance;
        }
        else {
            if(rawMetric.type_instance.equals("")) {
                this.type_instance = dsname;
            }
            else if(rawMetric.plugin_instance.equals("")) {
                //vmem plugin could report things a bit better... this swap makes a bit more sense
                this.plugin_instance = rawMetric.type_instance;
                this.type_instance = dsname;
            }
            else {
                // This shold not occur for the plugins we tested so far
                this.type_instance = rawMetric.type_instance + "_" + dsname;
                System.out.println(this.plugin + " has " + this.type_instance);
            }
        }

    }
    
    @Override
    public String toString() {
        return "Metric{" +
            "value=" + String.valueOf(this.value) + 
            ", dstype=\'" + this.dstype + "\'" +
            ", time=" + String.valueOf(this.time) +
            ", interval=" + String.valueOf(this.interval) +
            ", host=\'" + this.host + "\'" +
            ", plugin=\'" + this.plugin + "\'" +
            ", plugin_instance=\'" + this.plugin_instance + "\'" +
            ", type=\'" + this.type + "\'" +
            ", type_instance=\'" + this.type_instance + "\'";
    }
}

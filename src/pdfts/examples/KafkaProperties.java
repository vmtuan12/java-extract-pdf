package pdfts.examples;
import java.util.Properties;

public class KafkaProperties {
    public static Properties getInstance() {
        Properties prop = new Properties();
        prop.put("bootstrap.servers", "localhost:9091");

        prop.setProperty("group.id", "test-1");
        prop.setProperty("enable.auto.commit", "true");
        prop.setProperty("auto.commit.interval.ms", "1000");
        prop.setProperty("auto.offset.reset", "earliest");

        prop.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        prop.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        return prop;
    }
}

package pdfts.examples;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import pdfts.examples.KafkaProperties;

public class SimpleConsumer {
    public static void main(String[] args) throws ParseException, IOException, URISyntaxException {
        JSONParser parser = new JSONParser();
        Consumer<String, String> consumer = new KafkaConsumer<>(KafkaProperties.getInstance());

        // initialize consumer and read data from topic of kafka
        // topic: x_news_1
        consumer.subscribe(List.of("uploaded-file"));
        try {
            while(true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

                if(records.count() != 0) {
                    for(ConsumerRecord<String, String> record: records) {
                        JSONObject jsonObject = (JSONObject) parser.parse(record.value());
                        String content = (String) jsonObject.get("content");

                        System.out.println(content);
                    }
                    Thread.sleep(300000);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

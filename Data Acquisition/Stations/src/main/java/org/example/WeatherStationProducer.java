package org.example;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.json.JSONArray;

import java.util.Map;
import java.util.Properties;
import java.util.Random;


public class WeatherStationProducer {
    private static final Random RANDOM = new Random();
    private Properties properties;
    private String stationId;
    private String latitude;
    private String longitude;

    public WeatherStationProducer(String stationId, String latitude, String longitude) {
        this.stationId = stationId;
        this.latitude = latitude;
        this.longitude = longitude;
        properties = new Properties();
        // Set up Kafka producer properties
        Map<String, String> env = System.getenv();
        properties.put("bootstrap.servers", "localhost:9092");
//        String kafkaBroker = env.get("KAFKA_BROKER");
//        System.out.println(kafkaBroker);
//        properties.put("bootstrap.servers", kafkaBroker);
        properties.put("acks", "all");
        properties.put("retries", 0);
        properties.put("batch.size", 16384);
        properties.put("linger.ms", 1);
        properties.put("buffer.memory", 33554432);
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    }

    private static boolean isDrop() {
        int rand = RANDOM.nextInt(10);
        return rand == 1;
    }

    public int produce() {
        long s_no = 0;
        // Create a Kafka producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);
        GetData getData = new GetData(this.latitude, this.longitude);
        Weather weather = getData.getData();
        JSONArray temperature = weather.getTemperature();
        JSONArray humidity = weather.getHumidity();
        JSONArray windSpeed = weather.getWindSpeed();
        WeatherStatusMessage message = new WeatherStatusMessage(this.stationId);
        int count = 0;
        long currentUnixTimestamp = (System.currentTimeMillis() / 1000L) - 1;
        while (true) {
            currentUnixTimestamp++;
            s_no++;
            if (isDrop()) {
                if ((s_no % 24) == 1) {
                    weather = getData.getData();
                    temperature = weather.getTemperature();
                    humidity = weather.getHumidity();
                    windSpeed = weather.getWindSpeed();
                    count = 0;
                }
                continue;
            }
            message.generateWeatherStatusMessage(s_no, currentUnixTimestamp, (Double) temperature.get(count), (Integer) humidity.get(count), (Double) windSpeed.get(count));
            String value = message.toString();
            count++;
            ProducerRecord<String, String> record = new ProducerRecord<>("weather-status-messages", value);
            producer.send(record);
            System.out.println("Sent message: " + value);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if ((s_no % 24) == 1) {
                weather = getData.getData();
                temperature = weather.getTemperature();
                humidity = weather.getHumidity();
                windSpeed = weather.getWindSpeed();
                count = 0;
            }
        }
    }

}

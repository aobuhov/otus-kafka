package ru.otus.kafka.obukhov.hw3.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {
    public static Logger log = LoggerFactory.getLogger("appl");

    public static final String HOST = "localhost:9092";
    public static final String HOST_PRODUCER = "localhost:9092";

    public static final Map<String, Object> consumerConfig = Map.of(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, HOST,
        ConsumerConfig.GROUP_ID_CONFIG, "some-java-consumer",
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

    public static Map<String, Object> createConsumerConfig(Consumer<Map<String, Object>> builder) {
        var map = new HashMap<>(consumerConfig);
        builder.accept(map);
        return map;
    }

}

package ru.otus.kafka.obukhov;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import ru.otus.kafka.obukhov.models.Event;
import ru.otus.kafka.obukhov.serdes.EventSerdes;
import ru.otus.kafka.obukhov.utils.EventProducer;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Main {

    private static final String INPUT_TOPIC = "events";
    private static final String OUTPUT_TOPIC = "events-count";
    private static final String APPLICATION_ID = "kafka-streams-demo";

    public static void main(String[] args) {

        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");
        System.setProperty("org.slf4j.simpleLogger.log.org.apache.kafka", "error");
        System.setProperty("org.slf4j.simpleLogger.log.ru.otus.kafka.obukhov", "debug");

        Properties props = getStreamsProperties();
        Topology topology = buildTopology();

        // Создаем и запускаем Streams приложение
        KafkaStreams streams = new KafkaStreams(topology, props);

        // Создаем ExecutorService для продюсера
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        EventProducer producer = new EventProducer();

        // CountDownLatch для ожидания завершения
        CountDownLatch latch = new CountDownLatch(1);

        // Обработчик для graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down...");

            // Останавливаем продюсер
            log.info("Stopping event producer...");
            producer.stop();
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
            log.info("Event producer stopped. Total events sent: {}", producer.getTotalEventsSent());

            // Закрываем Streams приложение
            log.info("Closing Kafka Streams...");
            streams.close(Duration.ofSeconds(30));
            log.info("Kafka Streams closed");

            latch.countDown();
        }));

        try {
            log.info("=== Starting Kafka Streams Demo ===");
            log.info("Application topology: {}", topology.describe());

            // Запускаем Kafka Streams
            streams.start();
            log.info("Kafka Streams started successfully!");

            // Запускаем продюсер в отдельном потоке
            log.info("Starting event producer in separate thread...");
            executorService.submit(producer);

            // Ждем завершения (Ctrl+C)
            latch.await();

        } catch (Exception e) {
            log.error("Error in Kafka Streams application", e);
        } finally {
            log.info("=== Application terminated ===");
        }
    }

    private static Properties getStreamsProperties() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, APPLICATION_ID);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, EventSerdes.class.getName());
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.AT_LEAST_ONCE);

        // Дополнительные настройки для производительности
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);
        props.put(StreamsConfig.STATE_DIR_CONFIG, "/tmp/kafka-streams");

        return props;
    }

    private static Topology buildTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        // Получаем поток из топика events
        KStream<String, Event> sourceStream = builder.stream(
                INPUT_TOPIC,
                Consumed.with(Serdes.String(), new EventSerdes())
        );

        // Группируем по ключу и считаем события в сессионном окне
        KTable<Windowed<String>, Long> sessionCounts = sourceStream
                .groupByKey(Grouped.with(Serdes.String(), new EventSerdes()))
                .windowedBy(SessionWindows.ofInactivityGapWithNoGrace(Duration.ofMinutes(5)))
                .count(Materialized.as("session-counts"));

        // Преобразуем результат для вывода
        KStream<String, String> resultStream = sessionCounts
                .toStream()
                .map((windowedKey, count) -> {
                    // Формируем читаемое сообщение
                    String sessionInfo = String.format(
                            "Session for key='%s': start=%d, end=%d, count=%d",
                            windowedKey.key(),
                            windowedKey.window().start(),
                            windowedKey.window().end(),
                            count
                    );
                    log.info(sessionInfo);
                    return KeyValue.pair(windowedKey.key(), sessionInfo);
                });

        // Выводим результат в консоль (используем печать в лог)
        resultStream.peek((key, value) ->
                log.info("Result - Key: '{}', Value: '{}'", key, value)
        );

        //записываем результаты в выходной топик
        resultStream.to(
                OUTPUT_TOPIC,
                Produced.with(Serdes.String(), Serdes.String())
        );

        return builder.build();
    }
}
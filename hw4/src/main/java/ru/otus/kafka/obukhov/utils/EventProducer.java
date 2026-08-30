package ru.otus.kafka.obukhov.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import ru.otus.kafka.obukhov.models.Event;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class EventProducer implements Runnable {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Конфигурация
    private final String bootstrapServers;
    private final String topic;
    private final Duration runDuration;        // Продолжительность работы
    private final Duration minDelay;           // Минимальная задержка между событиями
    private final Duration maxDelay;           // Максимальная задержка между событиями

    // Список возможных ключей (пользователей)
    private static final List<String> USER_KEYS = Arrays.asList(
            "admin", "customer", "manager"
    );

    // Список возможных типов событий
    private static final List<String> EVENT_TYPES = Arrays.asList(
            "login", "logout", "click", "view", "purchase"
    );

    // Флаг для остановки генерации
    private final AtomicBoolean running = new AtomicBoolean(true);
    private KafkaProducer<String, String> producer;
    @Getter
    private int totalEventsSent = 0;

    public EventProducer() {
        this("localhost:9092", "events",
                Duration.ofMinutes(10),  // Работаем 10 минут
                Duration.ofSeconds(1),   // Минимальная задержка 1 секунда
                Duration.ofSeconds(10)); // Максимальная задержка 10 секунд
    }

    public EventProducer(String bootstrapServers, String topic,
                                 Duration runDuration, Duration minDelay, Duration maxDelay) {
        this.bootstrapServers = bootstrapServers;
        this.topic = topic;
        this.runDuration = runDuration;
        this.minDelay = minDelay;
        this.maxDelay = maxDelay;

        // Проверка валидности задержек
        if (minDelay.toMillis() > maxDelay.toMillis()) {
            throw new IllegalArgumentException("minDelay must be less than or equal to maxDelay");
        }
    }

    @Override
    public void run() {
        log.info("Starting Event Producer...");

        // Инициализация продюсера
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        producer = new KafkaProducer<>(props);

        // Запускаем генерацию событий
        generateEvents();

        // Закрываем ресурсы
        close();
    }

    private void generateEvents() {
        Random random = new Random();
        Instant startTime = Instant.now();
        Instant endTime = startTime.plus(runDuration);

        log.info("Event generation started at: {}", startTime);
        log.info("Will run until: {}", endTime);

        while (running.get() && Instant.now().isBefore(endTime)) {
            try {
                // Генерируем случайное событие
                String key = USER_KEYS.get(random.nextInt(USER_KEYS.size()));
                String eventType = EVENT_TYPES.get(random.nextInt(EVENT_TYPES.size()));

                Event event = new Event(
                        UUID.randomUUID().toString(),
                        System.currentTimeMillis(),
                        eventType + ":" + random.nextInt(1000)  // Добавляем случайный ID
                );

                String jsonValue = objectMapper.writeValueAsString(event);

                // Отправляем сообщение
                sendMessage(key, jsonValue);
                totalEventsSent++;

                // Логируем отправку
                log.info("Sent event #{}: key='{}', type='{}'",
                        totalEventsSent, key, eventType);

                // Случайная задержка перед следующим событием
                long delayMillis = minDelay.toMillis() +
                        (long) (random.nextDouble() * (maxDelay.toMillis() - minDelay.toMillis()));

                log.debug("Waiting {} ms before next event", delayMillis);
                Thread.sleep(delayMillis);

            } catch (InterruptedException e) {
                log.info("Event generation interrupted");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error generating event", e);
            }
        }

        log.info("Event generation completed. Total events sent: {}", totalEventsSent);
    }

    private void sendMessage(String key, String value) {
        try {
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);

            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    log.error("Failed to send message: key='{}', value='{}'", key, value, exception);
                } else {
                    log.debug("Message sent successfully: topic={}, partition={}, offset={}",
                            metadata.topic(), metadata.partition(), metadata.offset());
                }
            });
        } catch (Exception e) {
            log.error("Error sending message", e);
        }
    }

    public void stop() {
        log.info("Stopping event producer...");
        running.set(false);
    }

    private void close() {
        if (producer != null) {
            log.info("Closing Kafka producer...");
            producer.flush();  // Ждем отправки всех сообщений
            producer.close(Duration.ofSeconds(10));
            log.info("Kafka producer closed");
        }
    }

    public boolean isRunning() {
        return running.get();
    }
}

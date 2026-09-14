package ru.otus.kafka.obukhov.hw3.consumer.readCommited;

import ru.otus.kafka.obukhov.hw3.utils.Utils;
import ru.otus.kafka.obukhov.hw3.utils.LoggingConsumer;


public class ConsumerReadCommited {
    public static void main(String[] args) throws Exception {

        try (var consumer1 = new LoggingConsumer("1", "topic1", Utils.consumerConfig, true);
             var consumer2 = new LoggingConsumer("1", "topic2", Utils.consumerConfig, true);
        ) {
            Thread.sleep(1_000_000);
        }

    }


}

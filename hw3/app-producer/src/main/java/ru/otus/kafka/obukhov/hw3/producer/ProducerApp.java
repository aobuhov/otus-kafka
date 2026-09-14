package ru.otus.kafka.obukhov.hw3.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import ru.otus.kafka.obukhov.hw3.utils.Utils;

public class ProducerApp {
    public static void main(String[] args) throws Exception {

        final String TOPIC_1 = "topic1";
        final String TOPIC_2 = "topic2";
        final String MES_TEMPLATE = "%s mes %s %d";

        Utils.recreateTopics(1, 1, TOPIC_1, TOPIC_2);

        try (
                var producer = new KafkaProducer<String, String>(Utils.createProducerConfig(b -> {
                    b.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "tr1");
                }));
            ) {

            int GLOBAL_ID = 1;

            producer.initTransactions();

            producer.beginTransaction();
            System.out.println("Begin transaction");
            for (int i = 1; i <= 5; ++i) {
                producer.send(new ProducerRecord<>(TOPIC_1, String.format(MES_TEMPLATE, "+", TOPIC_1, GLOBAL_ID)));
                producer.send(new ProducerRecord<>(TOPIC_2, String.format(MES_TEMPLATE, "+", TOPIC_2, GLOBAL_ID)));
                System.out.println("published " + String.format(MES_TEMPLATE, "+", TOPIC_1, GLOBAL_ID));
                System.out.println("published " + String.format(MES_TEMPLATE, "+", TOPIC_2, GLOBAL_ID));
                GLOBAL_ID++;
                Thread.sleep(100);
            }
            producer.commitTransaction();
            System.out.println("Commit transaction");

            Thread.sleep(1000);

            producer.beginTransaction();
            System.out.println("Commit transaction");
            for (int i = 0; i < 2; ++i) {
                producer.send(new ProducerRecord<>(TOPIC_1, String.format(MES_TEMPLATE, "-", TOPIC_1, GLOBAL_ID)));
                producer.send(new ProducerRecord<>(TOPIC_2, String.format(MES_TEMPLATE, "-", TOPIC_2, GLOBAL_ID)));
                System.out.println("published " + String.format(MES_TEMPLATE, "-", TOPIC_1, i));
                System.out.println("published " + String.format(MES_TEMPLATE, "-", TOPIC_2, i));
                GLOBAL_ID++;
                Thread.sleep(100);
            }
            producer.abortTransaction();
            System.out.println("Abort transaction");


        }

    }
}

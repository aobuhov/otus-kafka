package ru.otus.kafka.obukhov.term.order.kafka;

import org.springframework.kafka.annotation.KafkaListener;

@KafkaListener
public class PaymentCompleteKafkaListener {
    //TODO
    // Consumer в этом же сервисе для payment.completed / restaurant.order.accepted — чтобы менять статус заказа.


}

package ru.otus.kafka.obukhov.term.delivery.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.kafka.obukhov.term.delivery.client.OrderClient;
import ru.otus.kafka.obukhov.term.delivery.dto.OrderDetailsDto;
import ru.otus.kafka.obukhov.term.delivery.entity.Delivery;
import ru.otus.kafka.obukhov.term.delivery.entity.DeliveryStatus;
import ru.otus.kafka.obukhov.term.delivery.event.PaymentCompletedEvent;
import ru.otus.kafka.obukhov.term.delivery.repository.DeliveryRepository;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCompletedListener {

    private final DeliveryRepository deliveryRepository;
    private final OrderClient orderClient;

    @KafkaListener(topics = "payment.completed", groupId = "ms-delivery")
    @Transactional
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        UUID orderId = event.getOrderId();
        log.info("Received payment.completed for orderId={}", orderId);

        if (deliveryRepository.existsByOrderId(orderId)) {
            log.warn("Delivery for orderId={} already exists, skip", orderId);
            return;
        }

        // Получаем customerId из ms-order (в событии его нет)
        OrderDetailsDto order = orderClient.getOrder(orderId);

        Delivery delivery = Delivery.builder()
                .orderId(orderId)
                .customerId(order.getCustomerId())
                .status(DeliveryStatus.NEW)
                .build();

        try {
            deliveryRepository.save(delivery);
            log.info("Created delivery id={} for orderId={}", delivery.getId(), orderId);
        } catch (DataIntegrityViolationException e) {
            // Гонка: другой поток уже создал доставку — ок, игнорируем
            log.warn("Duplicate delivery for orderId={}, skip", orderId);
        }
    }
}
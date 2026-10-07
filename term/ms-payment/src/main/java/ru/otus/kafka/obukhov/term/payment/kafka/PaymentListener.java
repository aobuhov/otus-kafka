package ru.otus.kafka.obukhov.term.payment.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.kafka.obukhov.term.payment.client.OrderClient;
import ru.otus.kafka.obukhov.term.payment.dto.*;
import ru.otus.kafka.obukhov.term.payment.entity.*;
import ru.otus.kafka.obukhov.term.payment.event.OrderCancelledEvent;
import ru.otus.kafka.obukhov.term.payment.event.PaymentResultEvent;
import ru.otus.kafka.obukhov.term.payment.event.RestaurantOrderAcceptedEvent;
import ru.otus.kafka.obukhov.term.payment.repository.*;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentListener {

    private final PaymentRepository paymentRepository;
    private final CancelledOrderRepository cancelledOrderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Random random = new Random();
    private final OrderClient orderClient;
    private final ProcessedEventRepository processedEventRepository;

    // === Обработка отмены заказа ===
    @KafkaListener(topics = "order.cancelled", groupId = "ms-payment")
    @Transactional
    public void onOrderCancelled(OrderCancelledEvent event) {

        if (processedEventRepository.existsById(event.getEventId())) {
            log.debug("event {} already processed", event.getEventId());
            return;
        }

        UUID orderId = event.getOrderId();
        log.info("Received order.cancelled for orderId={}", orderId);

        // Сохраняем факт отмены (идемпотентно)
        if (!cancelledOrderRepository.existsById(orderId)) {
            cancelledOrderRepository.save(CancelledOrder.builder()
                    .orderId(orderId)
                    .build());
            log.info("Recorded cancellation for orderId={}", orderId);
        }

        // Если платёж уже существует — помечаем CANCELLED
        paymentRepository.findByOrderId(orderId).ifPresent(payment -> {
            payment.setStatus(PaymentStatus.CANCELLED);
            paymentRepository.save(payment);
            log.info("Payment for orderId={} marked as CANCELLED", orderId);
        });

        processedEventRepository.save(ProcessedEvent.builder().id(event.getEventId()).build());

    }

    @KafkaListener(topics = "restaurant.orders.completed", groupId = "ms-payment")
    @Transactional
    public void onRestaurantCompleted(RestaurantOrderAcceptedEvent event) {

        if (processedEventRepository.existsById(event.getEventId())) {
            log.debug("event {} already processed", event.getEventId());
            return;
        }

        UUID orderId = event.getOrderId();
        log.info("Received restaurant.orders.completedS for orderId={}", orderId);

        // 1. Проверка идемпотентности: уже обрабатывали этот заказ?
        if (paymentRepository.findByOrderId(orderId).isPresent()) {
            log.warn("Payment for orderId={} already exists, skip", orderId);
            return;
        }

        OrderDetailsDto order = orderClient.getOrder(orderId);

        Payment payment = Payment.builder()
                .orderId(orderId)
                .customerId(order.getCustomerId())
                .amount(order.getTotalAmount())
                .build();


        // 2. Проверка: не отменён ли заказ клиентом?
        if (cancelledOrderRepository.existsById(orderId)) {
            log.warn("Order {} was cancelled by customer, aborting payment", orderId);
            // Создаём запись со статусом CANCELLED
            payment.setStatus(PaymentStatus.CANCELLED);
            paymentRepository.save(payment);
            publishResult(orderId, payment.getId(), "CANCELLED", "Order cancelled by customer");
            return;
        }

        // 3. Создаём платёж со статусом CREATED
        payment.setStatus(PaymentStatus.CREATED);
        payment = idempotentReceiver(payment);

        // 4. Симуляция оплаты: 90% успех, 10% отказ
        boolean success = random.nextDouble() < 0.9;

        if (success) {
            payment.setStatus(PaymentStatus.PAID);
            payment = idempotentReceiver(payment);
            log.info("Payment SUCCESS for orderId={}", orderId);
            publishResult(orderId, payment.getId(), "PAID", null);
        } else {
            payment.setStatus(PaymentStatus.ABORTED);
            payment = idempotentReceiver(payment);
            log.info("Payment FAILED for orderId={}", orderId);
            publishResult(orderId, payment.getId(), "ABORTED", "Payment declined");
        }
        processedEventRepository.save(ProcessedEvent.builder().id(event.getEventId()).build());
    }

    private Payment idempotentReceiver(Payment payment) {
        try {
            payment = paymentRepository.save(payment);
        } catch (DataIntegrityViolationException e) {
            log.warn("Duplicate payment for orderId={}, skip", payment.getOrderId());
        }
        return payment;
    }

    private void publishResult(UUID orderId, UUID paymentId, String status, String reason) {
        PaymentResultEvent event = PaymentResultEvent.builder()
                .orderId(orderId)
                .paymentId(paymentId)
                .status(status)
                .reason(reason)
                .eventId(UUID.randomUUID())
                .build();

        String topic = "PAID".equals(status) ? "payment.completed" : "payment.failed";
        kafkaTemplate.send(topic, orderId.toString(), event);
        log.info("Published {} for orderId={}", topic, orderId);
    }
}
package ru.otus.kafka.obukhov.term.order.kafka;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.otus.kafka.obukhov.term.order.dto.OrderStatusEvent;
import ru.otus.kafka.obukhov.term.order.entity.OrderStatus;
import ru.otus.kafka.obukhov.term.order.service.OrderService;


@Slf4j
@Component
@RequiredArgsConstructor
public class OrderStatusListener {

    private final OrderService orderService;

    @KafkaListener(topics = "restaurant.orders.accepted", groupId = "ms-order")
    public void onRestaurantAccepted(OrderStatusEvent event) {
        log.info("Received restaurant.orders.accepted: orderId={}", event.getOrderId());
        orderService.updateStatus(event.getOrderId(), OrderStatus.ACCEPTED);
    }

    @KafkaListener(topics = "restaurant.orders.rejected", groupId = "ms-order")
    public void onRestaurantRejected(OrderStatusEvent event) {
        log.info("Received restaurant.orders.rejected: orderId={}", event.getOrderId());
        orderService.updateStatus(event.getOrderId(), OrderStatus.REJECTED);
    }

    @KafkaListener(topics = "payment.completed", groupId = "ms-order")
    public void onPaymentCompleted(OrderStatusEvent event) {
        log.info("Received payment.completed: orderId={}", event.getOrderId());
        orderService.updateStatus(event.getOrderId(), OrderStatus.PAID);
    }

    @KafkaListener(topics = "payment.failed", groupId = "ms-order")
    public void onPaymentFailed(OrderStatusEvent event) {
        log.info("Received payment.failed: orderId={}", event.getOrderId());
        orderService.updateStatus(event.getOrderId(), OrderStatus.FAILED);
    }

    @KafkaListener(topics = "delivery.assigned", groupId = "ms-order")
    public void onDeliveryAssigned(OrderStatusEvent event) {
        log.info("Received delivery.assigned: orderId={}", event.getOrderId());
        orderService.updateStatus(event.getOrderId(), OrderStatus.ASSIGNED);
    }

    @KafkaListener(topics = "delivery.picked_up", groupId = "ms-order")
    public void onDeliveryPickedUp(OrderStatusEvent event) {
        log.info("Received delivery.picked_up: orderId={}", event.getOrderId());
        orderService.updateStatus(event.getOrderId(), OrderStatus.PICKED_UP);
    }

    @KafkaListener(topics = "delivery.completed", groupId = "ms-order")
    public void onDeliveryCompleted(OrderStatusEvent event) {
        log.info("Received delivery.completed: orderId={}", event.getOrderId());
        orderService.updateStatus(event.getOrderId(), OrderStatus.COMPLETED);
    }
}
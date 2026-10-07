package ru.otus.kafka.obukhov.term.order.service.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.kafka.obukhov.term.order.dto.CreateOrderRequest;
import ru.otus.kafka.obukhov.term.order.dto.OrderDetailsResponse;
import ru.otus.kafka.obukhov.term.order.dto.OrderItemDto;
import ru.otus.kafka.obukhov.term.order.dto.OrderResponse;
import ru.otus.kafka.obukhov.term.order.entity.*;
import ru.otus.kafka.obukhov.term.order.event.OrderCreatedEvent;
import ru.otus.kafka.obukhov.term.order.repository.OrderDishRepository;
import ru.otus.kafka.obukhov.term.order.repository.OrderRepository;
import ru.otus.kafka.obukhov.term.order.repository.OutboxRepository;
import ru.otus.kafka.obukhov.term.order.service.OrderService;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final String ORDER_CREATED_TOPIC = "order.created";

    private final OrderRepository orderRepository;
    private final OrderDishRepository orderDishRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = Order.builder()
                .customerId(request.getCustomerId())
                .restaurantId(request.getRestaurantId())
                .status(OrderStatus.CREATED)
                .totalAmount(request.getTotalAmount())
                .build();

        Order saved = orderRepository.save(order);

        request.getDishes().stream().forEach(od ->
                orderDishRepository.save(OrderDish.builder()
                                .order(saved)
                                .dishId(od.getDishId())
                                .cnt(od.getCnt())
                                .price(od.getPrice())
                        .build())

        );

        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(saved.getId())
                .customerId(saved.getCustomerId())
                .restaurantId(saved.getRestaurantId())
                .totalAmount(saved.getTotalAmount())
                .createdAt(saved.getCreatedAt())
                .eventId(UUID.randomUUID())
                .build();

        saveToOutbox(saved.getId(), event);

        return mapToResponse(saved);
    }

    private void saveToOutbox(UUID orderId, OrderCreatedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            OutboxEvent outbox = OutboxEvent.builder()
                    .aggregateId(orderId)
                    .eventType(EventType.ORDER_CREATED)
                    .aggregateType("ORDER")
                    .topic(ORDER_CREATED_TOPIC)
                    .payload(payload)
                    .status(EventStatus.NEW)
                    .build();

            outboxRepository.save(outbox);
            log.info("Saved outbox event ORDER_CREATED for orderId={}", orderId);
        } catch (Exception e) {
            // Если не смогли сериализовать — вся транзакция откатится
            throw new IllegalStateException(
                    "Failed to serialize OrderCreatedEvent for orderId=" + orderId, e);
        }
    }

    @Override
    public OrderResponse getOrder(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
        return mapToResponse(order);
    }

    @Override
    @Transactional
    public void updateStatus(UUID orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        OrderStatus current = order.getStatus();
        if (current == newStatus) {
            log.warn("Order {} already in status {}, skip", orderId, newStatus);
            return;
        }

        order.setStatus(newStatus);
        orderRepository.save(order);
        log.info("Order {} status changed: {} -> {}", orderId, current, newStatus);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailsResponse getOrderDetails(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));

        List<OrderItemDto> items = order.getDishes().stream()
                .map(dish -> OrderItemDto.builder()
                        .dishId(dish.getId())  // ⚠️ если у OrderDish есть dishId как поле
                        .cnt(dish.getCnt())
                        .price(dish.getPrice())
                        .build())
                .toList();

        return OrderDetailsResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .restaurantId(order.getRestaurantId())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .restaurantId(order.getRestaurantId())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
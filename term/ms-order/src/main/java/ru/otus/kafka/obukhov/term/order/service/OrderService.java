package ru.otus.kafka.obukhov.term.order.service;

import ru.otus.kafka.obukhov.term.order.dto.CreateOrderRequest;
import ru.otus.kafka.obukhov.term.order.dto.OrderDetailsResponse;
import ru.otus.kafka.obukhov.term.order.dto.OrderResponse;
import ru.otus.kafka.obukhov.term.order.entity.OrderStatus;

import java.util.UUID;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request);

    OrderResponse getOrder(UUID id);

    void updateStatus(UUID orderId, OrderStatus orderStatus);

    OrderDetailsResponse getOrderDetails(UUID id);
}
package ru.otus.kafka.obukhov.term.restaurant.kafka;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.otus.kafka.obukhov.term.restaurant.client.OrderClient;
import ru.otus.kafka.obukhov.term.restaurant.dto.OrderDetailsDto;
import ru.otus.kafka.obukhov.term.restaurant.dto.OrderItemDto;
import ru.otus.kafka.obukhov.term.restaurant.entity.RestaurantDish;
import ru.otus.kafka.obukhov.term.restaurant.entity.RestaurantDishId;
import ru.otus.kafka.obukhov.term.restaurant.event.OrderCreatedEvent;
import ru.otus.kafka.obukhov.term.restaurant.event.PaymentCompletedEvent;
import ru.otus.kafka.obukhov.term.restaurant.event.RestaurantOrderDecisionEvent;
import ru.otus.kafka.obukhov.term.restaurant.repository.RestaurantDishRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestaurantOrderListener {

    private final RestaurantDishRepository restaurantDishRepository;
    private final OrderClient orderClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "order.created", groupId = "ms-restaurant")
    @Transactional
    public void onOrderCreated(OrderCreatedEvent event) {
        UUID orderId = event.getOrderId();
        log.info("Received order.created for orderId={}", orderId);

        try {
            OrderDetailsDto order = orderClient.getOrder(orderId);

            // Собираем dishIds из заказа
            List<UUID> dishIds = order.getItems().stream()
                    .map(OrderItemDto::getDishId)
                    .toList();

            // Загружаем остатки для этих блюд в ресторане
            List<RestaurantDish> stocks = restaurantDishRepository
                    .findByRestaurantAndDishIds(order.getRestaurantId(), dishIds);

            // Строим map для быстрого доступа: dishId -> cnt в наличии
            Map<UUID, Integer> stockMap = stocks.stream()
                    .collect(Collectors.toMap(
                            rd -> rd.getDish().getId(),
                            RestaurantDish::getCnt));

            // Проверяем каждую позицию заказа

            for (OrderItemDto item : order.getItems()) {
                Integer available = stockMap.getOrDefault(item.getDishId(), 0);
                if (available < item.getCnt()) {
                    log.warn("Not enough stock for dishId={}: need {}, have {}",
                            item.getDishId(), item.getCnt(), available);
                    publishRejection(orderId, order.getRestaurantId(),
                            "Not enough stock for dish " + item.getDishId());
                    return;
                }
            }

            publishAcceptance(orderId, order.getRestaurantId());

        } catch (Exception e) {
            log.error("Failed to process payment.completed for orderId={}", orderId, e);
            // Не пробрасываем исключение, чтобы не зациклить consumer.
            // В проде — отправить в DLQ.
            throw e;  // Или обработать через DefaultErrorHandler
        }
    }

    @KafkaListener(topics = "payment.completed", groupId = "ms-restaurant")
    @Transactional
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        UUID orderId = event.getOrderId();
        log.info("Received payment.completed for orderId={}", orderId);

        try {
            OrderDetailsDto order = orderClient.getOrder(orderId);

            // Собираем dishIds из заказа
            List<UUID> dishIds = order.getItems().stream()
                    .map(OrderItemDto::getDishId)
                    .toList();

            // Загружаем остатки для этих блюд в ресторане
            List<RestaurantDish> stocks = restaurantDishRepository
                    .findByRestaurantAndDishIds(order.getRestaurantId(), dishIds);

            // Строим map для быстрого доступа: dishId -> cnt в наличии
            Map<UUID, Integer> stockMap = stocks.stream()
                    .collect(Collectors.toMap(
                            rd -> rd.getDish().getId(),
                            RestaurantDish::getCnt));

            // Проверяем каждую позицию заказа
            for (OrderItemDto item : order.getItems()) {
                Integer available = stockMap.getOrDefault(item.getDishId(), 0);
                if (available < item.getCnt()) {
                    log.warn("Not enough stock for dishId={}: need {}, have {}",
                            item.getDishId(), item.getCnt(), available);
                    publishRejection(orderId, order.getRestaurantId(),
                            "Not enough stock for dish " + item.getDishId());
                    return;
                }
            }

            // Всё хватает — принимаем заказ и списываем остатки
            for (OrderItemDto item : order.getItems()) {
                RestaurantDishId stockId = new RestaurantDishId(
                        order.getRestaurantId(), item.getDishId());
                RestaurantDish stock = restaurantDishRepository.findById(stockId)
                        .orElseThrow();
                stock.setCnt(stock.getCnt() - item.getCnt());
                restaurantDishRepository.save(stock);
            }

            publishAcceptance(orderId, order.getRestaurantId());

        } catch (Exception e) {
            log.error("Failed to process payment.completed for orderId={}", orderId, e);
            // Не пробрасываем исключение, чтобы не зациклить consumer.
            // В проде — отправить в DLQ.
            throw e;  // Или обработать через DefaultErrorHandler
        }
    }

    private void publishAcceptance(UUID orderId, UUID restaurantId) {
        RestaurantOrderDecisionEvent event = RestaurantOrderDecisionEvent.builder()
                .orderId(orderId)
                .restaurantId(restaurantId)
                .eventId(UUID.randomUUID())
                .build();
        kafkaTemplate.send("restaurant.orders.accepted", orderId.toString(), event);
        log.info("Published restaurant.orders.accepted for orderId={}", orderId);
    }


    private void publishReady(UUID orderId, UUID restaurantId) {
        RestaurantOrderDecisionEvent event = RestaurantOrderDecisionEvent.builder()
                .orderId(orderId)
                .restaurantId(restaurantId)
                .eventId(UUID.randomUUID())
                .build();
        kafkaTemplate.send("restaurant.orders.completed", orderId.toString(), event);
        log.info("Published restaurant.orders.accepted for orderId={}", orderId);
    }

    private void publishRejection(UUID orderId, UUID restaurantId, String reason) {
        RestaurantOrderDecisionEvent event = RestaurantOrderDecisionEvent.builder()
                .orderId(orderId)
                .restaurantId(restaurantId)
                .reason(reason)
                .eventId(UUID.randomUUID())
                .build();
        kafkaTemplate.send("restaurant.orders.rejected", orderId.toString(), event);
        log.info("Published restaurant.orders.rejected for orderId={}", orderId);
    }
}
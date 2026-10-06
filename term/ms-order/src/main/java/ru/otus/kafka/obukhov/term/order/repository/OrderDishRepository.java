package ru.otus.kafka.obukhov.term.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.kafka.obukhov.term.order.entity.OrderDish;

import java.util.UUID;

public interface OrderDishRepository extends JpaRepository<OrderDish, UUID> {
}

package ru.otus.kafka.obukhov.term.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.kafka.obukhov.term.order.entity.Order;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
}
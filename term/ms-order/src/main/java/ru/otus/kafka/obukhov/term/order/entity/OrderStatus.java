package ru.otus.kafka.obukhov.term.order.entity;

public enum OrderStatus {
    CREATED,
    ACCEPTED,
    REJECTED,
    PAID,
    FAILED,
    ASSIGNED,
    PICKED_UP,
    COMPLETED
}

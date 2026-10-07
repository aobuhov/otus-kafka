package ru.otus.kafka.obukhov.term.delivery.service;

import ru.otus.kafka.obukhov.term.delivery.entity.Delivery;

import java.util.UUID;

public interface DeliveryService {

    Delivery assignEmployee(UUID deliveryId, UUID employeeId);
    Delivery markPickedUp(UUID deliveryId);
    Delivery markCompleted(UUID deliveryId);

}

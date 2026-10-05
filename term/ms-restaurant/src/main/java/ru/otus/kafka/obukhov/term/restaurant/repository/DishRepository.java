package ru.otus.kafka.obukhov.term.restaurant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.kafka.obukhov.term.restaurant.entity.Dish;

import java.util.UUID;

public interface DishRepository extends JpaRepository<Dish, UUID> {

}

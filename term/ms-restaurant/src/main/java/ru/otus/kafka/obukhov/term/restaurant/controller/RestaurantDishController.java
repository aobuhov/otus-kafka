package ru.otus.kafka.obukhov.term.restaurant.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.otus.kafka.obukhov.term.restaurant.dto.RestaurantDishRequest;
import ru.otus.kafka.obukhov.term.restaurant.dto.RestaurantDishResponse;
import ru.otus.kafka.obukhov.term.restaurant.entity.Dish;
import ru.otus.kafka.obukhov.term.restaurant.entity.Restaurant;
import ru.otus.kafka.obukhov.term.restaurant.entity.RestaurantDish;
import ru.otus.kafka.obukhov.term.restaurant.entity.RestaurantDishId;
import ru.otus.kafka.obukhov.term.restaurant.repository.DishRepository;
import ru.otus.kafka.obukhov.term.restaurant.repository.RestaurantDishRepository;
import ru.otus.kafka.obukhov.term.restaurant.repository.RestaurantRepository;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/restaurant-dishes")
@RequiredArgsConstructor
public class RestaurantDishController {

    private final RestaurantDishRepository repository;
    private final RestaurantRepository restaurantRepository;
    private final DishRepository dishRepository;

    @GetMapping("/by-restaurant/{restaurantId}")
    public List<RestaurantDishResponse> getByRestaurant(@PathVariable UUID restaurantId) {
        return repository.findByRestaurantId(restaurantId).stream()
                .map(rd -> RestaurantDishResponse.builder()
                        .restaurantId(rd.getRestaurant().getId())
                        .restaurantName(rd.getRestaurant().getName())
                        .dishId(rd.getDish().getId())
                        .dishName(rd.getDish().getName())
                        .cnt(rd.getCnt())
                        .price(rd.getPrice())
                        .build())
                .toList();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody RestaurantDishRequest request) {
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));
        Dish dish = dishRepository.findById(request.getDishId())
                .orElseThrow(() -> new RuntimeException("Dish not found"));

        RestaurantDish entity = RestaurantDish.builder()
                .restaurant(restaurant)
                .dish(dish)
                .cnt(request.getCnt())
                .price(request.getPrice())
                .build();

        repository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody RestaurantDishRequest request) {
        RestaurantDishId id = new RestaurantDishId(
                request.getRestaurantId(), request.getDishId());

        return repository.findById(id)
                .map(rd -> {
                    rd.setCnt(request.getCnt());
                    rd.setPrice(request.getPrice());
                    repository.save(rd);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{restaurantId}/{dishId}")
    public ResponseEntity<Void> delete(@PathVariable UUID restaurantId,
                                       @PathVariable UUID dishId) {
        RestaurantDishId id = new RestaurantDishId(restaurantId, dishId);
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
package ru.otus.kafka.obukhov.term.restaurant.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.otus.kafka.obukhov.term.restaurant.entity.Dish;
import ru.otus.kafka.obukhov.term.restaurant.repository.DishRepository;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dishes")
@RequiredArgsConstructor
public class DishController {
    private final DishRepository dishRepository;

    @GetMapping
    public List<Dish> getAll() {
        return dishRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Dish> getById(@PathVariable UUID id) {
        return dishRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Dish> create(@RequestBody Dish dish) {
        Dish saved = dishRepository.save(dish);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Dish> update(@PathVariable UUID id,
                                             @RequestBody Dish updated) {
        return dishRepository.findById(id)
                .map(r -> {
                    r.setName(updated.getName());
                    return ResponseEntity.ok(dishRepository.save(r));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!dishRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        dishRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}

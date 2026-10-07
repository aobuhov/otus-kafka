package ru.otus.kafka.obukhov.term.delivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.kafka.obukhov.term.delivery.entity.Employee;

import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {}
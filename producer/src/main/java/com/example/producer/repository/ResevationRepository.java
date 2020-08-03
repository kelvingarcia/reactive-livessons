package com.example.producer.repository;

import com.example.producer.model.Reservation;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ResevationRepository extends ReactiveCrudRepository<Reservation, String> {
    Flux<Reservation> findByName(String name);
}

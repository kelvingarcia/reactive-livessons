package com.example.producer;

import com.example.producer.model.Reservation;
import com.example.producer.repository.ResevationRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@RunWith(SpringRunner.class)
@DataMongoTest
public class ReservationRepositoryTest {

    @Autowired
    private ResevationRepository repository;

    @Test
    public void persist() throws Exception{
        Flux<Reservation> reservationFlux = this.repository.deleteAll()
                .thenMany(repository.saveAll(Flux.just(
                        new Reservation(null, "A"),
                        new Reservation(null, "B"),
                        new Reservation(null, "C"),
                        new Reservation(null, "A")
                        )))
                .thenMany(this.repository.findByName("A"));

        StepVerifier
            .create(reservationFlux)
            .expectNextCount(2)
            .verifyComplete();
    }
}

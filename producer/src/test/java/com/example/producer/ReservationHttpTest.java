package com.example.producer;

import com.example.producer.config.ReservationHttpConfiguration;
import com.example.producer.model.Reservation;
import com.example.producer.repository.ResevationRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

@RunWith(SpringRunner.class)
@WebFluxTest
@Import(ReservationHttpConfiguration.class)
public class ReservationHttpTest {

    @MockBean
    private ResevationRepository reservationRepository;

    @Autowired
    private WebTestClient testClient;

    @Before
    public void before(){
        Mockito.when(this.reservationRepository.findAll())
                .thenReturn(Flux.just(new Reservation("1", "Jane")));
    }

    @Test
    public void getAllReservations() throws Exception {
        this.testClient
            .get()
            .uri("/reservations")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody().jsonPath("@.[0].name").isEqualTo("Jane");
    }
}

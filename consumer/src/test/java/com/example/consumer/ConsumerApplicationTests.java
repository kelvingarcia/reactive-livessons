package com.example.consumer;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureStubRunner(
        ids="com.example:producer:+:8080",
        stubsMode = StubRunnerProperties.StubsMode.LOCAL)
//@AutoConfigureWireMock(port = 8080)
public class ConsumerApplicationTests {

    @Autowired
    private ReservationClient client;

    @Test
    public void contextLoads() {
//        stubFor(
//                get(urlEqualTo("/reservations"))
//                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//                                .withBody("[{\"id\":\"1\",\"reservationName\":\"Jane\"}]")
//                                .withStatus(HttpStatus.OK.value())
//                        ));

        Flux<Reservation> reservations = this.client.getAllReservations();
        StepVerifier
                .create(reservations)
                .expectNextMatches(reservation ->
                        reservation.getId() != null &&
                                reservation.getName().equalsIgnoreCase("Jane"))
                .verifyComplete();
    }

}

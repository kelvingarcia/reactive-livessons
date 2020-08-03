package com.example.producer.config;

import com.example.producer.model.Reservation;
import com.example.producer.repository.ResevationRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Configuration
public class ReservationHttpConfiguration {

    @Bean
    RouterFunction<ServerResponse> routes(ResevationRepository rr){
        return route()
            .GET("/reservations", req -> ok().body(rr.findAll(), Reservation.class))
            .build();
    }
}

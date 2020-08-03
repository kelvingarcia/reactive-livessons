package com.example.reservationservice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@SpringBootApplication
public class ReservationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservationServiceApplication.class, args);
	}

	@Bean
	RouterFunction<ServerResponse> routes(ReservationRepository rr){
		return route()
			.GET("/reservations", r -> ok().body(rr.findAll(), Reservation.class))
			.build();
	}
}

@Component
@RequiredArgsConstructor
@Log4j2
class Initializer {
	private final ReservationRepository reservationRepository;

	@EventListener(ApplicationReadyEvent.class)
	public void ready(){
		this.reservationRepository
			.deleteAll()
			.thenMany(this.reservationRepository.saveAll(
					Flux.just(
						new Reservation(null, "Josh"),
						new Reservation(null, "Julie"),
						new Reservation(null, "Tammie"),
						new Reservation(null, "Kimly"),
						new Reservation(null, "Andrew")
					)
				)
			)
			.subscribe();
	}
}

interface ReservationRepository extends ReactiveCrudRepository<Reservation, String> {}

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
class Reservation {
	@Id
	private String id;
	private String name;
}

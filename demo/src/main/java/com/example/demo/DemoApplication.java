package com.example.demo;

import io.r2dbc.spi.ConnectionFactory;
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
import org.springframework.data.r2dbc.connectionfactory.R2dbcTransactionManager;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.util.context.Context;

import javax.annotation.processing.Generated;

@SpringBootApplication
@EnableTransactionManagement
public class DemoApplication {

	@Bean
	TransactionalOperator transactionalOperator(ReactiveTransactionManager rtm){
		return TransactionalOperator.create(rtm);
	}

	@Bean
	ReactiveTransactionManager r2dbTransactionManager(ConnectionFactory cf){
		return new R2dbcTransactionManager(cf);
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}

@Service
@RequiredArgsConstructor
@Transactional
class ReservationService {

	private final ReservationRepository reservationRepository;
	private final TransactionalOperator transactionalOperator;

	Flux<Reservation> saveAll(String ... names){
		Flux<Reservation> reservations = Flux
				.fromArray(names)
				.map(name -> new Reservation(null, name))
				.flatMap(this.reservationRepository::save)
				.doOnNext(this::assertValid);
		return reservations;
//		return this.transactionalOperator.transactional(reservations);
	}

	private void assertValid(Reservation r){
		Assert.isTrue(r.getName() != null &&
				r.getName().length() > 0 &&
				Character.isUpperCase(r.getName().charAt(0)),
				"the name must start with a capital letter");
	}
}

@Component
@RequiredArgsConstructor
@Log4j2
class SampleDataInitializer {

	private final ReservationRepository reservationRepository;
	private final ReservationService reservationService;
	private final DatabaseClient databaseClient;

	@EventListener(ApplicationReadyEvent.class)
	public void ready(){

		Flux<Reservation> reservations = this.reservationService.saveAll("Madhura", "Josh", "Olga", "Marcin",
				"Ria", "stéphane", "Violetta", "Dr. Syer");

		this.reservationRepository
				.deleteAll()
				.thenMany(reservations)
				.thenMany(this.reservationRepository.findAll())
				.subscribe(log::info);
	}
}

interface ReservationRepository extends ReactiveCrudRepository<Reservation, Integer> {
//	Flux<Reservation> findByName(String name);
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Reservation {

	@Id
	private Integer id;

	private String name;

}
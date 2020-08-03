package com.example.greetingservice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@SpringBootApplication
public class GreetingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GreetingServiceApplication.class, args);
	}

	@Bean
	RouterFunction<ServerResponse> routes(GreetingService greetingService){
		return route()
			.GET("/greeting/{name}", request -> ok().body(
					greetingService.greetOnce(new GreetingRequest(request.pathVariable("name"))), GreetingResponse.class
			))
			.GET("/greetings/{name}", request -> ok().contentType(MediaType.TEXT_EVENT_STREAM).body(
					greetingService.greetMany(new GreetingRequest(request.pathVariable("name"))), GreetingResponse.class
			))
			.build();
	}
}

@Service
class GreetingService {

	Mono<GreetingResponse> greetOnce(GreetingRequest request){
		return Mono.just(greet(request));
	}

	Flux<GreetingResponse> greetMany(GreetingRequest request){
		return Flux.fromStream(
				Stream.generate(() -> greet(request))
		).delayElements(Duration.ofSeconds(1));
	}

	private GreetingResponse greet(GreetingRequest request) {
		return new GreetingResponse("Hello " + request.getName() + " @ " + Instant.now() + "!");
	}
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class GreetingRequest {
	private String name;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class GreetingResponse {
	private String message;
}
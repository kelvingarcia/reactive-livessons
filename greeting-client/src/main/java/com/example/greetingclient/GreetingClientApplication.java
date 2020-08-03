package com.example.greetingclient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class GreetingClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(GreetingClientApplication.class, args);
    }

    @Bean
    WebClient webClient(WebClient.Builder builder) {
        return builder.baseUrl("http://localhost:8080")
//				.filter(ExchangeFilterFunctions.basicAuthentication())
                .build();
    }
}

@Component
@Log4j2
class Client {

    private final WebClient client;
    private final ReactiveCircuitBreaker reactiveCircuitBreaker;

    Client(WebClient client, ReactiveCircuitBreakerFactory reactiveCircuitBreakerFactory) {
        this.client = client;
        this.reactiveCircuitBreaker = reactiveCircuitBreakerFactory.create("greeting");
    }

    @EventListener(ApplicationReadyEvent.class)
    public void ready() {

        

        var name = "Spring Fans";

        Mono<String> http = this.client
                .get()
                .uri("/greeting/{name}", name)
                .retrieve()
                .bodyToMono(GreetingResponse.class)
                .map(GreetingResponse::getMessage);
//                .retry(10)
//                .onErrorMap(throwable -> new IllegalArgumentException("the original exception was " + throwable.toString()))
//                .onErrorResume(IllegalArgumentException.class, exception -> Mono.just(exception.toString()));

        this.reactiveCircuitBreaker
                .run(http,
                 throwable -> Mono.just("EEEK!"))
                .subscribe(gr -> log.info("Mono: " + gr));

/*		this.client
			.get()
			.uri("/greetings/{name}", name)
			.retrieve()
			.bodyToFlux(GreetingResponse.class)
			.subscribe(gr -> log.info("Flux: " + gr.getMessage()));*/
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

package com.example.greetingclientsecurity;

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
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class GreetingClientSecurityApplication {

	public static void main(String[] args) {
		SpringApplication.run(GreetingClientSecurityApplication.class, args);
	}

	@Bean
	WebClient webClient(WebClient.Builder builder){
		return builder
			.filter(ExchangeFilterFunctions.basicAuthentication("user", "password"))
			.build();
	}

}

@Component
@RequiredArgsConstructor
@Log4j2
class Consumer {
	private final WebClient client;

	@EventListener(ApplicationReadyEvent.class)
	public void ready(){
		this.client.get().uri("http://localhost:8080/greetings").retrieve()
			.bodyToFlux(GreetingResponse.class)
			.subscribe(log::info);
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

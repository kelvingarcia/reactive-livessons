package com.example.rsocketclientsecurity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.rsocket.messaging.RSocketStrategiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.security.rsocket.metadata.BasicAuthenticationEncoder;
import org.springframework.security.rsocket.metadata.UsernamePasswordMetadata;
import org.springframework.stereotype.Component;

import static org.springframework.security.rsocket.metadata.UsernamePasswordMetadata.BASIC_AUTHENTICATION_MIME_TYPE;

@SpringBootApplication
public class RsocketClientSecurityApplication {

	public static void main(String[] args) {
		SpringApplication.run(RsocketClientSecurityApplication.class, args);
	}

	@Bean
	RSocketRequester rSocketRequester(RSocketRequester.Builder builder){
		return builder.connectTcp("localhost", 8888).block();
	}

	@Bean
	RSocketStrategiesCustomizer rSocketStrategiesCustomizer(){
		return strategies -> strategies.encoder(new BasicAuthenticationEncoder());
	}
}

@Component
@RequiredArgsConstructor
@Log4j2
class Consumer {
	private final RSocketRequester rSocketRequester;

	@EventListener(ApplicationReadyEvent.class)
	public void ready(){
		var credentials = new UsernamePasswordMetadata("user", "password");
		this.rSocketRequester
			.route("greetings.2")
			.metadata(credentials, BASIC_AUTHENTICATION_MIME_TYPE)
			.retrieveFlux(GreetingResponse.class)
			.subscribe(log::info);
	}
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class GreetingResponse {
	private String message;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class GreetingRequest {
	private String name;
}

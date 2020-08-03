package com.example.greetingsservicersocketgateway;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

@SpringBootApplication
public class GreetingsServiceRsocketGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GreetingsServiceRsocketGatewayApplication.class, args);
	}

}

@Controller
class GreetingRSocketController {
	@MessageMapping("greetings.{timeInSeconds}")
	Flux<GreetingResponse> greet(@DestinationVariable int timeInSeconds, GreetingRequest request){
		return Flux.fromStream(
				Stream.generate(() ->
						new GreetingResponse(("Hello " + request.getName() + " @ " + Instant.now() + "!"))
				)
		).delayElements(Duration.ofSeconds(timeInSeconds));
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

package com.example.greetingsclientrsocketgateway;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.rsocket.client.BrokerClient;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class GreetingsClientRsocketGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GreetingsClientRsocketGatewayApplication.class, args);
	}

}

@Component
@RequiredArgsConstructor
@Log4j2
class RSocketGateway {
	private final BrokerClient client;

	@EventListener
	public void gatewayRSocketClient(PayloadApplicationEvent<RSocketRequester> event){
		event.getPayload()
				.route("greetings.2")
				.metadata(client.forwarding("greetings-service-rsocket-gateway"))
				.data(new GreetingRequest("Livelessons"))
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

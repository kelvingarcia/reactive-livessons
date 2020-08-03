package com.example.rsocketgreetingservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.rsocket.AbstractRSocket;
import io.rsocket.Payload;
import io.rsocket.RSocketFactory;
import io.rsocket.SocketAcceptor;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

@SpringBootApplication
public class RsocketGreetingServiceApplication {

	@SneakyThrows
	public static void main(String[] args) {
		SpringApplication.run(RsocketGreetingServiceApplication.class, args);
		System.in.read();
	}

}

@Component
@RequiredArgsConstructor
class JsonHelper{
	private final ObjectMapper objectMapper;

	@SneakyThrows
	<T> T read(String json, Class<T> clazz) {
		return this.objectMapper.readValue(json, clazz);
	}

	@SneakyThrows
	String write(Object o){
		return this.objectMapper.writeValueAsString(o);
	}
}

@Component
@RequiredArgsConstructor
@Log4j2
class Producer {

	private final JsonHelper jsonHelper;
	private final GreetingService greetingService;

	@EventListener(ApplicationReadyEvent.class)
	public void start(){
		log.info("starting Producer...");
		SocketAcceptor socketAcceptor = ((connectionSetupPayload, senderRSocket) -> {
			AbstractRSocket response = new AbstractRSocket() {
				@Override
				public Flux<Payload> requestStream(Payload payload) {
					String json = payload.getDataUtf8();
					GreetingRequest greetingRequest = jsonHelper.read(json, GreetingRequest.class);
					return greetingService.greet(greetingRequest)
						.map(jsonHelper::write)
						.map(DefaultPayload::create);
				}
			};
			return Mono.just(response);
		});

		TcpServerTransport tcpServerTransport = TcpServerTransport.create(7000);

		RSocketFactory
			.receive()
			.acceptor(socketAcceptor)
			.transport(tcpServerTransport)
			.start()
			.block();

	}
}

@Service
class GreetingService {
	Flux<GreetingResponse> greet(GreetingRequest request){
		return Flux.fromStream(
			Stream.generate(() ->
				new GreetingResponse("Hello " + request.getName() + " @ " + Instant.now() + "!")
			)
		).delayElements(Duration.ofSeconds(1));
	}
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class GreetingRequest {
	private String name;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class GreetingResponse {
	private String message;
}

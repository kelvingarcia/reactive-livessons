package com.example.rsocketgreetingclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.rsocket.Payload;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class RsocketGreetingClientApplication {

	@SneakyThrows
	public static void main(String[] args) {
		SpringApplication.run(RsocketGreetingClientApplication.class, args);
		System.in.read();
	}

}

@Component
@Log4j2
@RequiredArgsConstructor
class Consumer {
	private final JsonHelper jsonHelper;

	@EventListener(ApplicationReadyEvent.class)
	public void consume(){
		log.info("starting Consumer...");
		String jsonRequest = jsonHelper.write(new GreetingRequest("Livelessons"));

		TcpClientTransport tcpClientTransport = TcpClientTransport.create(7000);

		RSocketFactory
			.connect()
			.transport(tcpClientTransport)
			.start()
			.flatMapMany(sender -> sender.requestStream(DefaultPayload.create(jsonRequest)))
			.map(Payload::getDataUtf8)
			.map(json -> jsonHelper.read(json, GreetingResponse.class))
			.subscribe(result -> log.info(result.toString()));

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

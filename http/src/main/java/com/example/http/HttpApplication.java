package com.example.http;

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
import static org.springframework.web.reactive.function.server.ServerResponse.*;

@SpringBootApplication
public class HttpApplication {

    @Bean
    RouterFunction<ServerResponse> routes(GreetingService gs) {
        return route()
                .GET("/greeting/{name}", r ->
						ok().body(gs.greetOnce(
								new GreetingRequest(r.pathVariable("name"))
						), GreetingResponse.class))
				.GET("/greetings/{name}", r ->
						ok()
						.contentType(MediaType.TEXT_EVENT_STREAM)
						.body(gs.greetMany(
								new GreetingRequest(r.pathVariable("name"))
						), GreetingResponse.class))
				.build();
    }

    public static void main(String[] args) {
        SpringApplication.run(HttpApplication.class, args);
    }

}

/*@RestController
@RequiredArgsConstructor
class GreetingRestController {

	private final GreetingService greetingService;

	@GetMapping("/greetings/{name}")
	Mono<GreetingResponse> greet(@PathVariable String name){
		return this.greetingService
				.greet(new GreetingRequest(name));
	}
}*/


@Service
class GreetingService {

    private GreetingResponse greet(String name) {
        return new GreetingResponse("Hello " + name + " @ " + Instant.now());
    }

    Flux<GreetingResponse> greetMany(GreetingRequest request) {
        return Flux.fromStream(
                Stream.generate(() -> greet(request.getName()))
        ).delayElements(Duration.ofSeconds(1));
    }

    Mono<GreetingResponse> greetOnce(GreetingRequest request) {
        return Mono.just(greet(request.getName()));
    }
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class GreetingResponse {
    private String message;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class GreetingRequest {
    private String name;
}

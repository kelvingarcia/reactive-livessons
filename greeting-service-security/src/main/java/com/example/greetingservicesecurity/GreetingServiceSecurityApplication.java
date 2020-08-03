package com.example.greetingservicesecurity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.constraints.AssertTrue;
import java.security.Principal;
import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@SpringBootApplication
public class GreetingServiceSecurityApplication {

	public static void main(String[] args) {
		SpringApplication.run(GreetingServiceSecurityApplication.class, args);
	}

	@Bean
	MapReactiveUserDetailsService authentication(){
		UserDetails build = User.withDefaultPasswordEncoder().username("user").password("password").roles("USER").build();
		return new MapReactiveUserDetailsService(build);
	}

	@Bean
	SecurityWebFilterChain authorization(ServerHttpSecurity http){
		return http
			.csrf(ServerHttpSecurity.CsrfSpec::disable)
			.httpBasic(Customizer.withDefaults())
			.authorizeExchange(ae -> ae
				.pathMatchers("/greeting*").authenticated()
				.anyExchange().permitAll()
			)
			.build();
	}

	@Bean
	RouterFunction<ServerResponse> routes(GreetingService gs) {
		return route()
			.GET("/greetings", serverRequest -> {
				Flux<GreetingResponse> greetingResponseFlux = serverRequest
						.principal()
						.map(Principal::getName)
						.map(GreetingRequest::new)
						.flatMapMany(gs::greetMany);

				return ok().contentType(MediaType.TEXT_EVENT_STREAM).body(greetingResponseFlux, GreetingResponse.class);
			})
			.GET("/greeting", serverRequest -> {
				Mono<GreetingResponse> greetingResponseMono = serverRequest
						.principal()
						.map(Principal::getName)
						.map(GreetingRequest::new)
						.flatMap(gs::greetOnce);

				return ok().contentType(MediaType.TEXT_EVENT_STREAM).body(greetingResponseMono, GreetingResponse.class);
			})
			.build();
	}
}

@Service
class GreetingService {

	GreetingResponse greet(GreetingRequest request){
		return new GreetingResponse("Hello " + request.getName() + " @ " + Instant.now() + "!");
	}

	Flux<GreetingResponse> greetMany(GreetingRequest request){
		return Flux.fromStream(Stream.generate(() -> greet(request))).delayElements(Duration.ofSeconds(1));
	}

	Mono<GreetingResponse> greetOnce(GreetingRequest request){
		return Mono.just(greet(request));
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

package com.example.rsocketservicesecurity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.rsocket.EnableRSocketSecurity;
import org.springframework.security.config.annotation.rsocket.RSocketSecurity;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

@SpringBootApplication
public class RsocketServiceSecurityApplication {

	public static void main(String[] args) {
		SpringApplication.run(RsocketServiceSecurityApplication.class, args);
	}

}

@Configuration
@EnableRSocketSecurity
class RSocketSecurityConfiguration {

	@Bean
	PayloadSocketAcceptorInterceptor authorization(RSocketSecurity rsocket){
		return rsocket
			.authorizePayload(authorize -> authorize
					.route("greeting*").authenticated()
					.anyExchange().permitAll()
			)
			.basicAuthentication(Customizer.withDefaults())
			.build();
	}

	@Bean
	MapReactiveUserDetailsService authentication(){
		UserDetails build = User.withDefaultPasswordEncoder().username("user").password("password").roles("USER").build();
		return new MapReactiveUserDetailsService(build);
	}
}

@Controller
@RequiredArgsConstructor
class GreetingRSocketController {

	private final GreetingService greetingService;

	@MessageMapping("greetings.{timeInSeconds}")
	Flux<GreetingResponse> greet(@DestinationVariable int timeInSeconds){
		return ReactiveSecurityContextHolder
				.getContext()
				.map(SecurityContext::getAuthentication)
				.map(au -> (User) au.getPrincipal())
				.map(User::getUsername)
				.flatMapMany(username -> this.greetingService.greet(new GreetingRequest(username), timeInSeconds));
	}
}

@Service
class GreetingService {

	Flux<GreetingResponse> greet(GreetingRequest request, int duration){
		return Flux.fromStream(Stream.generate(() -> new GreetingResponse("Hello " + request.getName() + " @ " + Instant.now() + "!"))).delayElements(Duration.ofSeconds(duration));
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

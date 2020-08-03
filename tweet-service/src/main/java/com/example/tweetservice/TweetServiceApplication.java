package com.example.tweetservice;

import akka.actor.Actor;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Source;
import akka.stream.scaladsl.Sink;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.reactivestreams.Publisher;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Flow;
import java.util.stream.Collectors;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@SpringBootApplication
@Log4j2
public class TweetServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TweetServiceApplication.class, args);
	}

	@Bean
	RouterFunction<ServerResponse> routes(TweetService tweetService){
		return route()
			.GET("/tweets", req -> ok().body(tweetService.getAllTweets(), Tweet.class))
			.GET("/hashtags", req -> ok().body(tweetService.getAllHashTags(), HashTag.class))
			.build();
	}

	@Bean
	ApplicationRunner producer(TweetRepository repository){
		return args -> {
			Author jonas = new Author("jboner"),
				viktor =  new Author("viktorklang"),
				josh = new Author("starbuxman");

			Flux<Tweet> tweetFlux = Flux.just(
					new Tweet("Woot, Konrad will be talking about #Enterprise #Integration done right! #akka #alpakka", viktor),
					new Tweet("#scala implicits can easily be used to model capabilities, but can they encode obligations easily? Easily as in: ergonomcally?", viktor),
					new Tweet("This is so cool! #akka", viktor),
					new Tweet("Cross data center replication of event sourced #akka actor is soon available (using #CRDTs and more).", jonas),
					new Tweet("a reminder: @SpringBoot lets you pair-program with the #Spring team", josh),
					new Tweet("whatever you next #platform is, don't built it yourself. Even companies with the $$ and motivation to do it fail. A LOT", josh)
			);

			repository.deleteAll()
				.thenMany(repository.saveAll(tweetFlux))
				.thenMany(repository.findAll())
				.subscribe(log::info);
		};
	}
}

@Configuration
class AkkaConfiguration {
	@Bean
	ActorSystem actorSystem(){
		return ActorSystem.create("bootiful-akka-stream");
	}

	@Bean
	ActorMaterializer actorMaterializer(){
		return ActorMaterializer.create(this.actorSystem());
	}
}

@Service
@RequiredArgsConstructor
class TweetService {
	private final TweetRepository tweetRepository;
	private final ActorMaterializer actorMaterializer;

	Publisher<Tweet> getAllTweets(){
		return this.tweetRepository.findAll();
	}

	Publisher<HashTag> getAllHashTags(){
		return Source.fromPublisher(getAllTweets())
				.map(Tweet::getHashTags)
				.reduce(this::join)
				.mapConcat(hashTags -> hashTags)
				.runWith(Sink.asPublisher(true), this.actorMaterializer);
	}

	private <T> Set<T> join(Set<T> a, Set<T> b){
		Set<T> set = new HashSet<>();
		set.addAll(a);
		set.addAll(b);
		return set;
	}
}

interface TweetRepository extends ReactiveMongoRepository<Tweet, String> {}

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
class HashTag {
	@Id
	private String id;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
class Author {
	@Id
	private String handle;
}

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
class Tweet {
	private String id;
	private String text;
	private Author author;

	public Tweet(String text, Author author) {
		this.text = text;
		this.author = author;
	}

	public Set<HashTag> getHashTags(){
		return Arrays.stream(this.text.split(" "))
				.filter(t -> t.startsWith("#"))
				.map(word -> new HashTag(word.replaceAll("[~#\\w+]", "").toLowerCase()))
				.collect(Collectors.toSet());
	}
}

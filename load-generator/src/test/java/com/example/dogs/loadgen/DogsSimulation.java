package com.example.dogs.loadgen;

import java.time.Duration;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.http.HeaderNames.ContentType;
import static io.gatling.http.HeaderNames.Accept;
import static io.gatling.http.HeaderValues.ApplicationJson;
import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;

public class DogsSimulation extends Simulation {

	final Duration duration = Duration.ofMinutes(2);

	final int usersPerSec = 5;

	final HttpProtocolBuilder httpProtocol = http.baseUrl("http://localhost:8081")
		// .basicAuth("user", "password")
		.contentTypeHeader(ApplicationJson())
		.acceptHeader(ApplicationJson());

	final ChainBuilder dogsByOwner = exec(http("dogsByOwner")
		.get(session -> "/owner/%s/dogs".formatted(generateName()))
		.header(ContentType(), ApplicationJson())
		.header(Accept(), ApplicationJson()));

	String generateName() {
		double random = Math.random();
		if (random < 0.45) {
			return "tommy";
		}
		else if (random < 0.9) {
			return "jonatan";
		}

		return "qwerty";
	}

	{
		setUp(
			scenario("dogs")
				.exec(dogsByOwner)
				.injectOpen(constantUsersPerSec(usersPerSec).during(duration))
		).protocols(httpProtocol);
	}

}

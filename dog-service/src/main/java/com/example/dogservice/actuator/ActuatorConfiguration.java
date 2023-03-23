package com.example.dogservice.actuator;

import io.micrometer.observation.ObservationPredicate;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.observation.ServerRequestObservationContext;

@Configuration(proxyBeanMethods = false)
public class ActuatorConfiguration {

	@Bean
	ObservationPredicate noActuatorObservations() {
		return (name, context) -> {
			if (name.equals("http.server.requests") && context instanceof ServerRequestObservationContext serverContext) {
				return !serverContext.getCarrier().getRequestURI().startsWith("/actuator");
			}
			return true;
		};
	}

}

package com.example.dogservice.security;

import java.util.List;
import java.util.stream.Collectors;

import io.micrometer.observation.ObservationPredicate;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;

/**
 * Spring Security configuration.
 */
@Configuration(proxyBeanMethods = false)
public class SecurityConfiguration {

	@Bean
	ObservationPredicate noSpringSecurityObservations() {
		return (name, context) -> !name.startsWith("spring.security.");
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests((requests) -> {
			requests.requestMatchers("/dogs", "/actuator/**").permitAll();
			requests.requestMatchers(EndpointRequest.toAnyEndpoint()).hasAnyRole("admin");
			requests.anyRequest().authenticated();
		});
		http.httpBasic(Customizer.withDefaults());
		return http.build();
	}

	@Bean
	InMemoryUserDetailsManager inMemoryUserDetailsManager(SecurityProperties properties) {
		List<UserDetails> userDetails = properties.users().stream().map(this::asUserDetails)
				.collect(Collectors.toList());
		return new InMemoryUserDetailsManager(userDetails);
	}

	private UserDetails asUserDetails(UserProperties properties) {
		PropertyMapper map = PropertyMapper.get();
		UserBuilder builder = User.builder();
		map.from(properties::name).to(builder::username);
		map.from(properties::password).as("{noop}"::concat).to(builder::password);
		map.from(properties::roles).as(StringUtils::toStringArray).to(builder::roles);
		return builder.build();
	}

}

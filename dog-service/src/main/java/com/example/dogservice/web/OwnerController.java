package com.example.dogservice.web;

import java.util.List;

import com.example.dogservice.service.NoSuchDogOwnerException;
import com.example.dogservice.service.OwnerService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.ServerHttpObservationFilter;

/**
 * Spring MVC controller for {@literal /owner}.
 */
@RestController
public class OwnerController {

	private final static Logger logger = LoggerFactory.getLogger(OwnerController.class);

	private final OwnerService ownerService;

	public OwnerController(OwnerService dogService) {
		this.ownerService = dogService;
	}

	@GetMapping("/owner/{name}/dogs")
	List<String> dog(@PathVariable String name) {
		return this.ownerService.getOwnedDogNames(name);
	}

	@ExceptionHandler
	ProblemDetail onNoSuchDogOwner(HttpServletRequest request, NoSuchDogOwnerException ex) {
		logger.error("Ooops!", ex);
		ServerHttpObservationFilter.findObservationContext(request)
			.ifPresent(context -> context.setError(ex));
		ProblemDetail details = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
		details.setProperty("ownername", ex.getName());
		return details;
	}

}

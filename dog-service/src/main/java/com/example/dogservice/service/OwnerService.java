package com.example.dogservice.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.dogservice.domain.Dog;
import com.example.dogservice.domain.DogRepository;
import com.example.dogservice.domain.Owner;
import com.example.dogservice.domain.OwnerRepository;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;

/**
 * Dog owner service.
 */
@Service
public class OwnerService {

	private final OwnerRepository ownerRepository;

	private final DogRepository dogRepository;

	private final ObservationRegistry observationRegistry;

	public OwnerService(OwnerRepository ownerRepository, DogRepository dogRepository,
			ObservationRegistry observationRegistry) {
		this.ownerRepository = ownerRepository;
		this.dogRepository = dogRepository;
		this.observationRegistry = observationRegistry;
	}

	public List<String> getOwnedDogNames(String ownerName) {
		return Observation.createNotStarted("getDogs", observationRegistry)
				.contextualName("gettingOwnedDogs")
				.highCardinalityKeyValue("ownerName", ownerName)
				.observe(() -> getOwnedDogNamesInternal(ownerName));
	}

	private List<String> getOwnedDogNamesInternal(String ownerName) {
		Owner owner = this.ownerRepository.findByNameIgnoringCase(ownerName);
		if (owner == null) {
			throw new NoSuchDogOwnerException(ownerName);
		}
		return this.dogRepository.findByOwner(owner).stream().map(Dog::getName).toList();
	}

}

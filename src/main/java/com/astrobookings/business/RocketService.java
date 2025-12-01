package com.astrobookings.business;

import java.util.List;

import com.astrobookings.business.exception.ValidationException;
import com.astrobookings.business.models.CreateRocketCommand;
import com.astrobookings.persistence.RocketRepository;
import com.astrobookings.persistence.models.Rocket;

public class RocketService {
  private final RocketRepository rocketRepository;

  public RocketService() {
    this(new RocketRepository());
  }

  public RocketService(RocketRepository rocketRepository) {
    this.rocketRepository = rocketRepository;
  }

  public List<Rocket> getAll() {
    return rocketRepository.findAll();
  }

  public Rocket create(CreateRocketCommand command) {
    validate(command);

    Rocket rocket = new Rocket();
    rocket.setName(command.name());
    rocket.setCapacity(command.capacity());
    rocket.setSpeed(command.maxSpeed());
    return rocketRepository.save(rocket);
  }

  private void validate(CreateRocketCommand command) {
    if (command.name() == null || command.name().isBlank()) {
      throw new ValidationException("Rocket name is required");
    }
    if (command.capacity() <= 0 || command.capacity() > 10) {
      throw new ValidationException("Rocket capacity must be between 1 and 10");
    }
  }
}

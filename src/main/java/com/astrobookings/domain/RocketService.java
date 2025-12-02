package com.astrobookings.domain;

import java.util.List;

import com.astrobookings.domain.models.BusinessErrorCode;
import com.astrobookings.domain.models.BusinessException;
import com.astrobookings.domain.models.CreateRocketCommand;
import com.astrobookings.domain.models.Rocket;

public class RocketService {
  private final RocketRepository rocketRepository;

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
    if (command.capacity() <= 0 || command.capacity() > 10) {
      throw new BusinessException(BusinessErrorCode.VALIDATION, "Rocket capacity must be between 1 and 10");
    }
  }
}

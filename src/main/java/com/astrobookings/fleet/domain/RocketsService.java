package com.astrobookings.fleet.domain;

import java.util.List;

import com.astrobookings.shared.domain.BusinessErrorCode;
import com.astrobookings.shared.domain.BusinessException;
import com.astrobookings.fleet.domain.models.CreateRocketCommand;
import com.astrobookings.fleet.domain.models.Rocket;
import com.astrobookings.fleet.domain.ports.input.RocketsUseCases;
import com.astrobookings.fleet.domain.ports.output.RocketRepository;

public class RocketsService implements RocketsUseCases {
  private final RocketRepository rocketRepository;

  public RocketsService(RocketRepository rocketRepository) {
    this.rocketRepository = rocketRepository;
  }

  public List<Rocket> getAllRockets() {
    return rocketRepository.findAll();
  }

  public Rocket saveRocket(CreateRocketCommand command) {
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

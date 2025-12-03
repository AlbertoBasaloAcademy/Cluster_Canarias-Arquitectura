package com.astrobookings.fleet.domain;

import java.util.List;

import com.astrobookings.fleet.domain.models.CreateRocketCommand;
import com.astrobookings.fleet.domain.models.Rocket;
import com.astrobookings.fleet.domain.ports.input.RocketsUseCases;
import com.astrobookings.fleet.domain.ports.output.RocketRepository;
import com.astrobookings.shared.domain.Capacity;

public class RocketsService implements RocketsUseCases {
  private final RocketRepository rocketRepository;

  public RocketsService(RocketRepository rocketRepository) {
    this.rocketRepository = rocketRepository;
  }

  public List<Rocket> getAllRockets() {
    return rocketRepository.findAll();
  }

  public Rocket saveRocket(CreateRocketCommand command) {
    Capacity capacity = Capacity.from(command.capacity());
    Rocket rocket = Rocket.register(command.name(), capacity, command.maxSpeed());
    return rocketRepository.save(rocket);
  }
}

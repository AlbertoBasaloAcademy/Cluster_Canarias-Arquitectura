package com.astrobookings.infrastructure.presentation;

import com.astrobookings.domain.RocketService;
import com.astrobookings.domain.ports.input.RocketsUseCases;
import com.astrobookings.domain.ports.output.RocketRepository;

public class UseCasesAdapterFactory {

  public static RocketsUseCases getRocketsUseCase(RocketRepository rocketRepository) {
    return new RocketService(rocketRepository);
  }

}

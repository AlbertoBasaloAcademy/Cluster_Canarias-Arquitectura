package com.astrobookings.infrastructure;

import java.io.IOException;
import java.util.List;

import com.astrobookings.domain.models.CreateRocketCommand;
import com.astrobookings.domain.models.Rocket;
import com.astrobookings.domain.ports.RocketsUseCases;

public class RocketHandlerUseCase implements RocketsUseCases {
  @Override
  public List<Rocket> getAllRockets() throws IOException {
    throw new IOException("Not implemented yet");
  }

  @Override
  public Rocket saveRocket(CreateRocketCommand command) throws IOException {
    throw new IOException("Not implemented yet");
  }
}

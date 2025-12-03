package com.astrobookings.fleet.domain.ports.input;

import java.io.IOException;
import java.util.List;

import com.astrobookings.fleet.domain.models.CreateRocketCommand;
import com.astrobookings.fleet.domain.models.Rocket;

public interface RocketsUseCases {
  List<Rocket> getAllRockets() throws IOException;

  Rocket saveRocket(CreateRocketCommand command) throws IOException;
}

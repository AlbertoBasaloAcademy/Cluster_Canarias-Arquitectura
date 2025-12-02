package com.astrobookings.domain.ports;

import java.io.IOException;
import java.util.List;

import com.astrobookings.domain.models.CreateRocketCommand;
import com.astrobookings.domain.models.Rocket;

public interface RocketsUseCases {
  List<Rocket> getAllRockets() throws IOException;

  Rocket saveRocket(CreateRocketCommand command) throws IOException;
}

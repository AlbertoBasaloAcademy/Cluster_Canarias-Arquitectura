package com.astrobookings.business;

import java.util.List;
import com.astrobookings.business.models.CreateRocketCommand;
import com.astrobookings.persistence.models.Rocket;

public interface RocketService {
  List<Rocket> getAll();
  Rocket create(CreateRocketCommand command);
}

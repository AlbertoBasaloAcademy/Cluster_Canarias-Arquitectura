package com.astrobookings.fleet.domain.ports.output;

import java.util.List;

import com.astrobookings.fleet.domain.models.Rocket;

public interface RocketRepository {
  List<Rocket> findAll();

  Rocket findById(String id);

  Rocket save(Rocket rocket);
}

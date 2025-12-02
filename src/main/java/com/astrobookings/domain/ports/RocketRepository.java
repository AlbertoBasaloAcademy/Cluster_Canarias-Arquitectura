package com.astrobookings.domain.ports;

import java.util.List;

import com.astrobookings.domain.models.Rocket;

public interface RocketRepository {
  List<Rocket> findAll();

  Rocket findById(String id);

  Rocket save(Rocket rocket);
}

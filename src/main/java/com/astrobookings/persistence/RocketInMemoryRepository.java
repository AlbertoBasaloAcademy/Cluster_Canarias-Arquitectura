package com.astrobookings.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.astrobookings.persistence.models.Rocket;

public class RocketInMemoryRepository implements RocketRepository {
  private static final Map<String, Rocket> rockets = new HashMap<>();
  private static int nextId = 1;

  static {
    // Pre-load one rocket
    var rocketId = "00000000-0000-0000-0000-000000000001";
    Rocket falcon9 = new Rocket(rocketId, "Falcon 9", 7, 27000.0);
    rockets.put(rocketId, falcon9);
    nextId = 2;
  }

  public List<Rocket> findAll() {
    return new ArrayList<>(rockets.values());
  }

  public Rocket findById(String id) {
    return rockets.get(id);
  }

  public Rocket save(Rocket rocket) {
    if (rocket.getId() == null) {
      rocket.setId("r" + nextId++);
    }
    rockets.put(rocket.getId(), rocket);
    return rocket;
  }
}
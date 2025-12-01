package com.astrobookings.persistence;

import java.util.List;
import com.astrobookings.persistence.models.Rocket;

public interface RocketRepository {
  List<Rocket> findAll();
  Rocket findById(String id);
  Rocket save(Rocket rocket);
}

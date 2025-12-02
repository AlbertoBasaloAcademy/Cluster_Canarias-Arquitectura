package com.astrobookings.domain.ports;

import java.util.List;

import com.astrobookings.domain.model.Rocket;

public interface RocketRepositoryPort {
    List<Rocket> findAll();
    Rocket save(Rocket rocket);
}
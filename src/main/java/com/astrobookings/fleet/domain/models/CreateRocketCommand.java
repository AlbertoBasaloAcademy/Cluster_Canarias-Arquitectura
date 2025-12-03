package com.astrobookings.fleet.domain.models;

public record CreateRocketCommand(String name, int capacity, Double maxSpeed) {
}

package com.astrobookings.domain.models;

public record CreateRocketCommand(String name, int capacity, Double maxSpeed) {
}

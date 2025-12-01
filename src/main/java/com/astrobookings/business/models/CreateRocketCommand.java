package com.astrobookings.business.models;

public record CreateRocketCommand(String name, int capacity, Double maxSpeed) {
}

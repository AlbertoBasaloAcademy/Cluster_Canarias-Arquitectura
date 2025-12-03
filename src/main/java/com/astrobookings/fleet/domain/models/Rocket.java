package com.astrobookings.fleet.domain.models;

import com.astrobookings.shared.domain.Capacity;

public class Rocket {
  private String id;
  private final String name;
  private final Capacity capacity;
  private final Double speed;

  private Rocket(String id, String name, Capacity capacity, Double speed) {
    this.id = id;
    this.name = name;
    this.capacity = capacity;
    this.speed = speed;
  }

  public static Rocket register(String name, Capacity capacity, Double speed) {
    return new Rocket(null, name, capacity, speed);
  }

  public static Rocket restore(String id, String name, Capacity capacity, Double speed) {
    return new Rocket(id, name, capacity, speed);
  }

  public String getId() {
    return id;
  }

  public void assignId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public int getCapacity() {
    return capacity.maxPassengers();
  }

  public Capacity capacity() {
    return capacity;
  }

  public Double getSpeed() {
    return speed;
  }
}

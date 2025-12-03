package com.astrobookings.fleet.domain.models;

import java.time.LocalDateTime;

import com.astrobookings.shared.domain.BusinessErrorCode;
import com.astrobookings.shared.domain.BusinessException;
import com.astrobookings.shared.domain.Capacity;

public class Flight {
  private String id;
  private final String rocketId;
  private final LocalDateTime departureDate;
  private final double basePrice;
  private FlightStatus status;
  private final int minPassengers;
  private final Capacity capacity;

  private Flight(String id, String rocketId, LocalDateTime departureDate, double basePrice, FlightStatus status,
      int minPassengers, Capacity capacity) {
    this.id = id;
    this.rocketId = rocketId;
    this.departureDate = departureDate;
    this.basePrice = basePrice;
    this.status = status;
    this.minPassengers = minPassengers;
    this.capacity = capacity;
  }

  public static Flight schedule(String rocketId, LocalDateTime departureDate, double basePrice, Capacity capacity,
      int minPassengers) {
    validateSchedule(departureDate, basePrice, minPassengers, capacity);
    return new Flight(null, rocketId, departureDate, basePrice, FlightStatus.SCHEDULED, minPassengers, capacity);
  }

  public static Flight restore(String id, String rocketId, LocalDateTime departureDate, double basePrice,
      FlightStatus status, int minPassengers, Capacity capacity) {
    return new Flight(id, rocketId, departureDate, basePrice, status, minPassengers, capacity);
  }

  private static void validateSchedule(LocalDateTime departureDate, double basePrice, int minPassengers,
      Capacity capacity) {
    if (basePrice <= 0) {
      throw new BusinessException(BusinessErrorCode.VALIDATION, "Base price must be positive");
    }
    if (departureDate == null) {
      throw new BusinessException(BusinessErrorCode.VALIDATION, "Departure date is required");
    }
    LocalDateTime now = LocalDateTime.now();
    if (!departureDate.isAfter(now)) {
      throw new BusinessException(BusinessErrorCode.VALIDATION, "Departure date must be in the future");
    }
    if (departureDate.isAfter(now.plusYears(1))) {
      throw new BusinessException(BusinessErrorCode.VALIDATION, "Departure date cannot exceed 1 year");
    }
    if (minPassengers <= 0) {
      throw new BusinessException(BusinessErrorCode.VALIDATION, "Min passengers must be positive");
    }
    if (minPassengers > capacity.maxPassengers()) {
      throw new BusinessException(BusinessErrorCode.VALIDATION,
          "Min passengers cannot exceed rocket capacity (" + capacity.maxPassengers() + ")");
    }
  }

  public boolean confirmIfMinReached(int currentPassengers) {
    if (status == FlightStatus.SCHEDULED && currentPassengers >= minPassengers) {
      status = FlightStatus.CONFIRMED;
      return true;
    }
    return false;
  }

  public void markSoldOut() {
    if (status != FlightStatus.CANCELLED) {
      status = FlightStatus.SOLD_OUT;
    }
  }

  public boolean canAcceptNewPassenger(int currentPassengers) {
    if (status == FlightStatus.CANCELLED || status == FlightStatus.SOLD_OUT) {
      return false;
    }
    return !capacity.isFull(currentPassengers);
  }

  public boolean cancelDueToLowDemand(int currentPassengers, LocalDateTime cutoffDate) {
    if (status != FlightStatus.SCHEDULED) {
      return false;
    }
    if (departureDate.isAfter(cutoffDate)) {
      return false;
    }
    if (currentPassengers >= minPassengers) {
      return false;
    }
    status = FlightStatus.CANCELLED;
    return true;
  }

  public String getId() {
    return id;
  }

  public void assignId(String id) {
    this.id = id;
  }

  public String getRocketId() {
    return rocketId;
  }

  public LocalDateTime getDepartureDate() {
    return departureDate;
  }

  public double getBasePrice() {
    return basePrice;
  }

  public FlightStatus getStatus() {
    return status;
  }

  public int getMinPassengers() {
    return minPassengers;
  }

  public Capacity capacity() {
    return capacity;
  }
}

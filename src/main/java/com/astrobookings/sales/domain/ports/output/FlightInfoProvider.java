package com.astrobookings.sales.domain.ports.output;

import com.astrobookings.shared.domain.Capacity;

/**
 * Port to access Flight information from the Fleet module.
 * This abstraction prevents direct coupling between Sales and Fleet modules.
 * Acts as an Anti-Corruption Layer.
 */
public interface FlightInfoProvider {

  /**
   * Get flight information by ID
   * 
   * @return FlightInfo containing necessary data for booking process
   */
  FlightInfo getFlightById(String flightId);

  /**
   * Get rocket capacity for a given flight
   */
  Capacity getRocketCapacityForFlight(String flightId);

  /**
   * Get current passenger count for a flight
   */
  int getCurrentPassengerCount(String flightId);

  /**
   * Mark flight as sold out by delegating to Fleet aggregate
   */
  void markFlightSoldOut(String flightId);

  /**
   * Ask Fleet aggregate to confirm if min passengers was reached
   */
  boolean confirmFlightIfMinReached(String flightId, int passengerCount);

  /**
   * Check if flight can accept more passengers
   */
  boolean canAcceptPassengers(String flightId);

  /**
   * Get flights that need cancellation (scheduled before given date with
   * insufficient passengers)
   */
  java.util.List<FlightInfo> getFlightsForCancellation(java.time.LocalDateTime cutoffDate, int minPassengers);

  /**
   * DTO to transfer flight information across module boundaries
   */
  /**
   * Ask Fleet aggregate to cancel flight when low demand rules apply
   */
  boolean cancelFlightIfLowDemand(String flightId, int currentPassengers, java.time.LocalDateTime cutoffDate);

  record FlightInfo(
      String id,
      String rocketId,
      java.time.LocalDateTime departureDate,
      double basePrice,
      String status,
      int minPassengers,
      Capacity capacity) {
  }
}

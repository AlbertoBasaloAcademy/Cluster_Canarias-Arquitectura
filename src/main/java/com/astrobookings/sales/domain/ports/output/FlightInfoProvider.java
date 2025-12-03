package com.astrobookings.sales.domain.ports.output;

/**
 * Port to access Flight information from the Fleet module.
 * This abstraction prevents direct coupling between Sales and Fleet modules.
 * Acts as an Anti-Corruption Layer.
 */
public interface FlightInfoProvider {
  
  /**
   * Get flight information by ID
   * @return FlightInfo containing necessary data for booking process
   */
  FlightInfo getFlightById(String flightId);

  /**
   * Get rocket capacity for a given flight
   */
  int getRocketCapacityForFlight(String flightId);

  /**
   * Get current passenger count for a flight
   */
  int getCurrentPassengerCount(String flightId);

  /**
   * Update flight status
   */
  void updateFlightStatus(String flightId, String status);

  /**
   * Check if flight can accept more passengers
   */
  boolean canAcceptPassengers(String flightId);

  /**
   * Get flights that need cancellation (scheduled before given date with insufficient passengers)
   */
  java.util.List<FlightInfo> getFlightsForCancellation(java.time.LocalDateTime cutoffDate, int minPassengers);

  /**
   * DTO to transfer flight information across module boundaries
   */
  record FlightInfo(
      String id,
      String rocketId,
      java.time.LocalDateTime departureDate,
      double basePrice,
      String status,
      int minPassengers,
      int capacity
  ) {}
}

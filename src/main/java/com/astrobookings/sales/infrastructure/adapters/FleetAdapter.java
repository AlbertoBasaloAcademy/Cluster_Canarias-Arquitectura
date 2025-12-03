package com.astrobookings.sales.infrastructure.adapters;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import com.astrobookings.fleet.domain.models.Flight;
import com.astrobookings.fleet.domain.models.FlightStatus;
import com.astrobookings.fleet.domain.models.Rocket;
import com.astrobookings.fleet.domain.ports.output.FlightRepository;
import com.astrobookings.fleet.domain.ports.output.RocketRepository;
import com.astrobookings.sales.domain.ports.output.BookingRepository;
import com.astrobookings.sales.domain.ports.output.FlightInfoProvider;

/**
 * Adapter that connects Sales module to Fleet module.
 * Implements FlightInfoProvider port using Fleet's repositories.
 * Acts as an Anti-Corruption Layer between the two bounded contexts.
 * This is the Sales mechanism to access the Fleet, without knowing the details.
 */
public class FleetAdapter implements FlightInfoProvider {
  private final FlightRepository flightRepository;
  private final RocketRepository rocketRepository;
  private final BookingRepository bookingRepository;

  public FleetAdapter(
      FlightRepository flightRepository,
      RocketRepository rocketRepository,
      BookingRepository bookingRepository) {
    this.flightRepository = flightRepository;
    this.rocketRepository = rocketRepository;
    this.bookingRepository = bookingRepository;
  }

  @Override
  public FlightInfo getFlightById(String flightId) {
    Flight flight = flightRepository.findById(flightId);
    if (flight == null) {
      return null;
    }
    Rocket rocket = rocketRepository.findById(flight.getRocketId());
    int capacity = rocket != null ? rocket.getCapacity() : 0;
    
    return new FlightInfo(
        flight.getId(),
        flight.getRocketId(),
        flight.getDepartureDate(),
        flight.getBasePrice(),
        flight.getStatus().name(),
        flight.getMinPassengers(),
        capacity
    );
  }

  @Override
  public int getRocketCapacityForFlight(String flightId) {
    Flight flight = flightRepository.findById(flightId);
    if (flight == null) {
      return 0;
    }
    Rocket rocket = rocketRepository.findById(flight.getRocketId());
    return rocket != null ? rocket.getCapacity() : 0;
  }

  @Override
  public int getCurrentPassengerCount(String flightId) {
    return bookingRepository.findByFlightId(flightId).size();
  }

  @Override
  public void updateFlightStatus(String flightId, String status) {
    Flight flight = flightRepository.findById(flightId);
    if (flight != null) {
      flight.setStatus(FlightStatus.valueOf(status));
      flightRepository.save(flight);
    }
  }

  @Override
  public boolean canAcceptPassengers(String flightId) {
    Flight flight = flightRepository.findById(flightId);
    if (flight == null) {
      return false;
    }
    if (flight.getStatus() == FlightStatus.CANCELLED || flight.getStatus() == FlightStatus.SOLD_OUT) {
      return false;
    }
    Rocket rocket = rocketRepository.findById(flight.getRocketId());
    if (rocket == null) {
      return false;
    }
    int currentBookings = getCurrentPassengerCount(flightId);
    return currentBookings < rocket.getCapacity();
  }

  @Override
  public List<FlightInfo> getFlightsForCancellation(LocalDateTime cutoffDate, int minPassengers) {
    List<Flight> allFlights = flightRepository.findAll();
    LocalDateTime now = LocalDateTime.now();
    
    return allFlights.stream()
        .filter(flight -> flight.getStatus() == FlightStatus.SCHEDULED)
        .filter(flight -> {
          long daysUntilDeparture = ChronoUnit.DAYS.between(now, flight.getDepartureDate());
          return daysUntilDeparture <= 7;
        })
        .map(flight -> {
          Rocket rocket = rocketRepository.findById(flight.getRocketId());
          int capacity = rocket != null ? rocket.getCapacity() : 0;
          return new FlightInfo(
              flight.getId(),
              flight.getRocketId(),
              flight.getDepartureDate(),
              flight.getBasePrice(),
              flight.getStatus().name(),
              flight.getMinPassengers(),
              capacity
          );
        })
        .collect(Collectors.toList());
  }
}

package com.astrobookings.sales.infrastructure.adapters;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.astrobookings.fleet.domain.models.Flight;
import com.astrobookings.fleet.domain.models.FlightStatus;
import com.astrobookings.fleet.domain.ports.output.FlightRepository;
import com.astrobookings.sales.domain.ports.output.BookingRepository;
import com.astrobookings.sales.domain.ports.output.FlightInfoProvider;
import com.astrobookings.shared.domain.Capacity;

/**
 * Adapter that connects Sales module to Fleet module.
 * Implements FlightInfoProvider port using Fleet's repositories.
 * Acts as an Anti-Corruption Layer between the two bounded contexts.
 * This is the Sales mechanism to access the Fleet, without knowing the details.
 */
public class FleetAdapter implements FlightInfoProvider {
  private final FlightRepository flightRepository;
  private final BookingRepository bookingRepository;

  public FleetAdapter(
      FlightRepository flightRepository,
      BookingRepository bookingRepository) {
    this.flightRepository = flightRepository;
    this.bookingRepository = bookingRepository;
  }

  @Override
  public FlightInfo getFlightById(String flightId) {
    Flight flight = flightRepository.findById(flightId);
    if (flight == null) {
      return null;
    }
    return new FlightInfo(
        flight.getId(),
        flight.getRocketId(),
        flight.getDepartureDate(),
        flight.getBasePrice(),
        flight.getStatus().name(),
        flight.getMinPassengers(),
        flight.capacity());
  }

  @Override
  public Capacity getRocketCapacityForFlight(String flightId) {
    Flight flight = flightRepository.findById(flightId);
    if (flight == null) {
      return null;
    }
    return flight.capacity();
  }

  @Override
  public int getCurrentPassengerCount(String flightId) {
    return bookingRepository.findByFlightId(flightId).size();
  }

  @Override
  public void markFlightSoldOut(String flightId) {
    Flight flight = flightRepository.findById(flightId);
    if (flight != null) {
      flight.markSoldOut();
      flightRepository.save(flight);
    }
  }

  @Override
  public boolean confirmFlightIfMinReached(String flightId, int passengerCount) {
    Flight flight = flightRepository.findById(flightId);
    if (flight == null) {
      return false;
    }
    boolean confirmed = flight.confirmIfMinReached(passengerCount);
    if (confirmed) {
      flightRepository.save(flight);
    }
    return confirmed;
  }

  @Override
  public boolean canAcceptPassengers(String flightId) {
    Flight flight = flightRepository.findById(flightId);
    if (flight == null) {
      return false;
    }
    int currentBookings = getCurrentPassengerCount(flightId);
    return flight.canAcceptNewPassenger(currentBookings);
  }

  @Override
  public List<FlightInfo> getFlightsForCancellation(LocalDateTime cutoffDate, int minPassengers) {
    List<Flight> allFlights = flightRepository.findAll();

    return allFlights.stream()
        .filter(flight -> flight.getStatus() == FlightStatus.SCHEDULED)
        .filter(flight -> !flight.getDepartureDate().isAfter(cutoffDate))
        .filter(flight -> flight.getMinPassengers() >= minPassengers)
        .map(flight -> new FlightInfo(
            flight.getId(),
            flight.getRocketId(),
            flight.getDepartureDate(),
            flight.getBasePrice(),
            flight.getStatus().name(),
            flight.getMinPassengers(),
            flight.capacity()))
        .collect(Collectors.toList());
  }

  @Override
  public boolean cancelFlightIfLowDemand(String flightId, int currentPassengers, LocalDateTime cutoffDate) {
    Flight flight = flightRepository.findById(flightId);
    if (flight == null) {
      return false;
    }
    boolean cancelled = flight.cancelDueToLowDemand(currentPassengers, cutoffDate);
    if (cancelled) {
      flightRepository.save(flight);
    }
    return cancelled;
  }
}

package com.astrobookings.business;

import java.time.LocalDateTime;
import java.util.List;

import com.astrobookings.business.models.CreateFlightCommand;
import com.astrobookings.business.models.NotFoundException;
import com.astrobookings.business.models.ValidationException;
import com.astrobookings.persistence.FlightRepository;
import com.astrobookings.persistence.RocketRepository;
import com.astrobookings.persistence.models.Flight;
import com.astrobookings.persistence.models.FlightStatus;
import com.astrobookings.persistence.models.Rocket;

public class FlightService {
  private final FlightRepository flightRepository;
  private final RocketRepository rocketRepository;

  public FlightService() {
    this(new FlightRepository(), new RocketRepository());
  }

  public FlightService(FlightRepository flightRepository, RocketRepository rocketRepository) {
    this.flightRepository = flightRepository;
    this.rocketRepository = rocketRepository;
  }

  public List<Flight> getFlights(String statusFilter) {
    if (statusFilter != null && !statusFilter.isEmpty()) {
      return flightRepository.findByStatus(statusFilter);
    } else {
      return flightRepository.findAll();
    }
  }

  public Flight createFlight(CreateFlightCommand command) {
    Flight flight = new Flight();
    flight.setRocketId(command.rocketId());
    flight.setDepartureDate(command.departureDate());
    flight.setBasePrice(command.basePrice());
    flight.setStatus(FlightStatus.SCHEDULED);
    flight.setMinPassengers(command.minPassengers() == null ? 5 : command.minPassengers());

    validateFlight(flight);

    return flightRepository.save(flight);
  }

  private void validateFlight(Flight flight) {
    if (flight.getRocketId() == null || flight.getRocketId().isBlank()) {
      throw new ValidationException("Rocket ID must be provided");
    }
    if (flight.getDepartureDate() == null) {
      throw new ValidationException("Departure date must be provided");
    }
    if (flight.getBasePrice() <= 0) {
      throw new ValidationException("Base price must be positive");
    }
    if (flight.getMinPassengers() <= 0) {
      throw new ValidationException("Min passengers must be positive");
    }

    Rocket rocket = rocketRepository.findById(flight.getRocketId());
    if (rocket == null) {
      throw new NotFoundException("Rocket with id " + flight.getRocketId() + " does not exist");
    }

    LocalDateTime now = LocalDateTime.now();
    if (!flight.getDepartureDate().isAfter(now)) {
      throw new ValidationException("Departure date must be in the future");
    }

    LocalDateTime oneYearAhead = now.plusYears(1);
    if (flight.getDepartureDate().isAfter(oneYearAhead)) {
      throw new ValidationException("Departure date cannot be more than 1 year ahead");
    }
  }
}
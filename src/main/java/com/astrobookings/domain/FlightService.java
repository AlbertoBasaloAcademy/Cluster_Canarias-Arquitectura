package com.astrobookings.domain;

import java.time.LocalDateTime;
import java.util.List;

import com.astrobookings.domain.models.BusinessErrorCode;
import com.astrobookings.domain.models.BusinessException;
import com.astrobookings.domain.models.CreateFlightCommand;
import com.astrobookings.domain.models.Flight;
import com.astrobookings.domain.models.FlightStatus;
import com.astrobookings.domain.models.Rocket;
import com.astrobookings.domain.ports.output.FlightRepository;
import com.astrobookings.domain.ports.output.RocketRepository;

public class FlightService {
  private final FlightRepository flightRepository;
  private final RocketRepository rocketRepository;
  private static final int DEFAULT_MIN_PASSENGERS = 5;

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
    flight.setMinPassengers(Math.max(command.minPassengers(), DEFAULT_MIN_PASSENGERS));

    validateFlight(flight);

    return flightRepository.save(flight);
  }

  private void validateFlight(Flight flight) {
    if (flight.getBasePrice() <= 0) {
      throw new BusinessException(BusinessErrorCode.VALIDATION, "Base price must be positive");
    }
    if (flight.getMinPassengers() <= 0 || flight.getMinPassengers() > 10) {
      throw new BusinessException(BusinessErrorCode.VALIDATION, "Min passengers must be between 1 and 10");
    }

    Rocket rocket = rocketRepository.findById(flight.getRocketId());
    if (rocket == null) {
      throw new BusinessException(BusinessErrorCode.NOT_FOUND,
          "Rocket with id " + flight.getRocketId() + " does not exist");
    }

    LocalDateTime now = LocalDateTime.now();
    if (!flight.getDepartureDate().isAfter(now)) {
      throw new BusinessException(BusinessErrorCode.VALIDATION, "Departure date must be in the future");
    }

    LocalDateTime oneYearAhead = now.plusYears(1);
    if (flight.getDepartureDate().isAfter(oneYearAhead)) {
      throw new BusinessException(BusinessErrorCode.VALIDATION, "Departure date cannot be more than 1 year ahead");
    }
  }
}
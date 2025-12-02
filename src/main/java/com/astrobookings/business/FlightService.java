package com.astrobookings.business;

import java.time.LocalDateTime;
import java.util.List;

import com.astrobookings.business.models.BusinessErrorCode;
import com.astrobookings.business.models.BusinessException;
import com.astrobookings.business.models.CreateFlightCommand;
import com.astrobookings.persistence.FlightRepository;
import com.astrobookings.persistence.RepositoryFactory;
import com.astrobookings.persistence.RocketRepository;
import com.astrobookings.persistence.models.Flight;
import com.astrobookings.persistence.models.FlightStatus;
import com.astrobookings.persistence.models.Rocket;

public class FlightService {
  private final FlightRepository flightRepository = RepositoryFactory.getFlightRepository();
  private final RocketRepository rocketRepository = RepositoryFactory.getRocketRepository();
  private static final int DEFAULT_MIN_PASSENGERS = 5;

  public FlightService() {
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
package com.astrobookings.fleet.domain;

import java.util.List;

import com.astrobookings.fleet.domain.models.CreateFlightCommand;
import com.astrobookings.fleet.domain.models.Flight;
import com.astrobookings.fleet.domain.models.Rocket;
import com.astrobookings.fleet.domain.ports.output.FlightRepository;
import com.astrobookings.fleet.domain.ports.output.RocketRepository;
import com.astrobookings.shared.domain.BusinessErrorCode;
import com.astrobookings.shared.domain.BusinessException;

public class FlightsService implements com.astrobookings.fleet.domain.ports.input.FlightsUseCases {
  private final FlightRepository flightRepository;
  private final RocketRepository rocketRepository;
  private static final int DEFAULT_MIN_PASSENGERS = 5;

  public FlightsService(FlightRepository flightRepository, RocketRepository rocketRepository) {
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
    Rocket rocket = rocketRepository.findById(command.rocketId());
    if (rocket == null) {
      throw new BusinessException(BusinessErrorCode.NOT_FOUND,
          "Rocket with id " + command.rocketId() + " does not exist");
    }

    int requestedMin = command.minPassengers() == null || command.minPassengers() <= 0
        ? DEFAULT_MIN_PASSENGERS
        : command.minPassengers();
    int effectiveMin = Math.min(requestedMin, rocket.capacity().maxPassengers());

    Flight flight = Flight.schedule(
        command.rocketId(),
        command.departureDate(),
        command.basePrice(),
        rocket.capacity(),
        effectiveMin);

    return flightRepository.save(flight);
  }
}

package com.astrobookings.fleet.infrastructure;

import com.astrobookings.fleet.domain.FlightsService;
import com.astrobookings.fleet.domain.RocketsService;
import com.astrobookings.fleet.domain.ports.input.FlightsUseCases;
import com.astrobookings.fleet.domain.ports.input.RocketsUseCases;
import com.astrobookings.fleet.domain.ports.output.FlightRepository;
import com.astrobookings.fleet.domain.ports.output.RocketRepository;
import com.astrobookings.fleet.infrastructure.persistence.FlightInMemoryRepository;
import com.astrobookings.fleet.infrastructure.persistence.RocketInMemoryRepository;
import com.astrobookings.fleet.infrastructure.presentation.FlightsHandler;
import com.astrobookings.fleet.infrastructure.presentation.RocketsHandler;

/**
 * Factory that assembles the Fleet module (Supporting Subdomain).
 * Manages rockets and flights - the operational assets and scheduling.
 */
public class FleetFactory {
  // Repositories (singletons)
  private final RocketRepository rocketRepository;
  private final FlightRepository flightRepository;

  // Use Cases / Services
  private final RocketsUseCases rocketsUseCases;
  private final FlightsUseCases flightsUseCases;

  // Handlers
  private final RocketsHandler rocketsHandler;
  private final FlightsHandler flightsHandler;

  public FleetFactory() {
    // Create persistence adapters
    this.rocketRepository = new RocketInMemoryRepository();
    this.flightRepository = new FlightInMemoryRepository();

    // Create domain services
    this.rocketsUseCases = new RocketsService(rocketRepository);
    this.flightsUseCases = new FlightsService(flightRepository, rocketRepository);

    // Create presentation adapters
    this.rocketsHandler = new RocketsHandler(rocketsUseCases);
    this.flightsHandler = new FlightsHandler(flightsUseCases);
  }

  // Public API for other modules
  public RocketsUseCases getRocketsUseCases() {
    return rocketsUseCases;
  }

  public FlightsUseCases getFlightsUseCases() {
    return flightsUseCases;
  }

  public RocketRepository getRocketRepository() {
    return rocketRepository;
  }

  public FlightRepository getFlightRepository() {
    return flightRepository;
  }

  // Handlers for HTTP server registration
  public RocketsHandler getRocketsHandler() {
    return rocketsHandler;
  }

  public FlightsHandler getFlightsHandler() {
    return flightsHandler;
  }
}

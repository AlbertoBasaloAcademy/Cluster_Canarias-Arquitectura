package com.astrobookings.fleet.domain.ports.input;

import java.util.List;

import com.astrobookings.fleet.domain.models.CreateFlightCommand;
import com.astrobookings.fleet.domain.models.Flight;

public interface FlightsUseCases {
  List<Flight> getFlights(String statusFilter);

  Flight createFlight(CreateFlightCommand command);

}

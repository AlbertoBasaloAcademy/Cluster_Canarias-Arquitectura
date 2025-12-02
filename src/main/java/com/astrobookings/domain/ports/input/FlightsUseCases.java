package com.astrobookings.domain.ports.input;

import java.util.List;

import com.astrobookings.domain.models.CreateFlightCommand;
import com.astrobookings.domain.models.Flight;

public interface FlightsUseCases {
  List<Flight> getFlights(String statusFilter);

  Flight createFlight(CreateFlightCommand command);

}

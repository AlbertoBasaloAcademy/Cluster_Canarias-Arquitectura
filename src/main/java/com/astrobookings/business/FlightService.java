package com.astrobookings.business;

import java.util.List;
import com.astrobookings.business.models.CreateFlightCommand;
import com.astrobookings.persistence.models.Flight;

public interface FlightService {
  List<Flight> getFlights(String statusFilter);
  Flight createFlight(CreateFlightCommand command);
}

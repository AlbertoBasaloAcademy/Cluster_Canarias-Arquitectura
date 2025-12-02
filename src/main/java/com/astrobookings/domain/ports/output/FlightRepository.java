package com.astrobookings.domain.ports.output;

import java.util.List;

import com.astrobookings.domain.models.Flight;

public interface FlightRepository {
  List<Flight> findAll();

  Flight findById(String id);

  List<Flight> findByStatus(String status);

  Flight save(Flight flight);
}

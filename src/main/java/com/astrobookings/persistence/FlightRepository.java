package com.astrobookings.persistence;

import java.util.List;
import com.astrobookings.persistence.models.Flight;

public interface FlightRepository {
  List<Flight> findAll();
  Flight findById(String id);
  List<Flight> findByStatus(String status);
  Flight save(Flight flight);
}

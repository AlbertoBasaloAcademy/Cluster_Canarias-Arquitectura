package com.astrobookings.fleet.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.astrobookings.fleet.domain.models.Flight;
import com.astrobookings.fleet.domain.models.FlightStatus;
import com.astrobookings.fleet.domain.ports.output.FlightRepository;
import com.astrobookings.shared.domain.Capacity;

public class FlightInMemoryRepository implements FlightRepository {
  private static final Map<String, Flight> flights = new HashMap<>();
  private static int nextId = 1;

  static {
    var rocketId = "r1";
    // Pre-load flights
    var flight1Id = "f1";
    Flight flight1 = Flight.restore(flight1Id, rocketId, LocalDateTime.of(2026, 6, 1, 10, 0),
        1000.0, FlightStatus.SCHEDULED, 5, Capacity.from(7));
    flights.put(flight1Id, flight1);

    var flight2Id = "f2";
    Flight flight2 = Flight.restore(flight2Id, rocketId, LocalDateTime.of(2026, 12, 1, 10, 0),
        2000.0, FlightStatus.CANCELLED, 5, Capacity.from(7));
    flights.put(flight2Id, flight2);

    nextId = 3;
  }

  public List<Flight> findAll() {
    LocalDateTime now = LocalDateTime.now();
    return flights.values().stream()
        .filter(flight -> flight.getDepartureDate().isAfter(now))
        .collect(Collectors.toList());
  }

  public Flight findById(String id) {
    return flights.get(id);
  }

  public List<Flight> findByStatus(String status) {
    try {
      FlightStatus flightStatus = FlightStatus.valueOf(status.toUpperCase());
      LocalDateTime now = LocalDateTime.now();
      return flights.values().stream()
          .filter(flight -> flight.getDepartureDate().isAfter(now) && flight.getStatus() == flightStatus)
          .collect(Collectors.toList());
    } catch (IllegalArgumentException e) {
      return new ArrayList<>();
    }
  }

  public Flight save(Flight flight) {
    if (flight.getId() == null) {
      flight.assignId("f" + nextId++);
    }
    flights.put(flight.getId(), flight);
    return flight;
  }
}

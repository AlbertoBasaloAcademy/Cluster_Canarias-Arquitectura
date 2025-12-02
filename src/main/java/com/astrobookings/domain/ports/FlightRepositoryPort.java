package com.astrobookings.domain.ports;

import java.util.List;

import com.astrobookings.domain.model.Flight;

public interface FlightRepositoryPort {

    List<Flight> findAll();

    List<Flight> findByStatus(String status);

    Flight save(Flight flight);

}
package com.astrobookings.domain.ports;

import java.util.List;

import com.astrobookings.domain.model.Booking;

public interface BookingRepositoryPort {
    List<Booking> findAll();
    List<Booking> findByFlightId(String flightId);
    List<Booking> findByPassengerName(String passengerName);
    Booking save(Booking booking);
}
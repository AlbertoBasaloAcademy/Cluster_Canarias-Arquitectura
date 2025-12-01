package com.astrobookings.persistence;

import com.astrobookings.persistence.models.Booking;
import java.util.List;

public interface BookingRepositoryPort {
    List<Booking> findAll();
    List<Booking> findByFlightId(String flightId);
    List<Booking> findByPassengerName(String passengerName);
    Booking save(Booking booking);
}
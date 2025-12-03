package com.astrobookings.sales.domain.ports.output;

import java.util.List;

import com.astrobookings.sales.domain.models.Booking;

public interface BookingRepository {
  List<Booking> findAll();

  List<Booking> findByFlightId(String flightId);

  List<Booking> findByPassengerName(String passengerName);

  Booking save(Booking booking);
}

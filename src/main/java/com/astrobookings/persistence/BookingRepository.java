package com.astrobookings.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.astrobookings.persistence.models.Booking;

public class BookingRepository {
  private static final Map<String, Booking> bookings = new HashMap<>();
  private static int nextId = 1;

  public List<Booking> findAll() {
    return new ArrayList<>(bookings.values());
  }

  public List<Booking> findByFlightId(String flightId) {
    return bookings.values().stream()
        .filter(booking -> booking.getFlightId().equals(flightId))
        .collect(Collectors.toList());
  }

  public List<Booking> findByPassengerName(String passengerName) {
    return bookings.values().stream()
        .filter(booking -> booking.getPassengerName().equalsIgnoreCase(passengerName))
        .collect(Collectors.toList());
  }

  public Booking save(Booking booking) {
    if (booking.getId() == null) {
      booking.setId("b" + nextId++);
    }
    bookings.put(booking.getId(), booking);
    return booking;
  }
}
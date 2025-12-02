package com.astrobookings.domain.ports.input;

import java.util.List;

import com.astrobookings.domain.models.Booking;
import com.astrobookings.domain.models.CreateBookingCommand;

public interface BookingsUseCases {
  Booking createBooking(CreateBookingCommand command);

  List<Booking> getBookings(String flightId, String passengerName);
}

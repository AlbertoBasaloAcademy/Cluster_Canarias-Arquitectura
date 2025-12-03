package com.astrobookings.sales.domain.ports.input;

import java.util.List;

import com.astrobookings.sales.domain.models.Booking;
import com.astrobookings.sales.domain.models.CreateBookingCommand;

public interface BookingsUseCases {
  Booking createBooking(CreateBookingCommand command);

  List<Booking> getBookings(String flightId, String passengerName);
}

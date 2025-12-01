package com.astrobookings.business;

import java.util.List;
import com.astrobookings.business.models.CreateBookingCommand;
import com.astrobookings.persistence.models.Booking;

public interface BookingService {
  Booking createBooking(CreateBookingCommand command);
  List<Booking> getBookings(String flightId, String passengerName);
}

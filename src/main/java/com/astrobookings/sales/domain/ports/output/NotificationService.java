package com.astrobookings.sales.domain.ports.output;

import java.util.List;

import com.astrobookings.sales.domain.models.Booking;

public interface NotificationService {
  void notifyConfirmation(String flightId, int passengerCount);

  void notifyCancellation(String flightId, int passengerCount);

  void notifyCancellation(String flightId, List<Booking> bookings);
}

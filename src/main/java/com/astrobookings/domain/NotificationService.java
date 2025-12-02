package com.astrobookings.domain;

import java.util.List;
import com.astrobookings.domain.models.Booking;

public interface NotificationService {
  void notifyConfirmation(String flightId, int passengerCount);
  void notifyCancellation(String flightId, int passengerCount);
  void notifyCancellation(String flightId, List<Booking> bookings);
}

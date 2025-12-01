package com.astrobookings.business;

import java.util.List;
import com.astrobookings.persistence.models.Booking;

public interface NotificationService {
  void notifyConfirmation(String flightId, int passengerCount);
  void notifyCancellation(String flightId, int passengerCount);
  void notifyCancellation(String flightId, List<Booking> bookings);
}

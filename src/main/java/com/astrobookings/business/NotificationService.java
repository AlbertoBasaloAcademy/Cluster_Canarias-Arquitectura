package com.astrobookings.business;

public class NotificationService {
  public static void notifyConfirmation(String flightId, int passengerCount) {
    System.out.println(
        "[NOTIFICATION SERVICE] Flight " + flightId + " CONFIRMED - Notifying " + passengerCount + " passenger(s)");
  }

  public static void notifyCancellation(String flightId, int passengerCount) {
    System.out.println(
        "[NOTIFICATION SERVICE] Flight " + flightId + " CANCELLED - Notifying " + passengerCount + " passenger(s)");
  }
}
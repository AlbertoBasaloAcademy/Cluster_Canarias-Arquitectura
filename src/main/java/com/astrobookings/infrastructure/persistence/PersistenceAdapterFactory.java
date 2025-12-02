package com.astrobookings.infrastructure.persistence;

import com.astrobookings.domain.ports.output.BookingRepository;
import com.astrobookings.domain.ports.output.FlightRepository;
import com.astrobookings.domain.ports.output.NotificationService;
import com.astrobookings.domain.ports.output.PaymentGateway;
import com.astrobookings.domain.ports.output.RocketRepository;

public class PersistenceAdapterFactory {
  private static final RocketRepository rocketRepository = new RocketInMemoryRepository();
  private static final FlightRepository flightRepository = new FlightInMemoryRepository();
  private static final BookingRepository bookingRepository = new BookingInMemoryRepository();
  private static final PaymentGateway paymentGateway = new PaymentConsoleGateway();
  private static final NotificationService notificationService = new NotificationConsoleService();

  public static RocketRepository getRocketRepository() {
    return rocketRepository;
  }

  public static FlightRepository getFlightRepository() {
    return flightRepository;
  }

  public static BookingRepository getBookingRepository() {
    return bookingRepository;
  }

  public static PaymentGateway getPaymentGateway() {
    return paymentGateway;
  }

  public static NotificationService getNotificationService() {
    return notificationService;
  }
}

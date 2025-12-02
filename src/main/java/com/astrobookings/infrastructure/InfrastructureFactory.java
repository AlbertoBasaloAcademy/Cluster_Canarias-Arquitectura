package com.astrobookings.infrastructure;

import com.astrobookings.domain.BookingRepository;
import com.astrobookings.domain.FlightRepository;
import com.astrobookings.domain.RocketRepository;
import com.astrobookings.domain.PaymentGateway;
import com.astrobookings.domain.NotificationService;

public class InfrastructureFactory {
  private static final RocketRepository rocketRepository = new RocketInMemoryRepository();
  private static final FlightRepository flightRepository = new FlightInMemoryRepository();
  private static final BookingRepository bookingRepository = new BookingInMemoryRepository();
  private static final PaymentGateway paymentGateway = new PaymentGatewayAdapter();
  private static final NotificationService notificationService = new NotificationServiceAdapter();

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

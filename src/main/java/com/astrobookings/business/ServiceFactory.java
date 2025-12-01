package com.astrobookings.business;

import com.astrobookings.persistence.RepositoryFactory;

public class ServiceFactory {
  private static final RocketService rocketService = new RocketServiceImpl(
      RepositoryFactory.getRocketRepository());

  private static final FlightService flightService = new FlightServiceImpl(
      RepositoryFactory.getFlightRepository(),
      RepositoryFactory.getRocketRepository());

  private static final BookingService bookingService = new BookingServiceImpl(
      RepositoryFactory.getBookingRepository(),
      RepositoryFactory.getFlightRepository(),
      RepositoryFactory.getRocketRepository(),
      InfrastructureFactory.getPaymentGateway(),
      InfrastructureFactory.getNotificationService());

  private static final CancellationService cancellationService = new CancellationServiceImpl(
      RepositoryFactory.getFlightRepository(),
      RepositoryFactory.getBookingRepository(),
      InfrastructureFactory.getPaymentGateway(),
      InfrastructureFactory.getNotificationService());

  public static RocketService getRocketService() {
    return rocketService;
  }

  public static FlightService getFlightService() {
    return flightService;
  }

  public static BookingService getBookingService() {
    return bookingService;
  }

  public static CancellationService getCancellationService() {
    return cancellationService;
  }
}

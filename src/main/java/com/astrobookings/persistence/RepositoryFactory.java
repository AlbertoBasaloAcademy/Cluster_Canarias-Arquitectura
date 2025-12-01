package com.astrobookings.persistence;

public class RepositoryFactory {
  private static final RocketRepository rocketRepository = new RocketRepositoryImpl();
  private static final FlightRepository flightRepository = new FlightRepositoryImpl();
  private static final BookingRepository bookingRepository = new BookingRepositoryImpl();

  public static RocketRepository getRocketRepository() {
    return rocketRepository;
  }

  public static FlightRepository getFlightRepository() {
    return flightRepository;
  }

  public static BookingRepository getBookingRepository() {
    return bookingRepository;
  }
}

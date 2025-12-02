package com.astrobookings.persistence;

public class RepositoryFactory {
  private static final RocketRepository rocketRepository = new RocketInMemoryRepository();
  private static final FlightRepository flightRepository = new FlightInMemoryRepository();
  private static final BookingRepository bookingRepository = new BookingInMemoryRepository();

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

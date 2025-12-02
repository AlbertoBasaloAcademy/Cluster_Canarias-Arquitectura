package com.astrobookings;

import com.astrobookings.domain.ports.output.BookingRepository;
import com.astrobookings.domain.ports.output.FlightRepository;
import com.astrobookings.domain.ports.output.RocketRepository;
import com.astrobookings.infrastructure.persistence.PersistenceAdapterFactory;

public class HttpConfiguration {
  private static final RocketRepository rocketRepository = PersistenceAdapterFactory.getRocketRepository();
  private static final FlightRepository flightRepository = PersistenceAdapterFactory.getFlightRepository();
  private static final BookingRepository bookingRepository = PersistenceAdapterFactory.getBookingRepository();
  // To do : use cases factory
}

package com.astrobookings.infrastructure.presentation;

import com.astrobookings.domain.BookingsService;
import com.astrobookings.domain.CancellationService;
import com.astrobookings.domain.FlightsService;
import com.astrobookings.domain.RocketsService;
import com.astrobookings.domain.ports.input.BookingsUseCases;
import com.astrobookings.domain.ports.input.FlightsUseCases;
import com.astrobookings.domain.ports.input.RocketsUseCases;
import com.astrobookings.domain.ports.output.BookingRepository;
import com.astrobookings.domain.ports.output.FlightRepository;
import com.astrobookings.domain.ports.output.NotificationService;
import com.astrobookings.domain.ports.output.PaymentGateway;
import com.astrobookings.domain.ports.output.RocketRepository;

public class UseCasesAdapterFactory {

  public static RocketsUseCases getRocketsUseCase(RocketRepository rocketRepository) {
    return new RocketsService(rocketRepository);
  }

  public static FlightsUseCases getFlightsUseCase(FlightRepository flightRepository,
      RocketRepository rocketRepository) {
    return new FlightsService(flightRepository, rocketRepository);
  }

  public static BookingsUseCases getBookingsUseCase(BookingRepository bookingRepository,
      FlightRepository flightRepository,
      RocketRepository rocketRepository,
      PaymentGateway paymentGateway,
      NotificationService notificationService) {
    return new BookingsService(
        bookingRepository,
        flightRepository,
        rocketRepository,
        paymentGateway,
        notificationService);
  }

  public static CancellationService getCancellationService(FlightRepository flightRepository,
      BookingRepository bookingRepository,
      PaymentGateway paymentGateway,
      NotificationService notificationService) {
    return new CancellationService(
        flightRepository,
        bookingRepository,
        paymentGateway,
        notificationService);
  }

}

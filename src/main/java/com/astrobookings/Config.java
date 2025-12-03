package com.astrobookings;

import com.astrobookings.domain.ports.input.BookingsUseCases;
import com.astrobookings.domain.ports.input.CancellationUseCases;
import com.astrobookings.domain.ports.input.FlightsUseCases;
import com.astrobookings.domain.ports.input.RocketsUseCases;
import com.astrobookings.domain.ports.output.BookingRepository;
import com.astrobookings.domain.ports.output.FlightRepository;
import com.astrobookings.domain.ports.output.NotificationService;
import com.astrobookings.domain.ports.output.PaymentGateway;
import com.astrobookings.domain.ports.output.RocketRepository;
import com.astrobookings.infrastructure.persistence.PersistenceAdapterFactory;
import com.astrobookings.infrastructure.presentation.UseCasesAdapterFactory;

public class Config {
  static final RocketRepository rocketRepository = PersistenceAdapterFactory.getRocketRepository();
  static final FlightRepository flightRepository = PersistenceAdapterFactory.getFlightRepository();
  static final BookingRepository bookingRepository = PersistenceAdapterFactory.getBookingRepository();
  static final PaymentGateway paymentGateway = PersistenceAdapterFactory.getPaymentGateway();
  static final NotificationService notificationService = PersistenceAdapterFactory.getNotificationService();

  static final RocketsUseCases rocketUseCase = UseCasesAdapterFactory.getRocketsUseCase(rocketRepository);
  static final FlightsUseCases flightUseCase = UseCasesAdapterFactory.getFlightsUseCase(
      flightRepository, rocketRepository);
  static final BookingsUseCases bookingUseCase = UseCasesAdapterFactory.getBookingsUseCase(
      bookingRepository, flightRepository, rocketRepository,
      paymentGateway,
      notificationService);
  static final CancellationUseCases cancellationUseCases = UseCasesAdapterFactory.getCancellationService(
      flightRepository, bookingRepository, paymentGateway, notificationService);

}

package com.astrobookings.sales.domain;

import java.time.LocalDateTime;
import java.util.List;

import com.astrobookings.sales.domain.models.Booking;
import com.astrobookings.sales.domain.ports.input.CancellationUseCases;
import com.astrobookings.sales.domain.ports.output.BookingRepository;
import com.astrobookings.sales.domain.ports.output.FlightInfoProvider;
import com.astrobookings.sales.domain.ports.output.FlightInfoProvider.FlightInfo;
import com.astrobookings.sales.domain.ports.output.NotificationService;
import com.astrobookings.sales.domain.ports.output.PaymentGateway;

public class CancellationService implements CancellationUseCases {
  private final FlightInfoProvider flightInfoProvider;
  private final BookingRepository bookingRepository;
  private final PaymentGateway paymentGateway;
  private final NotificationService notificationService;

  public CancellationService(
      FlightInfoProvider flightInfoProvider,
      BookingRepository bookingRepository,
      PaymentGateway paymentGateway,
      NotificationService notificationService) {
    this.flightInfoProvider = flightInfoProvider;
    this.bookingRepository = bookingRepository;
    this.paymentGateway = paymentGateway;
    this.notificationService = notificationService;
  }

  public int cancelFlights() {
    LocalDateTime cutoffDate = LocalDateTime.now().plusDays(7);
    List<FlightInfo> flightsToCancel = flightInfoProvider.getFlightsForCancellation(cutoffDate, 5);

    int cancelledCount = 0;
    for (FlightInfo flight : flightsToCancel) {
      List<Booking> bookings = bookingRepository.findByFlightId(flight.id());

      boolean cancelled = flightInfoProvider.cancelFlightIfLowDemand(flight.id(), bookings.size(), cutoffDate);
      if (!cancelled) {
        continue;
      }

      System.out.println("[CANCELLATION SERVICE] Cancelling flight " + flight.id() + " - Only "
          + bookings.size() + "/" + flight.minPassengers() + " passengers");

      for (Booking booking : bookings) {
        paymentGateway.processRefund(booking.getPaymentTransactionId(), booking.getFinalPrice());
      }

      notificationService.notifyCancellation(flight.id(), bookings);
      cancelledCount++;
    }
    return cancelledCount;
  }
}

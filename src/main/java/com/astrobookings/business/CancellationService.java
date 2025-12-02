package com.astrobookings.business;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.astrobookings.persistence.BookingRepository;
import com.astrobookings.persistence.FlightRepository;
import com.astrobookings.persistence.RepositoryFactory;
import com.astrobookings.persistence.models.Booking;
import com.astrobookings.persistence.models.Flight;
import com.astrobookings.persistence.models.FlightStatus;

public class CancellationService {
  private final FlightRepository flightRepository = RepositoryFactory.getFlightRepository();
  private final BookingRepository bookingRepository = RepositoryFactory.getBookingRepository();
  private final PaymentGateway paymentGateway = new PaymentGateway();
  private final NotificationService notificationService = new NotificationService();

  public CancellationService() {
  }

  public int cancelFlights() {
    List<Flight> flights = flightRepository.findAll();
    int cancelledCount = 0;
    LocalDateTime now = LocalDateTime.now();

    for (Flight flight : flights) {
      if (flight.getStatus() == FlightStatus.SCHEDULED) {
        long daysUntilDeparture = ChronoUnit.DAYS.between(now, flight.getDepartureDate());
        if (daysUntilDeparture <= 7) {
          List<Booking> bookings = bookingRepository.findByFlightId(flight.getId());
          if (bookings.size() < flight.getMinPassengers()) {
            // Cancel flight
            System.out.println("[CANCELLATION SERVICE] Cancelling flight " + flight.getId() + " - Only "
                + bookings.size() + "/5 passengers, departing in " + daysUntilDeparture + " days");
            flight.setStatus(FlightStatus.CANCELLED);
            flightRepository.save(flight);

            // Refund bookings
            for (Booking booking : bookings) {
              paymentGateway.processRefund(booking.getPaymentTransactionId(), booking.getFinalPrice());
            }

            // Notify
            notificationService.notifyCancellation(flight.getId(), bookings);
            cancelledCount++;
          }
        }
      }
    }
    return cancelledCount;
  }
}
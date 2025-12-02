package com.astrobookings.domain;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.astrobookings.domain.model.Booking;
import com.astrobookings.domain.model.Flight;
import com.astrobookings.domain.model.FlightStatus;
import com.astrobookings.domain.ports.BookingRepositoryPort;
import com.astrobookings.domain.ports.FlightRepositoryPort;

public class CancellationService {
  private final FlightRepositoryPort flightRepository;
  private final BookingRepositoryPort bookingRepository;

  public CancellationService(FlightRepositoryPort flightRepository, BookingRepositoryPort bookingRepository) {
    this.flightRepository = flightRepository;
    this.bookingRepository = bookingRepository;
  }

  public String cancelFlights() throws Exception {
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
              PaymentGateway.processRefund(booking.getPaymentTransactionId(), booking.getFinalPrice());
            }

            // Notify
            NotificationService.notifyCancellation(flight.getId(), bookings);
            cancelledCount++;
          }
        }
      }
    }

    return "{\"message\": \"Flight cancellation check completed\", \"cancelledFlights\": " + cancelledCount + "}";
  }
}
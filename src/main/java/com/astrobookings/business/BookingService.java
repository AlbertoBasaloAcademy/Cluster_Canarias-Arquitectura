package com.astrobookings.business;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.astrobookings.persistence.BookingRepository;
import com.astrobookings.persistence.FlightRepository;
import com.astrobookings.persistence.RocketRepository;
import com.astrobookings.persistence.models.Booking;
import com.astrobookings.persistence.models.Flight;
import com.astrobookings.persistence.models.FlightStatus;
import com.astrobookings.persistence.models.Rocket;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BookingService {
  private final BookingRepository bookingRepository;
  private final FlightRepository flightRepository;
  private final RocketRepository rocketRepository;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public BookingService(BookingRepository bookingRepository, FlightRepository flightRepository,
      RocketRepository rocketRepository) {
    this.bookingRepository = bookingRepository;
    this.flightRepository = flightRepository;
    this.rocketRepository = rocketRepository;
  }

  public String createBooking(String flightId, String passengerName) throws Exception {
    // Validate input
    if (flightId == null || flightId.trim().isEmpty()) {
      throw new IllegalArgumentException("Flight ID must be provided");
    }
    if (passengerName == null || passengerName.trim().isEmpty()) {
      throw new IllegalArgumentException("Passenger name must be provided");
    }

    // Find flight
    Flight flight = flightRepository.findAll().stream()
        .filter(f -> f.getId().equals(flightId))
        .findFirst()
        .orElse(null);
    if (flight == null) {
      throw new IllegalArgumentException("Flight not found");
    }

    // Check flight status
    if (flight.getStatus() == FlightStatus.CANCELLED || flight.getStatus() == FlightStatus.SOLD_OUT) {
      throw new IllegalArgumentException("Flight is not available for booking");
    }

    // Get rocket capacity
    Rocket rocket = rocketRepository.findAll().stream()
        .filter(r -> r.getId().equals(flight.getRocketId()))
        .findFirst()
        .orElse(null);
    if (rocket == null) {
      throw new IllegalArgumentException("Rocket not found");
    }
    int capacity = rocket.getCapacity();

    // Count current bookings
    List<Booking> existingBookings = bookingRepository.findByFlightId(flightId);
    int currentBookings = existingBookings.size();

    if (currentBookings >= capacity) {
      throw new IllegalArgumentException("Flight is sold out");
    }

    // Calculate discount
    double discount = calculateDiscount(flight, currentBookings, capacity);

    // Final price
    double finalPrice = flight.getBasePrice() * (1 - discount);

    // Process payment
    String transactionId = PaymentGateway.processPayment(finalPrice);

    // Create booking
    Booking booking = new Booking();
    booking.setFlightId(flightId);
    booking.setPassengerName(passengerName);
    booking.setFinalPrice(finalPrice);
    booking.setPaymentTransactionId(transactionId);
    Booking savedBooking = bookingRepository.save(booking);

    // Update flight status
    currentBookings++;
    if (currentBookings >= capacity) {
      flight.setStatus(FlightStatus.SOLD_OUT);
    } else if (currentBookings >= flight.getMinPassengers() && flight.getStatus() == FlightStatus.SCHEDULED) {
      flight.setStatus(FlightStatus.CONFIRMED);
      NotificationService.notifyConfirmation(flightId, currentBookings);
    }
    flightRepository.save(flight);

    // Return JSON (mixing responsibility)
    return objectMapper.writeValueAsString(savedBooking);
  }

  private double calculateDiscount(Flight flight, int currentBookings, int capacity) {
    LocalDateTime now = LocalDateTime.now();
    long daysUntilDeparture = ChronoUnit.DAYS.between(now, flight.getDepartureDate());

    // Precedence: only one discount
    if (currentBookings + 1 == capacity) {
      return 0.0; // Last seat, no discount
    } else if (currentBookings + 1 == flight.getMinPassengers()) {
      return 0.3; // One short of min, 30% off
    } else if (daysUntilDeparture > 180) {
      return 0.1; // >6 months, 10% off
    } else if (daysUntilDeparture >= 7 && daysUntilDeparture <= 30) {
      return 0.2; // 1 month to 1 week, 20% off
    } else {
      return 0.0; // No discount
    }
  }

  public String getBookings(String flightId, String passengerName) throws Exception {
    List<Booking> bookings;
    if (flightId != null && !flightId.isEmpty()) {
      bookings = bookingRepository.findByFlightId(flightId);
      if (passengerName != null && !passengerName.isEmpty()) {
        bookings = bookings.stream()
            .filter(b -> b.getPassengerName().equalsIgnoreCase(passengerName))
            .collect(java.util.stream.Collectors.toList());
      }
    } else if (passengerName != null && !passengerName.isEmpty()) {
      bookings = bookingRepository.findByPassengerName(passengerName);
    } else {
      bookings = bookingRepository.findAll();
    }
    return objectMapper.writeValueAsString(bookings);
  }
}
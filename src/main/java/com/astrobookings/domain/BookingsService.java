package com.astrobookings.domain;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.astrobookings.domain.models.Booking;
import com.astrobookings.domain.models.BusinessErrorCode;
import com.astrobookings.domain.models.BusinessException;
import com.astrobookings.domain.models.CreateBookingCommand;
import com.astrobookings.domain.models.Flight;
import com.astrobookings.domain.models.FlightStatus;
import com.astrobookings.domain.models.Rocket;
import com.astrobookings.domain.ports.input.BookingsUseCases;
import com.astrobookings.domain.ports.output.BookingRepository;
import com.astrobookings.domain.ports.output.FlightRepository;
import com.astrobookings.domain.ports.output.NotificationService;
import com.astrobookings.domain.ports.output.PaymentGateway;
import com.astrobookings.domain.ports.output.RocketRepository;

public class BookingsService implements BookingsUseCases {
  private final BookingRepository bookingRepository;
  private final FlightRepository flightRepository;
  private final RocketRepository rocketRepository;
  private final PaymentGateway paymentGateway;
  private final NotificationService notificationService;

  public BookingsService(BookingRepository bookingRepository, FlightRepository flightRepository,
      RocketRepository rocketRepository, PaymentGateway paymentGateway, NotificationService notificationService) {
    this.bookingRepository = bookingRepository;
    this.flightRepository = flightRepository;
    this.rocketRepository = rocketRepository;
    this.paymentGateway = paymentGateway;
    this.notificationService = notificationService;
  }

  public Booking createBooking(CreateBookingCommand command) {
    Flight flight = flightRepository.findById(command.flightId());
    if (flight == null) {
      throw new BusinessException(BusinessErrorCode.NOT_FOUND, "Flight not found");
    }
    if (flight.getStatus() == FlightStatus.CANCELLED || flight.getStatus() == FlightStatus.SOLD_OUT) {
      throw new BusinessException(BusinessErrorCode.VALIDATION, "Flight is not available for booking");
    }

    Rocket rocket = rocketRepository.findById(flight.getRocketId());
    if (rocket == null) {
      throw new BusinessException(BusinessErrorCode.NOT_FOUND, "Rocket not found");
    }

    List<Booking> existingBookings = bookingRepository.findByFlightId(command.flightId());
    int capacity = rocket.getCapacity();
    int currentBookings = existingBookings.size();
    if (currentBookings >= capacity) {
      throw new BusinessException(BusinessErrorCode.CAPACITY, "Flight is sold out");
    }

    double discount = calculateDiscount(flight, currentBookings, capacity);
    double finalPrice = flight.getBasePrice() * (1 - discount);

    String transactionId;
    try {
      transactionId = paymentGateway.processPayment(finalPrice);
    } catch (Exception e) {
      throw new BusinessException(BusinessErrorCode.PAYMENT, e.getMessage());
    }

    Booking booking = new Booking();
    booking.setFlightId(command.flightId());
    booking.setPassengerName(command.passengerName());
    booking.setFinalPrice(finalPrice);
    booking.setPaymentTransactionId(transactionId);
    Booking savedBooking = bookingRepository.save(booking);

    currentBookings++;
    if (currentBookings >= capacity) {
      flight.setStatus(FlightStatus.SOLD_OUT);
    } else if (currentBookings >= flight.getMinPassengers() && flight.getStatus() == FlightStatus.SCHEDULED) {
      flight.setStatus(FlightStatus.CONFIRMED);
      notificationService.notifyConfirmation(command.flightId(), currentBookings);
    }
    flightRepository.save(flight);

    return savedBooking;
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

  public List<Booking> getBookings(String flightId, String passengerName) {
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
    return bookings;
  }
}
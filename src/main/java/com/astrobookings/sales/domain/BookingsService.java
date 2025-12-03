package com.astrobookings.sales.domain;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.astrobookings.sales.domain.models.Booking;
import com.astrobookings.sales.domain.models.CreateBookingCommand;
import com.astrobookings.sales.domain.ports.input.BookingsUseCases;
import com.astrobookings.sales.domain.ports.output.BookingRepository;
import com.astrobookings.sales.domain.ports.output.FlightInfoProvider;
import com.astrobookings.sales.domain.ports.output.FlightInfoProvider.FlightInfo;
import com.astrobookings.sales.domain.ports.output.NotificationService;
import com.astrobookings.sales.domain.ports.output.PaymentGateway;
import com.astrobookings.shared.domain.BusinessErrorCode;
import com.astrobookings.shared.domain.BusinessException;
import com.astrobookings.shared.domain.Capacity;

public class BookingsService implements BookingsUseCases {
  private final BookingRepository bookingRepository;
  private final FlightInfoProvider flightInfoProvider;
  private final PaymentGateway paymentGateway;
  private final NotificationService notificationService;

  public BookingsService(
      BookingRepository bookingRepository,
      FlightInfoProvider flightInfoProvider,
      PaymentGateway paymentGateway,
      NotificationService notificationService) {
    this.bookingRepository = bookingRepository;
    this.flightInfoProvider = flightInfoProvider;
    this.paymentGateway = paymentGateway;
    this.notificationService = notificationService;
  }

  public Booking createBooking(CreateBookingCommand command) {
    FlightInfo flight = flightInfoProvider.getFlightById(command.flightId());
    if (flight == null) {
      throw new BusinessException(BusinessErrorCode.NOT_FOUND, "Flight not found");
    }
    if ("CANCELLED".equals(flight.status()) || "SOLD_OUT".equals(flight.status())) {
      throw new BusinessException(BusinessErrorCode.VALIDATION, "Flight is not available for booking");
    }

    if (!flightInfoProvider.canAcceptPassengers(command.flightId())) {
      throw new BusinessException(BusinessErrorCode.CAPACITY, "Flight is sold out");
    }

    List<Booking> existingBookings = bookingRepository.findByFlightId(command.flightId());
    int currentBookings = existingBookings.size();
    Capacity capacity = flight.capacity();
    if (capacity == null) {
      throw new BusinessException(BusinessErrorCode.INTERNAL, "Flight capacity unavailable");
    }
    capacity.ensureCanBoard(currentBookings);

    double discount = calculateDiscount(flight, currentBookings, capacity);
    double finalPrice = flight.basePrice() * (1 - discount);

    String transactionId;
    try {
      transactionId = paymentGateway.processPayment(finalPrice);
    } catch (Exception e) {
      throw new BusinessException(BusinessErrorCode.PAYMENT, e.getMessage());
    }

    Booking booking = Booking.create(command.flightId(), command.passengerName(), finalPrice, transactionId);
    Booking savedBooking = bookingRepository.save(booking);

    int passengerCountAfterBooking = currentBookings + 1;
    if (capacity.isFull(passengerCountAfterBooking)) {
      flightInfoProvider.markFlightSoldOut(command.flightId());
    } else if (flightInfoProvider.confirmFlightIfMinReached(command.flightId(), passengerCountAfterBooking)) {
      notificationService.notifyConfirmation(command.flightId(), passengerCountAfterBooking);
    }

    return savedBooking;
  }

  private double calculateDiscount(FlightInfo flight, int currentBookings, Capacity capacity) {
    LocalDateTime now = LocalDateTime.now();
    long daysUntilDeparture = ChronoUnit.DAYS.between(now, flight.departureDate());
    int capacityLimit = capacity.maxPassengers();

    // Precedence: only one discount
    if (currentBookings + 1 == capacityLimit) {
      return 0.0; // Last seat, no discount
    } else if (currentBookings + 1 == flight.minPassengers()) {
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

package com.astrobookings.business;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.astrobookings.business.models.CapacityException;
import com.astrobookings.business.models.CreateBookingCommand;
import com.astrobookings.business.models.NotFoundException;
import com.astrobookings.business.models.PaymentException;
import com.astrobookings.business.models.ValidationException;
import com.astrobookings.persistence.BookingRepository;
import com.astrobookings.persistence.FlightRepository;
import com.astrobookings.persistence.RocketRepository;
import com.astrobookings.persistence.models.Booking;
import com.astrobookings.persistence.models.Flight;
import com.astrobookings.persistence.models.FlightStatus;
import com.astrobookings.persistence.models.Rocket;

public class BookingServiceImpl implements BookingService {
  private final BookingRepository bookingRepository;
  private final FlightRepository flightRepository;
  private final RocketRepository rocketRepository;
  private final PaymentGateway paymentGateway;
  private final NotificationService notificationService;

  public BookingServiceImpl(BookingRepository bookingRepository, FlightRepository flightRepository,
      RocketRepository rocketRepository, PaymentGateway paymentGateway, NotificationService notificationService) {
    this.bookingRepository = bookingRepository;
    this.flightRepository = flightRepository;
    this.rocketRepository = rocketRepository;
    this.paymentGateway = paymentGateway;
    this.notificationService = notificationService;
  }

  public Booking createBooking(CreateBookingCommand command) {
    if (command.flightId() == null || command.flightId().isBlank()) {
      throw new ValidationException("Flight ID must be provided");
    }
    if (command.passengerName() == null || command.passengerName().isBlank()) {
      throw new ValidationException("Passenger name must be provided");
    }

    Flight flight = flightRepository.findById(command.flightId());
    if (flight == null) {
      throw new NotFoundException("Flight not found");
    }
    if (flight.getStatus() == FlightStatus.CANCELLED || flight.getStatus() == FlightStatus.SOLD_OUT) {
      throw new ValidationException("Flight is not available for booking");
    }

    Rocket rocket = rocketRepository.findById(flight.getRocketId());
    if (rocket == null) {
      throw new NotFoundException("Rocket not found");
    }

    List<Booking> existingBookings = bookingRepository.findByFlightId(command.flightId());
    int capacity = rocket.getCapacity();
    int currentBookings = existingBookings.size();
    if (currentBookings >= capacity) {
      throw new CapacityException("Flight is sold out");
    }

    double discount = calculateDiscount(flight, currentBookings, capacity);
    double finalPrice = flight.getBasePrice() * (1 - discount);

    String transactionId;
    try {
      transactionId = paymentGateway.processPayment(finalPrice);
    } catch (Exception e) {
      throw new PaymentException(e.getMessage());
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
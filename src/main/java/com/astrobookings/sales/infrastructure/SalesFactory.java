package com.astrobookings.sales.infrastructure;

import com.astrobookings.fleet.infrastructure.FleetFactory;
import com.astrobookings.sales.domain.BookingsService;
import com.astrobookings.sales.domain.CancellationService;
import com.astrobookings.sales.domain.ports.input.BookingsUseCases;
import com.astrobookings.sales.domain.ports.input.CancellationUseCases;
import com.astrobookings.sales.domain.ports.output.BookingRepository;
import com.astrobookings.sales.domain.ports.output.FlightInfoProvider;
import com.astrobookings.sales.domain.ports.output.NotificationService;
import com.astrobookings.sales.domain.ports.output.PaymentGateway;
import com.astrobookings.sales.infrastructure.adapters.FleetAdapter;
import com.astrobookings.sales.infrastructure.persistence.BookingInMemoryRepository;
import com.astrobookings.sales.infrastructure.persistence.NotificationConsoleService;
import com.astrobookings.sales.infrastructure.persistence.PaymentConsoleGateway;
import com.astrobookings.sales.infrastructure.presentation.AdminHandler;
import com.astrobookings.sales.infrastructure.presentation.BookingsHandler;

/**
 * Factory that assembles the Sales module (Core Domain).
 * Manages bookings, payments, discounts, and commercial cancellations.
 * Depends on Fleet module for flight information.
 */
public class SalesFactory {
  // Repositories (singletons)
  private final BookingRepository bookingRepository;
  private final PaymentGateway paymentGateway;
  private final NotificationService notificationService;

  // Adapter to Fleet module
  private final FlightInfoProvider flightInfoProvider;

  // Use Cases / Services
  private final BookingsUseCases bookingsUseCases;
  private final CancellationUseCases cancellationUseCases;

  // Handlers
  private final BookingsHandler bookingsHandler;
  private final AdminHandler adminHandler;

  public SalesFactory(FleetFactory fleetFactory) {
    // Create persistence adapters
    this.bookingRepository = new BookingInMemoryRepository();
    this.paymentGateway = new PaymentConsoleGateway();
    this.notificationService = new NotificationConsoleService();

    // Create adapter to Fleet module
    this.flightInfoProvider = new FleetAdapter(
        fleetFactory.getFlightRepository(),
        fleetFactory.getRocketRepository(),
        bookingRepository
    );

    // Create domain services
    this.bookingsUseCases = new BookingsService(
        bookingRepository,
        flightInfoProvider,
        paymentGateway,
        notificationService
    );

    this.cancellationUseCases = new CancellationService(
        flightInfoProvider,
        bookingRepository,
        paymentGateway,
        notificationService
    );

    // Create presentation adapters
    this.bookingsHandler = new BookingsHandler(bookingsUseCases);
    this.adminHandler = new AdminHandler(cancellationUseCases);
  }

  // Handlers for HTTP server registration
  public BookingsHandler getBookingsHandler() {
    return bookingsHandler;
  }

  public AdminHandler getAdminHandler() {
    return adminHandler;
  }

  // Public API for testing or other modules
  public BookingsUseCases getBookingsUseCases() {
    return bookingsUseCases;
  }

  public CancellationUseCases getCancellationUseCases() {
    return cancellationUseCases;
  }
}

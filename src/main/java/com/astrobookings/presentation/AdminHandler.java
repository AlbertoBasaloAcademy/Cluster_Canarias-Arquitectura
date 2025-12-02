package com.astrobookings.presentation;

import java.io.IOException;
import java.util.Map;

import com.astrobookings.domain.CancellationService;
import com.astrobookings.infrastructure.InfrastructureFactory;
import com.sun.net.httpserver.HttpExchange;

public class AdminHandler extends BaseHandler {
  private final CancellationService cancellationService;

  public AdminHandler() {
    this.cancellationService = new CancellationService(
        InfrastructureFactory.getFlightRepository(),
        InfrastructureFactory.getBookingRepository(),
        InfrastructureFactory.getPaymentGateway(),
        InfrastructureFactory.getNotificationService());
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    String method = exchange.getRequestMethod();

    if ("POST".equals(method)) {
      handlePost(exchange);
    } else {
      this.handleMethodNotAllowed(exchange);
    }
  }

  private void handlePost(HttpExchange exchange) throws IOException {
    try {
      int cancelled = cancellationService.cancelFlights();
      sendJsonResponse(exchange, 200,
          Map.of("message", "Flight cancellation check completed", "cancelledFlights", cancelled));
    } catch (Exception e) {
      handleException(exchange, e);
    }
  }
}
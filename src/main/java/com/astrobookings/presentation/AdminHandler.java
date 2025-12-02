package com.astrobookings.presentation;

import java.io.IOException;
import java.util.Map;

import com.astrobookings.business.CancellationService;
import com.astrobookings.business.NotificationService;
import com.astrobookings.business.PaymentGateway;
import com.astrobookings.persistence.RepositoryFactory;
import com.sun.net.httpserver.HttpExchange;

public class AdminHandler extends BaseHandler {
  private final CancellationService cancellationService = new CancellationService(
      RepositoryFactory.getFlightRepository(),
      RepositoryFactory.getBookingRepository(),
      new PaymentGateway(),
      new NotificationService());

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
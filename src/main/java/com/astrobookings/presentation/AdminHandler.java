package com.astrobookings.presentation;

import java.io.IOException;

import com.astrobookings.business.CancellationService;
import com.astrobookings.persistence.BookingRepository;
import com.astrobookings.persistence.FlightRepository;
import com.sun.net.httpserver.HttpExchange;

public class AdminHandler extends BaseHandler {
  private final CancellationService cancellationService;

  public AdminHandler() {
    FlightRepository flightRepository = new FlightRepository();
    BookingRepository bookingRepository = new BookingRepository();
    this.cancellationService = new CancellationService(flightRepository, bookingRepository);
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    String method = exchange.getRequestMethod();

    if ("POST".equals(method)) {
      handlePost(exchange);
    } else {
      handleMethodNotAllowed(exchange);
    }
  }

  private void handlePost(HttpExchange exchange) throws IOException {
    String response = "";
    int statusCode = 200;

    try {
      response = cancellationService.cancelFlights();
    } catch (Exception e) {
      statusCode = 500;
      response = "{\"error\": \"Internal server error\"}";
    }

    sendResponse(exchange, statusCode, response);
  }

  private void handleMethodNotAllowed(HttpExchange exchange) throws IOException {
    sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
  }
}
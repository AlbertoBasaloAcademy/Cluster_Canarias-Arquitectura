package com.astrobookings.presentation;

import java.io.IOException;

import com.astrobookings.domain.CancellationService;
import com.astrobookings.infrastructure.RepositoryFactory;
import com.sun.net.httpserver.HttpExchange;

public class AdminHandler extends BaseHandler {
  private final CancellationService cancellationService;

  public AdminHandler() {
    this.cancellationService = new CancellationService(RepositoryFactory.getFlightInstance(), RepositoryFactory.getBookingInstance());
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
}
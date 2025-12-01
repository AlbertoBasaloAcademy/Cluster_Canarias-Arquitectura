package com.astrobookings.presentation;

import java.io.IOException;

import com.astrobookings.business.CancellationService;
import com.sun.net.httpserver.HttpExchange;

public class AdminHandler extends BaseHandler {
  private final CancellationService cancellationService = new CancellationService();

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
      String response = "{\"message\": \"Flight cancellation check completed\", \"cancelledFlights\": "
          + cancelled + "}";
      sendResponse(exchange, 200, response);
    } catch (Exception e) {
      handleException(exchange, e);
    }
  }
}
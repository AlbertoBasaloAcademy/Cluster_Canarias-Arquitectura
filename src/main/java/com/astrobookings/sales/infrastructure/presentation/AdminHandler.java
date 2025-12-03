package com.astrobookings.sales.infrastructure.presentation;

import java.io.IOException;
import java.util.Map;

import com.astrobookings.sales.domain.ports.input.CancellationUseCases;
import com.astrobookings.shared.infrastructure.presentation.BaseHandler;
import com.sun.net.httpserver.HttpExchange;

public class AdminHandler extends BaseHandler {
  private final CancellationUseCases cancellationUseCases;

  public AdminHandler(CancellationUseCases cancellationUseCases) {
    this.cancellationUseCases = cancellationUseCases;
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
      int cancelled = cancellationUseCases.cancelFlights();
      sendJsonResponse(exchange, 200,
          Map.of("message", "Flight cancellation check completed", "cancelledFlights", cancelled));
    } catch (Exception e) {
      handleException(exchange, e);
    }
  }
}

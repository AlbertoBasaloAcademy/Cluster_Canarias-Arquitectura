package com.astrobookings.presentation;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

import com.astrobookings.business.FlightService;
import com.astrobookings.business.models.CreateFlightCommand;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpExchange;

public class FlightHandler extends BaseHandler {
  private final FlightService flightService = new FlightService();

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    String method = exchange.getRequestMethod();

    if ("GET".equals(method)) {
      handleGet(exchange);
    } else if ("POST".equals(method)) {
      handlePost(exchange);
    } else {
      this.handleMethodNotAllowed(exchange);
    }
  }

  private void handleGet(HttpExchange exchange) throws IOException {
    try {
      Map<String, String> params = getQueryParams(exchange);
      String statusFilter = params.get("status");
      sendJsonResponse(exchange, 200, flightService.getFlights(statusFilter));
    } catch (Exception e) {
      handleException(exchange, e);
    }
  }

  private void handlePost(HttpExchange exchange) throws IOException {
    try {
      JsonNode jsonNode = readJsonBody(exchange);
      CreateFlightCommand command = mapCreateFlight(jsonNode);

      var saved = flightService.createFlight(command);
      sendJsonResponse(exchange, 201, saved);
    } catch (Exception e) {
      handleException(exchange, e);
    }
  }

  private CreateFlightCommand mapCreateFlight(JsonNode node) {
    String rocketId = requireText(node, "rocketId");
    LocalDateTime departureDate = parseDate(requireText(node, "departureDate"));
    double basePrice = requireDouble(node, "basePrice");
    Integer minPassengers = shouldBeInt(node, "minPassengers", 0);
    return new CreateFlightCommand(rocketId, departureDate, basePrice, minPassengers);
  }
}
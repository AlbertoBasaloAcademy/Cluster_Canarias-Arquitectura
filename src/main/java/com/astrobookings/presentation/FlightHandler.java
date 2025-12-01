package com.astrobookings.presentation;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;

import com.astrobookings.business.FlightService;
import com.astrobookings.business.models.CreateFlightCommand;
import com.astrobookings.business.models.ValidationException;
import com.astrobookings.persistence.models.Flight;
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
      URI uri = exchange.getRequestURI();
      String query = uri.getQuery();
      String statusFilter = null;
      if (query != null) {
        Map<String, String> params = this.parseQuery(query);
        statusFilter = params.get("status");
      }
      String response = this.objectMapper.writeValueAsString(flightService.getFlights(statusFilter));
      sendResponse(exchange, 200, response);
    } catch (Exception e) {
      handleException(exchange, e);
    }
  }

  private void handlePost(HttpExchange exchange) throws IOException {
    try {
      String body = readRequestBody(exchange);
      JsonNode jsonNode = this.objectMapper.readTree(body);
      CreateFlightCommand command = mapCreateFlight(jsonNode);

      Flight saved = flightService.createFlight(command);
      String response = this.objectMapper.writeValueAsString(saved);
      sendResponse(exchange, 201, response);
    } catch (Exception e) {
      handleException(exchange, e);
    }
  }

  private CreateFlightCommand mapCreateFlight(JsonNode node) {
    String rocketId = requireText(node, "rocketId");
    LocalDateTime departureDate = parseDate(requireText(node, "departureDate"));
    double basePrice = requireDouble(node, "basePrice");
    Integer minPassengers = null;
    if (node.has("minPassengers") && !node.get("minPassengers").isNull()) {
      JsonNode minNode = node.get("minPassengers");
      if (!minNode.canConvertToInt()) {
        throw new ValidationException("Field 'minPassengers' must be an integer");
      }
      minPassengers = minNode.asInt();
    }
    return new CreateFlightCommand(rocketId, departureDate, basePrice, minPassengers);
  }

  private String requireText(JsonNode node, String fieldName) {
    JsonNode value = node.get(fieldName);
    if (value == null || value.isNull() || value.asText().isBlank()) {
      throw new ValidationException("Field '" + fieldName + "' is required");
    }
    return value.asText();
  }

  private double requireDouble(JsonNode node, String fieldName) {
    JsonNode value = node.get(fieldName);
    if (value == null || value.isNull() || !value.isNumber()) {
      throw new ValidationException("Field '" + fieldName + "' must be numeric");
    }
    return value.asDouble();
  }

  private LocalDateTime parseDate(String value) {
    try {
      return LocalDateTime.parse(value);
    } catch (DateTimeParseException exception) {
      throw new ValidationException("Field 'departureDate' must follow ISO-8601 format");
    }
  }

}
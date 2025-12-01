package com.astrobookings.presentation;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import com.astrobookings.business.BookingService;
import com.astrobookings.business.ServiceFactory;
import com.astrobookings.business.models.CreateBookingCommand;
import com.astrobookings.business.models.ValidationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpExchange;

public class BookingHandler extends BaseHandler {
  private final BookingService bookingService = ServiceFactory.getBookingService();

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
      String flightId = null;
      String passengerName = null;
      if (query != null) {
        Map<String, String> params = this.parseQuery(query);
        flightId = params.get("flightId");
        passengerName = params.get("passengerName");
      }

      var bookings = bookingService.getBookings(flightId, passengerName);
      String response = this.objectMapper.writeValueAsString(bookings);
      sendResponse(exchange, 200, response);
    } catch (Exception e) {
      handleException(exchange, e);
    }
  }

  private void handlePost(HttpExchange exchange) throws IOException {
    try {
      String body = readRequestBody(exchange);
      JsonNode jsonNode = this.objectMapper.readTree(body);
      CreateBookingCommand command = mapCreateBooking(jsonNode);

      var booking = bookingService.createBooking(command);
      String response = this.objectMapper.writeValueAsString(booking);
      sendResponse(exchange, 201, response);
    } catch (Exception e) {
      handleException(exchange, e);
    }
  }

  private CreateBookingCommand mapCreateBooking(JsonNode node) {
    String flightId = requireText(node, "flightId");
    String passengerName = requireText(node, "passengerName");
    return new CreateBookingCommand(flightId, passengerName);
  }

  private String requireText(JsonNode node, String fieldName) {
    JsonNode value = node.get(fieldName);
    if (value == null || value.isNull() || value.asText().isBlank()) {
      throw new ValidationException("Field '" + fieldName + "' is required");
    }
    return value.asText();
  }
}
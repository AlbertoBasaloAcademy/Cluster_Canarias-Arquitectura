package com.astrobookings.presentation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.astrobookings.business.BookingService;
import com.astrobookings.persistence.BookingRepository;
import com.astrobookings.persistence.FlightRepository;
import com.astrobookings.persistence.RocketRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

public class BookingHandler extends BaseHandler {
  private final BookingService bookingService;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public BookingHandler() {
    BookingRepository bookingRepository = new BookingRepository();
    FlightRepository flightRepository = new FlightRepository();
    RocketRepository rocketRepository = new RocketRepository();
    this.bookingService = new BookingService(bookingRepository, flightRepository, rocketRepository);
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    String method = exchange.getRequestMethod();

    if ("GET".equals(method)) {
      handleGet(exchange);
    } else if ("POST".equals(method)) {
      handlePost(exchange);
    } else {
      handleMethodNotAllowed(exchange);
    }
  }

  private void handleGet(HttpExchange exchange) throws IOException {
    String response = "";
    int statusCode = 200;

    try {
      URI uri = exchange.getRequestURI();
      String query = uri.getQuery();
      String flightId = null;
      String passengerName = null;
      if (query != null) {
        Map<String, String> params = parseQuery(query);
        flightId = params.get("flightId");
        passengerName = params.get("passengerName");
      }

      response = bookingService.getBookings(flightId, passengerName);
    } catch (Exception e) {
      statusCode = 500;
      response = "{\"error\": \"Internal server error\"}";
    }

    sendResponse(exchange, statusCode, response);
  }

  private void handlePost(HttpExchange exchange) throws IOException {
    String response = "";
    int statusCode = 201;

    try {
      // Parse JSON body
      InputStream is = exchange.getRequestBody();
      String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
      JsonNode jsonNode = objectMapper.readTree(body);

      String flightId = jsonNode.get("flightId").asText();
      String passengerName = jsonNode.get("passengerName").asText();

      // Basic validation in handler (mixing)
      if (flightId == null || flightId.trim().isEmpty()) {
        statusCode = 400;
        response = "{\"error\": \"Flight ID must be provided\"}";
      } else if (passengerName == null || passengerName.trim().isEmpty()) {
        statusCode = 400;
        response = "{\"error\": \"Passenger name must be provided\"}";
      } else {
        response = bookingService.createBooking(flightId, passengerName);
      }
    } catch (IllegalArgumentException e) {
      String error = e.getMessage();
      if (error.contains("not found")) {
        statusCode = 404;
      } else if (error.contains("not available")) {
        statusCode = 400;
      } else {
        statusCode = 400;
      }
      response = "{\"error\": \"" + error + "\"}";
    } catch (Exception e) {
      if (e.getMessage() != null && e.getMessage().contains("FAILED")) {
        statusCode = 402;
        response = "{\"error\": \"Payment required\"}";
      } else {
        statusCode = 400;
        response = "{\"error\": \"Invalid JSON or request\"}";
      }
    }

    sendResponse(exchange, statusCode, response);
  }

  private void handleMethodNotAllowed(HttpExchange exchange) throws IOException {
    sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
  }

  private Map<String, String> parseQuery(String query) {
    Map<String, String> params = new HashMap<>();
    if (query != null) {
      String[] pairs = query.split("&");
      for (String pair : pairs) {
        String[] keyValue = pair.split("=");
        if (keyValue.length == 2) {
          params.put(keyValue[0], keyValue[1]);
        }
      }
    }
    return params;
  }
}
package com.astrobookings.presentation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.astrobookings.domain.BookingService;
import com.astrobookings.domain.ports.BookingRepositoryPort;
import com.astrobookings.domain.ports.FlightRepositoryPort;
import com.astrobookings.domain.ports.RocketRepositoryPort;
import com.astrobookings.infrastructure.RepositoryFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpExchange;

public class BookingHandler extends BaseHandler {
  private final BookingService bookingService;

  public BookingHandler() {
    BookingRepositoryPort bookingRepository = RepositoryFactory.getBookingInstance();
    FlightRepositoryPort flightRepository = RepositoryFactory.getFlightInstance();
    RocketRepositoryPort rocketRepository = RepositoryFactory.getRocketInstance();
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
      this.handleMethodNotAllowed(exchange);
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
        Map<String, String> params = this.parseQuery(query);
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
      JsonNode jsonNode = this.objectMapper.readTree(body);
      String errorMessage = validadorEntrada(jsonNode);
      if (errorMessage != null) {
        response = "{ \"error\": \"" + errorMessage + "\" }";
        statusCode = 400;
      } else {
        String flightId = jsonNode.get("flightId").asText();
        String passengerName = jsonNode.get("passengerName").asText();
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

  protected Map<String, String> parseQuery(String query) {
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

  private String validadorEntrada(JsonNode jsonNode) {
    if (jsonNode == null) {
        return "El cuerpo de la petición no puede estar vacío";
    }
    if (!jsonNode.has("flightId") || jsonNode.get("flightId").isNull() || jsonNode.get("flightId").asText().trim().isEmpty()) {
        return "El parámetro 'flightId' es obligatorio";
    }
    if (!jsonNode.has("passengerName") || jsonNode.get("passengerName").isNull() || jsonNode.get("passengerName").asText().trim().isEmpty()) {
        return "El parámetro 'passengerName' es obligatorio";
    }
    
    return null; 
  }
}
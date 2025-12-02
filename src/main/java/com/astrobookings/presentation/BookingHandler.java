package com.astrobookings.presentation;

import java.io.IOException;
import java.util.Map;

import com.astrobookings.business.BookingService;
import com.astrobookings.business.NotificationService;
import com.astrobookings.business.PaymentGateway;
import com.astrobookings.business.models.CreateBookingCommand;
import com.astrobookings.persistence.RepositoryFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpExchange;

public class BookingHandler extends BaseHandler {
  private final BookingService bookingService = new BookingService(
      RepositoryFactory.getBookingRepository(),
      RepositoryFactory.getFlightRepository(),
      RepositoryFactory.getRocketRepository(),
      new PaymentGateway(),
      new NotificationService());

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
      String flightId = params.get("flightId");
      String passengerName = params.get("passengerName");

      var bookings = bookingService.getBookings(flightId, passengerName);
      sendJsonResponse(exchange, 200, bookings);
    } catch (Exception e) {
      handleException(exchange, e);
    }
  }

  private void handlePost(HttpExchange exchange) throws IOException {
    try {
      JsonNode jsonNode = readJsonBody(exchange);
      CreateBookingCommand command = mapCreateBooking(jsonNode);

      var booking = bookingService.createBooking(command);
      sendJsonResponse(exchange, 201, booking);
    } catch (Exception e) {
      handleException(exchange, e);
    }
  }

  private CreateBookingCommand mapCreateBooking(JsonNode node) {
    String flightId = requireText(node, "flightId");
    String passengerName = requireText(node, "passengerName");
    return new CreateBookingCommand(flightId, passengerName);
  }
}
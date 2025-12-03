package com.astrobookings.sales.domain.models;

public record CreateBookingCommand(String flightId, String passengerName) {
}

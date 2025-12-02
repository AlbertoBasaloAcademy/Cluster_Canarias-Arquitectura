package com.astrobookings.domain.models;

public record CreateBookingCommand(String flightId, String passengerName) {
}

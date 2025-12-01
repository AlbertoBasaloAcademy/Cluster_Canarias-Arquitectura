package com.astrobookings.business.models;

public record CreateBookingCommand(String flightId, String passengerName) {
}

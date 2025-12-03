package com.astrobookings.fleet.domain.models;

import java.time.LocalDateTime;

public record CreateFlightCommand(String rocketId, LocalDateTime departureDate, double basePrice,
    Integer minPassengers) {
}

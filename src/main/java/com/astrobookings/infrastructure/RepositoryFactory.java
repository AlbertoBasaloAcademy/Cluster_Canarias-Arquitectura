package com.astrobookings.infrastructure;

import com.astrobookings.domain.ports.BookingRepositoryPort;
import com.astrobookings.domain.ports.FlightRepositoryPort;
import com.astrobookings.domain.ports.RocketRepositoryPort;

public class RepositoryFactory {
    private static BookingRepositoryPort bookinRepositoryInstance = new BookingAdapterReposioryInMemory();
    private static FlightRepositoryPort flightRepositoryInstance = new FlightAdapterRepository();
    private static RocketRepositoryPort rocketRepositoryInstance = new RocketAdapterRepository();

    public static BookingRepositoryPort getBookingInstance() {
        return bookinRepositoryInstance;
    }

    public static FlightRepositoryPort getFlightInstance() {
        return flightRepositoryInstance;
    }

    public static RocketRepositoryPort getRocketInstance() {
        return rocketRepositoryInstance;
    }
}

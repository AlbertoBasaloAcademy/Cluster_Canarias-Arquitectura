package com.astrobookings;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.astrobookings.infrastructure.persistence.PersistenceAdapterFactory;
import com.astrobookings.infrastructure.presentation.AdminHandler;
import com.astrobookings.infrastructure.presentation.BookingHandler;
import com.astrobookings.infrastructure.presentation.FlightHandler;
import com.astrobookings.infrastructure.presentation.RocketHandler;
import com.astrobookings.infrastructure.presentation.UseCasesAdapterFactory;
import com.sun.net.httpserver.HttpServer;

public class AstroBookingsApp {
  public static void main(String[] args) throws IOException {
    // Create HTTP server on port 8080
    HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
    var rocketRepository = PersistenceAdapterFactory.getRocketRepository();
    var rocketUseCase = UseCasesAdapterFactory.getRocketsUseCase(rocketRepository);
    // Register handlers for endpoints
    server.createContext("/rockets", new RocketHandler(rocketUseCase));

    server.createContext("/flights", new FlightHandler());
    server.createContext("/bookings", new BookingHandler());
    server.createContext("/admin/cancel-flights", new AdminHandler());

    // Start server
    server.setExecutor(null); // Use default executor
    server.start();
    System.out.println("Server started at http://localhost:8080");
  }
}

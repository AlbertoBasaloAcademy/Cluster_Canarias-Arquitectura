package com.astrobookings;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

public class AstroBookingsApp {
  public static void main(String[] args) throws IOException {
    // Create HTTP server on port 8080
    HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

    // Register Fleet handlers
    server.createContext("/rockets", Config.fleetFactory.getRocketsHandler());
    server.createContext("/flights", Config.fleetFactory.getFlightsHandler());
    
    // Register Sales handlers
    server.createContext("/bookings", Config.salesFactory.getBookingsHandler());
    server.createContext("/admin/cancel-flights", Config.salesFactory.getAdminHandler());

    // Start server
    server.setExecutor(null); // Use default executor
    server.start();
    System.out.println("Server started at http://localhost:8080");
  }
}
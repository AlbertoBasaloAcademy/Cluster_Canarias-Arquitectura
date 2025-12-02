package com.astrobookings.presentation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.astrobookings.domain.model.Rocket;
import com.astrobookings.domain.ports.RocketRepositoryPort;
import com.astrobookings.infrastructure.RepositoryFactory;
import com.sun.net.httpserver.HttpExchange;

public class RocketHandler extends BaseHandler {
  private final RocketRepositoryPort rocketRepository = RepositoryFactory.getRocketInstance();

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
      response = this.objectMapper.writeValueAsString(rocketRepository.findAll());
    } catch (Exception e) {
      statusCode = 500;
      response = "{\"error\": \"Internal server error\"}";
    }

    sendResponse(exchange, statusCode, response);
  }

  private void handlePost(HttpExchange exchange) throws IOException {
    String response = "";
    int statusCode = 200;

    try {
      // Parse JSON body
      InputStream is = exchange.getRequestBody();
      String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
      Rocket rocket = this.objectMapper.readValue(body, Rocket.class);

      // Business validations mixed with input validation
      String error = validateRocket(rocket);
      if (error != null) {
        statusCode = 400;
        response = "{\"error\": \"" + error + "\"}";
      } else {
        Rocket saved = rocketRepository.save(rocket);
        statusCode = 201;
        response = this.objectMapper.writeValueAsString(saved);
      }
    } catch (Exception e) {
      statusCode = 400;
      response = "{\"error\": \"Invalid JSON or request\"}";
    }

    sendResponse(exchange, statusCode, response);
  }

  private String validateRocket(Rocket rocket) {
    if (rocket.getName() == null || rocket.getName().trim().isEmpty()) {
      return "Rocket name must be provided";
    }
    if (rocket.getCapacity() <= 0 || rocket.getCapacity() > 10) {
      return "Rocket capacity must be between 1 and 10";
    }
    // Speed is optional, no validation
    return null;
  }

}
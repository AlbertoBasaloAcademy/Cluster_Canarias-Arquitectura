package com.astrobookings.presentation;

import java.io.IOException;

import com.astrobookings.domain.RocketService;
import com.astrobookings.domain.models.CreateRocketCommand;
import com.astrobookings.infrastructure.InfrastructureAdapterFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpExchange;

public class RocketHandler extends BaseHandler {
  private final RocketService rocketService;

  public RocketHandler() {
    this.rocketService = new RocketService(InfrastructureAdapterFactory.getRocketRepository());
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
    try {
      sendJsonResponse(exchange, 200, rocketService.getAll());
    } catch (Exception e) {
      handleException(exchange, e);
    }
  }

  private void handlePost(HttpExchange exchange) throws IOException {
    try {
      JsonNode jsonNode = readJsonBody(exchange);
      CreateRocketCommand command = mapCreateRocket(jsonNode);

      var saved = rocketService.create(command);
      sendJsonResponse(exchange, 201, saved);
    } catch (Exception e) {
      handleException(exchange, e);
    }
  }

  private CreateRocketCommand mapCreateRocket(JsonNode jsonNode) {
    String name = requireText(jsonNode, "name");
    int capacity = requireInt(jsonNode, "capacity");
    Double speed = requireDouble(jsonNode, "speed");
    return new CreateRocketCommand(name, capacity, speed);
  }
}
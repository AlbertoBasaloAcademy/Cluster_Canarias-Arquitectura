package com.astrobookings.infrastructure.presentation;

import java.io.IOException;

import com.astrobookings.domain.models.CreateRocketCommand;
import com.astrobookings.domain.ports.input.RocketsUseCases;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpExchange;

public class RocketHandler extends BaseHandler {
  // private final RocketService rocketService;
  private final RocketsUseCases rocketsUseCases;
  private HttpExchange exchange;

  public RocketHandler(RocketsUseCases rocketsUseCases) {
    // this.rocketService = null;
    // this.rocketService = new
    // RocketService(InfrastructureAdapterFactory.getRocketRepository());
    this.rocketsUseCases = rocketsUseCases;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    String method = exchange.getRequestMethod();
    this.exchange = exchange;
    if ("GET".equals(method)) {
      getAllRockets();
    } else if ("POST".equals(method)) {
      handlePost(exchange);
    } else {
      this.handleMethodNotAllowed(exchange);
    }
  }

  public void getAllRockets() throws IOException {
    try {
      sendJsonResponse(this.exchange, 200, rocketsUseCases.getAllRockets());
    } catch (Exception e) {
      handleException(exchange, e);
    }
  }

  private void handlePost(HttpExchange exchange) throws IOException {
    try {
      JsonNode jsonNode = readJsonBody(exchange);
      CreateRocketCommand command = mapCreateRocket(jsonNode);

      var saved = rocketsUseCases.saveRocket(command);
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
package com.astrobookings.presentation;

import java.io.IOException;

import com.astrobookings.business.RocketService;
import com.astrobookings.business.ServiceFactory;
import com.astrobookings.business.models.CreateRocketCommand;
import com.astrobookings.business.models.ValidationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpExchange;

public class RocketHandler extends BaseHandler {
  private final RocketService rocketService = ServiceFactory.getRocketService();

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
      String response = this.objectMapper.writeValueAsString(rocketService.getAll());
      sendResponse(exchange, 200, response);
    } catch (Exception e) {
      handleException(exchange, e);
    }
  }

  private void handlePost(HttpExchange exchange) throws IOException {
    try {
      String body = readRequestBody(exchange);
      JsonNode jsonNode = this.objectMapper.readTree(body);
      CreateRocketCommand command = mapCreateRocket(jsonNode);

      var saved = rocketService.create(command);
      String response = this.objectMapper.writeValueAsString(saved);
      sendResponse(exchange, 201, response);
    } catch (Exception e) {
      handleException(exchange, e);
    }
  }

  private CreateRocketCommand mapCreateRocket(JsonNode jsonNode) {
    String name = requireText(jsonNode, "name");
    int capacity = requireInt(jsonNode, "capacity");
    Double speed = jsonNode.hasNonNull("speed") ? jsonNode.get("speed").asDouble() : null;
    return new CreateRocketCommand(name, capacity, speed);
  }

  private String requireText(JsonNode node, String fieldName) {
    JsonNode value = node.get(fieldName);
    if (value == null || value.isNull() || value.asText().isBlank()) {
      throw new ValidationException("Field '" + fieldName + "' is required");
    }
    return value.asText();
  }

  private int requireInt(JsonNode node, String fieldName) {
    JsonNode value = node.get(fieldName);
    if (value == null || value.isNull() || !value.canConvertToInt()) {
      throw new ValidationException("Field '" + fieldName + "' must be an integer");
    }
    return value.asInt();
  }
}
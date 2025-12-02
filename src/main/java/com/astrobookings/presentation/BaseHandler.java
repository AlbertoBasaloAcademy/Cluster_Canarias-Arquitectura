package com.astrobookings.presentation;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

import com.astrobookings.business.models.BusinessErrorCode;
import com.astrobookings.business.models.BusinessException;
import com.astrobookings.presentation.models.ErrorResponse;
import com.astrobookings.presentation.models.ErrorResponseMapper;
import com.astrobookings.presentation.models.ErrorResponseMapper.ErrorPayload;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public abstract class BaseHandler implements HttpHandler {

  protected final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule())
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  protected void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
    exchange.getResponseHeaders().set("Content-Type", "application/json");
    exchange.sendResponseHeaders(statusCode, response.getBytes().length);
    try (OutputStream os = exchange.getResponseBody()) {
      os.write(response.getBytes());
    }
  }

  protected void handleMethodNotAllowed(HttpExchange exchange) throws IOException {
    String response = objectMapper.writeValueAsString(new ErrorResponse("METHOD_NOT_ALLOWED", "Method not allowed"));
    sendResponse(exchange, 405, response);
  }

  protected void handleException(HttpExchange exchange, Exception exception) throws IOException {
    ErrorPayload payload = ErrorResponseMapper.from(exception);
    String response = objectMapper.writeValueAsString(payload.response());
    sendResponse(exchange, payload.statusCode(), response);
  }

  protected String readRequestBody(HttpExchange exchange) throws IOException {
    return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
  }

  protected Map<String, String> parseQuery(String query) {
    Map<String, String> params = new HashMap<>();
    if (query != null) {
      String[] pairs = query.split("&");
      for (String pair : pairs) {
        String[] keyValue = pair.split("=");
        if (keyValue.length == 2) {
          params.put(keyValue[0], keyValue[1]);
        }
      }
    }
    return params;
  }

  protected Map<String, String> getQueryParams(HttpExchange exchange) {
    String query = exchange.getRequestURI().getQuery();
    return parseQuery(query);
  }

  protected String requireText(JsonNode node, String fieldName) {
    JsonNode value = node.get(fieldName);
    if (value == null || value.isNull() || value.asText().isBlank()) {
      throw new BusinessException(BusinessErrorCode.VALIDATION, "Field '" + fieldName + "' is required");
    }
    return value.asText();
  }

  protected int requireInt(JsonNode node, String fieldName) {
    JsonNode value = node.get(fieldName);
    if (value == null || value.isNull() || !value.canConvertToInt()) {
      throw new BusinessException(BusinessErrorCode.VALIDATION, "Field '" + fieldName + "' must be an integer");
    }
    return value.asInt();
  }

  protected double requireDouble(JsonNode node, String fieldName) {
    JsonNode value = node.get(fieldName);
    if (value == null || value.isNull() || !value.isNumber()) {
      throw new BusinessException(BusinessErrorCode.VALIDATION, "Field '" + fieldName + "' must be numeric");
    }
    return value.asDouble();
  }

  protected LocalDateTime parseDate(String value) {
    try {
      return LocalDateTime.parse(value);
    } catch (DateTimeParseException exception) {
      throw new BusinessException(BusinessErrorCode.VALIDATION, "Field 'departureDate' must follow ISO-8601 format");
    }
  }

  protected JsonNode readJsonBody(HttpExchange exchange) throws IOException {
    String body = readRequestBody(exchange);
    return objectMapper.readTree(body);
  }

  protected void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
    String response = objectMapper.writeValueAsString(data);
    sendResponse(exchange, statusCode, response);
  }
}
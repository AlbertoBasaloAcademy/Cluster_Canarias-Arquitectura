package com.astrobookings.presentation;

import com.astrobookings.business.exception.BusinessErrorCode;
import com.astrobookings.business.exception.BusinessException;

public final class ErrorResponseMapper {
  private ErrorResponseMapper() {
  }

  public static ErrorPayload from(Exception exception) {
    if (exception instanceof BusinessException businessException) {
      return mapBusinessException(businessException);
    }
    return new ErrorPayload(500, new ErrorResponse("INTERNAL_ERROR", "Internal server error"));
  }

  private static ErrorPayload mapBusinessException(BusinessException exception) {
    BusinessErrorCode code = exception.getErrorCode();
    return switch (code) {
      case VALIDATION -> new ErrorPayload(400,
          new ErrorResponse("VALIDATION_ERROR", exception.getMessage()));
      case NOT_FOUND -> new ErrorPayload(404,
          new ErrorResponse("NOT_FOUND", exception.getMessage()));
      case PAYMENT -> new ErrorPayload(402,
          new ErrorResponse("PAYMENT_REQUIRED", exception.getMessage()));
      case CAPACITY -> new ErrorPayload(409,
          new ErrorResponse("CAPACITY", exception.getMessage()));
      case INTERNAL -> new ErrorPayload(500,
          new ErrorResponse("BUSINESS_ERROR", exception.getMessage()));
    };
  }

  public record ErrorPayload(int statusCode, ErrorResponse response) {
  }
}

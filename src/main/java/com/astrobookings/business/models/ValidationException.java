package com.astrobookings.business.models;

public class ValidationException extends BusinessException {
  public ValidationException(String message) {
    super(BusinessErrorCode.VALIDATION, message);
  }
}

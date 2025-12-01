package com.astrobookings.business.exception;

public class ValidationException extends BusinessException {
  public ValidationException(String message) {
    super(BusinessErrorCode.VALIDATION, message);
  }
}

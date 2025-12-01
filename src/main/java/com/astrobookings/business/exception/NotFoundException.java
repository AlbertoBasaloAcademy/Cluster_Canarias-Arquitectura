package com.astrobookings.business.exception;

public class NotFoundException extends BusinessException {
  public NotFoundException(String message) {
    super(BusinessErrorCode.NOT_FOUND, message);
  }
}

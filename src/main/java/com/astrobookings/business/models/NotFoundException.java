package com.astrobookings.business.models;

public class NotFoundException extends BusinessException {
  public NotFoundException(String message) {
    super(BusinessErrorCode.NOT_FOUND, message);
  }
}

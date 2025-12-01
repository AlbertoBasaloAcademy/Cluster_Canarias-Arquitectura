package com.astrobookings.business.exception;

public class CapacityException extends BusinessException {
  public CapacityException(String message) {
    super(BusinessErrorCode.CAPACITY, message);
  }
}

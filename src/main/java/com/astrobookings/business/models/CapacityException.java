package com.astrobookings.business.models;

public class CapacityException extends BusinessException {
  public CapacityException(String message) {
    super(BusinessErrorCode.CAPACITY, message);
  }
}

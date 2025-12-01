package com.astrobookings.business.models;

public class PaymentException extends BusinessException {
  public PaymentException(String message) {
    super(BusinessErrorCode.PAYMENT, message);
  }
}

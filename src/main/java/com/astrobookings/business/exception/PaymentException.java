package com.astrobookings.business.exception;

public class PaymentException extends BusinessException {
  public PaymentException(String message) {
    super(BusinessErrorCode.PAYMENT, message);
  }
}

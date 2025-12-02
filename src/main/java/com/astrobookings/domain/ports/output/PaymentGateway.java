package com.astrobookings.domain.ports.output;

public interface PaymentGateway {
  String processPayment(double amount) throws Exception;

  void processRefund(String transactionId);

  void processRefund(String transactionId, double amount);
}

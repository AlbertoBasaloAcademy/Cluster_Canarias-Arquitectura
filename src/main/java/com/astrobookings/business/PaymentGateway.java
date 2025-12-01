package com.astrobookings.business;

public interface PaymentGateway {
  String processPayment(double amount) throws Exception;
  void processRefund(String transactionId);
  void processRefund(String transactionId, double amount);
}

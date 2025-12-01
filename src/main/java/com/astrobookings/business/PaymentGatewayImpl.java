package com.astrobookings.business;

import java.util.UUID;

public class PaymentGatewayImpl implements PaymentGateway {
  @Override
  public String processPayment(double amount) throws Exception {
    System.out.println("[PAYMENT GATEWAY] Processing payment... Amount: " + amount);
    if (amount > 10000) {
      throw new Exception("Payment FAILED - Amount exceeds limit");
    }
    String transactionId = "TXN-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    System.out.println("[PAYMENT GATEWAY] Transaction ID: " + transactionId);
    return transactionId;
  }

  @Override
  public void processRefund(String transactionId) {
    System.out.println("[PAYMENT GATEWAY] Processing refund for transaction: " + transactionId);
  }

  @Override
  public void processRefund(String transactionId, double amount) {
    System.out.println("[PAYMENT GATEWAY] Processing refund for transaction " + transactionId + ": $" + amount);
    System.out.println("[PAYMENT GATEWAY] Refund successful for transaction " + transactionId);
  }
}
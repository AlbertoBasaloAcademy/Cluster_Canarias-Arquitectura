package com.astrobookings.domain;

import java.util.UUID;

public class PaymentGateway {
  public static String processPayment(double amount) throws Exception {
    System.out.println("[PAYMENT GATEWAY] Processing payment... Amount: " + amount);
    if (amount > 10000) {
      throw new Exception("Payment FAILED - Amount exceeds limit");
    }
    String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    System.out.println("[PAYMENT GATEWAY] Transaction ID: " + transactionId);
    return transactionId;
  }

  public static void processRefund(String transactionId) {
    System.out.println("[PAYMENT GATEWAY] Processing refund for transaction: " + transactionId);
  }

  public static void processRefund(String transactionId, double amount) {
    System.out.println("[PAYMENT GATEWAY] Processing refund for transaction " + transactionId + ": $" + amount);
    System.out.println("[PAYMENT GATEWAY] Refund successful for transaction " + transactionId);
  }
}
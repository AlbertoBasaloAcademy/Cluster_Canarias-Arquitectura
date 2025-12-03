package com.astrobookings.sales.domain.models;

import com.astrobookings.shared.domain.BusinessErrorCode;
import com.astrobookings.shared.domain.BusinessException;

public class Booking {
  private String id;
  private final String flightId;
  private final String passengerName;
  private final double finalPrice;
  private final String paymentTransactionId;

  private Booking(String id, String flightId, String passengerName, double finalPrice, String paymentTransactionId) {
    this.id = id;
    this.flightId = flightId;
    this.passengerName = passengerName;
    this.finalPrice = finalPrice;
    this.paymentTransactionId = paymentTransactionId;
  }

  public static Booking create(String flightId, String passengerName, double finalPrice, String paymentTransactionId) {
    validate(flightId, passengerName, finalPrice, paymentTransactionId);
    return new Booking(null, flightId, passengerName.trim(), finalPrice, paymentTransactionId);
  }

  public static Booking restore(String id, String flightId, String passengerName, double finalPrice,
      String paymentTransactionId) {
    return new Booking(id, flightId, passengerName, finalPrice, paymentTransactionId);
  }

  private static void validate(String flightId, String passengerName, double finalPrice, String transactionId) {
    if (flightId == null || flightId.isBlank()) {
      throw new BusinessException(BusinessErrorCode.VALIDATION, "Flight id is required for booking");
    }
    if (passengerName == null || passengerName.isBlank()) {
      throw new BusinessException(BusinessErrorCode.VALIDATION, "Passenger name cannot be empty");
    }
    if (finalPrice <= 0) {
      throw new BusinessException(BusinessErrorCode.VALIDATION, "Final price must be positive");
    }
    if (transactionId == null || transactionId.isBlank()) {
      throw new BusinessException(BusinessErrorCode.PAYMENT, "Payment transaction is required");
    }
  }

  public void assignId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public String getFlightId() {
    return flightId;
  }

  public String getPassengerName() {
    return passengerName;
  }

  public double getFinalPrice() {
    return finalPrice;
  }

  public String getPaymentTransactionId() {
    return paymentTransactionId;
  }
}

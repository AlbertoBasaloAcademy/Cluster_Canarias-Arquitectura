package com.astrobookings.persistence.models;

public class Booking {
  private String id;
  private String flightId;
  private String passengerName;
  private double finalPrice;
  private String paymentTransactionId;

  public Booking() {
  }

  public Booking(String id, String flightId, String passengerName, double finalPrice, String paymentTransactionId) {
    this.id = id;
    this.flightId = flightId;
    this.passengerName = passengerName;
    this.finalPrice = finalPrice;
    this.paymentTransactionId = paymentTransactionId;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getFlightId() {
    return flightId;
  }

  public void setFlightId(String flightId) {
    this.flightId = flightId;
  }

  public String getPassengerName() {
    return passengerName;
  }

  public void setPassengerName(String passengerName) {
    this.passengerName = passengerName;
  }

  public double getFinalPrice() {
    return finalPrice;
  }

  public void setFinalPrice(double finalPrice) {
    this.finalPrice = finalPrice;
  }

  public String getPaymentTransactionId() {
    return paymentTransactionId;
  }

  public void setPaymentTransactionId(String paymentTransactionId) {
    this.paymentTransactionId = paymentTransactionId;
  }
}
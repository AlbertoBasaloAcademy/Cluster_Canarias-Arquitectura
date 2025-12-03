package com.astrobookings.shared.domain;

/**
 * Value Object that encapsulates the passenger capacity invariants shared by
 * Fleet and Sales.
 */
public final class Capacity {
  private static final int MIN_CAPACITY = 1;
  private static final int MAX_CAPACITY = 10;

  private final int value;

  private Capacity(int value) {
    this.value = value;
  }

  public static Capacity from(int rawValue) {
    if (rawValue < MIN_CAPACITY || rawValue > MAX_CAPACITY) {
      throw new BusinessException(BusinessErrorCode.VALIDATION,
          "Capacity must be between " + MIN_CAPACITY + " and " + MAX_CAPACITY);
    }
    return new Capacity(rawValue);
  }

  public int maxPassengers() {
    return value;
  }

  public void ensureCanBoard(int currentPassengers) {
    if (currentPassengers >= value) {
      throw new BusinessException(BusinessErrorCode.CAPACITY,
          "Flight is sold out (" + currentPassengers + "/" + value + ")");
    }
  }

  public boolean isFull(int currentPassengers) {
    return currentPassengers >= value;
  }

  @Override
  public String toString() {
    return "Capacity{" + value + "}";
  }
}

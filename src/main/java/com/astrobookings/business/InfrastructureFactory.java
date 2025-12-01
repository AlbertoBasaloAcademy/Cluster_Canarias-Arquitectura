package com.astrobookings.business;

public class InfrastructureFactory {
  private static final PaymentGateway paymentGateway = new PaymentGatewayImpl();
  private static final NotificationService notificationService = new NotificationServiceImpl();

  public static PaymentGateway getPaymentGateway() {
    return paymentGateway;
  }

  public static NotificationService getNotificationService() {
    return notificationService;
  }
}

package com.bittoo.payment.stripe;

import com.bittoo.payment.config.StripeConfig;
import com.bittoo.payment.model.PaymentDetail;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerListPaymentMethodsParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentMethodCreateParams;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@ApplicationScoped
public class StripeWrapper {

  @Inject StripeConfig stripeConfig;

  public String createCustomer() {
    Stripe.apiKey = "sk_test_4eC39HqLyjWDarjtT1zdp7dc";

    CustomerCreateParams params = CustomerCreateParams.builder().build();

    try {
      Customer customer = Customer.create(params);
      return customer.getId();
    } catch (StripeException e) {
      throw new RuntimeException(e);
    }
  }

  public Uni<PaymentDetail> createPaymentMethod(PaymentDetail paymentDetail) {
    Stripe.apiKey = stripeConfig.secretKey();
    PaymentMethod paymentMethod = null;
    try {
      paymentMethod = PaymentMethod.create(PaymentMethodCreateParams.builder().build());
    } catch (StripeException e) {
      throw new RuntimeException(e);
    }
    return Uni.createFrom().item(toResource(paymentMethod));
  }

  public Map<String, String> createPaymentIntent(
      String customerId, String paymentMethodId, Boolean confirm, String returnUrl) {
    Stripe.apiKey = stripeConfig.secretKey();

    var builder = PaymentIntentCreateParams.builder();
    if (customerId != null && !customerId.equals("")) {
      builder.setCustomer(customerId);
    }
    if (paymentMethodId != null && !"".equals(paymentMethodId)) {
      builder.setPaymentMethod(paymentMethodId);
    }
    if (confirm != null) {
      builder.setConfirm(confirm);
      builder.setReturnUrl(returnUrl);
    }
    final PaymentIntentCreateParams params =
        builder
            .setAmount(99L)
            .setSetupFutureUsage(PaymentIntentCreateParams.SetupFutureUsage.OFF_SESSION)
            .setCurrency("usd")
            .setAutomaticPaymentMethods(
                PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                    .setEnabled(true)
                    .build())
            .build();
    try {
      final PaymentIntent paymentIntent = PaymentIntent.create(params);
      final Map<String, String> map = new HashMap<>();
      map.put("client_secret", paymentIntent.getClientSecret());
      return map;
    } catch (StripeException e) {
      // TODO
      e.printStackTrace();
    }
    return null;
  }

  public Multi<PaymentDetail> getPaymentMethods(String customerId) {
    List<PaymentMethod> paymentMethodCollection = null;
    Stripe.apiKey = stripeConfig.secretKey();
    try {
      paymentMethodCollection =
          Customer.retrieve(customerId)
              .listPaymentMethods(
                  CustomerListPaymentMethodsParams.builder()
                      .setType(CustomerListPaymentMethodsParams.Type.CARD)
                      .build())
              .getData();
    } catch (StripeException e) {
      log.error("Error retrieving payment methods for customerId {}", customerId, e);
      return Multi.createFrom().empty();
    }

    // TODO: error handling
    return Multi.createFrom().items(paymentMethodCollection.stream().map(this::toResource));
  }

  public Uni<PaymentDetail> getPaymentMethod(String paymentMethodId) {
    PaymentMethod paymentMethodCollection = null;
    Stripe.apiKey = stripeConfig.secretKey();
    try {
      paymentMethodCollection = PaymentMethod.retrieve(paymentMethodId);
    } catch (StripeException e) {
      log.error("Error retrieving payment method for paymentId {}", paymentMethodId, e);
      return Uni.createFrom().nullItem();
    }

    // TODO: error handling
    return Uni.createFrom().item(paymentMethodCollection).map(this::toResource);
  }

  private PaymentDetail toResource(PaymentMethod pm) {
    PaymentMethod.Card c = pm.getCard();
    return PaymentDetail.builder()
        .id(pm.getId())
        .creditCard(
            PaymentDetail.CreditCard.builder()
                .brand(c.getBrand())
                .checks(c.getChecks())
                .country(c.getCountry())
                .description(c.getDescription())
                .expMonth(c.getExpMonth())
                .expYear(c.getExpYear())
                .last4(c.getLast4())
                .build())
        .customerId(pm.getCustomer())
        .build();
  }
}

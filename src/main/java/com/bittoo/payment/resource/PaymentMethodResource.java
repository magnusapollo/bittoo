package com.bittoo.payment.resource;

import com.bittoo.payment.model.PaymentDetail;
import com.bittoo.payment.stripe.StripeWrapper;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Map;

@RequestScoped
@Path("/v1/payment-methods")
public class PaymentMethodResource {

  @Inject StripeWrapper stripe;

  @GET
  public Multi<PaymentDetail> getPaymentMethods(@QueryParam("customerId") String customerId) {
    if (customerId == null) {
      return Multi.createFrom().empty();
    }
    return stripe.getPaymentMethods(customerId);
  }

  @GET
  @Path("id")
  public Uni<PaymentDetail> getPaymentMethod(
      @QueryParam("paymentMethodId") String paymentMethodId) {
    return stripe
        .getPaymentMethod(paymentMethodId)
        .onItem()
        .ifNull()
        .failWith(new NotFoundException());
  }

  @POST
  public Uni<PaymentDetail> createPaymentMethod(PaymentDetail paymentDetail) {
    return stripe.createPaymentMethod(paymentDetail);
  }

  @POST
  @Path("/intent")
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, String> createPaymentIntent(
      @QueryParam("saveNew") Boolean saveNew,
      @QueryParam("paymentMethodId") String paymentMethodId,
      @QueryParam("confirm") Boolean confirm,
      @QueryParam("customerId") String customerId,
      @QueryParam("returnUrl") String returnUrl) {
    String customer = customerId;
    if (saveNew != null && saveNew) {
      customer = stripe.createCustomer();
    }
    return stripe.createPaymentIntent(customer, paymentMethodId, confirm, returnUrl);
  }
}

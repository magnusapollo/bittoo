package com.bittoo.checkout.client;

import com.bittoo.payment.model.PaymentDetail;
import io.smallrye.mutiny.Multi;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

@RegisterRestClient(configKey = "payment")
public interface PaymentClient {
  @GET
  @Path("/v1/payment-methods/{id}")
  @ClientHeaderParam(name = "Content-Type", value = MediaType.APPLICATION_JSON)
  Multi<PaymentDetail> get(String id);
}

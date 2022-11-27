package com.bittoo.checkout.client;

import com.bittoo.cart.model.Cart;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

@RegisterRestClient(configKey = "cart")
public interface CartClient {
  @GET
  @Path("/v1/carts/{id}")
  @ClientHeaderParam(name = "Content-Type", value = MediaType.APPLICATION_JSON)
  Uni<Cart> get(String id);
}

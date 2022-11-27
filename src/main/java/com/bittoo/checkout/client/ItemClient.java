package com.bittoo.checkout.client;

import com.bittoo.item.model.Item;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

@RegisterRestClient(configKey = "item")
public interface ItemClient {
  @GET
  @Path("/v1/items/{id}")
  @ClientHeaderParam(name = "Content-Type", value = MediaType.APPLICATION_JSON)
  Uni<Item> get(String id);
}

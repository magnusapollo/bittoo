package com.bittoo.order.resource;

import com.bittoo.order.model.Order;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@RequestScoped
@Path("/v1/orders")
public class OrderResource {

    @GET
    @Path("{id}")
    public Uni<Order> get(String id) {
        return null;
    }

    @POST
    public Uni<Order> create(Order order) {
        return null;
    }
}

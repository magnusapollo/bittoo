package com.bittoo.checkout.resource;

import com.bittoo.cart.model.Cart;
import com.bittoo.cart.model.CartItem;
import com.bittoo.checkout.client.CartClient;
import com.bittoo.checkout.client.ItemClient;
import com.bittoo.checkout.db.entity.CheckoutAddressEntity;
import com.bittoo.checkout.db.entity.CheckoutEntity;
import com.bittoo.checkout.model.Checkout;
import com.bittoo.checkout.model.PaymentMethod;
import com.bittoo.checkout.model.ShippingAddress;
import com.bittoo.checkout.model.TaxInfo;
import com.bittoo.item.model.Item;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/v1/carts/{cartId}/checkout")
@RequestScoped
@Slf4j
public class CheckoutResource {
  @RestClient CartClient cartClient;

  @RestClient ItemClient itemClient;

  @GET
  @Path("/payment")
  @Produces(MediaType.APPLICATION_JSON)
  public Uni<PaymentMethod> getPayment(String cartId) {

    return null;
  }

  @POST
  @Path("/payment")
  @Consumes(MediaType.APPLICATION_JSON)
  public Uni<PaymentMethod> updatePayment(String cartId, PaymentMethod payment) {

    return null;
  }

  @GET
  @Path("/shipping")
  @Produces(MediaType.APPLICATION_JSON)
  public Uni<ShippingAddress> getShipping(String cartId) {

    return null;
  }

  @POST
  @Path("/shipping")
  @Consumes(MediaType.APPLICATION_JSON)
  public Uni<Checkout> updateShipping(String cartId, ShippingAddress shipping) {
    shipping.setAddressType(ShippingAddress.AddressType.SHIPPING);
    Uni<Checkout> checkoutUni =
        Panache.withTransaction(
                () ->
                    CheckoutEntity.<CheckoutEntity>find("cartId", UUID.fromString(cartId))
                        .firstResult()
                        .onItem()
                        .ifNull()
                        .continueWith(CheckoutEntity::new)
                        .map(
                            checkoutEntity -> {
                              checkoutEntity.setCartId(UUID.fromString(cartId));
                              checkoutEntity.setShipping(toEntity(null, shipping));
                              return checkoutEntity;
                            })
                        .flatMap(checkoutEntity -> checkoutEntity.<CheckoutEntity>persist()))
            .map(this::toResource);
    Uni<BigDecimal> shippingFee = calculateShippingFee(shipping);

    return Uni.combine()
        .all()
        .unis(checkoutUni, shippingFee)
        .combinedWith(
            (c, sp) -> {
              c.setShippingFee(sp);
              return c;
            });
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Uni<Checkout> get(String id) {
    return CheckoutEntity.<CheckoutEntity>findById(UUID.fromString(id)).map(this::toResource);
  }

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  public Uni<Checkout> create(@PathParam("cartId") String cartId, ShippingAddress shipping) {
    // calculate shipping fee (?),
    // get payment info, calculate taxInfo
    shipping.setAddressType(ShippingAddress.AddressType.SHIPPING);
    Uni<Cart> cartUni = cartClient.get(cartId);

    Multi<CartItem> cartItemMulti =
        cartUni
            .map(Cart::getCartItems)
            .onItem()
            .transformToMulti(list -> Multi.createFrom().iterable(list));
    Multi<Integer> quantityStream = cartItemMulti.map(CartItem::getQuantity);

    Multi<Item> itemMulti =
        cartItemMulti
            .map(CartItem::getItemId)
            .onItem()
            .transformToUni(itemClient::get)
            .collectFailures()
            .concatenate(); // This has to be in-order, (hence concatenate), to correspond to
    // quantity stream
    Multi<Tuple2<Item, Integer>> tupleMulti =
        Multi.createBy().combining().streams(itemMulti, quantityStream).asTuple();

    Uni<BigDecimal> totalPriceOfItems =
        tupleMulti
            .map(
                tuple ->
                    new BigDecimal(tuple.getItem1().getPrice())
                        .multiply(BigDecimal.valueOf(tuple.getItem2())))
            .collect()
            .with(Collectors.reducing(BigDecimal.ZERO, BigDecimal::add));

    Uni<BigDecimal> tax = calculateTax(itemMulti);
    Uni<BigDecimal> shippingFee = calculateShippingFee(shipping);
    Uni<Checkout> checkoutDBUni = getCheckoutDBUni(cartId, shipping);
    return Uni.combine()
        .all()
        .unis(checkoutDBUni, totalPriceOfItems, tax, shippingFee)
        .combinedWith(
            (c, tp, tx, sf) -> {
              c.setTotalPrice(tp);
              c.setTaxInfo(TaxInfo.builder().estimatedTax(tx).build());
              c.setShippingFee(sf);
              return c;
            });
  }

  private Uni<BigDecimal> calculateShippingFee(ShippingAddress shippingAddress) {
    return Uni.createFrom().item(new BigDecimal("10.0"));
  }

  private Uni<Checkout> getCheckoutDBUni(String cartId, ShippingAddress shippingAddress) {
    return Panache.withTransaction(
            () ->
                CheckoutEntity.<CheckoutEntity>find("cartId", UUID.fromString(cartId))
                    .firstResult()
                    .onItem()
                    .ifNull()
                    .continueWith(CheckoutEntity::new)
                    .map(
                        checkoutEntity -> {
                          checkoutEntity.setCartId(UUID.fromString(cartId));
                          checkoutEntity.setShipping(toEntity(null, shippingAddress));
                          return checkoutEntity;
                        })
                    .flatMap(checkoutEntity -> checkoutEntity.<CheckoutEntity>persist()))
        .map(this::toResource);
  }

  private ShippingAddress toResource(CheckoutAddressEntity entity) {
    if (entity == null) {
      return null;
    }
    return ShippingAddress.builder()
        .name(entity.getName())
        .line1(entity.getLine1())
        .line2(entity.getLine2())
        .line3(entity.getLine3())
        .city(entity.getCity())
        .state(entity.getState())
        .country(entity.getCountry())
        .addressType(ShippingAddress.AddressType.valueOf(entity.getAddressType()))
        .build();
  }

  private Checkout toResource(CheckoutEntity entity) {
    if (entity == null) {
      return null;
    }
    final ShippingAddress a = toResource(entity.getShipping());
    return Checkout.builder()
        .id(entity.getId().toString())
        .shippingAddress(a)
        .cartId(entity.getCartId().toString())
        .build();
  }

  private Uni<BigDecimal> calculateTax(Multi<Item> itemMulti) {
    return Uni.createFrom().item(new BigDecimal("10.0"));
  }

  private CheckoutAddressEntity toEntity(CheckoutAddressEntity existing, ShippingAddress resource) {
    if (existing == null) {
      existing = new CheckoutAddressEntity();
    }
    existing.setName(resource.getName());
    existing.setLine1(resource.getLine1());
    existing.setLine2(resource.getLine2());
    existing.setLine3(resource.getLine3());
    existing.setCity(resource.getCity());
    existing.setState(resource.getState());
    existing.setZipcode(resource.getZip());
    existing.setCountry(resource.getCountry());
    existing.setAddressType(resource.getAddressType().toString());
    return existing;
  }
}

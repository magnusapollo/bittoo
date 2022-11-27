package com.bittoo.checkout.resource;

import com.bittoo.cart.model.Cart;
import com.bittoo.cart.model.CartItem;
import com.bittoo.checkout.client.CartClient;
import com.bittoo.checkout.client.ItemClient;
import com.bittoo.checkout.db.entity.AddressEntity;
import com.bittoo.checkout.db.entity.CheckoutEntity;
import com.bittoo.checkout.model.Address;
import com.bittoo.checkout.model.Checkout;
import com.bittoo.checkout.model.PaymentMethod;
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
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
  public Uni<Address> getShipping(String cartId) {

    return null;
  }

  @POST
  @Path("/shipping")
  @Consumes(MediaType.APPLICATION_JSON)
  public Uni<Checkout> updateShipping(String cartId, Address shipping) {
    shipping.setAddressType(Address.AddressType.SHIPPING);
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
                          checkoutEntity.setShipping(toEntity(null, shipping));
                          return checkoutEntity;
                        })
                    .flatMap(checkoutEntity -> checkoutEntity.<CheckoutEntity>persist()))
        .map(this::toResource);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Uni<Checkout> get(String cartId) {
    // If customer is logged in, get address, calculate shipping fee (?),
    // get payment info, calculate taxInfo
    String customerId = getCustomer();
    Optional<Address> shipping = Optional.empty();
    if (isLoggedIn()) {
      shipping =
          // TODO: get customer address
          Stream.of(Address.builder().build(), Address.builder().build())
              .filter(a -> Address.AddressType.SHIPPING.equals(a.getAddressType()))
              .findFirst();
      if (shipping.isPresent()) {
        // calculate shipping fees?

      }
    }

    Multi<CartItem> cartItemMulti =
        cartClient
            .get(cartId)
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

    Uni<BigDecimal> tax = calculateTax();

    Uni<Checkout> checkoutDBUni = getCheckoutDBUni(cartId, shipping.orElse(null));
    return Uni.combine()
        .all()
        .unis(checkoutDBUni, totalPriceOfItems, tax)
        .combinedWith(
            (c, tp, tx) -> {
              c.setTotalPrice(tp);
              c.setTaxInfo(TaxInfo.builder().estimatedTax(tx).build());
              return c;
            });
  }

  private boolean isLoggedIn() {
    // TODO:
    return false;
  }

  private Uni<Checkout> getCheckoutDBUni(String cartId, Address shipping) {
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
                          if (shipping != null) {
                            checkoutEntity.setShipping(
                                toEntity(checkoutEntity.getShipping(), shipping));
                          }
                          return checkoutEntity;
                        })
                    .flatMap(checkoutEntity -> checkoutEntity.<CheckoutEntity>persist()))
        .map(this::toResource);
  }

  private Address toResource(AddressEntity entity) {
    if (entity == null) {
      return null;
    }
    return Address.builder()
        .line1(entity.getLine1())
        .line2(entity.getLine2())
        .line3(entity.getLine3())
        .city(entity.getCity())
        .state(entity.getState())
        .country(entity.getCountry())
        .addressType(Address.AddressType.valueOf(entity.getAddressType()))
        .build();
  }

  private Checkout toResource(CheckoutEntity entity) {
    if (entity == null) {
      return null;
    }
    final Address a = toResource(entity.getShipping());
    return Checkout.builder()
        .id(entity.getId().toString())
        .shippingAddress(a)
        .carId(entity.getCartId().toString())
        .build();
  }

  private String getCustomer() {
    return "1";
  }

  private Uni<BigDecimal> calculateTax() {
    return Uni.createFrom().item(new BigDecimal("10.0"));
  }

  private AddressEntity toEntity(AddressEntity existing, Address resource) {
    if (existing == null) {
      existing = new AddressEntity();
    }
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

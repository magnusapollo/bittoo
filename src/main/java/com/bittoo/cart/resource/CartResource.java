package com.bittoo.cart.resource;

import com.bittoo.cart.db.entity.CartEntity;
import com.bittoo.cart.db.entity.CartItemEntity;
import com.bittoo.cart.model.Cart;
import com.bittoo.cart.model.CartItem;
import com.bittoo.checkout.client.ItemClient;
import com.bittoo.item.model.Item;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestHeader;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;

@Slf4j
@Path("/v1/carts")
@RequestScoped
public class CartResource {

  @RestClient ItemClient itemClient;

  @GET
  @Path("{id}")
  public Uni<Cart> get(String id) {
    log.info("Get cart by id {}", id);
    // We need a panache transaction here because we also do a itemclient::get which is also a
    // transaction.Should be able to remove this with different servers for item and cart
    return Panache.withTransaction(
        () -> {
          Uni<Cart> cart =
              CartEntity.<CartEntity>findById(UUID.fromString(id)).map(this::toResource);
          Uni<BigDecimal> totalPriceOfItems = getTotalPrice(cart);
          return Uni.combine()
              .all()
              .unis(cart, totalPriceOfItems)
              .combinedWith(
                  (c, p) -> {
                    c.setTotalPrice(p);
                    return c;
                  });
        });
  }

  @GET
  public Uni<Cart> getCartForUser(@RestHeader("account_id") String accountId) {
    if (accountId == null || "".equals(accountId)) return Uni.createFrom().nullItem();
    // get userId from context
    return CartEntity.<CartEntity>find("account_id", UUID.fromString(accountId))
        .firstResult()
        .map(this::toResource);
  }

  @POST
  public Uni<String> create(@RestHeader("account_id") String accountId) {
    CartEntity cartEntity = toEntity(null, new Cart());
    cartEntity.setAccountId(UUID.fromString(accountId));
    return Panache.<CartEntity>withTransaction(cartEntity::persist)
        .map(inserted -> inserted.getId().toString());
  }

  @PUT
  @Path("{id}")
  // TODO
  public Uni<Response> update(String id, Cart cart) {
    return Uni.createFrom().item(Response.ok().build());
  }

  @DELETE
  @Path("{id}")
  // TODO
  public Uni<Response> delete(String id) {
    return Uni.createFrom().item(Response.ok().build());
  }

  @GET
  @Path("{id}/items/{itemId}")
  public Uni<Response> getItem(String id, String itemId) {
    return CartEntity.<CartEntity>findById(UUID.fromString(id))
        .onItem()
        .transform(
            (entity) ->
                entity.getCartItems().stream()
                    .filter(itemResource -> itemResource.getId().toString().equals(itemId))
                    .findAny()
                    .orElse(null))
        .onItem()
        .ifNull()
        .failWith(new NotFoundException())
        .onItem()
        .ifNotNull()
        .transform(this::toResourceItem)
        .onItem()
        .transform(cartItem -> Response.ok(cartItem).build());
  }

  @POST
  @Path("{id}/items")
  public Uni<Response> addItem(@PathParam("id") String cartId, CartItem newItem)
      throws ExecutionException, InterruptedException {
    // 1. get existing cart and cartItems
    // 2. loop through cart item
    // 3. if item already in cart, update qty
    // 4. else create item in cart
    return Panache.withTransaction(
            () ->
                CartEntity.<CartEntity>findById(UUID.fromString(cartId))
                    .onItem()
                    .transform(
                        (entity) -> {
                          var resource = toResource(entity);
                          if (resource.getCartItems() == null) {
                            resource.setCartItems(new ArrayList<>());
                          }
                          resource.getCartItems().add(newItem);
                          var mergedItems = this.mergeItems(resource.getCartItems());
                          resource.setCartItems(mergedItems);
                          toEntity(entity, resource);
                          return entity;
                        })
                    .flatMap(cartEntity -> cartEntity.<CartEntity>persist()))
        .onItem()
        .transform(this::toResource)
        .onItem()
        .transform(updated -> Response.ok(updated).build());
  }

  @PUT
  @Path("{id}/items/{itemId}")
  public Uni<Response> updateItem(String id, String itemId, CartItem cartItem) {
    return Panache.withTransaction(
            () ->
                CartEntity.<CartEntity>findById(UUID.fromString(id))
                    .onItem()
                    .transform(
                        (entity) -> {
                          entity.getCartItems().stream()
                              .filter(
                                  itemResource -> itemResource.getId().toString().equals(itemId))
                              .findAny()
                              .ifPresent(cartItemEntity -> toEntityItem(cartItemEntity, cartItem));
                          return entity;
                        })
                    .flatMap(cartEntity -> cartEntity.<CartEntity>persist()))
        .onItem()
        .transform(this::toResource)
        .onItem()
        .transform(updated -> Response.ok(updated).build());
  }

  @DELETE
  @Path("{id}/items/{itemId}")
  public Uni<Response> deleteItem(String id, String itemId) {
    return Panache.withTransaction(
            () ->
                CartEntity.<CartEntity>findById(UUID.fromString(id))
                    .onItem()
                    .transform(
                        (entity) -> {
                          if (entity.getCartItems() == null) return null;
                          entity
                              .getCartItems()
                              .removeIf(
                                  itemResource -> itemResource.getId().toString().equals(itemId));
                          return entity;
                        }))
        .onItem()
        .ifNotNull()
        .transform(this::toResource)
        .onItem()
        .transform(updated -> Response.ok(updated).build());
  }

  // Visible for testing
  List<CartItem> mergeItems(List<CartItem> cartItems) {
    final Map<String, Optional<CartItem>> cartItemsByItemId =
        cartItems.stream()
            .collect(
                groupingBy(
                    CartItem::getItemId,
                    reducing(
                        (c1, c2) -> {
                          c1.setQuantity(c1.getQuantity() + c2.getQuantity());
                          return c1;
                        })));
    return cartItemsByItemId.values().stream()
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }

  private Cart toResource(CartEntity entity) {
    if (entity == null) return null;
    Cart resource = new Cart();
    List<CartItemEntity> itemEntities = entity.getCartItems();
    if (itemEntities != null && !itemEntities.isEmpty()) {
      resource.setCartItems(
          itemEntities.stream().map(this::toResourceItem).collect(Collectors.toList()));
      double totalPrice = itemEntities.stream().mapToDouble(CartItemEntity::getQuantity).sum();
      resource.setTotalPrice(BigDecimal.valueOf(totalPrice));
    }
    resource.setId(entity.getId().toString());
    return resource;
  }

  private CartItem toResourceItem(CartItemEntity itemEntity) {
    if (itemEntity == null) return null;
    CartItem itemResource = new CartItem();
    itemResource.setItemId(itemEntity.getItemId().toString());
    if (itemEntity.getId() != null) {
      itemResource.setId(itemEntity.getId().toString());
    }
    itemResource.setQuantity(itemEntity.getQuantity());
    return itemResource;
  }

  private CartEntity toEntity(CartEntity entity, Cart resource) {
    if (entity == null) {
      entity = new CartEntity();
    }
    if (entity.getCartItems() == null) {
      entity.setCartItems(new ArrayList<>());
    }
    if (resource.getCartItems() != null) {
      var resourceItems =
          resource.getCartItems().stream()
              .filter(resourceItem -> resourceItem.getId() != null)
              .collect(Collectors.toList());

      var idItemMap = resourceItems.stream().collect(groupingBy(CartItem::getId));
      entity
          .getCartItems()
          .forEach(
              itemEntity -> {
                UUID itemEntityId = itemEntity.getId();
                CartItem itemResource = idItemMap.get(itemEntityId.toString()).get(0);
                if (itemResource == null) {
                  itemEntity.delete();
                } else {
                  toEntityItem(itemEntity, itemResource);
                }
              });
      var newItems =
          resource.getCartItems().stream()
              .filter(resourceItem -> resourceItem.getId() == null)
              .collect(Collectors.toList());
      entity.addAll(
          newItems.stream()
              .map(newItem -> toEntityItem(null, newItem))
              .collect(Collectors.toList()));
    }
    return entity;
  }

  private Uni<BigDecimal> getTotalPrice(Uni<Cart> cart) {
    Multi<CartItem> cartItemMulti =
        cart.map(Cart::getCartItems)
            .onItem()
            .ifNotNull()
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

    return tupleMulti
        .map(
            tuple ->
                new BigDecimal(tuple.getItem1().getPrice())
                    .multiply(BigDecimal.valueOf(tuple.getItem2())))
        .collect()
        .with(Collectors.reducing(BigDecimal.ZERO, BigDecimal::add));
  }

  private CartItemEntity toEntityItem(CartItemEntity itemEntity, CartItem itemResource) {
    if (itemEntity == null) {
      itemEntity = new CartItemEntity();
    }
    if (itemResource.getItemId() != null) {
      itemEntity.setItemId(UUID.fromString(itemResource.getItemId()));
    }
    itemEntity.setQuantity(itemResource.getQuantity());
    return itemEntity;
  }
}

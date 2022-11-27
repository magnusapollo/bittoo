package com.bittoo.cart.db.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity(name = "cart")
@Table(name = "cart")
@Data
public class CartEntity extends PanacheEntityBase {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  UUID id;

  @Column(name = "account_id")
  String accountId;

  // Bi-directional so that its easy to navigate for eg: getCart().getCartItems();
  @OneToMany(
      mappedBy = "cart",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.EAGER)
  List<CartItemEntity> cartItems;

  public void addAll(List<CartItemEntity> cartItem) {
    cartItems.addAll(cartItem);
    cartItem.forEach(ci -> ci.setCart(this));
  }

  public void removeItem(CartItemEntity cartItem) {
    cartItems.remove(cartItem);
    cartItem.setCart(null);
  }
}

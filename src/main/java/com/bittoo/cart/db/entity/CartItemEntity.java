package com.bittoo.cart.db.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.UUID;

// do not use `@Data` as it will generate equals and Hashcode which do not work well with hibernate
@Getter
@Setter
@Entity(name = "cart_item")
@Table(name = "cart_item")
public class CartItemEntity extends PanacheEntityBase {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  private UUID id;

  @Column(name = "item_id")
  private UUID itemId;

  @Column private int quantity;

  @ManyToOne
  @JoinColumn(name = "cart_id", foreignKey = @ForeignKey(name = "CART_ID_FK"))
  private CartEntity cart;
}

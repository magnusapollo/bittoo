package com.bittoo.checkout.db.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity(name = "checkout")
@Table(name = "checkout")
@Data
@EqualsAndHashCode(callSuper = true)
public class CheckoutEntity extends PanacheEntityBase {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  UUID id;

  @Column(name = "cart_id")
  UUID cartId;

  @Embedded AddressEntity shipping;
}

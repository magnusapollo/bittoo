package com.bittoo.checkout.db.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.UUID;

@Embeddable
public class PaymentMethodEntity extends PanacheEntityBase {

  @Column(name = "payment_id")
  UUID paymentId;
}

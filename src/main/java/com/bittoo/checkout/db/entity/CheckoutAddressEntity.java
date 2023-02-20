package com.bittoo.checkout.db.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@EqualsAndHashCode(callSuper = true)
@Embeddable
@Data
public class CheckoutAddressEntity extends PanacheEntityBase {

  @Column(name = "name")
  private String name;

  @Column(name = "line_1")
  private String line1;

  @Column(name = "line_2")
  private String line2;

  @Column(name = "line_3")
  private String line3;

  @Column(name = "city")
  private String city;

  @Column(name = "state")
  private String state;

  @Column(name = "zip_code")
  private String zipcode;

  @Column(name = "country")
  private String country;

  @Column(name = "addr_type")
  private String addressType;
}

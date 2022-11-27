package com.bittoo.checkout.db.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@EqualsAndHashCode(callSuper = true)
@Embeddable
@Data
public class AddressEntity extends PanacheEntityBase {

  @Column(name = "line_1")
  String line1;

  @Column(name = "line_2")
  String line2;

  @Column(name = "line_3")
  String line3;

  @Column(name = "city")
  String city;

  @Column(name = "state")
  String state;

  @Column(name = "zip_code")
  String zipcode;

  @Column(name = "country")
  String country;

  @Column(name = "addr_type")
  String addressType;
}

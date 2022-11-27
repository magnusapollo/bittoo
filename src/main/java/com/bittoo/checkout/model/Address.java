package com.bittoo.checkout.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Address {
  String line1;
  String line2;
  String line3;
  String city;
  String state;
  String zip;
  String country;
  AddressType addressType;

  public enum AddressType {
    SHIPPING,
    BILLING
  }
}

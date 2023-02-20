package com.bittoo.checkout.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShippingAddress {
  private String name;
  private String line1;
  private String line2;
  private String line3;
  private String city;
  private String state;
  private String zip;
  private String country;
  private AddressType addressType;

  public enum AddressType {
    SHIPPING,
    BILLING
  }
}

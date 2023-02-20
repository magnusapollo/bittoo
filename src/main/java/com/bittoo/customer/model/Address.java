package com.bittoo.customer.model;

import lombok.Data;

@Data
public class Address {
  private String line1;
  private String line2;
  private String line3;
  private String city;
  private String state;
  private String zip;
  private Boolean primary;
}

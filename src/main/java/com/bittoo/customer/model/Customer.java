package com.bittoo.customer.model;

import lombok.Data;

import java.util.List;

@Data
public class Customer {

  private String id;

  private String fullName;

  private String email;

  private List<Address> addresses;
}

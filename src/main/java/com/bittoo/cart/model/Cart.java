package com.bittoo.cart.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class Cart {
  private String id;
  private BigDecimal totalPrice;
  private List<CartItem> cartItems;
  private String customerId;
}

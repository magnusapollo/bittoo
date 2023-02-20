package com.bittoo.cart.model;

import lombok.Data;

@Data
public class CartItem {
  private String id;
  private String productId;
  private String itemId;
  private int quantity;
}

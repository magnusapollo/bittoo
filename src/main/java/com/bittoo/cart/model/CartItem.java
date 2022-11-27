package com.bittoo.cart.model;

import lombok.Data;

@Data
public class CartItem {
  String id;
  String productId;
  String itemId;
  int quantity;
}

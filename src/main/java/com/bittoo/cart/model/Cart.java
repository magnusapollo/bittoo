package com.bittoo.cart.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class Cart {
  String id;
  BigDecimal totalPrice;
  List<CartItem> cartItems;
}

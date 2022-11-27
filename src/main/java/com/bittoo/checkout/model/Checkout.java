package com.bittoo.checkout.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class Checkout {
  String id;
  /**
   * cartId to make GET idempotent If you update items in cart, GET on checkout should still give
   * you same result;
   *
   * <p>Disadvantage: Client will have to make another call to get info for items in cart
   */
  String carId;
  // shipping address;
  Address shippingAddress;
  // billing info;
  PaymentMethod paymentMethod;
  // tax info
  TaxInfo taxInfo;
  // shipping fee
  BigDecimal shippingFee;

  BigDecimal totalPrice;
}

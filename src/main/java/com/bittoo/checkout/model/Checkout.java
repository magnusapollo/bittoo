package com.bittoo.checkout.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class Checkout {
  private String id;
  /**
   * cartId instead of items to make GET idempotent If you update items in cart, GET on checkout
   * should still give you same result;
   *
   * <p>Disadvantage: Client will have to make another call to get info for items in cart
   */
  private String cartId;
  // shipping address;
  private ShippingAddress shippingAddress;
  // billing info;
  private PaymentMethod paymentMethod;
  // tax info
  private TaxInfo taxInfo;
  // shipping fee
  private BigDecimal shippingFee;

  BigDecimal totalPrice;
}

package com.bittoo.item.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Price {
  private final String price;
  private final String currency;
  private final PriceType priceType;

  public static enum PriceType {
    listPrice,
    offerPrice
  }
}

package com.bittoo.checkout.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TaxInfo {
  BigDecimal estimatedTax;
}

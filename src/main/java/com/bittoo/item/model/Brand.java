package com.bittoo.item.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Brand {
  private final String id;
  private final String name;
}

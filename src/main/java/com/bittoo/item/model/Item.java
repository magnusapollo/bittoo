package com.bittoo.item.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class Item {
  private String id;
  private String title;
  private Double price;
  private String flavor;
  private Map<String, String> otherInfo;
}

package com.bittoo.item.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class Product {

  private String id;

  private String title;

  private String shortDescription;

  private String longDescription;

  private String otherInfo;

  private Brand brand;

  private int petType;

  private Set<Item> items;

  private Set<Category> categories;

  // computed - price (lowest price among items)
  private List<Price> prices;
}

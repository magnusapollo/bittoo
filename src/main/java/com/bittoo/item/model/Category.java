package com.bittoo.item.model;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class Category {
  private Long id;
  private String title;
  private Set<Category> children;
}

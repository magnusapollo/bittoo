package com.bittoo.item.db.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity(name = "brand_priority")
@Table(name = "brand_priority")
@Data
public class BrandPriorityEntity extends PanacheEntityBase {
  @Id private int id;

  @Column(name = "description")
  private String description;

  @OneToMany(
      mappedBy = "brandPriority",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.EAGER)
  private List<BrandEntity> brands;
}

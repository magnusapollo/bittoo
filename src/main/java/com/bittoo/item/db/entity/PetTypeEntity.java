package com.bittoo.item.db.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity(name = "pet_type")
@Table(name = "pet_type")
@Data
public class PetTypeEntity extends PanacheEntityBase {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;

  @Column(name = "description")
  private String description;

  @OneToMany(
      mappedBy = "petType",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.EAGER)
  private List<ProductEntity> products;
}

package com.bittoo.item.db.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Set;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity(name = "product")
@Table(name = "product")
@Data
public class ProductEntity extends PanacheEntityBase {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  private UUID id;

  @Column(name = "title")
  private String title;

  @Column(name = "short_description")
  private String shortDescription;

  @Column(name = "long_description")
  private String longDescription;

  @Column(name = "other_info")
  private String otherInfo;

  @OneToMany(
      mappedBy = "product",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private Set<ItemEntity> items;

  @ManyToOne
  @JoinColumn(name = "brand_id", foreignKey = @ForeignKey(name = "fk002"))
  private BrandEntity brand;

  @ManyToOne
  @JoinColumn(name = "pet_type_id", foreignKey = @ForeignKey(name = "fk003"))
  private PetTypeEntity petType;

  @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
  @JoinTable(
      name = "cat_prod",
      joinColumns = {@JoinColumn(name = "product_id")},
      inverseJoinColumns = {@JoinColumn(name = "category_id")})
  private Set<CategoryEntity> categories;
}

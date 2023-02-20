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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity(name = "brand")
@Table(name = "brand")
@Data
public class BrandEntity extends PanacheEntityBase {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  private UUID id;

  @Column(name = "name")
  private String name;

  @ManyToOne
  @JoinColumn(name = "priority_id", foreignKey = @ForeignKey(name = "fk001"))
  private BrandPriorityEntity brandPriority;

  @Column(name = "ranking")
  private int ranking;

  @OneToMany(
      mappedBy = "brand",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.EAGER)
  private List<ProductEntity> products;
}

package com.bittoo.item.db.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Objects;
import java.util.UUID;

@Entity(name = "item")
@Table(name = "item")
@Data
public class ItemEntity extends PanacheEntityBase {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  private UUID id;

  @Column(name = "title")
  private String title;

  @Column(name = "price")
  private Double price;

  @Column(name = "flavor")
  private String flavor;

  @Column(name = "other_info")
  private String otherInfo;

  @ManyToOne
  @JoinColumn(name = "product_id", foreignKey = @ForeignKey(name = "fk_001"))
  private ProductEntity product;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ItemEntity that = (ItemEntity) o;
    return id == null ? Objects.equals(flavor, that.flavor) : Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}

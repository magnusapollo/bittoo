package com.bittoo.item.db.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity(name = "category")
@Table(name = "category")
@Data
public class CategoryEntity extends PanacheEntityBase {

  @Id
  @Column(name = "id")
  private Long id;

  @OneToOne
  @JoinColumn(name = "parent_id")
  private CategoryEntity parent;

  @OneToMany(mappedBy = "parent", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  private Set<CategoryEntity> children = new HashSet<>();

  @Column(name = "title")
  private String title;

  @Column(name = "ranking")
  private Integer ranking;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CategoryEntity that = (CategoryEntity) o;
    return id == null ? Objects.equals(title, that.title) : Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}

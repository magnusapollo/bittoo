package com.bittoo.customer.db.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.GenericGenerator;

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
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity(name = "customer")
@Table(name = "customer")
@Data
public class CustomerEntity extends PanacheEntityBase {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  private UUID id;

  @Column(name = "full_name")
  private String fullName;

  @Column(name = "email")
  private String email;

  @OneToMany(
      mappedBy = "customer",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.EAGER)
  private List<AddressEntity> addresses;
}

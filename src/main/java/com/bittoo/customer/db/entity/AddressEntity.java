package com.bittoo.customer.db.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity(name = "address")
@Table(name = "address")
@Data
public class AddressEntity extends PanacheEntityBase {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  private UUID id;

  @Column(name = "line1")
  private String line1;

  @Column(name = "line2")
  private String line2;

  @Column(name = "line3")
  private String line3;

  @Column(name = "city")
  private String city;

  @Column(name = "state")
  private String state;

  @Column(name = "zip")
  private String zip;

  @Column(name = "primary_address")
  private Boolean primary;

  @ManyToOne
  @JoinColumn(name = "customer_id", foreignKey = @ForeignKey(name = "CUSTOMER_ID_FK"))
  private CustomerEntity customer;
}

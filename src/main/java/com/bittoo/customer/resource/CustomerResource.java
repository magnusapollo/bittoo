package com.bittoo.customer.resource;

import com.bittoo.customer.db.entity.AddressEntity;
import com.bittoo.customer.db.entity.CustomerEntity;
import com.bittoo.customer.model.Address;
import com.bittoo.customer.model.Customer;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/v1/customers")
public class CustomerResource {
  @POST
  public Uni<Customer> create(Customer customer) {
    var customerEntity = toEntity(customer);
    return Panache.<CustomerEntity>withTransaction(customerEntity::persist).map(this::toResource);
  }

  @GET
  @Path("{id}")
  public Uni<Customer> get(String id) {
    return CustomerEntity.<CustomerEntity>findById(UUID.fromString(id))
        .onItem()
        .ifNotNull()
        .transform(this::toResource);
  }

  @POST
  @Path("{id}/addresses")
  public Uni<Customer> addAddress(String id, Address address) {
    Uni<CustomerEntity> customerEntityUni = CustomerEntity.findById(UUID.fromString(id));
    return customerEntityUni
        .map(
            customerEntity -> {
              AddressEntity newAddress = toEntity(address);
              customerEntity.getAddresses().add(newAddress);
              newAddress.setCustomer(customerEntity);
              return customerEntity;
            })
        .flatMap(CustomerEntity::<CustomerEntity>persistAndFlush)
        .map(this::toResource);
  }

  private CustomerEntity toEntity(Customer resource) {
    var customerEntity = new CustomerEntity();
    customerEntity.setFullName(resource.getFullName());
    customerEntity.setEmail(resource.getEmail());
    if (resource.getId() != null) {
      customerEntity.setId(UUID.fromString(resource.getId()));
    }
    List<AddressEntity> addressEntityStream =
        resource.getAddresses().stream().map(this::toEntity).collect(Collectors.toList());
    customerEntity.setAddresses(addressEntityStream);
    customerEntity.getAddresses().forEach(addr -> addr.setCustomer(customerEntity));
    return customerEntity;
  }

  private AddressEntity toEntity(Address resource) {
    var addressEntity = new AddressEntity();
    addressEntity.setPrimary(resource.getPrimary());
    addressEntity.setLine1(resource.getLine1());
    addressEntity.setLine2(resource.getLine2());
    addressEntity.setLine3(resource.getLine3());
    addressEntity.setCity(resource.getCity());
    addressEntity.setState(resource.getState());
    addressEntity.setZip(resource.getZip());
    return addressEntity;
  }

  private Customer toResource(CustomerEntity customerEntity) {
    var customer = new Customer();
    customer.setFullName(customerEntity.getFullName());
    customer.setId(customerEntity.getId().toString());
    customer.setEmail(customerEntity.getEmail());
    customer.setAddresses(
        customerEntity.getAddresses().stream().map(this::toResource).collect(Collectors.toList()));
    return customer;
  }

  private Address toResource(AddressEntity entity) {
    Address resource = new Address();
    resource.setLine1(entity.getLine1());
    resource.setLine2(entity.getLine2());
    resource.setLine3(entity.getLine3());
    resource.setCity(entity.getCity());
    resource.setState(entity.getState());
    resource.setZip(entity.getZip());
    resource.setPrimary(entity.getPrimary());
    return resource;
  }
}

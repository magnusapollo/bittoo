package com.bittoo.item.resource;

import com.bittoo.item.db.entity.BrandEntity;
import com.bittoo.item.model.Brand;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Path("/v1/brands")
@RequestScoped
@RequiredArgsConstructor
public class BrandResource {

  @GET
  public Uni<List<Brand>> getAll() {
    return BrandEntity.<BrandEntity>listAll().map(this::toResource);
  }

  private List<Brand> toResource(List<BrandEntity> brandEntityList) {
    return brandEntityList.stream()
        .map(
            brandEntity ->
                Brand.builder()
                    .name(brandEntity.getName())
                    .id(brandEntity.getId().toString())
                    .build())
        .collect(Collectors.toList());
  }
}

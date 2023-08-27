package com.bittoo.item.resource;

import com.bittoo.item.db.entity.CategoryEntity;
import com.bittoo.item.model.Category;
import io.smallrye.mutiny.Uni;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Path("/v1/categories")
@RequestScoped
@RequiredArgsConstructor
public class CategoryResource {

  @GET
  public Uni<List<Category>> getAll() {
    return CategoryEntity.<CategoryEntity>listAll().map(this::toResource);
  }

  @GET
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Uni<Category> get(@NonNull Long id) {
    return CategoryEntity.<CategoryEntity>findById(id).map(this::toResource);
  }

  private List<Category> toResource(List<CategoryEntity> entities) {
    return entities.stream().map(this::toResource).collect(Collectors.toList());
  }

  private Category toResource(CategoryEntity entity) {
    var children = entity.getChildren().stream().map(this::toResource).collect(Collectors.toSet());
    return Category.builder()
        .id(entity.getId())
        .title(entity.getTitle())
        .children(children)
        .build();
  }
}

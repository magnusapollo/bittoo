package com.bittoo.item.resource;

import com.bittoo.item.db.entity.BrandEntity;
import com.bittoo.item.db.entity.CategoryEntity;
import com.bittoo.item.db.entity.ItemEntity;
import com.bittoo.item.db.entity.PetTypeEntity;
import com.bittoo.item.db.entity.ProductEntity;
import com.bittoo.item.model.Category;
import com.bittoo.item.model.Item;
import com.bittoo.item.model.Product;
import com.bittoo.search.service.SearchService;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Path("/v1/products")
@RequestScoped
@RequiredArgsConstructor
public class ProductResource {

  public static final String BASE_PRODUCT_QUERY =
      "Select distinct p from product p "
          + "left join fetch p.items "
          + "left join fetch p.categories c "
          + "left join fetch c.children ";
  private static final String PRODUCTS_BY_CAT_ID_QUERY = BASE_PRODUCT_QUERY + "where c.id = ?1";
  private static final String PRODUCTS_BY_QUERY =
      BASE_PRODUCT_QUERY + "left join fetch p.petType pet left join fetch p.brand brand where ";
  private static final String PET_CLAUSE = "pet.id = ?";
  private static final String AND = " and ";
  private static final String CATEGORY_CLAUSE = "c.id = ?";
  private static final String BRAND_CLAUSE = "brand.id = ?";
  private static final String PRODUCTS_BY_PET_ID_QUERY =
      BASE_PRODUCT_QUERY + "left join fetch p.petType pet where pet.id = ?1";

  private static final String PRODUCT_BY_ID_QUERY = BASE_PRODUCT_QUERY + "where p.id =?1 ";

  @Inject private final SearchService searchService;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("{id}")
  public Uni<Response> get(String id) {
    return getProduct(id)
        .onItem()
        .ifNull()
        .failWith(new NotFoundException())
        .onItem()
        .ifNotNull()
        .transform(Response::ok)
        .map(Response.ResponseBuilder::build);
  }

  @POST
  public Uni<String> create(Product newProduct) {
    ProductEntity productEntity = toEntity(newProduct);
    return Panache.<ProductEntity>withTransaction(productEntity::persist)
        .onItem()
        .invoke(
            () ->
                searchService.addToIndex(toResource(productEntity)).subscribe().asCompletionStage())
        .map(inserted -> inserted.getId().toString());
  }

  @GET
  @Path("/totalInCategory")
  @Produces(MediaType.APPLICATION_JSON)
  public Uni<Long> getByCategoryTotal(@NonNull @QueryParam("categoryId") Long categoryId) {
    return getProductsByCategoryCount(categoryId);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Multi<Product> getBy(
      @QueryParam("categoryId") Long categoryId,
      @QueryParam("petTypeId") Long petTypeId,
      @QueryParam("brandId") String brandId) {
    return getProductsFilter(petTypeId, categoryId, brandId);
  }

  @GET
  @Path("/totalInPetType")
  @Produces(MediaType.APPLICATION_JSON)
  public Uni<Long> getByPetTypeTotal(@NonNull @QueryParam("petTypeId") Long petTypeId) {
    return getProductsByPetTypeCount(petTypeId);
  }

  @POST
  @Path("{id}/items/")
  public Uni<Product> addItem(@PathParam("id") String id, Item item) {
    // Have to use the full query (fetching categories etc, for returning the full Product object.
    return ProductEntity.<ProductEntity>find(PRODUCT_BY_ID_QUERY, UUID.fromString(id))
        .firstResult()
        .map(
            productEntity -> {
              var newItem = toEntity(item);
              productEntity.getItems().add(newItem);
              newItem.setProduct(productEntity);
              return productEntity;
            })
        .flatMap(ProductEntity::<ProductEntity>persistAndFlush)
        .map(this::toResource);
  }

  private String buildQuery(
      boolean isPetTypePresent, boolean isCategoryPresent, boolean isBrandPresent) {
    StringBuilder b = new StringBuilder(PRODUCTS_BY_QUERY);
    if (!(isPetTypePresent || isBrandPresent || isCategoryPresent)) {
      throw new RuntimeException(
          "Atleast one of petType, brand or category should be present in the filter query");
    }
    int i = 0;
    if (isPetTypePresent) {
      b.append(PET_CLAUSE.trim());
      b.append(++i);
    }
    if (isBrandPresent) {
      if (i > 0) {
        b.append(AND);
      }
      b.append(BRAND_CLAUSE.trim());
      b.append(++i);
    }
    if (isCategoryPresent) {
      if (i > 0) {
        b.append(AND);
      }
      b.append(CATEGORY_CLAUSE.trim());
      b.append(++i);
    }
    return b.toString();
  }

  private Object[] buildQueryParams(Long petTypeId, Long categoryId, String brandId) {
    List<Object> objectList = new ArrayList<>();
    if (petTypeId != null) {
      objectList.add(petTypeId.intValue());
    }
    if (categoryId != null) {
      objectList.add(categoryId);
    }
    if (brandId != null) {
      objectList.add(UUID.fromString(brandId));
    }
    return objectList.toArray();
  }

  private Uni<Product> getProduct(String id) {
    return ProductEntity.<ProductEntity>find(PRODUCT_BY_ID_QUERY, UUID.fromString(id))
        .firstResult()
        .onItem()
        .ifNotNull()
        .transform(this::toResource);
  }

  private Multi<Product> getProductsFilter(Long petTypeId, Long categoryId, String brandId) {
    return ProductEntity.<ProductEntity>find(
            buildQuery(petTypeId != null, categoryId != null, brandId != null),
            buildQueryParams(petTypeId, categoryId, brandId))
        .stream()
        .map(this::toResource);
  }

  private Uni<Long> getProductsByCategoryCount(Long categoryId) {
    return ProductEntity.<ProductEntity>find(
            "Select distinct p from product p left join p.categories c " + "where c.id = ?1",
            categoryId)
        .count();
  }

  private Uni<Long> getProductsByPetTypeCount(Long petTypeId) {
    return ProductEntity.<ProductEntity>find(
            "Select distinct p from product p left join p.petType pt " + "where pt.id = ?1",
            petTypeId.intValue())
        .count();
  }

  private ProductEntity toEntity(Product newProduct) {
    final var entity = new ProductEntity();
    entity.setTitle(newProduct.getTitle());
    entity.setLongDescription(newProduct.getLongDescription());
    entity.setShortDescription(newProduct.getShortDescription());
    entity.setOtherInfo(newProduct.getOtherInfo());
    var brandEntity = new BrandEntity();
    brandEntity.setId(UUID.fromString(newProduct.getBrand()));
    entity.setBrand(brandEntity);
    var petTypeEntity = new PetTypeEntity();
    petTypeEntity.setId(newProduct.getPetType());
    entity.setPetType(petTypeEntity);
    Set<ItemEntity> items = new HashSet<>();
    newProduct
        .getItems()
        .forEach(
            item -> {
              var e = this.toEntity(item);
              items.add(e);
              e.setProduct(entity);
            });
    entity.setItems(items);
    var categories =
        newProduct.getCategories().stream()
            .map(Category::getId)
            .map(
                id -> {
                  var categoryEntity = new CategoryEntity();
                  categoryEntity.setId(id);
                  return categoryEntity;
                })
            .collect(Collectors.toSet());
    entity.setCategories(categories);
    return entity;
  }

  private ItemEntity toEntity(Item item) {
    var entity = new ItemEntity();
    entity.setPrice(item.getPrice());
    entity.setTitle(item.getTitle());
    entity.setFlavor(item.getFlavor());
    if (item.getOtherInfo() != null) {
      entity.setOtherInfo(item.getOtherInfo().toString());
    }
    return entity;
  }

  private Product toResource(ProductEntity entity) {
    var productBuilder = Product.builder();
    productBuilder
        .id(entity.getId().toString())
        .title(entity.getTitle())
        .shortDescription(entity.getShortDescription())
        .longDescription(entity.getLongDescription())
        .petType(entity.getPetType().getId())
        .brand(entity.getBrand().getName())
        .items(entity.getItems().stream().map(this::toResource).collect(Collectors.toSet()))
        .otherInfo(entity.getOtherInfo())
        .price(
            String.valueOf(
                entity.getItems().stream()
                    .map(this::toResource)
                    .mapToDouble(Item::getPrice)
                    .min()
                    .orElse(0.00)))
        .categories(
            entity.getCategories() != null && !entity.getCategories().isEmpty()
                ? entity.getCategories().stream().map(this::toResource).collect(Collectors.toSet())
                : new HashSet<>());
    return productBuilder.build();
  }

  private Item toResource(ItemEntity item) {
    return Item.builder()
        .id(item.getId().toString())
        .title(item.getTitle())
        .flavor(item.getFlavor())
        .price(item.getPrice())
        .build();
  }

  private Category toResource(CategoryEntity entity) {
    return Category.builder().name(entity.getTitle()).id(entity.getId()).build();
  }
}

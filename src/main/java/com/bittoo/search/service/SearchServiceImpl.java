package com.bittoo.search.service;

import com.bittoo.item.model.Product;
import com.bittoo.search.client.ElasticSearchClient;
import com.bittoo.search.client.model.SearchResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class SearchServiceImpl implements SearchService {

  @RestClient ElasticSearchClient client;

  @Inject ObjectMapper objectMapper;

  @Override
  @Blocking
  public List<Product> search(String query) {
    return client
        .search(query)
        .onItem()
        .transform(SearchResult::getHits)
        .onItem()
        .transformToMulti((hits) -> Multi.createFrom().iterable(hits))
        .onItem()
        .transform(this::buildProductFromSource)
        .subscribe()
        .asStream()
        .collect(Collectors.toList());
  }

  @Override
  public Uni<Void> addToIndex(Product product) {
    Map<String, Object> map = objectMapper.convertValue(product, Map.class);
    return client.add(product.getId(), map);
  }

  private Product buildProductFromSource(Object object) {
    final Map<String, Object> sourceMap =
        (Map<String, Object>) ((Map<String, Object>) object).get("_source");
    final Product.ProductBuilder prod = Product.builder();
    final Product p = objectMapper.convertValue(sourceMap, Product.class);
    prod.title(sourceMap.getOrDefault("title", "null").toString());
    prod.id(sourceMap.getOrDefault("id", "null").toString());
    // prod.setPrice(sourceMap.getOrDefault("price", "-1").toString());
    return p;
  }
}

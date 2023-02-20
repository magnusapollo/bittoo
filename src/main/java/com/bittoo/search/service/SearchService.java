package com.bittoo.search.service;

import com.bittoo.item.model.Product;
import io.smallrye.mutiny.Uni;

import java.util.List;

public interface SearchService {

  List<Product> search(String query);

  Uni<Void> addToIndex(Product item);
}

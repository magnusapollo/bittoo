package com.bittoo.search.service;

import com.bittoo.item.model.Item;
import io.smallrye.mutiny.Uni;

import java.util.List;

public interface SearchService {

    List<Item> search(String query);

    Uni<Void> addToIndex(Item item);
}

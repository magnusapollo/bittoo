package com.bittoo.search.service;

import com.bittoo.item.model.Item;
import com.bittoo.search.client.ElasticSearchClient;
import com.bittoo.search.client.model.Hit;
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

    @RestClient
    ElasticSearchClient client;

    @Inject
    ObjectMapper objectMapper;

    @Override
    @Blocking
    public List<Item> search(String query) {
        return client.search(query)
                .onItem().transform(SearchResult::getHits)
                .onItem().transformToMulti((hits) -> Multi.createFrom().iterable(hits))
                .onItem().transform(Hit::getSource)
                .onItem().transform(this::buildItemFromSource)
                .subscribe().asStream().collect(Collectors.toList());
    }

    @Override
    public Uni<Void> addToIndex(Item item) {
        Map<String, Object> map = objectMapper.convertValue(item, Map.class);
        return client.add(item.getId(), map);
    }

    private Item buildItemFromSource(Object object) {
        final Map<String, Object> sourceMap = (Map<String, Object>) object;
        final Item item = new Item();
        item.setDescription(sourceMap.getOrDefault("description", "null").toString());
        item.setTitle(sourceMap.getOrDefault("title", "null").toString());
        item.setId(sourceMap.getOrDefault("id", "null").toString());
        item.setPrice(sourceMap.getOrDefault("price", "-1").toString());
        return item;
    }
}

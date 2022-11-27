package com.bittoo.search.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SearchResult {

    List<Hit> hits;

    @JsonProperty("hits")
    private void unpackNameFromNestedObject(Map<String, Object> outerHits) {
        this.hits = (List<Hit>) outerHits.get("hits");
    }
}

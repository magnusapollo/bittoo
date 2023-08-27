package com.bittoo.search.client;

import com.bittoo.search.client.model.SearchResult;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Map;

@RegisterRestClient(configKey = "search-api")
public interface ElasticSearchClient {

    @GET
    @Path("/data/_search")
    @ClientHeaderParam(name = "Content-Type", value = MediaType.APPLICATION_JSON)
    Uni<SearchResult> search(@QueryParam("q") String query);

    @PUT
    @Path("/data/_doc/{id}?pretty")
    @ClientHeaderParam(name = "Content-Type", value = MediaType.APPLICATION_JSON)
    Uni<Void> add(String id, Map<String, Object> objectMap);
}
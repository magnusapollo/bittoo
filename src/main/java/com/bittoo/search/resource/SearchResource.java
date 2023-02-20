package com.bittoo.search.resource;

import com.bittoo.item.model.Product;
import com.bittoo.search.service.SearchService;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.List;

@RequestScoped
@Path("/v1/search")
public class SearchResource {

  @Inject SearchService service;

  @GET
  public List<Product> search(@QueryParam("q") String query) {
    return service.search(query);
  }
}

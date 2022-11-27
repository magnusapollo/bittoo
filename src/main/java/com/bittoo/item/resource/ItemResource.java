package com.bittoo.item.resource;

import com.bittoo.item.model.Item;
import com.bittoo.search.service.SearchService;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

@Slf4j
@Path("/v1/items")
@RequestScoped
public class ItemResource {

  @Inject SearchService searchService;

  @Inject MySQLPool mySQLPool;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("{id}")
  public Uni<Response> get(String id) {
    return getItem(id)
        .onItem()
        .ifNull()
        .failWith(new NotFoundException())
        .onItem()
        .ifNotNull()
        .transform(Response::ok)
        .map(Response.ResponseBuilder::build);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Multi<Item> getItems(@QueryParam("limit") Long limit) {
    Uni<RowSet<Row>> rowSetUni =
        mySQLPool
            .preparedQuery(
                "Select BIN_TO_UUID(id) as id, title, description, price from Item LIMIT ?")
            .execute(Tuple.of(limit));
    return rowSetUni
        .onItem()
        .transformToMulti(rows -> Multi.createFrom().iterable(rows))
        .map(this::toItem);
  }

  @POST
  public Uni<Response> save(Item newItem) {
    UUID uuid = UUID.randomUUID();
    newItem.setId(uuid.toString());
    return mySQLPool
        .preparedQuery(
            "Insert into Item (id, title, description, price) values (UUID_TO_BIN(?), ?, ?, ?)")
        .execute(Tuple.of(uuid, newItem.getTitle(), newItem.getDescription(), newItem.getPrice()))
        // add new Item to search index as a side effect
        .onItem()
        .invoke(() -> searchService.addToIndex(newItem).subscribe().asCompletionStage())
        .onFailure()
        .invoke((f) -> log.error("Error while saving to db", f))
        .onItem()
        .transform(rows -> Response.created(URI.create("/items/" + uuid)))
        .onItem()
        .transform(Response.ResponseBuilder::build);
  }

  @PUT
  @Path("add")
  public CompletionStage<Void> addSearch(Item item) {
    return searchService.addToIndex(item).subscribe().asCompletionStage();
  }

  private Item toItem(Row row) {
    Item item = new Item();
    item.setId(row.getString("id"));
    item.setTitle(row.getString("title"));
    item.setPrice(row.getNumeric("price").toString());
    item.setDescription(row.getString("description"));
    return item;
  }

  private Uni<Item> getItem(String id) {
    Uni<RowSet<Row>> rowSetUni =
        mySQLPool
            .preparedQuery(
                "Select BIN_TO_UUID(id) as id, "
                    + "title, description, price from Item where id = UUID_TO_BIN(?)")
            .execute(Tuple.of(id));
    return rowSetUni
        .onItem()
        .transform(RowSet::iterator)
        .onItem()
        .transform(iterator -> iterator.hasNext() ? toItem(iterator.next()) : null);
  }
}

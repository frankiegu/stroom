package stroom.index.impl.api;


import io.swagger.annotations.Api;
import stroom.util.shared.RestResource;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Api(value = "stroom-index shard - /v1")
@Path("/stroom-index/shard/v1")
@Produces(MediaType.APPLICATION_JSON)
public interface IndexShardResource extends RestResource {
}

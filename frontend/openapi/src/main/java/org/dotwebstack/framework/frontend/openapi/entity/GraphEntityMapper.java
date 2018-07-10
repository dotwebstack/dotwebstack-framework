package org.dotwebstack.framework.frontend.openapi.entity;

import io.swagger.v3.oas.models.media.Schema;
import javax.ws.rs.core.MediaType;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.entity.schema.SchemaMapperAdapter;
import org.dotwebstack.framework.frontend.openapi.entity.schema.ValueContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public final class GraphEntityMapper implements EntityMapper<GraphEntity> {

  private final SchemaMapperAdapter schemaMapperAdapter;

  @Autowired
  public GraphEntityMapper(@NonNull SchemaMapperAdapter schemaMapperAdapter) {
    this.schemaMapperAdapter = schemaMapperAdapter;
  }

  @Override
  public Object map(@NonNull GraphEntity entity, @NonNull MediaType mediaType) {
    // TODO: Check if we still need the ResponseProperty wrapper (which was necessary because the
    // OASv2 parser did not support vendorextensions at response level
    // Schema schema = new ResponseProperty(entity.getResponse());
    Schema schema = null; // Temporary "fix" to clean up compiler error
    ValueContext valueContext = ValueContext.builder().build();

    return schemaMapperAdapter.mapGraphValue(schema, entity, valueContext, schemaMapperAdapter);
  }

}

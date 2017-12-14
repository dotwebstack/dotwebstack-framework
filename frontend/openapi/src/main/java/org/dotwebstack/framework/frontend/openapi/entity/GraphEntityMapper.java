package org.dotwebstack.framework.frontend.openapi.entity;

import io.swagger.models.properties.Property;
import javax.ws.rs.core.MediaType;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.entity.schema.SchemaMapperAdapter;
import org.dotwebstack.framework.frontend.openapi.entity.schema.ValueContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public final class GraphEntityMapper implements EntityMapper<GraphEntity> {

  private static final Logger LOG = LoggerFactory.getLogger(GraphEntityMapper.class);

  private final SchemaMapperAdapter schemaMapperAdapter;

  @Autowired
  public GraphEntityMapper(@NonNull SchemaMapperAdapter schemaMapperAdapter) {
    this.schemaMapperAdapter = schemaMapperAdapter;
  }

  @Override
  public Object map(@NonNull GraphEntity entity, @NonNull MediaType mediaType) {
    GraphEntityContext graphEntityContext = entity.getEntityContext();
    Property schema = entity.getSchemaMap().get(mediaType);
    ValueContext valueContext = ValueContext.builder().build();

    LOG.debug("Map graph entity to representation.");
    if (schema == null) {
      throw new EntityMapperRuntimeException(
          String.format("No schema found for media type '%s'.", mediaType.toString()));
    }

    return schemaMapperAdapter.mapGraphValue(schema, graphEntityContext, valueContext,
        schemaMapperAdapter);
  }

}

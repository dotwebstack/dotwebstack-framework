package org.dotwebstack.framework.frontend.openapi.entity;

import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import javax.ws.rs.core.MediaType;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.schema.SchemaMapperAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public final class GraphEntityMapper implements EntityMapper<GraphEntity> {

  private static final Logger LOG = LoggerFactory.getLogger(GraphEntityMapper.class);

  private SchemaMapperAdapter schemaMapperAdapter;


  @Autowired
  public GraphEntityMapper(@NonNull SchemaMapperAdapter schemaMapperAdapter) {
    this.schemaMapperAdapter = schemaMapperAdapter;
  }


  @Override
  public Object mapGraph(@NonNull GraphEntity entity, @NonNull MediaType mediaType,
      @NonNull GraphEntityContext graphEntityContext) {
    Property schema = entity.getSchemaProperty();

    if (schema == null) {
      throw new EntityMapperRuntimeException(
          String.format("No schema found for media type '%s'.", mediaType.toString()));
    }


    if (schema instanceof ArrayProperty) {
      // return mapCollection(entity, (ArrayProperty) schema);
    }
    return schemaMapperAdapter.mapGraphValue(schema, graphEntityContext,schemaMapperAdapter, null);

  }



  @Override
  public Object mapTuple(GraphEntity entity, MediaType mediaType) {
    throw new UnsupportedOperationException("No support for tuples");
  }
}

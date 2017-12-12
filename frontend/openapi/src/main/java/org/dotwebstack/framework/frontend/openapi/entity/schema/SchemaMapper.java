package org.dotwebstack.framework.frontend.openapi.entity.schema;

import io.swagger.models.properties.Property;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntityContext;

public interface SchemaMapper<S extends Property, T> {

  T mapTupleValue(S schema, SchemaMapperContext schemaMapperContext);

  T mapGraphValue(S schema, GraphEntityContext entityContext,
      SchemaMapperContext schemaMapperContext, SchemaMapperAdapter schemaMapperAdapter);

  boolean supports(Property schema);

}

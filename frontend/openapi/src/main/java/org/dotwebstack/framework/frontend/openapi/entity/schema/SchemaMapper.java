package org.dotwebstack.framework.frontend.openapi.entity.schema;

import io.swagger.models.properties.Property;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntityContext;

public interface SchemaMapper<S extends Property, T> {

  T mapTupleValue(S schema, @NonNull SchemaMapperContext schemaMapperContext);

  T mapGraphValue(S schema, GraphEntityContext entityContext,
                  @NonNull  SchemaMapperContext schemaMapperContext,
                  @NonNull SchemaMapperAdapter schemaMapperAdapter);

  boolean supports(Property schema);

}

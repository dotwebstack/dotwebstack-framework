package org.dotwebstack.framework.frontend.openapi.entity.schema;

import io.swagger.models.properties.Property;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntityContext;
import org.eclipse.rdf4j.model.Value;

public interface SchemaMapper<S extends Property, T> {

  T mapTupleValue(S schema, Value value);

  T mapGraphValue(S schema, GraphEntityContext entityContext,
                  SchemaMapperAdapter schemaMapperAdapter, Value value);

  boolean supports(Property schema);

}

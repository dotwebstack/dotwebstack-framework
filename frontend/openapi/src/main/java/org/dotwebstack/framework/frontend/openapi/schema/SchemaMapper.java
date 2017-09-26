package org.dotwebstack.framework.frontend.openapi.schema;

import io.swagger.models.properties.Property;
import org.eclipse.rdf4j.model.Value;

public interface SchemaMapper<S extends Property, T> {

  T mapTupleValue(S schema, Value value);

  boolean supports(Property schema);

}

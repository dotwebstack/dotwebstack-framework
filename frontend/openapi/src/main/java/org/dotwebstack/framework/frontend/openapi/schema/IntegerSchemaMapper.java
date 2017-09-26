package org.dotwebstack.framework.frontend.openapi.schema;

import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.Property;
import lombok.NonNull;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.springframework.stereotype.Service;

@Service
class IntegerSchemaMapper implements SchemaMapper<IntegerProperty, Integer> {

  @Override
  public Integer mapTupleValue(@NonNull IntegerProperty schema, @NonNull Value value) {
    if (!(value instanceof Literal)) {
      throw new SchemaHandlerRuntimeException(
          String.format("Schema '%s' is not a literal value.", schema.getName()));
    }

    return ((Literal) value).intValue();
  }

  @Override
  public boolean supports(@NonNull Property schema) {
    return IntegerProperty.class.isInstance(schema);
  }

}

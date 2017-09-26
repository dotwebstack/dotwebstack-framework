package org.dotwebstack.framework.frontend.openapi.schema;

import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.Property;
import lombok.NonNull;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.springframework.stereotype.Service;

@Service
class BooleanSchemaMapper implements SchemaMapper<BooleanProperty, Boolean> {

  @Override
  public Boolean mapTupleValue(@NonNull BooleanProperty schema, @NonNull Value value) {
    if (!(value instanceof Literal)) {
      throw new SchemaHandlerRuntimeException(
          String.format("Schema '%s' is not a literal value.", schema.getName()));
    }

    return ((Literal) value).booleanValue();
  }

  @Override
  public boolean supports(@NonNull Property schema) {
    return BooleanProperty.class.isInstance(schema);
  }

}

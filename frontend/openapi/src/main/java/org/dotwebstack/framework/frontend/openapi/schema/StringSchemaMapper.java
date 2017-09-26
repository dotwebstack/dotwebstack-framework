package org.dotwebstack.framework.frontend.openapi.schema;

import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;
import lombok.NonNull;
import org.eclipse.rdf4j.model.Value;
import org.springframework.stereotype.Service;

@Service
class StringSchemaMapper implements SchemaMapper<StringProperty, String> {

  @Override
  public String mapTupleValue(@NonNull StringProperty schema, @NonNull Value value) {
    return value.stringValue();
  }

  @Override
  public boolean supports(@NonNull Property schema) {
    return StringProperty.class.isInstance(schema);
  }

}

package org.dotwebstack.framework.frontend.openapi.schema;

import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.Property;
import lombok.NonNull;
import org.eclipse.rdf4j.model.Value;
import org.springframework.stereotype.Service;

@Service
class DoubleSchemaMapper implements SchemaMapper<DoubleProperty, Double> {

  @Override
  public Double mapTupleValue(@NonNull DoubleProperty schema, @NonNull Value value) {
    return SchemaMapperUtils.castLiteralValue(value).doubleValue();
  }

  @Override
  public boolean supports(@NonNull Property schema) {
    return schema instanceof DoubleProperty;
  }

}

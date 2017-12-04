package org.dotwebstack.framework.frontend.openapi.entity.schema;

import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.Property;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntityContext;
import org.eclipse.rdf4j.model.Value;
import org.springframework.stereotype.Service;

@Service
class DoubleSchemaMapper implements SchemaMapper<DoubleProperty, Double> {

  @Override
  public Double mapTupleValue(@NonNull DoubleProperty schema, @NonNull Value value) {
    return SchemaMapperUtils.castLiteralValue(value).doubleValue();
  }

  @Override
  public Double mapGraphValue(DoubleProperty schema, GraphEntityContext graphEntityContext,
      SchemaMapperAdapter schemaMapperAdapter, Value value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean supports(@NonNull Property schema) {
    return schema instanceof DoubleProperty;
  }

}

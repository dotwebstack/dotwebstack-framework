package org.dotwebstack.framework.frontend.openapi.entity.schema;

import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.Property;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntityContext;
import org.springframework.stereotype.Service;

@Service
class DoubleSchemaMapper implements SchemaMapper<DoubleProperty, Double> {

  @Override
  public Double mapTupleValue(@NonNull DoubleProperty schema,
      @NonNull ValueContext valueContext) {
    return SchemaMapperUtils.castLiteralValue(valueContext.getValue()).doubleValue();
  }

  @Override
  public Double mapGraphValue(DoubleProperty schema, GraphEntityContext graphEntityContext,
      ValueContext valueContext, SchemaMapperAdapter schemaMapperAdapter) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean supports(@NonNull Property schema) {
    return schema instanceof DoubleProperty;
  }

}

package org.dotwebstack.framework.frontend.openapi.entity.schema;

import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.Property;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.TupleEntity;
import org.springframework.stereotype.Service;

@Service
class BooleanSchemaMapper implements SchemaMapper<BooleanProperty, Boolean> {

  @Override
  public Boolean mapTupleValue(@NonNull BooleanProperty schema, @NonNull TupleEntity entity,
      @NonNull ValueContext valueContext) {
    return SchemaMapperUtils.castLiteralValue(valueContext.getValue()).booleanValue();
  }

  @Override
  public Boolean mapGraphValue(@NonNull BooleanProperty schema, @NonNull GraphEntity entity,
      @NonNull ValueContext valueContext, @NonNull SchemaMapperAdapter schemaMapperAdapter) {
    return SchemaMapperUtils.castLiteralValue(valueContext.getValue()).booleanValue();
  }

  @Override
  public boolean supports(@NonNull Property schema) {
    return schema instanceof BooleanProperty;
  }

}

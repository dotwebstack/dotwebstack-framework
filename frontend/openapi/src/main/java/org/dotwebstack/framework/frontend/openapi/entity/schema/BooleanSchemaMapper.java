package org.dotwebstack.framework.frontend.openapi.entity.schema;

import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.Property;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntityContext;
import org.eclipse.rdf4j.model.Value;
import org.springframework.stereotype.Service;

@Service
class BooleanSchemaMapper implements SchemaMapper<BooleanProperty, Boolean> {

  @Override
  public Boolean mapTupleValue(@NonNull BooleanProperty schema, @NonNull Value value) {
    return SchemaMapperUtils.castLiteralValue(value).booleanValue();
  }

  @Override
  public Boolean mapGraphValue(BooleanProperty schema, GraphEntityContext graphEntityContext,
      SchemaMapperAdapter schemaMapperAdapter,Value value) {
    return SchemaMapperUtils.castLiteralValue(value).booleanValue();
  }

  @Override
  public boolean supports(@NonNull Property schema) {
    return schema instanceof BooleanProperty;
  }

}

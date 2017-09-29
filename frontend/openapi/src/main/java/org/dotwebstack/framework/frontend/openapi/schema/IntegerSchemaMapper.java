package org.dotwebstack.framework.frontend.openapi.schema;

import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.Property;
import lombok.NonNull;
import org.eclipse.rdf4j.model.Value;
import org.springframework.stereotype.Service;

@Service
class IntegerSchemaMapper implements SchemaMapper<IntegerProperty, Integer> {

  @Override
  public Integer mapTupleValue(@NonNull IntegerProperty schema, @NonNull Value value) {
    return SchemaMapperUtils.castLiteralValue(value).intValue();
  }

  @Override
  public boolean supports(@NonNull Property schema) {
    return schema instanceof IntegerProperty;
  }

}

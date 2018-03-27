package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import io.swagger.models.Model;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.TupleEntity;
import org.springframework.stereotype.Service;

@Service
public class RefSchemaMapper implements SchemaMapper<RefProperty, Object> {

  @Override
  public Object mapTupleValue(RefProperty schema, @NonNull TupleEntity entity,
      ValueContext valueContext) {
    throw new UnsupportedOperationException("Tuple query not supported.");
  }

  @Override
  public Object mapGraphValue(@NonNull RefProperty schema, @NonNull GraphEntity entity,
      @NonNull ValueContext valueContext, @NonNull SchemaMapperAdapter schemaMapperAdapter) {

    Model refModel = entity.getSwaggerDefinitions().get(schema.getSimpleRef());

    if (refModel == null) {
      throw new SchemaMapperRuntimeException(String.format(
          "Unable to resolve reference to swagger model: '%s'.", schema.getSimpleRef()));
    }

    Builder<String, Object> builder = ImmutableMap.builder();
    refModel.getProperties().forEach((propKey, propValue) -> builder.put(propKey,
        Optional.fromNullable(schemaMapperAdapter.mapGraphValue(propValue, entity, valueContext,
            schemaMapperAdapter))));

    return builder.build();
  }

  public boolean supports(@NonNull Property property) {
    return RefProperty.class.isInstance(property);
  }

}

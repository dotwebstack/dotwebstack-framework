package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import io.swagger.models.Model;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import java.util.function.BiConsumer;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
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
    refModel.getProperties().forEach(
        mapPropertiesInRef(entity, valueContext, schemaMapperAdapter, builder));

    return builder.build();
  }

  private BiConsumer<String, Property> mapPropertiesInRef(@NonNull GraphEntity entity, //
      @NonNull ValueContext valueContext, @NonNull SchemaMapperAdapter schemaMapperAdapter, //
      Builder<String, Object> builder) { //

    return (propKey, propValue) -> {
      Object value =
          schemaMapperAdapter.mapGraphValue(propValue, entity, valueContext, schemaMapperAdapter);

      Boolean showWhenNull = true;
      if (propValue.getVendorExtensions().containsKey(
          OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_EMPTY_OR_NULL)) {
        Object bool = propValue.getVendorExtensions().get(
            OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_EMPTY_OR_NULL);
        if (bool instanceof Boolean) {
          showWhenNull = !(Boolean) bool;
        }
      }
      if (showWhenNull || value != null) {
        builder.put(propKey, Optional.fromNullable(value));
      }
    };
  }

  public boolean supports(@NonNull Property property) {
    return RefProperty.class.isInstance(property);
  }

}

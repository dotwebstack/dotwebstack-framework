package org.dotwebstack.framework.frontend.openapi.schema;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import io.swagger.models.Model;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import java.util.Optional;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntityContext;
import org.eclipse.rdf4j.model.Value;
import org.springframework.stereotype.Service;

@Service
public class RefSchemaMapper implements SchemaMapper<RefProperty, Object> {



  @Override
  public Object mapTupleValue(RefProperty schema, Value value) {
    return null;
  }

  @Override
  public Object mapGraphValue(RefProperty schema, GraphEntityContext graphEntityContext,
      SchemaMapperAdapter schemaMapperAdapter, Value value) {

    Model refModel = graphEntityContext.getSwaggerDefinitions().get(schema.getSimpleRef());

    if (refModel == null) {
      throw new SchemaMapperRuntimeException(String.format(
          "Unable to resolve reference to swagger model: '%s'.", schema.getSimpleRef()));
    }

    Builder<String, Object> builder = ImmutableMap.builder();
    refModel.getProperties().forEach((propKey, propValue) -> {
      builder.put(propKey, Optional.ofNullable(schemaMapperAdapter.mapGraphValue(propValue,
          graphEntityContext, schemaMapperAdapter, value)));
    });

    return builder.build();
  }

  public boolean supports(Property property) {
    return RefProperty.class.isInstance(property);
  }

}

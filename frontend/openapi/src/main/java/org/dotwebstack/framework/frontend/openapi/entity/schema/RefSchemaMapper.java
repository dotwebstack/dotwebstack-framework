package org.dotwebstack.framework.frontend.openapi.entity.schema;

import io.swagger.models.Model;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntityContext;
import org.eclipse.rdf4j.model.Value;
import org.springframework.stereotype.Service;

@Service
public class RefSchemaMapper implements SchemaMapper<RefProperty, Object> {

  @Override
  public Object mapTupleValue(RefProperty schema, Value value) {
    throw new UnsupportedOperationException("Tuple query not supported.");
  }

  @Override
  public Object mapGraphValue(@NonNull RefProperty schema,
      @NonNull GraphEntityContext graphEntityContext,
      @NonNull SchemaMapperAdapter schemaMapperAdapter, Value value) {

    Model refModel = graphEntityContext.getSwaggerDefinitions().get(schema.getSimpleRef());

    if (refModel == null) {
      throw new SchemaMapperRuntimeException(String.format(
          "Unable to resolve reference to swagger model: '%s'.", schema.getSimpleRef()));
    }

    ObjectProperty objectProperty = new ObjectProperty(refModel.getProperties());

    objectProperty.setDescription(refModel.getDescription());
    objectProperty.setExample(refModel.getExample());
    objectProperty.setProperties(refModel.getProperties());
    objectProperty.setTitle(refModel.getTitle());
    objectProperty.setVendorExtensions(refModel.getVendorExtensions());

    return schemaMapperAdapter.mapGraphValue(objectProperty, graphEntityContext,
        schemaMapperAdapter, value);
  }

  public boolean supports(Property property) {
    return RefProperty.class.isInstance(property);
  }

}

package org.dotwebstack.framework.frontend.openapi.entity.schema;

import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.TupleEntity;
import org.springframework.stereotype.Service;

@Service
public class SelfLinkSchemaMapper implements SchemaMapper<ObjectProperty, Object> {

  @Override
  public Object mapTupleValue(@NonNull ObjectProperty schema, @NonNull TupleEntity entity,
      @NonNull ValueContext valueContext) {
    return null;
  }

  @Override
  public Object mapGraphValue(@NonNull ObjectProperty schema, @NonNull GraphEntity entity,
      @NonNull ValueContext valueContext, @NonNull SchemaMapperAdapter schemaMapperAdapter) {
    String operationPath =
        entity.getRequestContext().getApiOperation().getRequestPath().normalised();
    return SchemaMapperUtils.createLink(operationPath);
  }

  @Override
  public boolean supports(Property schema) {
    return schema instanceof ObjectProperty && OpenApiSpecificationExtensions.TYPE_SELF_LINK.equals(
        schema.getVendorExtensions().get(OpenApiSpecificationExtensions.TYPE));
  }

}

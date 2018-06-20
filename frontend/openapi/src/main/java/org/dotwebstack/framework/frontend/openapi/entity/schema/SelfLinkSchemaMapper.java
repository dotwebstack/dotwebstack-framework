package org.dotwebstack.framework.frontend.openapi.entity.schema;

import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.TupleEntity;
import org.springframework.stereotype.Service;

@Service
public class SelfLinkSchemaMapper extends AbstractLinkSchemaMapper {

  @Override
  public Object mapTupleValue(@NonNull ObjectSchema schema, @NonNull TupleEntity entity,
      @NonNull ValueContext valueContext) {
    return SchemaMapperUtils.createLink(buildUri(entity.getRequestContext(), null));
  }

  @Override
  public Object mapGraphValue(@NonNull ObjectSchema schema, @NonNull GraphEntity entity,
      @NonNull ValueContext valueContext, @NonNull SchemaMapperAdapter schemaMapperAdapter) {
    return SchemaMapperUtils.createLink(buildUri(entity.getRequestContext(), null));
  }

  @Override
  public boolean supports(Schema schema) {
    return schema instanceof ObjectSchema && OpenApiSpecificationExtensions.TYPE_SELF_LINK.equals(
        schema.getExtensions().get(OpenApiSpecificationExtensions.TYPE));
  }

}

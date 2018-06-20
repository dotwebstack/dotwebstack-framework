package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.google.common.collect.ImmutableMap;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.TupleEntity;
import org.dotwebstack.framework.frontend.openapi.handlers.RequestContext;
import org.springframework.stereotype.Service;

@Service
public class PrevLinkSchemaMapper extends AbstractLinkSchemaMapper {

  @Override
  public Object mapTupleValue(@NonNull ObjectSchema schema, @NonNull TupleEntity entity,
      @NonNull ValueContext valueContext) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object mapGraphValue(@NonNull ObjectSchema schema, @NonNull GraphEntity entity,
      @NonNull ValueContext valueContext, @NonNull SchemaMapperAdapter schemaMapperAdapter) {
    RequestContext requestContext = entity.getRequestContext();

    int page = getPageTermParameter(requestContext).handle(requestContext.getParameters());

    if (page == 1) {
      return null;
    }

    Map<String, String> extraParams = ImmutableMap.of(
        getPageQueryParameter(requestContext).getName(), Integer.toString(page - 1));

    return SchemaMapperUtils.createLink(buildUri(requestContext, extraParams));
  }

  @Override
  public boolean supports(Schema schema) {
    return schema instanceof ObjectSchema && OpenApiSpecificationExtensions.TYPE_PREV_LINK.equals(
        schema.getExtensions().get(OpenApiSpecificationExtensions.TYPE));
  }

}

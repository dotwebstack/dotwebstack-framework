package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.google.common.collect.ImmutableMap;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
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
  public Object mapTupleValue(@NonNull ObjectProperty schema, @NonNull TupleEntity entity,
      @NonNull ValueContext valueContext) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object mapGraphValue(@NonNull ObjectProperty schema, @NonNull GraphEntity entity,
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
  public boolean supports(Property schema) {
    return schema instanceof ObjectProperty && OpenApiSpecificationExtensions.TYPE_PREV_LINK.equals(
        schema.getVendorExtensions().get(OpenApiSpecificationExtensions.TYPE));
  }

}

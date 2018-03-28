package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.atlassian.oai.validator.model.ApiOperation;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import java.net.URI;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.UriBuilder;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.TupleEntity;
import org.dotwebstack.framework.frontend.openapi.handlers.RequestContext;
import org.springframework.stereotype.Service;

@Service
public class SelfLinkSchemaMapper implements SchemaMapper<ObjectProperty, Object> {

  @Override
  public Object mapTupleValue(@NonNull ObjectProperty schema, @NonNull TupleEntity entity,
      @NonNull ValueContext valueContext) {
    return SchemaMapperUtils.createLink(buildUri(entity.getRequestContext()));
  }

  @Override
  public Object mapGraphValue(@NonNull ObjectProperty schema, @NonNull GraphEntity entity,
      @NonNull ValueContext valueContext, @NonNull SchemaMapperAdapter schemaMapperAdapter) {
    return SchemaMapperUtils.createLink(buildUri(entity.getRequestContext()));
  }

  @Override
  public boolean supports(Property schema) {
    return schema instanceof ObjectProperty && OpenApiSpecificationExtensions.TYPE_SELF_LINK.equals(
        schema.getVendorExtensions().get(OpenApiSpecificationExtensions.TYPE));
  }

  private static URI buildUri(RequestContext requestContext) {
    ApiOperation apiOperation = requestContext.getApiOperation();
    String path = apiOperation.getRequestPath().normalised();

    List<Parameter> operationParams = apiOperation.getOperation().getParameters();
    Map<String, String> requestParams = requestContext.getParameters();

    UriBuilder builder = UriBuilder.fromPath(requestContext.getBaseUri()).path(path);

    // @formatter:off
    operationParams.stream()
        .filter(p -> p.getIn().equalsIgnoreCase("query"))
        .map(p -> (QueryParameter) p)
        .filter(p -> requestParams.get(p.getName()) != null)
        .filter(p -> p.getDefault() == null
            || !requestParams.get(p.getName()).equals(p.getDefault().toString()))
        .forEach(p -> builder.queryParam(p.getName(), requestParams.get(p.getName())));
    // @formatter:on

    return builder.buildFromMap(requestContext.getParameters());
  }

}

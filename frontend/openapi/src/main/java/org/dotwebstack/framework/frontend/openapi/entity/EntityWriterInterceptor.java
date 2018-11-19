package org.dotwebstack.framework.frontend.openapi.entity;

import com.google.common.collect.ImmutableMap;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.responses.ApiResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.param.Parameter;
import org.dotwebstack.framework.param.ParameterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public final class EntityWriterInterceptor implements WriterInterceptor {

  private static final Logger LOG = LoggerFactory.getLogger(EntityWriterInterceptor.class);

  private final TupleEntityMapper tupleEntityMapper;

  private final GraphEntityMapper graphEntityMapper;

  @Autowired
  public EntityWriterInterceptor(@NonNull GraphEntityMapper graphEntityMapper,
      @NonNull TupleEntityMapper tupleEntityMapper) {
    this.graphEntityMapper = graphEntityMapper;
    this.tupleEntityMapper = tupleEntityMapper;
  }

  private static Map<String, Object> createResponseHeaders(GraphEntity entity) {
    ApiResponse response = entity.getResponse();
    Map<String, Header> headers = response.getHeaders();

    if (headers == null) {
      return ImmutableMap.of();
    }

    Map<String, Object> result = new HashMap<>();

    headers.forEach((name, header) -> {
      Map<String, Object> vendorExtensions = header.getExtensions();

      // TODO Workaround for issue in swagger-parser where headers are not properly resolved
      if (vendorExtensions == null) {
        Header resolvedHeader = entity.getOpenApiComponents().getHeaders().get(name);
        vendorExtensions = resolvedHeader.getExtensions();
      }

      LOG.debug("Header '{}' has vendorextensions: {}", name, vendorExtensions);

      if (vendorExtensions == null) {
        return;
      }

      Object parameterIdString = vendorExtensions.get(OpenApiSpecificationExtensions.PARAMETER);

      if (parameterIdString == null) {
        return;
      }

      InformationProduct product = entity.getRequestContext().getInformationProduct();
      Parameter<?> parameter =
          ParameterUtils.getParameter(product.getParameters(), (String) parameterIdString);
      result.put(name, parameter.handle(entity.getRequestContext().getParameters()));
    });

    return result;
  }

  @Override
  public void aroundWriteTo(@NonNull WriterInterceptorContext context) throws IOException {
    MediaType mediaType = context.getMediaType();

    if (context.getEntity() instanceof TupleEntity) {
      TupleEntity entity = (TupleEntity) context.getEntity();
      Object mappedEntity = tupleEntityMapper.map(entity, mediaType);
      context.setEntity(mappedEntity);
    }

    if (context.getEntity() instanceof GraphEntity) {
      GraphEntity entity = (GraphEntity) context.getEntity();
      Object mappedEntity = graphEntityMapper.map(entity, mediaType);
      context.setEntity(mappedEntity);

      Map<String, Object> headers = createResponseHeaders(entity);
      headers.entrySet().forEach(e -> context.getHeaders().add(e.getKey(), e.getValue()));
    }

    context.proceed();
  }

}

package org.dotwebstack.framework.frontend.openapi.entity;

import com.google.common.collect.ImmutableMap;
import io.swagger.models.Response;
import io.swagger.models.properties.Property;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.informationproduct.InformationProductUtils;
import org.dotwebstack.framework.param.Parameter;
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
    Response response = entity.getResponse();
    Map<String, Property> headers = response.getHeaders();

    if (headers == null) {
      return ImmutableMap.of();
    }

    Map<String, Object> result = new HashMap<>();

    for (Entry<String, Property> header : headers.entrySet()) {
      Map<String, Object> vendorExtensions = header.getValue().getVendorExtensions();

      LOG.debug("Vendor extensions for header param '{}': {}", header.getKey(), vendorExtensions);

      Object parameterIdString = vendorExtensions.get(OpenApiSpecificationExtensions.PARAMETER);

      if (parameterIdString == null) {
        continue;
      }

      InformationProduct product = entity.getRequestContext().getInformationProduct();
      Parameter<?> parameter =
          InformationProductUtils.getParameter(product, (String) parameterIdString);
      Object value = parameter.handle(entity.getRequestContext().getParameters());

      result.put(header.getKey(), value);
    }

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

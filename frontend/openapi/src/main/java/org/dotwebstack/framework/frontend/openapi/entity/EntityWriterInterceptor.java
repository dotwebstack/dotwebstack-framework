package org.dotwebstack.framework.frontend.openapi.entity;

import io.swagger.models.Response;
import io.swagger.models.properties.Property;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import lombok.NonNull;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.schema.ResponseProperty;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.param.Parameter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public final class EntityWriterInterceptor implements WriterInterceptor {

  private static final Logger LOG = LoggerFactory.getLogger(EntityWriterInterceptor.class);

  private static final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

  private final TupleEntityMapper tupleEntityMapper;

  private final GraphEntityMapper graphEntityMapper;

  @Autowired
  public EntityWriterInterceptor(@NonNull GraphEntityMapper graphEntityMapper,
      @NonNull TupleEntityMapper tupleEntityMapper) {
    this.graphEntityMapper = graphEntityMapper;
    this.tupleEntityMapper = tupleEntityMapper;
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

      Response response = ((ResponseProperty) entity.getSchemaMap().get(mediaType)).getResponse();
      Map<String, Property> headers = response.getHeaders();

      if (headers != null) {
        for (Entry<String, Property> header : headers.entrySet()) {
          Map<String, Object> vendorExtensions = header.getValue().getVendorExtensions();

          LOG.debug("Vendor extensions for header param '{}': {}", header.getKey(),
              vendorExtensions);

          Object parameterIdString = vendorExtensions.get(OpenApiSpecificationExtensions.PARAMETER);

          if (parameterIdString == null) {
            continue;
          }

          InformationProduct product = entity.getEntityContext().getInformationProduct();
          Parameter<?> parameter = getParameter(product, (String) parameterIdString);
          Object value = parameter.handle(entity.getEntityContext().getResponseParameters());

          context.getHeaders().add(parameter.getName(), value);
        }
      }
    }

    context.proceed();
  }

  private static Parameter<?> getParameter(InformationProduct product, String parameterIdString) {
    IRI iri = VALUE_FACTORY.createIRI((String) parameterIdString);
    Parameter<?> parameter = null;

    for (Parameter<?> productParameter : product.getParameters()) {
      if (productParameter.getIdentifier().equals(iri)) {
        parameter = productParameter;
      }
    }

    if (parameter == null) {
      throw new ConfigurationException(
          String.format("No parameter found for vendor extension value: '%s'", parameterIdString));
    }
    return parameter;
  }

}

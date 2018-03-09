package org.dotwebstack.framework.frontend.ld.mappers;

import javax.ws.rs.HttpMethod;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.http.ExpandFormatParameter;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.ld.SupportedMediaTypesScanner;
import org.dotwebstack.framework.frontend.ld.endpoint.AbstractEndPoint;
import org.dotwebstack.framework.frontend.ld.endpoint.DirectEndPoint;
import org.dotwebstack.framework.frontend.ld.endpoint.DirectEndPointResourceProvider;
import org.dotwebstack.framework.frontend.ld.handlers.EndPointRequestHandlerFactory;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.glassfish.jersey.server.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LdEndPointRequestMapper {

  private static final Logger LOG = LoggerFactory.getLogger(LdEndPointRequestMapper.class);

  private final DirectEndPointResourceProvider endPointResourceProvider;

  private final SupportedMediaTypesScanner supportedMediaTypesScanner;

  private final EndPointRequestHandlerFactory endPointRequestHandlerFactory;

  @Autowired
  public LdEndPointRequestMapper(@NonNull DirectEndPointResourceProvider endPointResourceProvider,
      @NonNull SupportedMediaTypesScanner supportedMediaTypesScanner,
      @NonNull EndPointRequestHandlerFactory endPointRequestHandlerFactory) {
    this.endPointResourceProvider = endPointResourceProvider;
    this.supportedMediaTypesScanner = supportedMediaTypesScanner;
    this.endPointRequestHandlerFactory = endPointRequestHandlerFactory;
  }

  public void loadEndPoints(HttpConfiguration httpConfiguration) {
    for (AbstractEndPoint endPoint : endPointResourceProvider.getAll().values()) {
      if (endPoint.getStage() != null) {
        mapRepresentation(endPoint, httpConfiguration);
      } else {
        LOG.warn("Endpoint '{}' is not mapped to a stage.", endPoint.getIdentifier());
      }
    }
  }

  private void mapRepresentation(AbstractEndPoint endPoint, HttpConfiguration httpConfiguration) {
    if (endPoint instanceof DirectEndPoint) {
      final Representation representation = ((DirectEndPoint) endPoint).getGetRepresentation();
      String basePath = endPoint.getStage().getFullPath();
      String absolutePath = basePath.concat(endPoint.getPathPattern());
      Resource.Builder resourceBuilder = Resource.builder().path(absolutePath);
      resourceBuilder.addMethod(HttpMethod.GET).handledBy(
          endPointRequestHandlerFactory.newEndPointRequestHandler(endPoint)).produces(
              supportedMediaTypesScanner.getMediaTypes(
                  representation.getInformationProduct().getResultType())).nameBindings(
                      ExpandFormatParameter.class);
      if (!httpConfiguration.resourceAlreadyRegistered(absolutePath)) {
        httpConfiguration.registerResources(resourceBuilder.build());
        LOG.debug("Mapped GET operation for request path {}", absolutePath);
      } else {
        LOG.error("Resource <{}> is not registered", absolutePath);
      }
    }
  }

}

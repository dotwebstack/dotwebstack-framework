package org.dotwebstack.framework.frontend.ld.mappers;

import javax.ws.rs.HttpMethod;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.http.ExpandFormatParameter;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.ld.SupportedWriterMediaTypesScanner;
import org.dotwebstack.framework.frontend.ld.endpoint.DynamicEndPoint;
import org.dotwebstack.framework.frontend.ld.endpoint.DynamicEndPointResourceProvider;
import org.dotwebstack.framework.frontend.ld.handlers.RepresentationRequestHandlerFactory;
import org.glassfish.jersey.server.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DynamicEndPointRequestMapper {

  private static final Logger LOG = LoggerFactory.getLogger(DirectEndPointRequestMapper.class);

  private final DynamicEndPointResourceProvider dynamicEndPointResourceProvider;

  private final SupportedWriterMediaTypesScanner supportedWriterMediaTypesScanner;

  private final RepresentationRequestHandlerFactory representationRequestHandlerFactory;

  @Autowired
  public DynamicEndPointRequestMapper(
      @NonNull DynamicEndPointResourceProvider dynamicEndPointResourceProvider,
      @NonNull SupportedWriterMediaTypesScanner supportedWriterMediaTypesScanner,
      @NonNull RepresentationRequestHandlerFactory representationRequestHandlerFactory) {
    this.dynamicEndPointResourceProvider = dynamicEndPointResourceProvider;
    this.supportedWriterMediaTypesScanner = supportedWriterMediaTypesScanner;
    this.representationRequestHandlerFactory = representationRequestHandlerFactory;
  }

  public void loadDynamicEndPoints(HttpConfiguration httpConfiguration) {
    for (DynamicEndPoint endPoint : dynamicEndPointResourceProvider.getAll().values()) {
      if (endPoint.getStage() != null) {
        mapService(endPoint, httpConfiguration);
      } else {
        LOG.warn("DynamicEndpoint '{}' is not mapped to a stage.", endPoint.getIdentifier());
      }
    }
  }

  private void mapService(DynamicEndPoint endPoint, HttpConfiguration httpConfiguration) {
    String basePath = endPoint.getStage().getFullPath();
    String absolutePath = basePath.concat(endPoint.getPathPattern());
    Resource.Builder resourceBuilder = Resource.builder().path(absolutePath);
    resourceBuilder.addMethod(HttpMethod.GET).handledBy(
        representationRequestHandlerFactory.newRepresentationRequestHandler(endPoint)).produces(
            supportedWriterMediaTypesScanner.getAllSupportedMediaTypes()).nameBindings(
                ExpandFormatParameter.class);
    if (!httpConfiguration.resourceAlreadyRegistered(absolutePath, HttpMethod.GET)) {
      httpConfiguration.registerResources(resourceBuilder.build());
      LOG.debug("Mapped {} operation for request path {}",
          resourceBuilder.build().getResourceMethods(), absolutePath);
    } else {
      LOG.error("Resource <{}> is not registered", absolutePath);
    }
  }

}

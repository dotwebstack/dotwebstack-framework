package org.dotwebstack.framework.frontend.ld.mappers;

import javax.ws.rs.HttpMethod;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.http.ExpandFormatParameter;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.ld.SupportedWriterMediaTypesScanner;
import org.dotwebstack.framework.frontend.ld.endpoint.DynamicEndpoint;
import org.dotwebstack.framework.frontend.ld.endpoint.DynamicEndpointResourceProvider;
import org.dotwebstack.framework.frontend.ld.handlers.RepresentationRequestHandlerFactory;
import org.glassfish.jersey.server.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DynamicEndpointRequestMapper {

  private static final Logger LOG = LoggerFactory.getLogger(DirectEndpointRequestMapper.class);

  private final DynamicEndpointResourceProvider dynamicEndpointResourceProvider;

  private final SupportedWriterMediaTypesScanner supportedWriterMediaTypesScanner;

  private final RepresentationRequestHandlerFactory representationRequestHandlerFactory;

  @Autowired
  public DynamicEndpointRequestMapper(
      @NonNull DynamicEndpointResourceProvider dynamicEndpointResourceProvider,
      @NonNull SupportedWriterMediaTypesScanner supportedWriterMediaTypesScanner,
      @NonNull RepresentationRequestHandlerFactory representationRequestHandlerFactory) {
    this.dynamicEndpointResourceProvider = dynamicEndpointResourceProvider;
    this.supportedWriterMediaTypesScanner = supportedWriterMediaTypesScanner;
    this.representationRequestHandlerFactory = representationRequestHandlerFactory;
  }

  public void loadDynamicEndpoints(HttpConfiguration httpConfiguration) {
    for (DynamicEndpoint endPoint : dynamicEndpointResourceProvider.getAll().values()) {
      if (endPoint.getStage() != null) {
        mapService(endPoint, httpConfiguration);
      } else {
        LOG.warn("DynamicEndpoint '{}' is not mapped to a stage.", endPoint.getIdentifier());
      }
    }
  }

  private void mapService(DynamicEndpoint endPoint, HttpConfiguration httpConfiguration) {
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
      LOG.debug("Resource <{}> is not registered", absolutePath);
    }
  }

}

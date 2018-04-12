package org.dotwebstack.framework.frontend.ld.mappers;

import java.util.Optional;
import javax.ws.rs.HttpMethod;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.http.ExpandFormatParameter;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.ld.SupportedReaderMediaTypesScanner;
import org.dotwebstack.framework.frontend.ld.SupportedWriterMediaTypesScanner;
import org.dotwebstack.framework.frontend.ld.endpoint.DirectEndpoint;
import org.dotwebstack.framework.frontend.ld.endpoint.DirectEndpointResourceProvider;
import org.dotwebstack.framework.frontend.ld.handlers.RepresentationRequestHandlerFactory;
import org.dotwebstack.framework.frontend.ld.handlers.ServiceRequestHandlerFactory;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.dotwebstack.framework.frontend.ld.service.Service;
import org.glassfish.jersey.server.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Service
public class DirectEndpointRequestMapper {

  private static final Logger LOG = LoggerFactory.getLogger(DirectEndpointRequestMapper.class);

  private final DirectEndpointResourceProvider directEndpointResourceProvider;

  private final SupportedWriterMediaTypesScanner supportedWriterMediaTypesScanner;

  private final SupportedReaderMediaTypesScanner supportedReaderMediaTypesScanner;

  private final RepresentationRequestHandlerFactory representationRequestHandlerFactory;

  private final ServiceRequestHandlerFactory serviceRequestHandlerFactory;

  @Autowired
  public DirectEndpointRequestMapper(
      @NonNull DirectEndpointResourceProvider directEndpointResourceProvider,
      @NonNull SupportedWriterMediaTypesScanner supportedWriterMediaTypesScanner,
      @NonNull SupportedReaderMediaTypesScanner supportedReaderMediaTypesScanner,
      @NonNull RepresentationRequestHandlerFactory representationRequestHandlerFactory,
      @NonNull ServiceRequestHandlerFactory serviceRequestHandlerFactory) {
    this.directEndpointResourceProvider = directEndpointResourceProvider;
    this.supportedWriterMediaTypesScanner = supportedWriterMediaTypesScanner;
    this.supportedReaderMediaTypesScanner = supportedReaderMediaTypesScanner;
    this.representationRequestHandlerFactory = representationRequestHandlerFactory;
    this.serviceRequestHandlerFactory = serviceRequestHandlerFactory;
  }

  public void loadDirectEndpoints(HttpConfiguration httpConfiguration) {
    for (DirectEndpoint endPoint : directEndpointResourceProvider.getAll().values()) {
      if (endPoint.getStage() != null) {
        mapService(endPoint, httpConfiguration);
        mapRepresentation(endPoint, httpConfiguration);
      } else {
        LOG.warn("DirectEndpoint '{}' is not mapped to a stage.", endPoint.getIdentifier());
      }
    }
  }

  private void mapService(DirectEndpoint endPoint, HttpConfiguration httpConfiguration) {
    String basePath = endPoint.getStage().getFullPath();
    String absolutePath = basePath.concat(endPoint.getPathPattern());
    final Optional<Service> deleteService = Optional.ofNullable(endPoint.getDeleteService());
    deleteService.ifPresent(service -> registerTransaction(service, HttpMethod.DELETE, absolutePath,
        httpConfiguration));
    final Optional<Service> postService = Optional.ofNullable(endPoint.getPostService());
    postService.ifPresent(
        service -> registerTransaction(service, HttpMethod.POST, absolutePath, httpConfiguration));
    final Optional<Service> putService = Optional.ofNullable(endPoint.getPutService());
    putService.ifPresent(
        service -> registerTransaction(service, HttpMethod.PUT, absolutePath, httpConfiguration));
  }

  private void mapRepresentation(DirectEndpoint endPoint, HttpConfiguration httpConfiguration) {
    String basePath = endPoint.getStage().getFullPath();
    String absolutePath = basePath.concat(endPoint.getPathPattern());
    Resource.Builder resourceBuilder = Resource.builder().path(absolutePath);
    Optional.ofNullable(endPoint.getGetRepresentation()).ifPresent(
        getRepresentation -> buildResource(httpConfiguration, resourceBuilder, absolutePath,
            HttpMethod.GET, getRepresentation, endPoint));
    Optional.ofNullable(endPoint.getPostRepresentation()).ifPresent(
        postRepresentation -> buildResource(httpConfiguration, resourceBuilder, absolutePath,
            HttpMethod.POST, postRepresentation, endPoint));
  }

  private void registerTransaction(Service service, String httpMethod, String absolutePath,
      HttpConfiguration httpConfiguration) {
    Resource.Builder resourceBuilder = Resource.builder().path(absolutePath);
    resourceBuilder.addMethod(httpMethod).handledBy(
        serviceRequestHandlerFactory.newServiceRequestHandler(service)).consumes(
            supportedReaderMediaTypesScanner.getMediaTypes());
    buildResource(httpConfiguration, resourceBuilder, absolutePath, httpMethod, null, null);
  }

  private void buildResource(HttpConfiguration httpConfiguration,
      final Resource.Builder resourceBuilder, String absolutePath, String httpMethod,
      Representation representation, DirectEndpoint endPoint) {
    Optional.ofNullable(representation).ifPresent(
        optionalRepresentation -> Optional.ofNullable(endPoint).ifPresent(optionalEndpoint -> {
          resourceBuilder.addMethod(httpMethod).handledBy(
              representationRequestHandlerFactory.newRepresentationRequestHandler(
                  optionalEndpoint)).produces(supportedWriterMediaTypesScanner.getMediaTypes(
                      optionalRepresentation.getInformationProduct().getResultType())).nameBindings(
                          ExpandFormatParameter.class);
          LOG.debug("Found representation {} for method {}", representation.getIdentifier(),
              httpMethod);
        }));
    if (!httpConfiguration.resourceAlreadyRegistered(absolutePath, httpMethod)) {
      httpConfiguration.registerResources(resourceBuilder.build());
      LOG.debug("Mapped {} operation for request path {}",
          resourceBuilder.build().getResourceMethods(), absolutePath);
    } else {
      LOG.debug("Resource <{}> is not registered", absolutePath);
    }
  }

}

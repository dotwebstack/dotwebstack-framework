package org.dotwebstack.framework.frontend.ld.mappers;

import static javax.ws.rs.HttpMethod.DELETE;
import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.HttpMethod.PUT;

import java.util.Optional;
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
    directEndpointResourceProvider.getAll().values().stream() //
        .filter(DirectEndpointRequestMapper::noMappedStage) //
        .forEach(endpoint -> {
          registerRepresentation(httpConfiguration, endpoint, GET);
          registerRepresentation(httpConfiguration, endpoint, POST);
          registerService(httpConfiguration, endpoint, DELETE);
          registerService(httpConfiguration, endpoint, POST);
          registerService(httpConfiguration, endpoint, PUT);
        });
  }

  private static boolean noMappedStage(DirectEndpoint endpoint) {
    if (endpoint.getStage() != null) {
      return true;
    }
    LOG.warn("DirectEndpoint '{}' is not mapped to a stage.", endpoint.getIdentifier());
    return false;
  }

  private void registerRepresentation(HttpConfiguration config, DirectEndpoint endpoint,
      String method) {
    Representation representation = endpoint.getRepresentationFor(method);

    validateAndGetBuilder(config, endpoint, method, representation)//
        .ifPresent(builder -> {
          builder//
              .addMethod(method)//
              .handledBy(
                  representationRequestHandlerFactory.newRepresentationRequestHandler(endpoint))//
              .produces(supportedWriterMediaTypesScanner.getMediaTypes(
                  representation.getInformationProduct().getResultType()))//
              .nameBindings(ExpandFormatParameter.class);
          registerResource(config, method, builder.build());
        });
  }

  private Optional<Resource.Builder> validateAndGetBuilder(HttpConfiguration httpConfiguration,
      DirectEndpoint endpoint,
      String method, Representation representation) {
    if (representation == null) {
      return Optional.empty();
    }

    String absolutePath = endpoint.getStage().getFullPath().concat(endpoint.getPathPattern());
    if (httpConfiguration.resourceAlreadyRegistered(absolutePath, method)) {
      LOG.debug("Method {} was already registered for resource <{}>", method, absolutePath);
      return Optional.empty();
    }

    return Optional.of(Resource.builder(absolutePath));
  }

  private void registerResource(HttpConfiguration config, String method, Resource resource) {
    config.registerResources(resource);
    LOG.debug("Registered {} method for request path {}", method, resource.getPath());
  }

  private void registerService(HttpConfiguration config, DirectEndpoint endpoint, String method) {
    Service service = endpoint.getServiceFor(method);
    validateAndGetBuilder(config, endpoint, method, service)//
        .ifPresent(builder -> {
          builder//
              .addMethod(method)//
              .handledBy(serviceRequestHandlerFactory.newServiceRequestHandler(service))//
              .consumes(supportedReaderMediaTypesScanner.getMediaTypes());//
          registerResource(config, method, builder.build());
        });
  }

}

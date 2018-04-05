package org.dotwebstack.framework.frontend.ld.mappers;

import java.util.Optional;
import javax.ws.rs.HttpMethod;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.http.ExpandFormatParameter;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.ld.SupportedReaderMediaTypesScanner;
import org.dotwebstack.framework.frontend.ld.SupportedWriterMediaTypesScanner;
import org.dotwebstack.framework.frontend.ld.endpoint.AbstractEndPoint;
import org.dotwebstack.framework.frontend.ld.endpoint.DirectEndPoint;
import org.dotwebstack.framework.frontend.ld.endpoint.DirectEndPointResourceProvider;
import org.dotwebstack.framework.frontend.ld.handlers.RepresentationRequestHandlerFactory;
import org.dotwebstack.framework.frontend.ld.handlers.ServiceRequestHandlerFactory;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.glassfish.jersey.server.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DirectEndPointRequestMapper {

  private static final Logger LOG = LoggerFactory.getLogger(DirectEndPointRequestMapper.class);

  private DirectEndPointResourceProvider directEndPointResourceProvider;

  private SupportedWriterMediaTypesScanner supportedWriterMediaTypesScanner;

  private SupportedReaderMediaTypesScanner supportedReaderMediaTypesScanner;

  private RepresentationRequestHandlerFactory representationRequestHandlerFactory;

  private ServiceRequestHandlerFactory serviceRequestHandlerFactory;

  @Autowired
  public DirectEndPointRequestMapper(
      @NonNull DirectEndPointResourceProvider directEndPointResourceProvider,
      @NonNull SupportedWriterMediaTypesScanner supportedWriterMediaTypesScanner,
      @NonNull SupportedReaderMediaTypesScanner supportedReaderMediaTypesScanner,
      @NonNull RepresentationRequestHandlerFactory representationRequestHandlerFactory,
      @NonNull ServiceRequestHandlerFactory serviceRequestHandlerFactory) {
    this.directEndPointResourceProvider = directEndPointResourceProvider;
    this.supportedWriterMediaTypesScanner = supportedWriterMediaTypesScanner;
    this.supportedReaderMediaTypesScanner = supportedReaderMediaTypesScanner;
    this.representationRequestHandlerFactory = representationRequestHandlerFactory;
    this.serviceRequestHandlerFactory = serviceRequestHandlerFactory;
  }

  public void loadDirectEndPoints(HttpConfiguration httpConfiguration) {
    for (DirectEndPoint endPoint : directEndPointResourceProvider.getAll().values()) {
      if (endPoint.getStage() != null) {
        mapService(endPoint, httpConfiguration);
        mapRepresentation(endPoint, httpConfiguration);
      } else {
        LOG.warn("DirectEndpoint '{}' is not mapped to a stage.", endPoint.getIdentifier());
      }
    }
  }

  private void mapService(DirectEndPoint endPoint, HttpConfiguration httpConfiguration) {
    String basePath = endPoint.getStage().getFullPath();
    String absolutePath = basePath.concat(endPoint.getPathPattern());
    final Optional<org.dotwebstack.framework.frontend.ld.service.Service> deleteService =
        Optional.ofNullable(endPoint.getDeleteService());
    deleteService.ifPresent(service -> registerTransaction(service, HttpMethod.DELETE, absolutePath,
        httpConfiguration));
    final Optional<org.dotwebstack.framework.frontend.ld.service.Service> postService =
        Optional.ofNullable(endPoint.getPostService());
    postService.ifPresent(
        service -> registerTransaction(service, HttpMethod.POST, absolutePath, httpConfiguration));
    final Optional<org.dotwebstack.framework.frontend.ld.service.Service> putService =
        Optional.ofNullable(endPoint.getPutService());
    putService.ifPresent(
        service -> registerTransaction(service, HttpMethod.PUT, absolutePath, httpConfiguration));
  }

  private void mapRepresentation(AbstractEndPoint endPoint, HttpConfiguration httpConfiguration) {
    String basePath = endPoint.getStage().getFullPath();
    String absolutePath = basePath.concat(endPoint.getPathPattern());
    Optional<Representation> getRepresentation =
        Optional.ofNullable(((DirectEndPoint) endPoint).getGetRepresentation());
    Optional<Representation> postRepresentation =
        Optional.ofNullable(((DirectEndPoint) endPoint).getPostRepresentation());


  }

  private void registerTransaction(org.dotwebstack.framework.frontend.ld.service.Service service,
      String httpMethod, String absolutePath, HttpConfiguration httpConfiguration) {
    Resource.Builder resourceBuilder = Resource.builder().path(absolutePath);
    resourceBuilder.addMethod(httpMethod).handledBy(
        serviceRequestHandlerFactory.newServiceRequestHandler(service.getTransaction())).consumes(
            supportedReaderMediaTypesScanner.getMediaTypes());
    buildResource(httpConfiguration, resourceBuilder, absolutePath, httpMethod, null, null);
  }

  private void buildResource(HttpConfiguration httpConfiguration,
      final Resource.Builder resourceBuilder, String absolutePath, String httpMethod,
      Representation representation, DirectEndPoint endPoint) {
    resourceBuilder.addMethod(httpMethod).handledBy(
        representationRequestHandlerFactory.newRepresentationRequestHandler(endPoint)).produces(
            supportedWriterMediaTypesScanner.getMediaTypes(
                representation.getInformationProduct().getResultType())).nameBindings(
                    ExpandFormatParameter.class);
    if (!httpConfiguration.resourceAlreadyRegistered(absolutePath, httpMethod)) {
      httpConfiguration.registerResources(resourceBuilder.build());
      LOG.debug("Mapped {} operation for request path {}",
          resourceBuilder.build().getResourceMethods(), absolutePath);
    } else {
      LOG.error("Resource <{}> is not registered", absolutePath);
    }
  }

}

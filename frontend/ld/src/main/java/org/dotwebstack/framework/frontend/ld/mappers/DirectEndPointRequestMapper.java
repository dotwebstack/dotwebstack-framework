package org.dotwebstack.framework.frontend.ld.mappers;

import java.util.Arrays;
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
import org.dotwebstack.framework.frontend.ld.handlers.ServiceRequestHandler;
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

  private RepresentationRequestHandlerFactory representationRequestHandlerFactory;

  private ServiceRequestHandlerFactory serviceRequestHandlerFactory;

  private SupportedReaderMediaTypesScanner supportedReaderMediaTypesScanner;

  @Autowired
  public DirectEndPointRequestMapper(
      @NonNull DirectEndPointResourceProvider directEndPointResourceProvider,
      @NonNull SupportedWriterMediaTypesScanner supportedWriterMediaTypesScanner,
      @NonNull SupportedReaderMediaTypesScanner supportedReaderMediaTypesScanner,
      @NonNull RepresentationRequestHandlerFactory representationRequestHandlerFactory,
      @NonNull ServiceRequestHandlerFactory serviceRequestHandlerFactory) {
    this.directEndPointResourceProvider = directEndPointResourceProvider;
    this.supportedWriterMediaTypesScanner = supportedWriterMediaTypesScanner;
    this.representationRequestHandlerFactory = representationRequestHandlerFactory;
    this.serviceRequestHandlerFactory = serviceRequestHandlerFactory;
    this.supportedReaderMediaTypesScanner = supportedReaderMediaTypesScanner;
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
    endPoint.getDeleteService().ifPresent(deleteService -> registerTransaction(deleteService,
        HttpMethod.DELETE, absolutePath, httpConfiguration));
    endPoint.getPostService().ifPresent(postService -> registerTransaction(postService,
        HttpMethod.POST, absolutePath, httpConfiguration));
    endPoint.getPutService().ifPresent(putService -> registerTransaction(putService, HttpMethod.PUT,
        absolutePath, httpConfiguration));
  }

  private void mapRepresentation(AbstractEndPoint endPoint, HttpConfiguration httpConfiguration) {
    String basePath = endPoint.getStage().getFullPath();
    String absolutePath = basePath.concat(endPoint.getPathPattern());
    Optional<Representation> getRepresentation =
        Optional.ofNullable(((DirectEndPoint) endPoint).getGetRepresentation());
    Optional<Representation> postRepresentation =
        Optional.ofNullable(((DirectEndPoint) endPoint).getPostRepresentation());

    getRepresentation.ifPresent(representation -> {
      Resource.Builder resourceBuilder = Resource.builder().path(absolutePath);
      resourceBuilder.addMethod(HttpMethod.GET).handledBy(
          representationRequestHandlerFactory.newEndPointRequestHandler(endPoint)).produces(
              supportedWriterMediaTypesScanner.getMediaTypes(
                  representation.getInformationProduct().getResultType())).nameBindings(
                      ExpandFormatParameter.class);
      buildResource(httpConfiguration, resourceBuilder, absolutePath, HttpMethod.GET);
    });
    postRepresentation.ifPresent(representation -> {
      Resource.Builder resourceBuilder = Resource.builder().path(absolutePath);
      resourceBuilder.addMethod(HttpMethod.POST).handledBy(
          representationRequestHandlerFactory.newEndPointRequestHandler(endPoint)).produces(
              supportedWriterMediaTypesScanner.getMediaTypes(
                  representation.getInformationProduct().getResultType())).nameBindings(
                      ExpandFormatParameter.class);
      buildResource(httpConfiguration, resourceBuilder, absolutePath, HttpMethod.POST);
    });
  }

  private void registerTransaction(org.dotwebstack.framework.frontend.ld.service.Service service,
      String httpMethod, String absolutePath, HttpConfiguration httpConfiguration) {
    Resource.Builder resourceBuilder = Resource.builder().path(absolutePath);
    resourceBuilder.addMethod(httpMethod).handledBy(
        serviceRequestHandlerFactory.newServiceRequestHandler(service.getTransaction()),
        Arrays.stream(ServiceRequestHandler.class.getMethods()).filter(
            method -> method.getName() == "apply").findFirst().get()).consumes(
                supportedReaderMediaTypesScanner.getMediaTypes());
    buildResource(httpConfiguration, resourceBuilder, absolutePath, httpMethod);
  }

  private void buildResource(HttpConfiguration httpConfiguration, Resource.Builder resourceBuilder,
      String absolutePath, String httpMethod) {
    if (!httpConfiguration.resourceAlreadyRegistered(absolutePath, httpMethod)) {
      httpConfiguration.registerResources(resourceBuilder.build());
      LOG.debug("Mapped {} operation for request path {}",
          resourceBuilder.build().getResourceMethods(), absolutePath);
    } else {
      LOG.error("Resource <{}> is not registered", absolutePath);
    }
  }

}

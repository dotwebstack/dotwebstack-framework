package org.dotwebstack.framework.frontend.ld.mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.HttpMethod;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.http.ExpandFormatParameter;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.ld.SupportedMediaTypesScanner;
import org.dotwebstack.framework.frontend.ld.endpoint.AbstractEndPoint;
import org.dotwebstack.framework.frontend.ld.endpoint.DirectEndPoint;
import org.dotwebstack.framework.frontend.ld.endpoint.DirectEndPointResourceProvider;
import org.dotwebstack.framework.frontend.ld.endpoint.DynamicEndPointResourceProvider;
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

  private final DirectEndPointResourceProvider directEndPointResourceProvider;

  private final DynamicEndPointResourceProvider dynamicEndPointResourceProvider;

  private final SupportedMediaTypesScanner supportedMediaTypesScanner;

  private final EndPointRequestHandlerFactory endPointRequestHandlerFactory;

  @Autowired
  public LdEndPointRequestMapper(
      @NonNull DirectEndPointResourceProvider directEndPointResourceProvider,
      @NonNull DynamicEndPointResourceProvider dynamicEndPointResourceProvider,
      @NonNull SupportedMediaTypesScanner supportedMediaTypesScanner,
      @NonNull EndPointRequestHandlerFactory endPointRequestHandlerFactory) {
    this.directEndPointResourceProvider = directEndPointResourceProvider;
    this.dynamicEndPointResourceProvider = dynamicEndPointResourceProvider;
    this.supportedMediaTypesScanner = supportedMediaTypesScanner;
    this.endPointRequestHandlerFactory = endPointRequestHandlerFactory;
  }

  public void loadEndPoints(HttpConfiguration httpConfiguration) {
    List<AbstractEndPoint> allEndPoints = new ArrayList<>();
    allEndPoints.addAll(directEndPointResourceProvider.getAll().values());
    allEndPoints.addAll(dynamicEndPointResourceProvider.getAll().values());
    for (AbstractEndPoint endPoint : allEndPoints) {
      if (endPoint.getStage() != null) {
        mapRepresentation(endPoint, httpConfiguration);
      } else {
        LOG.warn("Endpoint '{}' is not mapped to a stage.", endPoint.getIdentifier());
      }
    }
  }

  private void mapRepresentation(AbstractEndPoint endPoint, HttpConfiguration httpConfiguration) {
    String basePath = endPoint.getStage().getFullPath();
    String absolutePath = basePath.concat(endPoint.getPathPattern());
    Resource.Builder resourceBuilder = Resource.builder().path(absolutePath);
    if (endPoint instanceof DirectEndPoint) {
      Optional<Representation> getRepresentation =
          Optional.ofNullable(((DirectEndPoint) endPoint).getGetRepresentation());
      Optional<Representation> postRepresentation =
          Optional.ofNullable(((DirectEndPoint) endPoint).getPostRepresentation());

      getRepresentation.ifPresent(representation -> {
        resourceBuilder.addMethod(HttpMethod.GET).handledBy(
            endPointRequestHandlerFactory.newEndPointRequestHandler(endPoint)).produces(
                supportedMediaTypesScanner.getMediaTypes(
                    representation.getInformationProduct().getResultType())).nameBindings(
                        ExpandFormatParameter.class);
      });
      postRepresentation.ifPresent(representation -> {
        resourceBuilder.addMethod(HttpMethod.POST).handledBy(
            endPointRequestHandlerFactory.newEndPointRequestHandler(endPoint)).produces(
                supportedMediaTypesScanner.getMediaTypes(
                    representation.getInformationProduct().getResultType())).nameBindings(
                        ExpandFormatParameter.class);
      });
    } else {
      resourceBuilder.addMethod(HttpMethod.GET).handledBy(
          endPointRequestHandlerFactory.newEndPointRequestHandler(endPoint)).produces(
              supportedMediaTypesScanner.getAllSupportedMediaTypes()).nameBindings(
                  ExpandFormatParameter.class);
    }
    if (!httpConfiguration.resourceAlreadyRegistered(absolutePath)) {
      httpConfiguration.registerResources(resourceBuilder.build());
      LOG.debug("Mapped GET operation for request path {}", absolutePath);
    } else {
      LOG.error("Resource <{}> is not registered", absolutePath);
    }
  }

}

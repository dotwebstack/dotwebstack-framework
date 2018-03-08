package org.dotwebstack.framework.frontend.ld.mappers;

import javax.ws.rs.HttpMethod;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.http.ExpandFormatParameter;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.ld.SupportedMediaTypesScanner;
import org.dotwebstack.framework.frontend.ld.endpoint.DirectEndPoint;
import org.dotwebstack.framework.frontend.ld.endpoint.EndPoint;
import org.dotwebstack.framework.frontend.ld.endpoint.EndPointResourceProvider;
import org.dotwebstack.framework.frontend.ld.handlers.RepresentationRequestHandlerFactory;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.glassfish.jersey.server.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LdEndPointRequestMapper {

  private static final Logger LOG = LoggerFactory.getLogger(LdEndPointRequestMapper.class);

  // private final RepresentationResourceProvider representationResourceProvider;

  private final EndPointResourceProvider endPointResourceProvider;

  private final SupportedMediaTypesScanner supportedMediaTypesScanner;

  private final RepresentationRequestHandlerFactory representationRequestHandlerFactory;

  @Autowired
  public LdEndPointRequestMapper(@NonNull EndPointResourceProvider endPointResourceProvider,
      @NonNull SupportedMediaTypesScanner supportedMediaTypesScanner,
      @NonNull RepresentationRequestHandlerFactory representationRequestHandlerFactory) {
    this.endPointResourceProvider = endPointResourceProvider;
    this.supportedMediaTypesScanner = supportedMediaTypesScanner;
    this.representationRequestHandlerFactory = representationRequestHandlerFactory;
  }

  public void loadEndPoints(HttpConfiguration httpConfiguration) {
    for (EndPoint endPoint : endPointResourceProvider.getAll().values()) {
      if (endPoint.getStage() != null) {
        mapEndPoint(endPoint, httpConfiguration);
      } else {
        LOG.warn("Endpoint '{}' is not mapped to a stage.", endPoint.getIdentifier());
      }
    }
  }

  private void mapEndPoint(EndPoint endPoint, HttpConfiguration httpConfiguration) {
    if (endPoint instanceof DirectEndPoint) {
      String basePath = endPoint.getStage().getFullPath();

      final Representation representation = ((DirectEndPoint) endPoint).getRepresentationGet();

      representation.getAppliesTo().forEach(appliesTo -> {
        String absolutePath = basePath.concat(appliesTo);

        Resource.Builder resourceBuilder = Resource.builder().path(absolutePath);
        resourceBuilder.addMethod(HttpMethod.GET).handledBy(
            representationRequestHandlerFactory.newRepresentationRequestHandler(
                representation)).produces(
                    supportedMediaTypesScanner.getMediaTypes(
                        representation.getInformationProduct().getResultType())).nameBindings(
                            ExpandFormatParameter.class);

        if (!httpConfiguration.resourceAlreadyRegistered(absolutePath)) {
          httpConfiguration.registerResources(resourceBuilder.build());
          LOG.debug("Mapped GET operation for request path {}", absolutePath);
        } else {
          LOG.error("Resource <%s> is not registered", absolutePath);
        }
      });
    }
  }

}

package org.dotwebstack.framework.frontend.ld;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import javax.ws.rs.HttpMethod;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.ld.handlers.GetRequestHandler;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.dotwebstack.framework.frontend.ld.representation.RepresentationResourceProvider;
import org.glassfish.jersey.server.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RequestMapper {

  private static final Logger LOG = LoggerFactory.getLogger(RequestMapper.class);

  private final RepresentationResourceProvider representationResourceProvider;

  private final SupportedMediaTypesScanner supportedMediaTypeScanner;

  @Autowired
  public RequestMapper(RepresentationResourceProvider representationResourceProvider,
      SupportedMediaTypesScanner supportedMediaTypeScanner) {
    this.representationResourceProvider = Objects.requireNonNull(representationResourceProvider);
    this.supportedMediaTypeScanner = supportedMediaTypeScanner;
  }

  void loadRepresentations(HttpConfiguration httpConfiguration) {

    for (Representation representation : representationResourceProvider.getAll().values()) {
      try {
        if (representation.getStage() != null) {
          mapRepresentation(representation, httpConfiguration);
        } else {
          LOG.warn("Representation '{}' is not mapped to a stage.", representation.getIdentifier());
        }
      } catch (URISyntaxException ex) {
        LOG.error("Representation '{}' is not mapped, because of not found base path.",
            representation.getIdentifier());
      }
    }
  }

  private void mapRepresentation(Representation representation, HttpConfiguration httpConfiguration)
      throws URISyntaxException {
    String basePath = createBasePath(representation);

    representation.getUrlPatterns().forEach(path -> {
      String absolutePath = basePath.concat(path);

      Resource.Builder resourceBuilder = Resource.builder().path(absolutePath);
      resourceBuilder.addMethod(HttpMethod.GET).handledBy(
          new GetRequestHandler(representation)).produces(
              supportedMediaTypeScanner.getMediaTypes(
                  representation.getInformationProduct().getResultType()));

      representation.getInformationProduct().getResultType();

      if (!httpConfiguration.resourceAlreadyRegistered(absolutePath)) {
        httpConfiguration.registerResources(resourceBuilder.build());
        LOG.debug("Mapped GET operation for request path {}", absolutePath);
      } else {
        LOG.error(String.format("Resource <%s> is registered", absolutePath));
      }
    });
  }


  private String createBasePath(Representation representation) throws URISyntaxException {
    Objects.requireNonNull(representation.getStage());
    Objects.requireNonNull(representation.getStage().getSite());

    URI uri = new URI(representation.getStage().getSite().getDomain());
    return "/" + uri.getPath() + representation.getStage().getBasePath();
  }
}

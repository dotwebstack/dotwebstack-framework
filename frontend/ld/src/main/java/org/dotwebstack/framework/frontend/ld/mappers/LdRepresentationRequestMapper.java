package org.dotwebstack.framework.frontend.ld.mappers;

import javax.ws.rs.HttpMethod;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.http.ExpandFormatParameter;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.ld.SupportedMediaTypesScanner;
import org.dotwebstack.framework.frontend.ld.handlers.RepresentationRequestHandlerFactory;
import org.dotwebstack.framework.frontend.ld.handlers.TransactionRequestHandlerFactory;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.dotwebstack.framework.frontend.ld.representation.RepresentationResourceProvider;
import org.glassfish.jersey.server.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LdRepresentationRequestMapper {

  private static final Logger LOG = LoggerFactory.getLogger(LdRepresentationRequestMapper.class);

  private final RepresentationResourceProvider representationResourceProvider;

  private final SupportedMediaTypesScanner supportedMediaTypesScanner;
  private final RepresentationRequestHandlerFactory representationRequestHandlerFactory;
  private final TransactionRequestHandlerFactory transactionRequestHandlerFactory;

  @Autowired
  public LdRepresentationRequestMapper(
      @NonNull RepresentationResourceProvider representationResourceProvider,
      @NonNull SupportedMediaTypesScanner supportedMediaTypesScanner,
      @NonNull RepresentationRequestHandlerFactory representationRequestHandlerFactory,
      @NonNull TransactionRequestHandlerFactory transactionRequestHandlerFactory) {
    this.representationResourceProvider = representationResourceProvider;
    this.supportedMediaTypesScanner = supportedMediaTypesScanner;
    this.representationRequestHandlerFactory = representationRequestHandlerFactory;
    this.transactionRequestHandlerFactory = transactionRequestHandlerFactory;
  }

  public void loadRepresentations(HttpConfiguration httpConfiguration) {
    for (Representation representation : representationResourceProvider.getAll().values()) {
      if (representation.getStage() != null) {
        mapRepresentation(representation, httpConfiguration);
      } else {
        LOG.warn("Representation '{}' is not mapped to a stage.", representation.getIdentifier());
      }
    }
  }

  private void mapRepresentation(Representation representation,
      HttpConfiguration httpConfiguration) {
    String basePath = representation.getStage().getFullPath();

    representation.getPathPatterns().forEach(path -> {
      String absolutePath = basePath.concat(path);

      Resource.Builder resourceBuilder = Resource.builder().path(absolutePath);
      if (representation.getInformationProduct() != null) {
        resourceBuilder.addMethod(HttpMethod.GET).handledBy(
            representationRequestHandlerFactory.newRepresentationRequestHandler(
                representation)).produces(
            supportedMediaTypesScanner.getMediaTypes(
                representation.getInformationProduct().getResultType())).nameBindings(
            ExpandFormatParameter.class);
      } else if (representation.getTransaction() != null) {
        resourceBuilder.addMethod(HttpMethod.POST).handledBy(
            transactionRequestHandlerFactory.newTransactionRequestHandler(
                representation.getTransaction()));
        resourceBuilder.addMethod(HttpMethod.PUT).handledBy(
            transactionRequestHandlerFactory.newTransactionRequestHandler(
                representation.getTransaction()));
      }

      if (!httpConfiguration.resourceAlreadyRegistered(absolutePath)) {
        httpConfiguration.registerResources(resourceBuilder.build());
        LOG.debug("Mapped {} operation(s) for request path {}",
            resourceBuilder.build().getResourceMethods(), absolutePath);
      } else {
        LOG.error("Resource <%s> is not registered", absolutePath);
      }
    });
  }

}

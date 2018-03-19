package org.dotwebstack.framework.frontend.ld.mappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import org.dotwebstack.framework.frontend.ld.endpoint.DynamicEndPointResourceProvider;
import org.dotwebstack.framework.frontend.ld.handlers.EndPointRequestHandlerFactory;
import org.dotwebstack.framework.frontend.ld.handlers.TransactionRequestHandler;
import org.dotwebstack.framework.frontend.ld.handlers.TransactionRequestHandlerFactory;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.glassfish.jersey.server.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LdEndPointRequestMapper {

  private static final Logger LOG = LoggerFactory.getLogger(LdEndPointRequestMapper.class);

  private DirectEndPointResourceProvider directEndPointResourceProvider;

  private DynamicEndPointResourceProvider dynamicEndPointResourceProvider;

  private SupportedWriterMediaTypesScanner supportedWriterMediaTypesScanner;

  private EndPointRequestHandlerFactory endPointRequestHandlerFactory;

  private TransactionRequestHandlerFactory transactionRequestHandlerFactory;

  private SupportedReaderMediaTypesScanner supportedReaderMediaTypesScanner;

  @Autowired
  public LdEndPointRequestMapper(
      @NonNull DirectEndPointResourceProvider directEndPointResourceProvider,
      @NonNull DynamicEndPointResourceProvider dynamicEndPointResourceProvider,
      @NonNull SupportedWriterMediaTypesScanner supportedWriterMediaTypesScanner,
      @NonNull SupportedReaderMediaTypesScanner supportedReaderMediaTypesScanner,
      @NonNull EndPointRequestHandlerFactory endPointRequestHandlerFactory,
      @NonNull TransactionRequestHandlerFactory transactionRequestHandlerFactory) {
    this.directEndPointResourceProvider = directEndPointResourceProvider;
    this.dynamicEndPointResourceProvider = dynamicEndPointResourceProvider;
    this.supportedWriterMediaTypesScanner = supportedWriterMediaTypesScanner;
    this.endPointRequestHandlerFactory = endPointRequestHandlerFactory;
    this.transactionRequestHandlerFactory = transactionRequestHandlerFactory;
    this.supportedReaderMediaTypesScanner = supportedReaderMediaTypesScanner;
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
    if (endPoint instanceof DirectEndPoint) {
      Optional<Representation> getRepresentation =
          Optional.ofNullable(((DirectEndPoint) endPoint).getGetRepresentation());
      Optional<Representation> postRepresentation =
          Optional.ofNullable(((DirectEndPoint) endPoint).getPostRepresentation());

      getRepresentation.ifPresent(representation -> {
        Resource.Builder resourceBuilder = Resource.builder().path(absolutePath);
        resourceBuilder.addMethod(HttpMethod.GET).handledBy(
            endPointRequestHandlerFactory.newEndPointRequestHandler(endPoint)).produces(
                supportedWriterMediaTypesScanner.getMediaTypes(
                    representation.getInformationProduct().getResultType())).nameBindings(
                        ExpandFormatParameter.class);
        buildResource(httpConfiguration, resourceBuilder, absolutePath);
      });
      postRepresentation.ifPresent(representation -> {
        Resource.Builder resourceBuilder = Resource.builder().path(absolutePath);
        resourceBuilder.addMethod(HttpMethod.POST).handledBy(
            transactionRequestHandlerFactory.newTransactionRequestHandler(
                representation.getTransaction()),
            Arrays.stream(TransactionRequestHandler.class.getMethods()).filter(
                method -> method.getName() == "apply").findFirst().get()).consumes(
                    supportedReaderMediaTypesScanner.getMediaTypes());
        buildResource(httpConfiguration, resourceBuilder, absolutePath);
      });
    } else {
      Resource.Builder resourceBuilder = Resource.builder().path(absolutePath);
      resourceBuilder.addMethod(HttpMethod.GET).handledBy(
          endPointRequestHandlerFactory.newEndPointRequestHandler(endPoint)).produces(
              supportedWriterMediaTypesScanner.getAllSupportedMediaTypes()).nameBindings(
                  ExpandFormatParameter.class);
      buildResource(httpConfiguration, resourceBuilder, absolutePath);
    }

  }

  private void buildResource(HttpConfiguration httpConfiguration, Resource.Builder resourceBuilder,
      String absolutePath) {
    if (!httpConfiguration.resourceAlreadyRegistered(absolutePath)) {
      httpConfiguration.registerResources(resourceBuilder.build());
      LOG.debug("Mapped {} operation for request path {}",
          resourceBuilder.build().getResourceMethods(), absolutePath);
    } else {
      LOG.error("Resource <{}> is not registered", absolutePath);
    }
  }

}

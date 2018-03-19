package org.dotwebstack.framework.frontend.ld.mappers;

// import java.util.Arrays;
// import javax.ws.rs.HttpMethod;
// import lombok.NonNull;
// import org.dotwebstack.framework.frontend.http.ExpandFormatParameter;
// import org.dotwebstack.framework.frontend.http.HttpConfiguration;
// import org.dotwebstack.framework.frontend.ld.SupportedReaderMediaTypesScanner;
// import org.dotwebstack.framework.frontend.ld.SupportedWriterMediaTypesScanner;
// import org.dotwebstack.framework.frontend.ld.endpoint.AbstractEndPoint;
// import org.dotwebstack.framework.frontend.ld.handlers.EndPointRequestHandlerFactory;
// import org.dotwebstack.framework.frontend.ld.handlers.TransactionRequestHandler;
// import org.dotwebstack.framework.frontend.ld.handlers.TransactionRequestHandlerFactory;
// import org.dotwebstack.framework.frontend.ld.representation.Representation;
// import org.dotwebstack.framework.frontend.ld.representation.RepresentationResourceProvider;
// import org.glassfish.jersey.server.model.Resource;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;
//
// @Service
// public class LdRepresentationRequestMapper {
//
// private static final Logger LOG = LoggerFactory.getLogger(LdRepresentationRequestMapper.class);
//
// private final RepresentationResourceProvider representationResourceProvider;
//
// private final SupportedWriterMediaTypesScanner supportedWriterMediaTypesScanner;
// private final SupportedReaderMediaTypesScanner supportedReaderMediaTypesScanner;
// private final EndPointRequestHandlerFactory endPointRequestHandlerFactory;
// private final TransactionRequestHandlerFactory transactionRequestHandlerFactory;
//
// @Autowired
// public LdRepresentationRequestMapper(
// @NonNull RepresentationResourceProvider representationResourceProvider,
// @NonNull SupportedWriterMediaTypesScanner supportedWriterMediaTypesScanner,
// @NonNull SupportedReaderMediaTypesScanner supportedReaderMediaTypesScanner,
// @NonNull EndPointRequestHandlerFactory endPointRequestHandlerFactory,
// @NonNull TransactionRequestHandlerFactory transactionRequestHandlerFactory) {
// this.representationResourceProvider = representationResourceProvider;
// this.supportedWriterMediaTypesScanner = supportedWriterMediaTypesScanner;
// this.supportedReaderMediaTypesScanner = supportedReaderMediaTypesScanner;
// this.endPointRequestHandlerFactory = endPointRequestHandlerFactory;
// this.transactionRequestHandlerFactory = transactionRequestHandlerFactory;
// }
//
// public void loadRepresentations(HttpConfiguration httpConfiguration) {
// for (Representation representation : representationResourceProvider.getAll().values()) {
// if (representation.getStage() != null) {
// mapRepresentation(representation, httpConfiguration);
// } else {
// LOG.warn("Representation '{}' is not mapped to a stage.", representation.getIdentifier());
// }
// }
// }
//
// private void mapRepresentation(AbstractEndPoint endPoint,
// HttpConfiguration httpConfiguration) {
// String basePath = representation.getStage().getFullPath();
//
// endPoint.getPathPatterns().forEach(path -> {
// String absolutePath = basePath.concat(path);
//
// Resource.Builder resourceBuilder = Resource.builder().path(absolutePath);
// if (representation.getInformationProduct() != null) {
// resourceBuilder.addMethod(HttpMethod.GET).handledBy(
// representationRequestHandlerFactory.newRepresentationRequestHandler(
// representation)).produces(
// supportedWriterMediaTypesScanner.getMediaTypes(
// representation.getInformationProduct().getResultType())).nameBindings(
// ExpandFormatParameter.class);
// } else if (representation.getTransaction() != null) {
// resourceBuilder.addMethod(HttpMethod.POST).handledBy(
// transactionRequestHandlerFactory.newTransactionRequestHandler(
// representation.getTransaction()),
// Arrays.stream(TransactionRequestHandler.class.getMethods()).filter(
// method -> method.getName() == "apply").findFirst().get()).consumes(
// supportedReaderMediaTypesScanner.getMediaTypes());
// }
//
// if (!httpConfiguration.resourceAlreadyRegistered(absolutePath)) {
// httpConfiguration.registerResources(resourceBuilder.build());
// LOG.debug("Mapped {} operation(s) for request path {}",
// resourceBuilder.build().getResourceMethods(), absolutePath);
// } else {
// LOG.error("Resource <{}> is not registered", absolutePath);
// }
// });
// }
//
// }

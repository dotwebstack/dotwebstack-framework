package org.dotwebstack.framework.frontend.ld.handlers;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.ld.entity.GraphEntity;
import org.dotwebstack.framework.frontend.ld.entity.TupleEntity;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.dotwebstack.framework.frontend.ld.representation.RepresentationResourceProvider;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.glassfish.jersey.process.Inflector;

@Slf4j
public abstract class RequestHandler<T> implements Inflector<ContainerRequestContext, Response> {

  protected final T endpoint;

  protected final EndpointRequestParameterMapper endpointRequestParameterMapper;

  protected final RepresentationResourceProvider representationResourceProvider;

  public RequestHandler(@NonNull T endpoint,
      @NonNull EndpointRequestParameterMapper endpointRequestParameterMapper,
      @NonNull RepresentationResourceProvider representationResourceProvider) {
    this.endpoint = endpoint;
    this.endpointRequestParameterMapper = endpointRequestParameterMapper;
    this.representationResourceProvider = representationResourceProvider;
  }

  protected EndpointRequestParameterMapper getEndpointRequestParameterMapper() {
    return endpointRequestParameterMapper;
  }

  protected Response applyRepresentation(@NonNull Representation representation,
      @NonNull ContainerRequestContext containerRequestContext,
      @NonNull Map<String, String> parameterValues) {
    InformationProduct informationProduct = representation.getInformationProduct();

    endpointRequestParameterMapper.map(informationProduct, containerRequestContext)//
        .forEach(parameterValues::put);
    representation.getParameterMappers().stream()//
        .map(parameterMapper ->  parameterMapper.map(containerRequestContext))//
        .forEach(parameterValues::putAll);

    List<Variant> reqVariants = Variant.mediaTypes(MediaType.TEXT_HTML_TYPE).build();
    Variant prefered = containerRequestContext.getRequest().selectVariant(reqVariants);

    if (prefered.getMediaType().isCompatible(MediaType.TEXT_HTML_TYPE)) {
      URI uri = containerRequestContext.getUriInfo().getAbsolutePath();
      return generateHtmlResponse(representation.getHtmlTemplate(), uri.toString());
    }

    Object result = informationProduct.getResult(parameterValues);

    switch (informationProduct.getResultType()) {
      case GRAPH:
        return Response.ok(new GraphEntity((GraphQueryResult) result, representation)).build();
      case TUPLE:
        return Response.ok(new TupleEntity((TupleQueryResult) result, representation)).build();
      default:
        throw new ConfigurationException(
            String.format("Result type %s not supported for information product %s",
                informationProduct.getResultType(), informationProduct.getIdentifier()));
    }
  }

  private Response generateHtmlResponse(Template htmlTemplate, String uri)  {
    if (htmlTemplate == null) {
      return Response.notAcceptable(Collections.singletonList(
          new Variant(MediaType.TEXT_HTML_TYPE, "en", "UTF-8"))).build();
    }

    Map<String, Object> freeMarkerDataModel = new HashMap<>();
    freeMarkerDataModel.put("result", uri);
    StringWriter stringWriter = new StringWriter();
    try {
      htmlTemplate.process(freeMarkerDataModel, stringWriter);
      StringBuffer buffer = stringWriter.getBuffer();
      String htmlString = buffer != null ? buffer.toString() : "UNKNOWN";
      return Response.ok(htmlString).build();
    } catch (TemplateException te) {
      LOG.error(te.getMessage(), te);
      return Response.noContent().build();
    } catch (IOException ioe) {
      LOG.error(ioe.getMessage(), ioe);
      return Response.serverError().build();
    }

  }
}

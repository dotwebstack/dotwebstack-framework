package org.dotwebstack.framework.frontend.ld.handlers;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import javax.ws.rs.core.UriInfo;
import javax.xml.ws.http.HTTPException;
import lombok.NonNull;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.ld.entity.GraphEntity;
import org.dotwebstack.framework.frontend.ld.entity.TupleEntity;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.dotwebstack.framework.frontend.ld.representation.RepresentationResourceProvider;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.glassfish.jersey.process.Inflector;

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

    endpointRequestParameterMapper.map(informationProduct, containerRequestContext).forEach(
        parameterValues::put);
    representation.getParameterMappers().forEach(
        parameterMapper -> parameterValues.putAll(parameterMapper.map(containerRequestContext)));

    Object result = informationProduct.getResult(parameterValues);

    MultivaluedMap<String, String> headers = containerRequestContext.getHeaders();
    List<String> acceptHeaders = headers.get("accept") != null
        ? headers.get("accept") : Collections.emptyList();

    if (acceptHeaders.contains("text/html") || acceptHeaders.contains("application/html")) {
      return fillTemplate(representation, containerRequestContext.getUriInfo());
    }
    if (ResultType.GRAPH.equals(informationProduct.getResultType())) {
      return Response.ok(new GraphEntity((GraphQueryResult) result, representation)).build();
    }
    if (ResultType.TUPLE.equals(informationProduct.getResultType())) {
      return Response.ok(new TupleEntity((TupleQueryResult) result, representation)).build();
    }

    throw new ConfigurationException(
        String.format("Result type %s not supported for information product %s",
            informationProduct.getResultType(), informationProduct.getIdentifier()));
  }

  private Response fillTemplate(Representation representation, UriInfo requeestURI) {
    Template htmlTemplate = representation.getHtmlTemplate();

    if (htmlTemplate != null) {
      Map<String, Object> freeMarkerDataModel = new HashMap<>();
      freeMarkerDataModel.put("result", requeestURI.getAbsolutePath().toString());
      try {
        StringWriter stringWriter = new StringWriter();
        htmlTemplate.process(freeMarkerDataModel, stringWriter);
        StringBuffer buffer = stringWriter.getBuffer();
        String htmlString = buffer != null ? buffer.toString() : "UNKNOWN";
        return Response.ok(htmlString).build();
      } catch (IOException e) {
        System.out.print("faal");
      } catch (TemplateException e) {
        System.out.print("faal2");
      }
    }
    throw new ConfigurationException(String.format("No HTML template defined for %s", representation.getIdentifier()));
  }

}

package org.dotwebstack.framework.frontend.ld.handlers;

import com.github.jsonldjava.utils.Obj;
import freemarker.template.Template;
import freemarker.template.TemplateException;

//import java.io.*;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.ld.entity.GraphEntity;
import org.dotwebstack.framework.frontend.ld.entity.TupleEntity;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.dotwebstack.framework.frontend.ld.representation.RepresentationResourceProvider;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.impl.BackgroundGraphResult;
import org.eclipse.rdf4j.query.resultio.helpers.BackgroundTupleResult;
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

    endpointRequestParameterMapper.map(informationProduct, containerRequestContext).forEach(
        parameterValues::put);
    representation.getParameterMappers().forEach(
        parameterMapper -> parameterValues.putAll(parameterMapper.map(containerRequestContext)));

    Object result = informationProduct.getResult(parameterValues);

    if (ResultType.GRAPH.equals(informationProduct.getResultType())) {
      return Response.ok(new GraphEntity((GraphQueryResult) result, representation)).build();
    }
    if (ResultType.TUPLE.equals(informationProduct.getResultType())) {
      return Response.ok(new TupleEntity((TupleQueryResult) result, representation)).build();
    }
    if (ResultType.HTML.equals(informationProduct.getResultType())) {
      return fillTemplate(result, representation);
    }

    throw new ConfigurationException(
        String.format("Result type %s not supported for information product %s",
            informationProduct.getResultType(), informationProduct.getIdentifier()));
  }

  private Response fillTemplate(Object result, Representation representation) {
    Template htmlTemplate = representation.getHtmlTemplate();

    if (htmlTemplate != null) {
      Map<String, Object> freeMarkerDataModel = new HashMap<>();
      freeMarkerDataModel.put("result", representation.getIdentifier());
      try {
        // File file = new File("test.html");
        //        file.createNewFile();
        //        Writer out = new OutputStreamWriter(new FileOutputStream(file, false));
        StringWriter stringWriter = new StringWriter();
        htmlTemplate.process(freeMarkerDataModel, stringWriter);
        String testString = stringWriter.getBuffer().toString();
        System.out.print(testString);
        //        out.close();
        return Response.ok(testString).build();
      } catch (IOException e) {
        System.out.print("faal");
      } catch (TemplateException e) {
        System.out.print("faal2");
      }
      return Response.ok(htmlTemplate).build();
    }
    return Response.status(406).build();
  }

}

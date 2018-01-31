package org.dotwebstack.framework.frontend.openapi.handlers;

import com.atlassian.oai.validator.model.ApiOperation;
import com.google.common.collect.Maps;
import io.swagger.models.Swagger;
import io.swagger.models.properties.Property;
import java.util.Map;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.Entity;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.TupleEntity;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.param.Parameter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.glassfish.jersey.process.Inflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GetRequestHandler implements Inflector<ContainerRequestContext, Response> {

  private static final Logger LOG = LoggerFactory.getLogger(GetRequestHandler.class);

  private final ApiOperation apiOperation;

  private final InformationProduct informationProduct;

  private final Map<MediaType, Property> schemaMap;

  private final RequestParameterMapper requestParameterMapper;

  private final Swagger swagger;

  private final ApiRequestValidator apiRequestValidator;

  GetRequestHandler(@NonNull ApiOperation apiOperation,
      @NonNull InformationProduct informationProduct, @NonNull Map<MediaType, Property> schemaMap,
      @NonNull RequestParameterMapper requestParameterMapper,
      @NonNull ApiRequestValidator apiRequestValidator, @NonNull Swagger swagger) {
    this.apiRequestValidator = apiRequestValidator;
    this.apiOperation = apiOperation;
    this.informationProduct = informationProduct;
    this.schemaMap = schemaMap;
    this.requestParameterMapper = requestParameterMapper;
    this.swagger = swagger;
  }

  public InformationProduct getInformationProduct() {
    return informationProduct;
  }

  public Map<MediaType, Property> getSchemaMap() {
    return schemaMap;
  }

  @Override
  public Response apply(@NonNull ContainerRequestContext context) {
    String path = context.getUriInfo().getPath();

    LOG.debug("Handling {} request for path {}", context.getMethod(), path);

    RequestParameters requestParameters =
        apiRequestValidator.validate(apiOperation, swagger, context);

    Map<String, String> parameterValues = requestParameterMapper.map(apiOperation.getOperation(),
        informationProduct, requestParameters);

    // ############################################################################################

    Map<String, io.swagger.models.Response> responses = apiOperation.getOperation().getResponses();
    io.swagger.models.Response response = responses.get("200");
    Map<String, Property> headers = response.getHeaders();
    for (Property header : headers.values()) {
      Map<String, Object> vendorExtensions = header.getVendorExtensions();

      LOG.debug("Vendor extensions for header param '{}': {}", header.getName(), vendorExtensions);

      Object parameterIdString = vendorExtensions.get(OpenApiSpecificationExtensions.PARAMETER);

      if (parameterIdString == null) {
        continue;
      }

      IRI iri = SimpleValueFactory.getInstance().createIRI((String) parameterIdString);
      Parameter<?> parameter = null;

      for (Parameter<?> productParameter : informationProduct.getParameters()) {
        if (productParameter.getIdentifier().equals(iri)) {
          parameter = productParameter;
        }
      }

      if (parameter == null) {
        throw new ConfigurationException(String.format(
            "No parameter found for vendor extension value: '%s'", parameterIdString));
      }

      Map<String, String> responseParameters = Maps.newHashMap();
      responseParameters.put(parameter.getName(), null);
      System.out.println(responseParameters);
    }

    // ############################################################################################


    Response responseOk = null;
    if (ResultType.TUPLE.equals(informationProduct.getResultType())) {

      TupleQueryResult result = (TupleQueryResult) informationProduct.getResult(parameterValues);
      TupleEntity entity =
          TupleEntity.builder().withQueryResult(result).withSchemaMap(schemaMap).build();

      responseOk = responseOk(entity);
    }
    if (ResultType.GRAPH.equals(informationProduct.getResultType())) {
      org.eclipse.rdf4j.query.GraphQueryResult result =
          (org.eclipse.rdf4j.query.GraphQueryResult) informationProduct.getResult(parameterValues);
      GraphEntity entity =
          (GraphEntity) GraphEntity.builder().withSchemaMap(schemaMap).withQueryResult(
              result).withApiDefinitions(swagger).withLdPathNamespaces(
                  swagger).withRequestParameters(parameterValues).build();
      responseOk = responseOk(entity);
    }
    if (responseOk != null) {
      return responseOk;
    } else {
      LOG.error("Result type {} not supported for information product {}",
          informationProduct.getResultType(), informationProduct.getIdentifier());
    }
    return Response.serverError().build();
  }

  private Response responseOk(Entity entity) {
    if (entity != null) {
      return Response.ok(entity).build();
    }
    return null;
  }
}


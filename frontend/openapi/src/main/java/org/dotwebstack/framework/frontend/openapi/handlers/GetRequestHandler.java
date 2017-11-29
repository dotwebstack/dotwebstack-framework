package org.dotwebstack.framework.frontend.openapi.handlers;

import com.atlassian.oai.validator.model.ApiOperation;
import io.swagger.models.properties.Property;
import java.util.Map;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.frontend.openapi.entity.TupleEntity;
import org.dotwebstack.framework.informationproduct.InformationProduct;
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

  private final ApiRequestValidator apiRequestValidator;

  GetRequestHandler(@NonNull ApiOperation apiOperation,
      @NonNull InformationProduct informationProduct, @NonNull Map<MediaType, Property> schemaMap,
      @NonNull RequestParameterMapper requestParameterMapper,
      @NonNull ApiRequestValidator apiRequestValidator) {
    this.apiRequestValidator = apiRequestValidator;
    this.apiOperation = apiOperation;
    this.informationProduct = informationProduct;
    this.schemaMap = schemaMap;
    this.requestParameterMapper = requestParameterMapper;
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
    LOG.debug("Handling GET request for path {}", path);

    if (ResultType.TUPLE.equals(informationProduct.getResultType())) {

      RequestParameters requestParameters = apiRequestValidator.validate(apiOperation, context);

      Map<String, Object> parameterValues = requestParameterMapper.map(apiOperation.getOperation(),
          informationProduct, requestParameters);

      TupleQueryResult result = (TupleQueryResult) informationProduct.getResult(parameterValues);
      TupleEntity entity = new TupleEntity(schemaMap, result);

      return Response.ok(entity).build();
    } else {
      LOG.error("Result type {} not supported for information product {}",
          informationProduct.getResultType(), informationProduct.getIdentifier());
    }

    return Response.serverError().build();
  }

}


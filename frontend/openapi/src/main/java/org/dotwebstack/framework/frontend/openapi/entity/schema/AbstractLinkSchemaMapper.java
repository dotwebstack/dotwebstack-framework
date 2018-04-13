package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.atlassian.oai.validator.model.ApiOperation;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.ObjectProperty;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.ws.rs.core.UriBuilder;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.handlers.RequestContext;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.param.term.IntegerTermParameter;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;

abstract class AbstractLinkSchemaMapper implements SchemaMapper<ObjectProperty, Object> {

  AbstractLinkSchemaMapper() {}

  protected static URI buildUri(@NonNull RequestContext requestContext,
      Map<String, String> extraParams) {
    ApiOperation apiOperation = requestContext.getApiOperation();
    String path = apiOperation.getRequestPath().normalised();

    List<Parameter> operationParams = apiOperation.getOperation().getParameters();
    Map<String, String> requestParams = requestContext.getParameters();

    UriBuilder builder = UriBuilder.fromPath(requestContext.getBaseUri()).path(path);

    // @formatter:off
    operationParams.stream()
        .filter(isQueryParameter())
        .map(toQueryParameter())
        .filter(p -> requestParams.get(p.getName()) != null)
        .filter(p -> p.getDefault() == null
            || !requestParams.get(p.getName()).equals(p.getDefault().toString()))
        .forEach(p -> builder.queryParam(p.getName(), requestParams.get(p.getName())));
    // @formatter:on

    if (extraParams != null) {
      extraParams.forEach(builder::replaceQueryParam);
    }

    return builder.buildFromMap(requestContext.getParameters());
  }

  protected static IntegerTermParameter getPageTermParameter(
      @NonNull RequestContext requestContext) {
    return getTermParameter(requestContext.getInformationProduct(), ELMO.PAGE_PARAMETER);
  }

  protected static IntegerTermParameter getPageSizeTermParameter(
      @NonNull RequestContext requestContext) {
    return getTermParameter(requestContext.getInformationProduct(), ELMO.PAGE_SIZE_PARAMETER);
  }

  private static IntegerTermParameter getTermParameter(InformationProduct informationProduct,
      IRI termParameter) {
    // @formatter:off
    return informationProduct.getParameters().stream()
        .filter(p -> p.getIdentifier().equals(termParameter))
        .map(p -> (IntegerTermParameter) p)
        .findFirst()
        .orElseThrow(() -> new SchemaMapperRuntimeException(String.format(
            "Information product requires a <%s> parameter.", termParameter)));
    // @formatter:on
  }

  protected static QueryParameter getPageQueryParameter(@NonNull RequestContext requestContext) {
    return getQueryParameter(requestContext, ELMO.PAGE_PARAMETER);
  }

  private static QueryParameter getQueryParameter(RequestContext requestContext,
      IRI termParameter) {
    List<Parameter> operationParams =
        requestContext.getApiOperation().getOperation().getParameters();

    // @formatter:off
    return operationParams.stream()
        .filter(isQueryParameter())
        .map(toQueryParameter())
        .filter(p -> mapsToTermParameter(p, termParameter))
        .findFirst()
        .orElseThrow(() -> new SchemaMapperRuntimeException(String.format(
            "API operation requires a <%s> query parameter.", termParameter)));
    // @formatter:on
  }

  private static Function<Parameter, QueryParameter> toQueryParameter() {
    return parameter -> (QueryParameter) parameter;
  }

  private static Predicate<Parameter> isQueryParameter() {
    return parameter -> parameter.getIn().equalsIgnoreCase("query");
  }

  private static boolean mapsToTermParameter(QueryParameter parameter, IRI termParameter) {
    if (parameter.getVendorExtensions() == null) {
      return false;
    }

    return termParameter.stringValue().equals(
        parameter.getVendorExtensions().get(OpenApiSpecificationExtensions.PARAMETER));
  }

}

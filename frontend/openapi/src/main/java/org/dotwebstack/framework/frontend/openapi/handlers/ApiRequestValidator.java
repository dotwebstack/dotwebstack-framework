package org.dotwebstack.framework.frontend.openapi.handlers;

import com.atlassian.oai.validator.interaction.RequestValidator;
import com.atlassian.oai.validator.model.ApiOperation;
import com.atlassian.oai.validator.model.Request.Method;
import com.atlassian.oai.validator.model.SimpleRequest;
import com.atlassian.oai.validator.model.SimpleRequest.Builder;
import com.atlassian.oai.validator.report.ValidationReport;
import com.google.common.collect.ImmutableList;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.UriInfo;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.http.error.InvalidParamsBadRequestException.InvalidParameter;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;

class ApiRequestValidator {
  private static final List<String> FILTERED_HEADERS = ImmutableList.of("accept", "content-type");

  private static final List<String> FORBIDDEN_CHARS = ImmutableList.of("?", "$");

  private final RequestValidator requestValidator;

  private final RequestParameterExtractor requestParameterExtractor;

  ApiRequestValidator(@NonNull RequestValidator requestValidator,
      @NonNull RequestParameterExtractor requestParameterExtractor) {
    this.requestValidator = requestValidator;
    this.requestParameterExtractor = requestParameterExtractor;
  }

  /**
   * Validates a request by building {@link SimpleRequest}. Result contains validated request
   * parameters. It would also set default values for missing parameters
   *
   * @param apiOperation api operation model to be validated
   * @return a map of (validated) parameters
   * @throws WebApplicationException in case validation fails
   */
  RequestParameters validate(@NonNull ApiOperation apiOperation, @NonNull Swagger swagger,
      @NonNull ContainerRequestContext requestContext) {
    UriInfo uriInfo = requestContext.getUriInfo();

    checkForForbiddenChars(uriInfo);

    String strMethod = requestContext.getMethod();
    Method method = Method.valueOf(strMethod.toUpperCase());
    Builder builder = new SimpleRequest.Builder(method, uriInfo.getPath());

    requestContext.getHeaders().entrySet().stream().filter(
        entry -> !FILTERED_HEADERS.contains(entry.getKey().toLowerCase())).forEach(
            entry -> builder.withHeader(entry.getKey(), entry.getValue()));
    uriInfo.getPathParameters().forEach(builder::withQueryParam);
    uriInfo.getQueryParameters().forEach(builder::withQueryParam);

    RequestParameters requestParameters =
        requestParameterExtractor.extract(apiOperation, swagger, requestContext);

    String body = requestParameters.getRawBody();
    builder.withBody(body);

    /*
     * The OAS can contain parameters that are there just for documentation purposes. Therefore
     * parameters that are not used in DWS (don't have the x-dotwebstack-parameter extension) but
     * are in the OAS, do not need to be validated.
     */
    ApiOperation apiOperationCopy = new ApiOperation(apiOperation.getApiPath(),
        apiOperation.getRequestPath(), apiOperation.getMethod(), apiOperation.getOperation());

    List<Parameter> params = apiOperationCopy.getOperation().getParameters().stream().filter(
        param -> param.getVendorExtensions().containsKey(
            OpenApiSpecificationExtensions.PARAMETER)).collect(Collectors.toList());
    apiOperationCopy.getOperation().setParameters(params);

    /*
     * NB: Swagger API 2 does not have an option for case insensitivity for enums; for example, you
     * may expect validation to fail for 'POint' (if the enum is defined as 'Point')
     */
    ValidationReport report = requestValidator.validateRequest(builder.build(), apiOperationCopy);

    if (report.hasErrors()) {
      throw createException(report);
    }

    return requestParameters;
  }

  private static void checkForForbiddenChars(UriInfo uriInfo) {
    List<InvalidParameter> invalidParams =
        Stream.of(uriInfo.getPathParameters(), uriInfo.getQueryParameters()) //
            .map(Map::entrySet) //
            .flatMap(Collection::stream) //
            .filter(containsForbiddenChars()) //
            .map(createInvalidParameter()) //
            .collect(Collectors.toList());

    if (!invalidParams.isEmpty()) {
      throw new RequestValidationException("Request parameters didn't validate.", invalidParams);
    }
  }

  private static Predicate<? super Entry<String, List<String>>> containsForbiddenChars() {
    return entry -> entry.getValue().stream().anyMatch(
        param -> FORBIDDEN_CHARS.stream().anyMatch(param::contains));
  }

  private static Function<? super Entry<String, List<String>>,
      ? extends InvalidParameter> createInvalidParameter() {
    return badEntry -> new InvalidParameter("validation.request.parameter.contains.forbidden.char",
        String.format(
            "Value '%s' for parameter '%s' is not allowed. "
                + "Parameters cannot contain any of: %s.",
            badEntry.getValue().get(0), badEntry.getKey(), FORBIDDEN_CHARS));
  }

  private RequestValidationException createException(ValidationReport report) {


    List<InvalidParameter> invalidParams = report.getMessages().stream() //
        .map(message -> new InvalidParameter(message.getKey(), message.getMessage())) //
        .collect(Collectors.toList());

    return new RequestValidationException("Request parameters didn't validate.", invalidParams);
  }

}

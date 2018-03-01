package org.dotwebstack.framework.frontend.openapi.handlers;

import com.atlassian.oai.validator.interaction.RequestValidator;
import com.atlassian.oai.validator.model.ApiOperation;
import com.atlassian.oai.validator.model.Request.Method;
import com.atlassian.oai.validator.model.SimpleRequest;
import com.atlassian.oai.validator.model.SimpleRequest.Builder;
import com.atlassian.oai.validator.report.ValidationReport;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.swagger.models.Swagger;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response.Status;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ApiRequestValidator {
  private static final Logger LOG = LoggerFactory.getLogger(ApiRequestValidator.class);

  private static final List<String> FILTERED_HEADERS = ImmutableList.of("accept", "content-type");

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
    String strMethod = requestContext.getMethod();
    Method method = Method.valueOf(strMethod.toUpperCase());
    Builder builder = new SimpleRequest.Builder(method, requestContext.getUriInfo().getPath());

    requestContext.getHeaders().entrySet().stream().filter(
        entry -> !FILTERED_HEADERS.contains(entry.getKey().toLowerCase())).forEach(
            entry -> builder.withHeader(entry.getKey(), entry.getValue()));
    requestContext.getUriInfo().getPathParameters().forEach(builder::withQueryParam);
    requestContext.getUriInfo().getQueryParameters().forEach(builder::withQueryParam);

    RequestParameters requestParameters =
        requestParameterExtractor.extract(apiOperation, swagger, requestContext);

    String body = requestParameters.getRawBody();
    builder.withBody(body);

    /*
     * NB: Swagger API 2 does not have an option for case insensitivity for enums; for example, you
     * may expect validation to fail for 'POint' (if the enum is defined as 'Point')
     */
    ValidationReport report = requestValidator.validateRequest(builder.build(), apiOperation);

    if (report.hasErrors()) {
      LOG.error("Request parameter validation failed, with following errors: {}",
          report.getMessages());
      throw createException(report);
    }

    return requestParameters;
  }

  private RequestValidationException createException(ValidationReport report) {

    ImmutableList.Builder<Object> invalidParamsBuilder = new ImmutableList.Builder<>();

    report.getMessages().forEach(message -> {
      ImmutableMap.Builder<String, String> paramBuilder = new ImmutableMap.Builder<>();
      paramBuilder.put("name", message.getKey());
      paramBuilder.put("reason", message.getMessage());
      invalidParamsBuilder.add(paramBuilder.build());
    });

    ImmutableMap<String, Object> details =
        ImmutableMap.of("invalid-params", invalidParamsBuilder.build());

    return new RequestValidationException("Request parameters didn't validate.", Status.BAD_REQUEST,
        details);
  }

}

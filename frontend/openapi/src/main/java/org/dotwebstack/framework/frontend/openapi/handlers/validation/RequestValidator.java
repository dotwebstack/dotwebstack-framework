package org.dotwebstack.framework.frontend.openapi.handlers.validation;

import static com.atlassian.oai.validator.report.ValidationReport.MessageContext.Location.REQUEST;
import static java.lang.Boolean.TRUE;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import com.atlassian.oai.validator.model.ApiOperation;
import com.atlassian.oai.validator.model.NormalisedPath;
import com.atlassian.oai.validator.model.Request;
import com.atlassian.oai.validator.report.MessageResolver;
import com.atlassian.oai.validator.report.ValidationReport;
import com.atlassian.oai.validator.report.ValidationReport.MessageContext;
import com.atlassian.oai.validator.schema.SchemaValidator;
import com.google.common.collect.ImmutableList;
import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;

/**
 * Validate a request against a given API operation.
 */
// All classes in this package are copied from
// atlassian's swagger-request-validator
public class RequestValidator {

  private final MessageResolver messages;
  private final ParameterValidator parameterValidator;
  private final RequestBodyValidator requestBodyValidator;

  /**
   * Construct a new request validator with the given schema validator.
   *
   * @param schemaValidator The schema validator to use when validating request bodies
   * @param messages The message resolver to use
   */
  public RequestValidator(@NonNull SchemaValidator schemaValidator,
      @NonNull MessageResolver messages) {
    this.messages = messages;
    this.parameterValidator = new ParameterValidator(schemaValidator, messages);
    this.requestBodyValidator = new RequestBodyValidator(messages, schemaValidator);
  }

  /**
   * Validate the request against the given API operation
   *
   * @param request The request to validate
   * @param apiOperation The operation to validate the request against
   *
   * @return A validation report containing validation errors
   */
  public ValidationReport validateRequest(@NonNull Request request,
      @NonNull ApiOperation apiOperation) {

    MessageContext context =
        MessageContext.create().in(REQUEST).withApiOperation(apiOperation).withRequestPath(
            apiOperation.getRequestPath().original()).withRequestMethod(
                request.getMethod()).build();

    return validateHeaders(request, apiOperation).merge(validatePathParameters(apiOperation)).merge(
        validateBodyParameters(request, apiOperation)).merge(
            validateQueryParameters(request, apiOperation)).withAdditionalContext(context);
  }

  private ValidationReport validateBodyParameters(Request request, ApiOperation apiOperation) {
    return requestBodyValidator.validateRequestBody(request,
        apiOperation.getOperation().getRequestBody());
  }

  private ValidationReport validatePathParameters(ApiOperation apiOperation) {
    ValidationReport validationReport = ValidationReport.empty();
    final NormalisedPath requestPath = apiOperation.getRequestPath();
    for (int i = 0; i < apiOperation.getApiPath().numberOfParts(); i++) {
      if (!apiOperation.getApiPath().hasParams(i)) {
        continue;
      }

      final ValidationReport pathPartValidation =
          apiOperation.getApiPath().paramValues(i, requestPath.part(i)).entrySet().stream().map(
              param -> validatePathParameter(apiOperation, param)).reduce(ValidationReport.empty(),
                  ValidationReport::merge);

      validationReport = validationReport.merge(pathPartValidation);
    }
    return validationReport;
  }

  private ValidationReport validatePathParameter(ApiOperation apiOperation,
      Map.Entry<String, Optional<String>> param) {
    return defaultIfNull(apiOperation.getOperation().getParameters(),
        ImmutableList.<Parameter>of()).stream().filter(RequestValidator::isPathParam).filter(
            p -> p.getName().equalsIgnoreCase(param.getKey())).findFirst().map(
                p -> parameterValidator.validate(param.getValue().orElse(null), p)).orElse(
                    ValidationReport.empty());
  }

  private ValidationReport validateQueryParameters(Request request, ApiOperation apiOperation) {
    return defaultIfNull(apiOperation.getOperation().getParameters(),
        ImmutableList.<Parameter>of()).stream().filter(RequestValidator::isQueryParam).map(
            p -> validateParameter(
                apiOperation, p,
                request.getQueryParameterValues(p.getName()),
                "validation.request.parameter.query.missing")).reduce(ValidationReport.empty(),
                    ValidationReport::merge);
  }

  @Nonnull
  private ValidationReport validateHeaders(Request request, ApiOperation apiOperation) {
    return defaultIfNull(apiOperation.getOperation().getParameters(),
        ImmutableList.<Parameter>of()).stream().filter(RequestValidator::isHeaderParam).filter(
            param -> param.getExtensions() != null && param.getExtensions().containsKey(
                OpenApiSpecificationExtensions.PARAMETER)).map(p -> validateParameter(
                    apiOperation, p,
                    request.getHeaderValues(p.getName()),
                    "validation.request.parameter.header.missing")).reduce(ValidationReport.empty(),
                        ValidationReport::merge);
  }

  @Nonnull
  private ValidationReport validateParameter(ApiOperation apiOperation,
      Parameter parameter,
      Collection<String> parameterValues,
      String missingKey) {

    final ValidationReport.MessageContext context =
        ValidationReport.MessageContext.create().withParameter(parameter).build();

    if (parameterValues.isEmpty() && TRUE.equals(parameter.getRequired())) {
      return ValidationReport.singleton(
          messages.get(missingKey, parameter.getName(),
              apiOperation.getApiPath().original())).withAdditionalContext(context);
    }

    return parameterValues.stream().map(v -> parameterValidator.validate(v, parameter)).reduce(
        ValidationReport.empty(), ValidationReport::merge);
  }

  private static boolean isPathParam(final Parameter p) {
    return isParam(p, "path");
  }

  private static boolean isQueryParam(final Parameter p) {
    return isParam(p, "query");
  }

  private static boolean isHeaderParam(final Parameter p) {
    return isParam(p, "header");
  }

  private static boolean isParam(final Parameter p, final String type) {
    return p != null && p.getIn() != null && p.getIn().equalsIgnoreCase(type);
  }
}

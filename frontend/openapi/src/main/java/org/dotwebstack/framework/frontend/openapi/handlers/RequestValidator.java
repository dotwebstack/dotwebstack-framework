package org.dotwebstack.framework.frontend.openapi.handlers;

import static com.atlassian.oai.validator.report.ValidationReport.empty;
import static com.atlassian.oai.validator.util.ContentTypeUtils.isJsonContentType;
import static java.util.Objects.requireNonNull;

import com.atlassian.oai.validator.model.ApiOperation;
import com.atlassian.oai.validator.model.NormalisedPath;
import com.atlassian.oai.validator.model.Request;
import com.atlassian.oai.validator.parameter.ParameterValidators;
import com.atlassian.oai.validator.report.MessageResolver;
import com.atlassian.oai.validator.report.ValidationReport;
import com.atlassian.oai.validator.report.ValidationReport.MessageContext;
import com.atlassian.oai.validator.schema.SchemaValidator;
import com.atlassian.oai.validator.util.ContentTypeUtils;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import java.util.Collection;
import java.util.Optional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;

/**
 * Validate a request against a given API operation.
 */
@Slf4j
public class RequestValidator {

  private final SchemaValidator schemaValidator;
  private final ParameterValidators parameterValidators;
  private final MessageResolver messages;

  /**
   * Construct a new request validator with the given schema validator.
   *
   * @param schemaValidator The schema validator to use when validating request bodies
   * @param messages The message resolver to use
   */
  public RequestValidator(@NonNull final SchemaValidator schemaValidator,
      @NonNull final MessageResolver messages) {
    this.schemaValidator = requireNonNull(schemaValidator, "A schema validator is required");
    parameterValidators = new ParameterValidators(schemaValidator, messages);
    this.messages = requireNonNull(messages, "A message resolver is required");
  }

  /**
   * Validate the request against the given API operation
   *
   * @param request The request to validate
   * @param apiOperation The operation to validate the request against
   *
   * @return A validation report containing validation errors
   */
  public ValidationReport validateRequest(@NonNull final Request request,
      @NonNull final ApiOperation apiOperation) {

    final MessageContext context = MessageContext.create().in(
        ValidationReport.MessageContext.Location.REQUEST).withApiOperation(apiOperation).build();

    return validateHeaders(request, apiOperation) //
        .merge(validatePathParameters(apiOperation)) //
        .merge(validateRequestBody(request, apiOperation)) //
        .merge(validateQueryParameters(request, apiOperation)) //
        .withAdditionalContext(context);
  }

  private ValidationReport validateRequestBody(@NonNull final Request request,
      @NonNull final ApiOperation apiOperation) {
    final Optional<Parameter> bodyParameter =
        apiOperation.getOperation().getParameters().stream().filter(
            RequestValidator::isBodyParam).findFirst();

    final MessageContext context =
        MessageContext.create().withParameter(bodyParameter.orElse(null)).build();

    final Optional<String> requestBody = request.getBody();
    if (requestBody.isPresent() && !requestBody.get().isEmpty() && !bodyParameter.isPresent()) {
      return ValidationReport.singleton(messages.get("validation.request.body.unexpected",
          apiOperation.getMethod(), apiOperation.getApiPath().original())).withAdditionalContext(
              context);
    }

    if (!bodyParameter.isPresent()) {
      return empty();
    }

    if (!requestBody.isPresent() || requestBody.get().isEmpty()) {
      if (bodyParameter.get().getRequired()) {
        return ValidationReport.singleton(messages.get("validation.request.body.missing",
            apiOperation.getMethod(), apiOperation.getApiPath().original())).withAdditionalContext(
                context);
      }
      return empty();
    }

    if (ContentTypeUtils.hasContentType(request) && !isJsonContentType(request)) {
      log.debug("Non-JSON request body found. No validation will be applied.");
      return empty();
    }

    return schemaValidator.validate(requestBody.get(),
        ((BodyParameter) bodyParameter.get()).getSchema()).withAdditionalContext(context);
  }

  private ValidationReport validatePathParameters(@NonNull final ApiOperation apiOperation) {

    ValidationReport validationReport = empty();
    final NormalisedPath requestPath = apiOperation.getRequestPath();
    for (int i = 0; i < apiOperation.getApiPath().numberOfParts(); i++) {
      if (!apiOperation.getApiPath().hasParams(i)) {
        continue;
      }

      final ValidationReport pathPartValidation =
          apiOperation.getApiPath().paramValues(i, requestPath.part(i)).entrySet().stream().map(
              param -> validatePathParameter(apiOperation, param.getKey(),
                  param.getValue())).reduce(empty(), ValidationReport::merge);

      validationReport = validationReport.merge(pathPartValidation);
    }
    return validationReport;
  }

  private ValidationReport validatePathParameter(@NonNull final ApiOperation apiOperation,
      @NonNull final String paramName, @NonNull final Optional<String> paramValue) {
    return apiOperation.getOperation().getParameters().stream().filter(
        RequestValidator::isPathParam).filter(
            p -> p.getName().equalsIgnoreCase(paramName)).findFirst().map(
                p -> parameterValidators.validate(paramValue.orElse(null), p)).orElse(empty());
  }

  private ValidationReport validateQueryParameters(@NonNull final Request request,
      @NonNull final ApiOperation apiOperation) {
    return apiOperation.getOperation().getParameters().stream().filter(
        RequestValidator::isQueryParam).map(
            p -> validateParameter(apiOperation, p, request.getQueryParameterValues(p.getName()),
                "validation.request.parameter.query.missing")).reduce(empty(),
                    ValidationReport::merge);
  }

  private ValidationReport validateHeaders(@NonNull final Request request,
      @NonNull final ApiOperation apiOperation) {
    return apiOperation.getOperation().getParameters().stream().filter(
        RequestValidator::isHeaderParam).filter(
            param -> param.getVendorExtensions().containsKey(
                OpenApiSpecificationExtensions.PARAMETER)).map(
                    p -> validateParameter(apiOperation, p, request.getHeaderValues(p.getName()),
                        "validation.request.parameter.header.missing")).reduce(empty(),
                            ValidationReport::merge);
  }

  private ValidationReport validateParameter(@NonNull final ApiOperation apiOperation,
      @NonNull final Parameter parameter, @NonNull final Collection<String> parameterValues,
      @NonNull final String missingKey) {

    if (parameterValues.isEmpty() && parameter.getRequired()) {
      return ValidationReport.singleton(
          messages.get(missingKey, parameter.getName(), apiOperation.getApiPath().original()));
    }

    return parameterValues.stream().map(v -> parameterValidators.validate(v, parameter)).reduce(
        empty(), ValidationReport::merge);
  }

  private static boolean isBodyParam(final Parameter p) {
    return isParam(p, "body");
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

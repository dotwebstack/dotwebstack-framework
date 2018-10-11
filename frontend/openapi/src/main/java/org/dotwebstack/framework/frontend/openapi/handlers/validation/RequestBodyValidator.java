package org.dotwebstack.framework.frontend.openapi.handlers.validation;
// All classes in this package are copied from
// atlassian's swagger-request-validator

import static com.atlassian.oai.validator.report.ValidationReport.empty;
import static com.atlassian.oai.validator.util.ContentTypeUtils.findMostSpecificMatch;
import static com.atlassian.oai.validator.util.ContentTypeUtils.isFormDataContentType;
import static com.atlassian.oai.validator.util.ContentTypeUtils.isJsonContentType;
import static com.atlassian.oai.validator.util.HttpParsingUtils.parseUrlEncodedFormDataBodyAsJson;
import static java.lang.Boolean.TRUE;
import static java.util.Objects.requireNonNull;

import com.atlassian.oai.validator.model.Request;
import com.atlassian.oai.validator.report.MessageResolver;
import com.atlassian.oai.validator.report.ValidationReport;
import com.atlassian.oai.validator.schema.SchemaValidator;
import com.google.common.annotations.VisibleForTesting;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.RequestBody;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Validation for a request body.
 * <p>
 * The schema to validate is selected based on the content-type header of the incoming request.
 */
@Slf4j
class RequestBodyValidator {

  private final MessageResolver messages;

  private final SchemaValidator schemaValidator;

  @VisibleForTesting
  RequestBodyValidator(final SchemaValidator schemaValidator) {
    this(new MessageResolver(), schemaValidator);
  }

  RequestBodyValidator(@Nullable final MessageResolver messages, final SchemaValidator schemaValidator) {
    this.schemaValidator = requireNonNull(schemaValidator, "A schema validator is required");
    this.messages = messages == null ? new MessageResolver() : messages;
  }

  @Nonnull
  ValidationReport validateRequestBody(final Request request,
                                       @Nullable final RequestBody apiRequestBodyDefinition) {

    final Optional<String> requestBody = request.getBody();

    if (apiRequestBodyDefinition == null) {
      // A request body exists, but no request body is defined in the spec
      if (requestBody.isPresent() && !requestBody.get().isEmpty()) {
        return ValidationReport.singleton(
            messages.get("validation.request.body.unexpected")
        );
      }

      // No request body exists, and no request body is defined in the spec. Nothing to do.
      return empty();
    }

    ValidationReport.MessageContext context = ValidationReport.MessageContext.create()
        .withApiRequestBodyDefinition(apiRequestBodyDefinition)
        .build();

    if (!requestBody.isPresent() || requestBody.get().isEmpty()) {
      // No request body, but is required in the spec
      if (TRUE.equals(apiRequestBodyDefinition.getRequired())) {
        return ValidationReport.singleton(
            messages.get("validation.request.body.missing")
        ).withAdditionalContext(context);
      }

      // No request body, and isn't required. Nothing to do.
      return empty();
    }

    final Optional<Pair<String, MediaType>> maybeApiMediaTypeForRequest =
        findApiMediaTypeForRequest(request, apiRequestBodyDefinition);

    // No matching media type found. Validation of mismatched content-type is handled elsewhere. Nothing to do.
    if (!maybeApiMediaTypeForRequest.isPresent()) {
      return empty();
    }

    context = ValidationReport.MessageContext.from(context)
        .withMatchedApiContentType(maybeApiMediaTypeForRequest.get().getLeft())
        .build();

    if (isJsonContentType(request)) {
      return schemaValidator
          .validate(
              requestBody.get(),
              maybeApiMediaTypeForRequest.get().getRight().getSchema(),
              "request.body")
          .withAdditionalContext(context);
    }

    if (isFormDataContentType(request)) {
      final String bodyAsJson = parseUrlEncodedFormDataBodyAsJson(requestBody.get());
      return schemaValidator
          .validate(
              bodyAsJson,
              maybeApiMediaTypeForRequest.get().getRight().getSchema(),
              "request.body")
          .withAdditionalContext(context);
    }

    // TODO: Validate multi-part form data

    LOG.info("Validation of '{}' not supported. Request body not validated.",
        maybeApiMediaTypeForRequest.get().getLeft());
    return empty();
  }

  private Optional<Pair<String, MediaType>> findApiMediaTypeForRequest(final Request request,
                                                                       @Nullable final RequestBody apiRequestBodyDefinition) {
    if (apiRequestBodyDefinition == null || apiRequestBodyDefinition.getContent() == null) {
      return Optional.empty();
    }

    final Optional<String> mostSpecificMatch = findMostSpecificMatch(request, apiRequestBodyDefinition.getContent().keySet());
    if (!mostSpecificMatch.isPresent()) {
      return Optional.empty();
    }

    final MediaType mediaType = apiRequestBodyDefinition.getContent().get(mostSpecificMatch.get());
    return Optional.of(Pair.of(mostSpecificMatch.get(), mediaType));
  }

}
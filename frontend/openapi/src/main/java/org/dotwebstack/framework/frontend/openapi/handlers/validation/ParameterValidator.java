package org.dotwebstack.framework.frontend.openapi.handlers.validation;

import static java.lang.Boolean.TRUE;
import static java.util.Objects.requireNonNull;

import com.atlassian.oai.validator.report.MessageResolver;
import com.atlassian.oai.validator.report.ValidationReport;
import com.atlassian.oai.validator.schema.SchemaValidator;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import lombok.NonNull;

// All classes in this package are copied from
// atlassian's swagger-request-validator
class ParameterValidator {

  private final SchemaValidator schemaValidator;
  private final MessageResolver messages;

  /**
   * Create a new validators object with the given schema validator. If none is provided a default
   * (empty) schema validator will be used and no <code>ref</code> validation will be performed.
   * @param schemaValidator The schema validator to use. If not provided a default (empty) validator
   *        will be used.
   * @param messages The message resolver to use.
   */
  ParameterValidator(@Nullable final SchemaValidator schemaValidator,
                     final MessageResolver messages) {
    this.schemaValidator =
        schemaValidator == null ? new SchemaValidator(messages) : schemaValidator;
    this.messages = requireNonNull(messages);
  }

  /**
   * Validate the given value against the given parameter.
   * If the parameter is an array type, the given value will be split according to the parameter
   * style and each sub-value validated independently.
   * @param value The value to validate
   * @param parameter The parameter to validate against
   * @return A report with any validation errors
   */
  ValidationReport validate(@Nullable final String value,
                            @NonNull Parameter parameter) {

    final ValidationReport.MessageContext context =
        ValidationReport.MessageContext.create().withParameter(parameter).build();

    if (TRUE.equals(parameter.getRequired()) && (value == null || value.trim().isEmpty())) {
      return ValidationReport.singleton(
          messages.get("validation.request.parameter.missing", parameter.getName()))
          .withAdditionalContext(context);
    }

    if (value == null || value.trim().isEmpty()) {
      return ValidationReport.empty();
    }

    if (parameter.getSchema() instanceof ArraySchema) {
      return validateArrayParam(value, parameter).withAdditionalContext(context);
    }

    return schemaValidator.validate(value, parameter.getSchema(), "request.parameter")
        .withAdditionalContext(context);
  }

  private ValidationReport validateArrayParam(final String value,
                                              final Parameter parameter) {
    return validateArrayParam(ArraySeparator.from(parameter).split(value), parameter);
  }

  private ValidationReport validateArrayParam(final Collection<String> values,
                                              final Parameter parameter) {
    final ValidationReport report = Stream.of(
        validateMaxItems(values, parameter),
        validateMinItems(values, parameter),
        validateUniqueItems(values, parameter))
        .reduce(ValidationReport.empty(), ValidationReport::merge);

    if (parameter.getSchema().getEnum() != null && !parameter.getSchema().getEnum().isEmpty()) {
      final Set<String> enumValues = new HashSet<>(parameter.getSchema().getEnum());
      return values.stream()
          .filter(v -> !enumValues.contains(v))
          .map(v -> ValidationReport.singleton(
              messages.get("validation.request.parameter.enum.invalid", v,
                  parameter.getName(), parameter.getSchema().getEnum())))
          .reduce(report, ValidationReport::merge);
    }

    return values.stream().map(v -> schemaValidator.validate(
        v, ((ArraySchema) parameter.getSchema()).getItems(), "request.parameter"))
        .reduce(report, ValidationReport::merge);
  }

  private ValidationReport validateUniqueItems(final Collection<String> values,
                                               final Parameter parameter) {
    if (TRUE.equals(parameter.getSchema().getUniqueItems())
        && values.stream().distinct().count() != values.size()) {
      return ValidationReport.singleton(
          messages.get("validation.request.parameter.collection.duplicateItems",
              parameter.getName()));
    }
    return ValidationReport.empty();
  }

  private ValidationReport validateMinItems(final Collection<String> values,
                                            final Parameter parameter) {
    if (parameter.getSchema().getMinItems() != null
        && values.size() < parameter.getSchema().getMinItems()) {
      return ValidationReport.singleton(
          messages.get("validation.request.parameter.collection.tooFewItems",
              parameter.getName(), parameter.getSchema().getMinItems(), values.size()));
    }
    return ValidationReport.empty();
  }

  private ValidationReport validateMaxItems(final Collection<String> values,
                                            final Parameter parameter) {
    if (parameter.getSchema().getMaxItems() != null
        && values.size() > parameter.getSchema().getMaxItems()) {
      return ValidationReport.singleton(
          messages.get("validation.request.parameter.collection.tooManyItems",
              parameter.getName(), parameter.getSchema().getMaxItems(), values.size()));
    }
    return ValidationReport.empty();
  }

}


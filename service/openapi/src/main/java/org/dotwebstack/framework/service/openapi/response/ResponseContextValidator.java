package org.dotwebstack.framework.service.openapi.response;

import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.invalidOpenApiConfigurationException;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.ARRAY_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.OBJECT_TYPE;

import io.swagger.v3.oas.models.media.Schema;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.NonNull;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.service.openapi.mapping.TypeValidator;
import org.springframework.stereotype.Component;

@Component
public class ResponseContextValidator {
  private final TypeValidator typeValidator = new TypeValidator();

  public void validate(@NonNull ResponseObject responseObject, @NonNull GraphQlField field,
      Set<Schema<?>> validatedSchemas) {
    String graphQlType = field.getType();
    ResponseSchema template = responseObject.getSchema();
    String oasType = template.getType();
    switch (oasType) {
      case ARRAY_TYPE:
        ResponseObject fieldTemplate = template.getItems()
            .get(0);
        if (!validatedSchemas.contains(fieldTemplate.getSchema()
            .getSchema())) {
          validate(fieldTemplate, field, validatedSchemas);
        }
        break;
      case OBJECT_TYPE:
        List<ResponseObject> children = template.getChildren();
        if (Objects.nonNull(template.getSchema())) {
          validatedSchemas.add(template.getSchema());
        }
        children.stream()
            .filter(child -> Objects.isNull(child.getSchema()
                .getDwsExpr())
                && !validatedSchemas.contains(child.getSchema()
                    .getSchema()))
            .forEach(child -> {
              if (child.getSchema()
                  .isEnvelope()) {
                ResponseObject embedded = child.getSchema()
                    .getChildren()
                    .get(0);
                validate(embedded, field, validatedSchemas);
              } else {
                GraphQlField graphQlChildField = field.getFields()
                    .stream()
                    .filter(childField -> childField.getName()
                        .equals(child.getIdentifier()))
                    .findFirst()
                    .orElseThrow(() -> invalidOpenApiConfigurationException(
                        "OAS field '{}' not found in matching GraphQl object '{}' for schema type '{}'",
                        child.getIdentifier(), field.getName(), field.getType()));

                validate(child, graphQlChildField, validatedSchemas);
              }
            });
        break;
      default:
        this.typeValidator.validateTypesGraphQlToOpenApi(oasType, graphQlType, responseObject.getIdentifier());
    }
  }
}

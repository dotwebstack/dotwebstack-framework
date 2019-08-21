package org.dotwebstack.framework.service.openapi.response;

import static org.dotwebstack.framework.service.openapi.helper.OasConstants.ARRAY_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.OBJECT_TYPE;

import java.util.List;
import java.util.Objects;
import lombok.NonNull;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.service.openapi.mapping.TypeValidator;
import org.springframework.stereotype.Component;

@Component
public class ResponseContextValidator {
  private final TypeValidator typeValidator = new TypeValidator();

  public void validate(@NonNull ResponseObject template, @NonNull GraphQlField field) {
    String graphQlType = field.getType();
    String oasType = template.getType();
    switch (oasType) {
      case ARRAY_TYPE:
        ResponseObject fieldTemplate = template.getItems()
            .get(0);
        validate(fieldTemplate, field);
        break;
      case OBJECT_TYPE:
        List<ResponseObject> children = template.getChildren();
        children.stream()
            .filter(child -> Objects.isNull(child.getDwsTemplate()))
            .forEach(child -> {
              if (child.isEnvelope()) {
                ResponseObject embedded = child.getChildren()
                    .get(0);
                validate(embedded, field);
              } else {
                GraphQlField graphQlChildField = field.getFields()
                    .stream()
                    .filter(childField -> childField.getName()
                        .equals(child.getIdentifier()))
                    .findFirst()
                    .orElseThrow(() -> ExceptionHelper.invalidConfigurationException(
                        "OAS field '{}' not found in matching GraphQl object '{}'.", child.getIdentifier(),
                        field.getName()));
                validate(child, graphQlChildField);
              }
            });
        break;
      default:
        this.typeValidator.validateOpenApiToGraphQlTypes(oasType, graphQlType, template.getIdentifier());
    }
  }
}

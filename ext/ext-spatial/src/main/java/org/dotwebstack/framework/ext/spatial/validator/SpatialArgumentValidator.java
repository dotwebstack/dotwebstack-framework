package org.dotwebstack.framework.ext.spatial.validator;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.TypeHelper.getTypeName;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.ARGUMENT_BBOX;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.ARGUMENT_TYPE;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.SelectedField;
import java.util.function.Predicate;
import org.dotwebstack.framework.core.backend.validator.GraphQlValidator;
import org.dotwebstack.framework.ext.spatial.SpatialConstants;
import org.springframework.stereotype.Component;

@Component
public class SpatialArgumentValidator implements GraphQlValidator {

  @Override
  public void validate(DataFetchingEnvironment environment) {
    environment.getSelectionSet()
        .getFields("**")
        .stream()
        .filter(isGeometryType())
        .forEach(this::validate);
  }

  private void validate(SelectedField selectedField) {
    String type = (String) selectedField.getArguments()
        .get(ARGUMENT_TYPE);

    Boolean bbox = (Boolean) selectedField.getArguments()
        .get(ARGUMENT_BBOX);

    if (type != null && Boolean.TRUE.equals(bbox)) {
      throw illegalArgumentException(String.format("Type argument is not allowed when argument bbox is true (%s).",
          selectedField.getQualifiedName()));
    }
  }

  private Predicate<SelectedField> isGeometryType() {
    return field -> getTypeName(field.getType()).map(SpatialConstants.GEOMETRY::equals)
        .orElse(false);
  }
}

package org.dotwebstack.framework.service.openapi.query.mapping;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_EXPANDED_PARAMS;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.NonNull;
import org.dotwebstack.framework.service.openapi.handler.OperationRequest;
import org.dotwebstack.framework.service.openapi.helper.OasConstants;

public class MappingContextBuilder {
  private MappingContextBuilder() {}

  @SuppressWarnings("unchecked")
  public static MappingContext build(@NonNull OperationRequest operationRequest) {

    var expandedParam = operationRequest.getParameters()
        .get(X_DWS_EXPANDED_PARAMS);
    if (expandedParam != null && !(expandedParam instanceof List)) {
      throw invalidConfigurationException("Parameter {} should be a List, but is a {}", X_DWS_EXPANDED_PARAMS,
          expandedParam.getClass());
    }

    Set<String> expandedPaths = expandedParam != null ? new HashSet<>((List<String>) expandedParam) : Set.of();
    List<Parameter> parameters = operationRequest.getContext()
        .getOperation()
        .getParameters();

    if (parameters != null) {
      var expandable = parameters.stream()
          .map(MappingContextBuilder::toExpanded)
          .filter(s -> !s.isEmpty())
          .findFirst()
          .orElse(Set.of());

      return new MappingContext(expandable, expandedPaths);
    }
    return new MappingContext(Set.of(), expandedPaths);
  }

  private static Set<String> toExpanded(Parameter parameter) {
    if (parameter.getExtensions() != null && parameter.getExtensions()
        .containsKey(OasConstants.X_DWS_TYPE) && OasConstants.X_DWS_EXPAND_TYPE.equals(
            parameter.getExtensions()
                .get(OasConstants.X_DWS_TYPE))) {
      var schema = parameter.getSchema();
      return getEnum(schema);
    }
    return Collections.emptySet();
  }

  @SuppressWarnings("unchecked")
  private static Set<String> getEnum(Schema<?> schema) {
    if (schema instanceof StringSchema) {
      var e = (List<String>) schema.getEnum();
      if (e != null) {
        return new HashSet<>(e);
      }
    } else if (schema instanceof ArraySchema) {
      var items = ((ArraySchema) schema).getItems();
      return getEnum(items);
    }
    return Collections.emptySet();
  }
}

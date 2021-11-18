package org.dotwebstack.framework.service.openapi.query.expand;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.dotwebstack.framework.service.openapi.handler.OperationRequest;
import org.dotwebstack.framework.service.openapi.helper.OasConstants;

public class QueryExpandBuilder {

  @SuppressWarnings("unchecked")
  public static QueryExpand build(OperationRequest operationRequest) {
    var expandedPathList = (List<String>) operationRequest.getParameters()
        .get(OasConstants.X_DWS_EXPANDED_PARAMS);
    Set<String> expandedPaths = expandedPathList != null ? new HashSet<>(expandedPathList) : Set.of();
    List<Parameter> parameters = operationRequest.getContext()
        .getOperation()
        .getParameters();
    if (parameters != null) {
      var expandable = parameters.stream()
          .map(QueryExpandBuilder::toExpanded)
          .filter(Objects::nonNull)
          .findFirst()
          .orElse(Set.of());

      return new QueryExpand(expandable, expandedPaths);
    }
    return new QueryExpand(Set.of(), expandedPaths);
  }

  private static Set<String> toExpanded(io.swagger.v3.oas.models.parameters.Parameter p) {
    if (p.getExtensions() != null && p.getExtensions()
        .containsKey(OasConstants.X_DWS_TYPE) && OasConstants.X_DWS_EXPAND_TYPE.equals(
            p.getExtensions()
                .get(OasConstants.X_DWS_TYPE))) {
      var schema = p.getSchema();
      return getEnum(schema);
    }
    return null;
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
    return null;
  }
}

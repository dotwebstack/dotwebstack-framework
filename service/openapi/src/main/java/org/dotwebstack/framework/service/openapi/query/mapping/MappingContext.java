package org.dotwebstack.framework.service.openapi.query.mapping;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_EXPANDED_PARAMS;
import static org.dotwebstack.framework.service.openapi.mapping.MapperUtils.isEnvelope;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.NonNull;
import org.dotwebstack.framework.service.openapi.handler.OperationRequest;
import org.dotwebstack.framework.service.openapi.helper.OasConstants;

@Getter
public class MappingContext {

  private final Map<String, Object> parameters;

  private final Set<String> expandablePaths;

  private final Set<String> expandedPaths;

  private final List<String> path;

  private boolean rootFound;

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
          .map(MappingContext::toExpanded)
          .filter(s -> !s.isEmpty())
          .findFirst()
          .orElse(Set.of());

      return new MappingContext(Collections.unmodifiableMap(operationRequest.getParameters()), expandable,
          expandedPaths);
    }
    return new MappingContext(Map.of(), Set.of(), expandedPaths);
  }

  public MappingContext(Map<String, Object> parameters, Set<String> expandablePaths, Set<String> expandedPaths) {
    this(parameters, expandablePaths, expandedPaths, List.of(), false);
  }

  public MappingContext(Map<String, Object> parameters, Set<String> expandablePaths, Set<String> expandedPaths,
      List<String> path, boolean rootFound) {
    this.parameters = parameters;
    this.expandablePaths = expandablePaths;
    this.expandedPaths = expandedPaths;
    this.path = path;
    this.rootFound = rootFound;
  }

  public MappingContext updatePath(String key, Schema<?> schema) {
    if (!isEnvelope(schema) && rootFound) {
      return new MappingContext(parameters, expandablePaths, expandedPaths, createNewPath(key), rootFound);
    }
    rootFound = rootFound || !isEnvelope(schema);
    return this;
  }

  public MappingContext updatePath(Schema<?> schema) {
    return new MappingContext(parameters, expandablePaths, expandedPaths, createNewPath(),
        rootFound || (!isEnvelope(schema)));
  }

  public boolean atBase() {
    return path.isEmpty();
  }

  public String toString() {
    return String.join(".", path);
  }

  public boolean expanded() {
    var pathString = this.toString();
    return !expandablePaths.contains(pathString) || expandedPaths.contains(pathString);
  }

  public boolean isExpandable() {
    return expandablePaths.contains(this.toString());
  }

  private List<String> createNewPath(String key) {
    var newPath = new LinkedList<>(path);
    newPath.add(key);
    return Collections.unmodifiableList(newPath);
  }

  private List<String> createNewPath() {
    var newPath = new LinkedList<>(path);
    return Collections.unmodifiableList(newPath);
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
    } else if (schema instanceof ArraySchema arraySchema) {
      var items = arraySchema.getItems();
      return getEnum(items);
    }
    return Collections.emptySet();
  }
}

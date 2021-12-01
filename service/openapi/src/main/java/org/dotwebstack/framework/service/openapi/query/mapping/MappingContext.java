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
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.NonNull;
import org.dotwebstack.framework.service.openapi.handler.OperationRequest;
import org.dotwebstack.framework.service.openapi.helper.OasConstants;

public class MappingContext {
  private final Set<String> expandablePaths;

  private final Set<String> expandedPaths;

  private final List<String> path;

  private boolean rootFound;

  @Getter
  private final Set<String> requiredFields;

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

    var requiredFields = Collections.unmodifiableSet(new LinkedHashSet<>(operationRequest.getContext()
        .getQueryProperties()
        .getRequiredFields()));

    if (parameters != null) {
      var expandable = parameters.stream()
          .map(MappingContext::toExpanded)
          .filter(s -> !s.isEmpty())
          .findFirst()
          .orElse(Set.of());

      return new MappingContext(expandable, expandedPaths, requiredFields);
    }
    return new MappingContext(Set.of(), expandedPaths, requiredFields);
  }

  public MappingContext(Set<String> expandablePaths, Set<String> expandedPaths, Set<String> requiredFields) {
    this(expandablePaths, expandedPaths, List.of(), false, requiredFields);
  }

  public MappingContext(Set<String> expandablePaths, Set<String> expandedPaths, List<String> path, boolean rootFound,
      Set<String> requiredFields) {
    this.expandablePaths = expandablePaths;
    this.expandedPaths = expandedPaths;
    this.path = path;
    this.rootFound = rootFound;
    this.requiredFields = requiredFields;
  }

  public MappingContext updatePath(String key, Schema<?> schema) {
    if (!isEnvelope(schema) && rootFound) {
      return new MappingContext(expandablePaths, expandedPaths, createNewPath(key), rootFound, requiredFields);
    }
    rootFound = rootFound || !isEnvelope(schema);
    return this;
  }

  public MappingContext updatePath(Schema<?> schema) {
    return new MappingContext(expandablePaths, expandedPaths, createNewPath(), rootFound || (!isEnvelope(schema)),
        requiredFields);
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
    } else if (schema instanceof ArraySchema) {
      var items = ((ArraySchema) schema).getItems();
      return getEnum(items);
    }
    return Collections.emptySet();
  }
}

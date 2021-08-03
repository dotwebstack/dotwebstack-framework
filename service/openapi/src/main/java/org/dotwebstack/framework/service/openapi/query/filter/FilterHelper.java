package org.dotwebstack.framework.service.openapi.query.filter;

import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_SELECT;

import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.dotwebstack.framework.service.openapi.query.model.Field;
import org.dotwebstack.framework.service.openapi.query.model.GraphQlQuery;
import org.dotwebstack.framework.service.openapi.response.RequestBodyContext;
import org.springframework.http.MediaType;

public class FilterHelper {

  private FilterHelper() {}

  public static void addFilters(@NonNull GraphQlQuery query, RequestBodyContext requestBodyContext,
      @NonNull List<Parameter> parameters, @NonNull Map<String, Object> inputParams, MediaType mediaType) {
    Set<Select> selects = getParamSelects(parameters, inputParams);
    selects.addAll(getRequestBodySelects(requestBodyContext, inputParams, mediaType));

    selects.forEach(select -> {
      String[] path = select.getFieldPath()
          .split("\\.");
      Field field = query.getField();
      for (int i = 0; i < path.length - 1; i++) {
        int finalI = i;
        field = field.getChildren()
            .stream()
            .filter(f -> f.getName()
                .equals(path[finalI]))
            .findFirst()
            .orElseThrow();
      }
      field.getArguments()
          .put(path[path.length - 1], select.getValue());
    });
  }

  @SuppressWarnings("rawtypes")
  private static Set<Select> getRequestBodySelects(RequestBodyContext requestBodyContext,
      @NonNull Map<String, Object> inputParams, MediaType mediaType) {
    if (requestBodyContext == null || mediaType == null) {
      return Set.of();
    }
    Content content = requestBodyContext.getRequestBodySchema()
        .getContent();
    if (content == null) {
      return Set.of();
    }
    Schema<?> schema = content.get(mediaType.toString())
        .getSchema();
    if (schema == null) {
      return Set.of();
    }
    return schema.getProperties()
        .entrySet()
        .stream()
        .map(e -> {
          Schema<?> propertySchema = e.getValue();
          String name = e.getKey();
          if (propertySchema.getExtensions() != null && propertySchema.getExtensions()
              .get(X_DWS_SELECT) != null) {
            return new Select.SelectBuilder().fieldPath((String) propertySchema.getExtensions()
                .get(X_DWS_SELECT))
                .value(inputParams.get(name))
                .build();
          }
          return null;
        })
        .filter(Objects::nonNull)
        .filter(s -> s.getValue() != null)
        .collect(Collectors.toSet());
  }

  private static Set<Select> getParamSelects(@NonNull List<Parameter> parameters,
      @NonNull Map<String, Object> inputParams) {
    return parameters.stream()
        .map(p -> {
          String name = p.getName();
          if (p.getExtensions() != null) {
            String select = (String) p.getExtensions()
                .get(X_DWS_SELECT);
            if (select != null && inputParams.get(name) != null) {
              return new Select.SelectBuilder().fieldPath(select)
                  .value(inputParams.get(name))
                  .build();
            }
          }
          return null;
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
  }
}

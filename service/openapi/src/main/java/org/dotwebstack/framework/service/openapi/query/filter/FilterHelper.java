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
import org.dotwebstack.framework.service.openapi.response.QueryFilter;
import org.dotwebstack.framework.service.openapi.response.QueryFilterClause;
import org.dotwebstack.framework.service.openapi.response.QueryFilterParam;
import org.dotwebstack.framework.service.openapi.response.RequestBodyContext;
import org.springframework.http.MediaType;

public class FilterHelper {

  private FilterHelper() {}

  public static void addSelections(@NonNull GraphQlQuery query, RequestBodyContext requestBodyContext,
      @NonNull List<Parameter> parameters, @NonNull Map<String, Object> inputParams, MediaType mediaType) {
    Set<Select> selects = getParamSelects(parameters, inputParams);
    selects.addAll(getRequestBodySelects(requestBodyContext, inputParams, mediaType));

    selects.forEach(select -> {
      String[] path = select.getFieldPath()
          .split("\\.");
      Field field = resolveField(query, path);
      field.getArguments()
          .put(path[path.length - 1], select.getValue());
    });
  }

  protected static Field resolveField(GraphQlQuery query, String[] path) {
    Field field = query.getField();
    for (int i = 0; i < path.length - 1; i++) {
      // TODO: throw error if not found
      int finalI = i;
      field = field.getChildren()
          .stream()
          .filter(f -> f.getName()
              .equals(path[finalI]))
          .findFirst()
          .orElseThrow();
    }
    return field;
  }

  protected static Field resolveFilterField(GraphQlQuery query, String[] path) {
    Field field = query.getField();
    for (int i = 0; i < path.length; i++) {
      // TODO: throw error if not found
      int finalI = i;
      field = field.getChildren()
          .stream()
          .filter(f -> f.getName()
              .equals(path[finalI]))
          .findFirst()
          .orElseThrow();
    }
    return field;
  }

  public static void addFilters(@NonNull GraphQlQuery query, @NonNull List<QueryFilter> filters,
      @NonNull Map<String, Object> inputParams) {
    filters.forEach(filter -> {
      Filter.FilterBuilder builder = Filter.builder();
      String[] fieldPath = filter.getField();
      List<FilterClause> clauses = filter.getClauses()
          .stream()
          .map(fc -> toFilterClause(fc, inputParams))
          .filter(Objects::nonNull)
          .collect(Collectors.toList());
      if (!clauses.isEmpty()) {
        builder.filterClauses(clauses);
        Field field = resolveFilterField(query, fieldPath);
        // TODO: check that no other filters are present
        field.setFilter(builder.build());
      }
    });

  }

  private static FilterClause toFilterClause(QueryFilterClause clause, Map<String, Object> inputParams) {
    FilterClause.FilterClauseBuilder builder = FilterClause.builder();

    builder.operator(clause.getOperator());
    QueryFilterParam param = clause.getFilterParam();
    if (param != null) {
      builder.field(clause.getField());
      Object value = inputParams.get(param.getParamName());
      if (value == null) {
        return null;
      }
      builder.value(value);
    } else {
      List<FilterClause> subClauses = clause.getClauses()
          .stream()
          .map(fc -> toFilterClause(fc, inputParams))
          .filter(Objects::nonNull)
          .collect(Collectors.toList());
      if (subClauses.isEmpty()) {
        return null;
      }
      builder.clauses(subClauses);
    }

    return builder.build();

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
        .filter(s -> s.getValue() != null)
        .collect(Collectors.toSet());
  }

}

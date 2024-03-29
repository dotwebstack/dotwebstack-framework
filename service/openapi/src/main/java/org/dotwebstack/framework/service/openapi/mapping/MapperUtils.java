package org.dotwebstack.framework.service.openapi.mapping;

import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.FIRST_ARGUMENT_NAME;
import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.NODES_FIELD_NAME;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.invalidOpenApiConfigurationException;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLTypeUtil;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.dotwebstack.framework.service.openapi.helper.OasConstants;
import org.springframework.http.HttpStatus;

public class MapperUtils {

  private MapperUtils() {}

  public static <T> Collector<T, ?, Optional<T>> collectExactlyOne() {
    return Collectors.collectingAndThen(Collectors.toList(),
        list -> list.size() == 1 ? Optional.of(list.get(0)) : Optional.empty());
  }

  public static Map.Entry<HttpStatus, ApiResponse> getHandleableResponseEntry(Operation operation) {
    return operation.getResponses()
        .entrySet()
        .stream()
        .map(responseEntry -> Map.entry(HttpStatus.valueOf(Integer.parseInt(responseEntry.getKey())),
            responseEntry.getValue()))
        .filter(responseEntry -> responseEntry.getKey()
            .is3xxRedirection()
            || responseEntry.getKey()
                .is2xxSuccessful())
        .collect(MapperUtils.collectExactlyOne())
        .orElseThrow(
            () -> invalidOpenApiConfigurationException("Operation does not contain exactly one handleable response."));
  }

  public static boolean isEnvelope(Schema<?> schema) {
    return Optional.ofNullable(schema.getExtensions())
        .map(extensions -> Boolean.TRUE.equals(extensions.get(OasConstants.X_DWS_ENVELOPE)))
        .orElse(false);
  }

  public static boolean isExpr(Schema<?> schema) {
    return Optional.ofNullable(schema.getExtensions())
        .map(extensions -> extensions.containsKey(OasConstants.X_DWS_EXPR))
        .orElse(false);
  }

  public static boolean isMappable(Schema<?> schema) {
    return !isEnvelope(schema) && !isExpr(schema);
  }

  public static GraphQLFieldDefinition getObjectField(GraphQLFieldsContainer fieldsContainer, String fieldName) {
    return Optional.ofNullable(fieldsContainer.getFieldDefinition(fieldName))
        .orElseThrow(() -> invalidConfigurationException("Field '{}' not found for `{}` type.", fieldName,
            fieldsContainer.getName()));
  }

  public static boolean isPageableField(GraphQLFieldDefinition fieldDefinition) {
    var rawType = GraphQLTypeUtil.unwrapAll(fieldDefinition.getType());
    return fieldDefinition.getArgument(FIRST_ARGUMENT_NAME) != null
        && ((GraphQLObjectType) rawType).getFieldDefinition(NODES_FIELD_NAME) != null;
  }

  public static boolean isQueryField(GraphQLFieldDefinition fieldDefinition,
      GraphQLFieldsContainer parentFieldsContainer) {
    return parentFieldsContainer instanceof GraphQLObjectType && parentFieldsContainer.getName()
        .equals("Query")
        && parentFieldsContainer.getFields()
            .contains(fieldDefinition);
  }
}

package org.dotwebstack.framework.service.openapi.response;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_EXPR;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_EXPR_FALLBACK_VALUE;
import static org.dotwebstack.framework.service.openapi.mapping.MapperUtils.isMappable;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLTypeUtil;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.MapContext;
import org.dotwebstack.framework.core.datafetchers.paging.PagingConstants;
import org.dotwebstack.framework.core.jexl.JexlHelper;
import org.dotwebstack.framework.service.openapi.handler.OperationContext;
import org.dotwebstack.framework.service.openapi.handler.OperationRequest;
import org.dotwebstack.framework.service.openapi.mapping.EnvironmentProperties;
import org.dotwebstack.framework.service.openapi.mapping.MapperUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class JsonBodyMapper implements BodyMapper {

  private static final Pattern MEDIA_TYPE_PATTERN = Pattern.compile("^application/([a-z]+\\+)?json$");

  private final GraphQLSchema graphQlSchema;

  private final JexlHelper jexlHelper;

  private final EnvironmentProperties environmentProperties;

  public JsonBodyMapper(@NonNull GraphQLSchema graphQlSchema, @NonNull JexlEngine jexlEngine,
      @NonNull EnvironmentProperties environmentProperties) {
    this.graphQlSchema = graphQlSchema;
    this.jexlHelper = new JexlHelper(jexlEngine);
    this.environmentProperties = environmentProperties;
  }

  @Override
  public Mono<Object> map(OperationRequest operationRequest, Object result) {
    var queryField = graphQlSchema.getQueryType()
        .getFieldDefinition(operationRequest.getContext()
            .getQueryProperties()
            .getField());

    var jexlContext = createJexlContext(operationRequest);

    return Mono.just(mapSchema(operationRequest.getResponseSchema(), queryField, result, jexlContext));
  }

  private Object mapSchema(Schema<?> schema, GraphQLFieldDefinition fieldDefinition, Object data,
      JexlContext jexlContext) {
    if (data == null) {
      return null;
    }

    if (schema instanceof ObjectSchema || schema.getType() == null) {
      return mapObjectSchema(schema, fieldDefinition, data, jexlContext);
    }

    if (schema instanceof ArraySchema) {
      return mapArraySchema((ArraySchema) schema, fieldDefinition, data, jexlContext);
    }

    return evaluateScalarData(schema, data, jexlContext);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private Object mapObjectSchema(Schema<?> schema, GraphQLFieldDefinition fieldDefinition, Object data,
      JexlContext jexlContext) {
    if (MapperUtils.isEnvelope(schema)) {
      return schema.getProperties()
          .entrySet()
          .stream()
          .collect(HashMap::new, (acc, entry) -> {
            var nestedSchema = entry.getValue();
            Object nestedValue;

            if (!MapperUtils.isMappable(nestedSchema, schema)) {
              nestedValue = mapSchema(nestedSchema, fieldDefinition, data, jexlContext);
            } else {
              if (!(data instanceof Map)) {
                throw illegalStateException("Data is not compatible with object schema.");
              }

              var dataMap = (Map<String, Object>) data;
              nestedValue = mapSchema(entry.getValue(), fieldDefinition, dataMap.get(entry.getKey()), jexlContext);
            }

            if (nestedValue != null || Boolean.TRUE.equals(nestedSchema.getNullable())) {
              acc.put(entry.getKey(), nestedValue);
            }
          }, HashMap::putAll);
    }

    if (!(data instanceof Map)) {
      throw illegalStateException("Data is not compatible with object schema.");
    }

    var dataMap = (Map<String, Object>) data;

    return schema.getProperties()
        .entrySet()
        .stream()
        .collect(HashMap::new, (acc, entry) -> {
          var nestedSchema = entry.getValue();
          var value =
              mapObjectSchemaProperty(entry.getKey(), nestedSchema, schema, fieldDefinition, dataMap, jexlContext);

          if (value != null || Boolean.TRUE.equals(nestedSchema.getNullable())) {
            acc.put(entry.getKey(), value);
          }
        }, HashMap::putAll);
  }

  private Object mapObjectSchemaProperty(String name, Schema<?> schema, Schema<?> parentSchema,
      GraphQLFieldDefinition parentFieldDefinition, Map<String, Object> data, JexlContext jexlContext) {
    if (!isMappable(schema, parentSchema)) {
      return mapSchema(schema, parentFieldDefinition, data, jexlContext);
    }

    var rawType = (GraphQLObjectType) GraphQLTypeUtil.unwrapAll(parentFieldDefinition.getType());
    var fieldDefinition = rawType.getFieldDefinition(name);

    updateJexlContext(data, jexlContext);

    return mapSchema(schema, fieldDefinition, data.get(name), jexlContext);
  }

  @SuppressWarnings("unchecked")
  private Collection<Object> mapArraySchema(ArraySchema schema, GraphQLFieldDefinition fieldDefinition, Object data,
      JexlContext jexlContext) {
    if (MapperUtils.isPageableField(fieldDefinition)) {
      if (!(data instanceof Map)) {
        throw illegalStateException("Data is not compatible with pageable array schema.");
      }

      var dataMap = (Map<String, Object>) data;
      var items = (Collection<Object>) dataMap.get(PagingConstants.NODES_FIELD_NAME);

      if (!(dataMap instanceof Collection)) {
        throw illegalStateException("Data is not compatible with array schema.");
      }

      var rawType = (GraphQLObjectType) GraphQLTypeUtil.unwrapAll(fieldDefinition.getType());

      return items.stream()
          .map(item -> mapSchema(schema.getItems(), rawType.getFieldDefinition(PagingConstants.NODES_FIELD_NAME), item,
              jexlContext))
          .collect(Collectors.toList());
    }

    if (!(data instanceof Collection)) {
      throw illegalStateException("Data is not compatible with array schema.");
    }

    return ((Collection<Object>) data).stream()
        .map(item -> mapSchema(schema.getItems(), fieldDefinition, item, jexlContext))
        .collect(Collectors.toList());
  }

  private JexlContext createJexlContext(OperationRequest operationRequest) {
    Map<String, Object> parameters = operationRequest.getParameters();
    MapContext result = new MapContext();

    result.set("$body", parameters);
    result.set("$query", parameters);
    result.set("$path", parameters);
    result.set("$header", parameters);

    environmentProperties.getAllProperties()
        .forEach((prop, value) -> result.set(String.format("env.%s", prop), value));

    return result;
  }

  private void updateJexlContext(Map<String, Object> dataMap, JexlContext jexlContext) {
    dataMap.entrySet()
        .stream()
        .filter(entry -> !(entry.getValue() instanceof Map))
        .forEach(entry -> jexlContext.set(String.format("fields.%s", entry.getKey()), entry.getValue()));
  }

  private Object evaluateScalarData(Schema<?> schema, Object data, JexlContext jexlContext) {
    if (schema.getExtensions() != null && schema.getExtensions()
        .containsKey(X_DWS_EXPR)) {

      Object defaultValue = schema.getDefault();

      String expression = schema.getExtensions()
          .get(X_DWS_EXPR)
          .toString();

      String fallBackValue = schema.getExtensions()
          .containsKey(X_DWS_EXPR_FALLBACK_VALUE)
              ? schema.getExtensions()
                  .get(X_DWS_EXPR_FALLBACK_VALUE)
                  .toString()
              : null;

      return this.jexlHelper.evaluateScriptWithFallback(expression, fallBackValue, jexlContext, Object.class)
          .orElse(defaultValue);
    }

    if (data == null) {
      return schema.getDefault();
    }

    return data;
  }

  @Override
  public boolean supports(MediaType mediaType, OperationContext operationContext) {
    var mediaTypeString = mediaType.toString();

    var schema = operationContext.getSuccessResponse()
        .getContent()
        .get(mediaTypeString)
        .getSchema();

    return schema != null && MEDIA_TYPE_PATTERN.matcher(mediaTypeString)
        .matches();
  }
}

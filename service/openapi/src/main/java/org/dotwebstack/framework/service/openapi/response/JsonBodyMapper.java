package org.dotwebstack.framework.service.openapi.response;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_EXPR;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_EXPR_FALLBACK_VALUE;
import static org.dotwebstack.framework.service.openapi.mapping.MapperUtils.isEnvelope;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
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
import org.dotwebstack.framework.service.openapi.mapping.TypeMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class JsonBodyMapper implements BodyMapper {

  private static final Pattern MEDIA_TYPE_PATTERN = Pattern.compile("^application/([a-z]+\\+)?json$");

  private final GraphQLSchema graphQlSchema;

  private final JexlHelper jexlHelper;

  private final EnvironmentProperties environmentProperties;

  private final Map<String, TypeMapper> typeMappers;

  public JsonBodyMapper(@NonNull GraphQLSchema graphQlSchema, @NonNull JexlEngine jexlEngine,
      @NonNull EnvironmentProperties environmentProperties, @NonNull Collection<TypeMapper> typeMappers) {
    this.graphQlSchema = graphQlSchema;
    this.jexlHelper = new JexlHelper(jexlEngine);
    this.environmentProperties = environmentProperties;
    this.typeMappers = typeMappers.stream()
        .collect(Collectors.toMap(TypeMapper::typeName, Function.identity()));
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
      return emptyValue(schema);
    }

    var newContext = updateJexlContext(data, jexlContext);

    if ("object".equals(schema.getType())) {
      return mapObjectSchema(schema, fieldDefinition, data, newContext);
    }

    if (schema instanceof ArraySchema) {
      return mapArraySchema((ArraySchema) schema, fieldDefinition, data, newContext);
    }

    return evaluateScalarData(schema, data, newContext);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private Object mapObjectSchema(Schema<?> schema, GraphQLFieldDefinition fieldDefinition, Object data,
      JexlContext jexlContext) {
    if (MapperUtils.isEnvelope(schema)) {
      return mapEnvelopeObjectSchema(schema, fieldDefinition, data, jexlContext);
    }

    var rawType = GraphQLTypeUtil.unwrapAll(fieldDefinition.getType());

    if (typeMappers.containsKey(rawType.getName())) {
      return typeMappers.get(rawType.getName())
          .fieldToBody(data);
    }

    if (!(data instanceof Map)) {
      throw illegalStateException("Data is not compatible with object schema.");
    }

    var dataMap = (Map<String, Object>) data;

    return schema.getProperties()
        .entrySet()
        .stream()
        .collect(HashMap::new, (acc, entry) -> {
          var property = entry.getKey();
          var nestedSchema = entry.getValue();
          var value = mapObjectSchemaProperty(property, nestedSchema, fieldDefinition, dataMap, jexlContext);

          if ((schema.getRequired() != null && schema.getRequired()
              .contains(property)) || !valueIsEmpty(value)) {
            acc.put(entry.getKey(), value);
          }
        }, HashMap::putAll);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private Object mapEnvelopeObjectSchema(Schema<?> schema, GraphQLFieldDefinition fieldDefinition, Object data,
      JexlContext jexlContext) {
    var rawType = (GraphQLObjectType) GraphQLTypeUtil.unwrapAll(fieldDefinition.getType());

    var envelopeValue = schema.getProperties()
        .entrySet()
        .stream()
        .collect(HashMap::new, (acc, entry) -> {
          var property = entry.getKey();
          var nestedSchema = entry.getValue();
          var nestedFieldDefinition = rawType.getFieldDefinition(property);
          Object nestedValue;

          if (nestedFieldDefinition == null || isEnvelope(nestedSchema)) {
            nestedValue = mapSchema(nestedSchema, fieldDefinition, data, jexlContext);
          } else {
            if (!(data instanceof Map)) {
              throw illegalStateException("Data is not compatible with object schema.");
            }

            var dataMap = (Map<String, Object>) data;
            nestedValue = mapSchema(nestedSchema, nestedFieldDefinition, dataMap.get(property), jexlContext);
          }

          if ((schema.getRequired() != null && schema.getRequired()
              .contains(property)) || !valueInEnvelopeIsEmpty(nestedValue)) {
            acc.put(entry.getKey(), nestedValue);
          }
        }, HashMap::putAll);

    return valueInEnvelopeIsEmpty(envelopeValue) && Boolean.TRUE.equals(schema.getNullable()) ? null : envelopeValue;
  }

  private Object emptyValue(Schema<?> schema) {
    if (Boolean.TRUE.equals(schema.getNullable())) {
      return null;
    }

    if (schema instanceof ArraySchema) {
      return List.of();
    } else if (schema instanceof ObjectSchema) {
      return Map.of();
    } else {
      return null;
    }
  }

  private boolean valueIsEmpty(Object value) {
    if (value instanceof Collection<?>) {
      return ((Collection<?>) value).isEmpty();
    } else if (value instanceof Map<?, ?>) {
      return ((Map<?, ?>) value).isEmpty();
    } else {
      return value == null;
    }
  }

  private boolean valueInEnvelopeIsEmpty(Object value) {
    if (value instanceof Collection<?>) {
      return ((Collection<?>) value).isEmpty();
    } else if (value instanceof Map<?, ?>) {
      var valueMap = ((Map<?, ?>) value);
      return valueMap.values()
          .isEmpty()
          || valueMap.values()
              .stream()
              .allMatch(Objects::isNull);
    } else {
      return value == null;
    }
  }

  private Object mapObjectSchemaProperty(String name, Schema<?> schema, GraphQLFieldDefinition parentFieldDefinition,
      Map<String, Object> data, JexlContext jexlContext) {
    if (!isMappable(schema)) {
      return mapSchema(schema, parentFieldDefinition, data, jexlContext);
    }

    var rawType = (GraphQLObjectType) GraphQLTypeUtil.unwrapAll(parentFieldDefinition.getType());
    var fieldDefinition = rawType.getFieldDefinition(name);

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

      if (!(items instanceof Collection)) {
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
    var parameters = operationRequest.getParameters();
    var context = new MapContext();

    context.set("args", parameters);

    environmentProperties.getAllProperties()
        .forEach((prop, value) -> context.set(String.format("env.%s", prop), value));

    return context;
  }

  @SuppressWarnings("unchecked")
  private JexlContext updateJexlContext(Object data, JexlContext jexlContext) {
    if (!(data instanceof Map)) {
      return jexlContext;
    }

    var newContext = new MapContext();

    newContext.set("args", jexlContext.get("args"));

    environmentProperties.getAllProperties()
        .forEach((prop, value) -> newContext.set(String.format("env.%s", prop), value));

    newContext.set("data", data);

    ((Map<String, Object>) data).entrySet()
        .stream()
        .filter(entry -> !(entry.getValue() instanceof Map))
        .forEach(entry -> newContext.set(String.format("fields.%s", entry.getKey()), entry.getValue()));

    return newContext;
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

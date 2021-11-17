package org.dotwebstack.framework.service.openapi.response;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_EXPR;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_EXPR_FALLBACK_VALUE;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLTypeUtil;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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

    if (schema instanceof StringSchema) {
      return mapStringSchema((StringSchema) schema, fieldDefinition, evaluateScalarData(schema, data, jexlContext));
    }

    if (schema instanceof IntegerSchema) {
      return mapIntegerSchema((IntegerSchema) schema, fieldDefinition, evaluateScalarData(schema, data, jexlContext));
    }

    if (schema instanceof NumberSchema) {
      return mapNumberSchema((NumberSchema) schema, fieldDefinition, evaluateScalarData(schema, data, jexlContext));
    }

    if (schema instanceof BooleanSchema) {
      return mapBooleanSchema((BooleanSchema) schema, fieldDefinition, evaluateScalarData(schema, data, jexlContext));
    }

    throw invalidConfigurationException("Schema type '{}' not supported.", schema.getName());
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

  @SuppressWarnings({"unchecked", "rawtypes"})
  private Map<String, Object> mapObjectSchema(Schema<?> schema, GraphQLFieldDefinition fieldDefinition, Object data,
      JexlContext jexlContext) {
    if (MapperUtils.isEnvelope(schema) && data instanceof Collection) {
      return schema.getProperties()
          .entrySet()
          .stream()
          .collect(Collectors.toMap(Map.Entry::getKey,
              entry -> mapSchema(entry.getValue(), fieldDefinition, data, jexlContext)));
    }

    if (!(data instanceof Map)) {
      throw invalidConfigurationException("Data is not compatible with object schema.");
    }

    var fieldType = GraphQLTypeUtil.unwrapAll(fieldDefinition.getType());

    if (!(fieldType instanceof GraphQLObjectType)) {
      throw invalidConfigurationException("Field type is not compatible with object schema.");
    }

    var dataMap = (Map<String, Object>) data;

    return schema.getProperties()
        .entrySet()
        .stream()
        .collect(HashMap::new, (acc, entry) -> {
          var property = entry.getKey();
          var nestedSchema = entry.getValue();

          if (MapperUtils.isEnvelope(nestedSchema)) {
            var value = mapObjectSchema(nestedSchema, fieldDefinition, data, jexlContext);

            if (Boolean.TRUE.equals(nestedSchema.getNullable()) || value != null) {
              acc.put(property, value);
              return;
            }
          }

          updateJexlContext(dataMap, jexlContext);

          var nestedFieldDefinition = MapperUtils.getObjectField((GraphQLObjectType) fieldType, property);
          var value = mapSchema(nestedSchema, nestedFieldDefinition, dataMap.get(property), jexlContext);

          if (Boolean.TRUE.equals(nestedSchema.getNullable()) || value != null) {
            acc.put(property, value);
          }
        }, HashMap::putAll);
  }

  private void updateJexlContext(Map<String, Object> dataMap, JexlContext jexlContext) {
    dataMap.entrySet()
        .stream()
        .filter(entry -> !(entry.getValue() instanceof Map))
        .forEach(entry -> jexlContext.set(String.format("fields.%s", entry.getKey()), entry.getValue()));
  }

  @SuppressWarnings("unchecked")
  private Object unwrapPagedData(Object data) {
    if (!(data instanceof Map)) {
      throw invalidConfigurationException("Pageable node is not compatible with object schema.");
    }

    return ((Map<String, Object>) data).get(PagingConstants.NODES_FIELD_NAME);
  }

  private GraphQLFieldDefinition unwrapPagedField(GraphQLFieldDefinition fieldDefinition) {
    var fieldType = GraphQLTypeUtil.unwrapAll(fieldDefinition.getType());

    return MapperUtils.getObjectField((GraphQLObjectType) fieldType, PagingConstants.NODES_FIELD_NAME);
  }

  private List<?> mapArraySchema(ArraySchema schema, GraphQLFieldDefinition fieldDefinition, Object data,
      JexlContext jexlContext) {
    Object dataToMap;
    GraphQLFieldDefinition fieldDefinitionToMap;
    if (MapperUtils.isPageableField(fieldDefinition)) {
      dataToMap = unwrapPagedData(data);
      fieldDefinitionToMap = unwrapPagedField(fieldDefinition);
    } else {
      dataToMap = data;
      fieldDefinitionToMap = fieldDefinition;
    }

    if (!(dataToMap instanceof Collection)) {
      throw invalidConfigurationException("Data is not compatible with array schema.");
    }

    return ((Collection<?>) dataToMap).stream()
        .map(item -> mapSchema(schema.getItems(), fieldDefinitionToMap, item, jexlContext))
        .collect(Collectors.toList());
  }

  private String mapStringSchema(StringSchema schema, GraphQLFieldDefinition fieldDefinition, Object data) {
    return data.toString();
  }

  private Number mapIntegerSchema(IntegerSchema schema, GraphQLFieldDefinition fieldDefinition, Object data) {
    if (data instanceof Integer) {
      return (Integer) data;
    }

    if (data instanceof BigInteger) {
      return (BigInteger) data;
    }

    throw invalidConfigurationException("Data is not compatible with integer schema.");
  }

  private Number mapNumberSchema(NumberSchema schema, GraphQLFieldDefinition fieldDefinition, Object data) {
    if (data instanceof Double) {
      return (Double) data;
    }

    if (data instanceof Float) {
      return (Float) data;
    }

    if (data instanceof BigDecimal) {
      return (BigDecimal) data;
    }

    throw invalidConfigurationException("Data is not compatible with number schema.");
  }

  private Boolean mapBooleanSchema(BooleanSchema schema, GraphQLFieldDefinition fieldDefinition, Object data) {
    if (!(data instanceof Boolean)) {
      throw invalidConfigurationException("Data is not compatible with boolean schema.");
    }

    return (Boolean) data;
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

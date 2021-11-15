package org.dotwebstack.framework.service.openapi.response;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import graphql.ExecutionResult;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLTypeUtil;
import io.swagger.v3.oas.models.OpenAPI;
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
import org.dotwebstack.framework.core.datafetchers.paging.PagingConstants;
import org.dotwebstack.framework.service.openapi.handler.OperationContext;
import org.dotwebstack.framework.service.openapi.handler.OperationRequest;
import org.dotwebstack.framework.service.openapi.mapping.MapperUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class JsonBodyMapper implements BodyMapper {

  private static final Pattern MEDIA_TYPE_PATTERN = Pattern.compile("^application/([a-z]+\\+)?json$");

  private final OpenAPI openApi;

  private final GraphQLSchema graphQlSchema;

  public JsonBodyMapper(OpenAPI openApi, GraphQLSchema graphQlSchema) {
    this.openApi = openApi;
    this.graphQlSchema = graphQlSchema;
  }

  @Override
  public Mono<Object> map(OperationRequest operationRequest, ExecutionResult executionResult) {
    var queryField = graphQlSchema.getQueryType()
        .getFieldDefinition(operationRequest.getContext()
            .getQueryProperties()
            .getField());

    Map<String, Object> data = executionResult.getData();
    var body = mapSchema(operationRequest.getResponseSchema(), queryField, data.get(queryField.getName()));

    return Mono.justOrEmpty(body);
  }

  private Object mapSchema(Schema<?> schema, GraphQLFieldDefinition fieldDefinition, Object data) {
    if (data == null) {
      return null;
    }

    if (schema instanceof ObjectSchema || schema.getType() == null) {
      return mapObjectSchema(schema, fieldDefinition, data);
    }

    if (schema instanceof ArraySchema) {
      return mapArraySchema((ArraySchema) schema, fieldDefinition, data);
    }

    if (schema instanceof StringSchema) {
      return mapStringSchema((StringSchema) schema, fieldDefinition, data);
    }

    if (schema instanceof IntegerSchema) {
      return mapIntegerSchema((IntegerSchema) schema, fieldDefinition, data);
    }

    if (schema instanceof NumberSchema) {
      return mapNumberSchema((NumberSchema) schema, fieldDefinition, data);
    }

    if (schema instanceof BooleanSchema) {
      return mapBooleanSchema((BooleanSchema) schema, fieldDefinition, data);
    }

    throw invalidConfigurationException("Schema type '{}' not supported.", schema.getName());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private Map<String, Object> mapObjectSchema(Schema<?> schema, GraphQLFieldDefinition fieldDefinition, Object data) {
    if (MapperUtils.isEnvelope(schema)) {
      return schema.getProperties()
          .entrySet()
          .stream()
          .collect(Collectors.toMap(Map.Entry::getKey, entry -> mapSchema(entry.getValue(), fieldDefinition, data)));
    }

    if (!(data instanceof Map)) {
      throw invalidConfigurationException("Data is not compatible with object schema.");
    }

    var fieldType = GraphQLTypeUtil.unwrapAll(fieldDefinition.getType());

    if (!(fieldType instanceof GraphQLObjectType)) {
      throw invalidConfigurationException("Field type is not compatible with object schema.");
    }

    List<String> requiredProperties = schema.getRequired();

    return ((Map<String, Object>) data).entrySet()
        .stream()
        .collect(HashMap::new, (acc, entry) -> {
          var property = entry.getKey();
          var nestedSchema = schema.getProperties()
              .get(property);

          var nestedFieldDefinition = MapperUtils.getObjectField((GraphQLObjectType) fieldType, entry.getKey());

          var mappedValue = mapSchema(nestedSchema, nestedFieldDefinition, entry.getValue());

          if (requiredProperties.contains(property)) {
            acc.put(property, mappedValue);
          } else {
            if (!isNullOrEmpty(mappedValue)) {
              acc.put(property, mappedValue);
            }
          }
        }, HashMap::putAll);
  }

  private boolean isNullOrEmpty(Object mappedValue) {
    if (mappedValue == null) {
      return true;
    }
    if (mappedValue instanceof Collection<?>) {
      return ((Collection<?>) mappedValue).isEmpty();
    }
    return false;
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

  private List<?> mapArraySchema(ArraySchema schema, GraphQLFieldDefinition fieldDefinition, Object data) {
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
        .map(item -> mapSchema(schema.getItems(), fieldDefinitionToMap, item))
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

package org.dotwebstack.framework.service.openapi.response;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.core.jexl.JexlHelper.getJexlContext;
import static org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper.getJexlExpression;
import static org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper.resolveDwsName;
import static org.dotwebstack.framework.service.openapi.jexl.JexlUtils.evaluateJexlExpression;
import static org.dotwebstack.framework.service.openapi.mapping.MapperUtils.isEnvelope;
import static org.dotwebstack.framework.service.openapi.mapping.MapperUtils.isMappable;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.GraphQL;
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

  public JsonBodyMapper(@NonNull GraphQL graphQL, @NonNull JexlEngine jexlEngine,
      @NonNull EnvironmentProperties environmentProperties, @NonNull Collection<TypeMapper> typeMappers) {
    this.graphQlSchema = graphQL.getGraphQLSchema();
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

    var jexlContext = getJexlContext(environmentProperties.getAllProperties(), operationRequest.getServerRequest(),
        operationRequest.getParameters());

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

    if (schema instanceof ArraySchema arraySchema) {
      return mapArraySchema(arraySchema, fieldDefinition, data, newContext);
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
          .fieldToBody(data, schema);
    }

    Map<String, Object> dataMap;

    if (!(data instanceof Map)) {
      try {
        dataMap = new ObjectMapper().convertValue(data, Map.class);
      } catch (IllegalArgumentException e) {
        throw illegalStateException("Data is not compatible with object schema.", e);
      }
    } else {
      dataMap = (Map<String, Object>) data;
    }

    if (schema.getProperties() == null) {
      return dataMap;
    }

    return schema.getProperties()
        .entrySet()
        .stream()
        .collect(HashMap::new, (acc, entry) -> {
          var property = entry.getKey();
          var dwsProperty = resolveDwsName(entry.getValue(), property);
          var nestedSchema = entry.getValue();
          var value = mapObjectSchemaProperty(dwsProperty, nestedSchema, fieldDefinition, dataMap, jexlContext);

          if ((schema.getRequired() != null && schema.getRequired()
              .contains(property)) || !valueIsEmpty(value)) {
            acc.put(property, value);
          }
        }, HashMap::putAll);
  }

  @SuppressWarnings({"unchecked"})
  private Object mapEnvelopeObjectSchema(Schema<?> schema, GraphQLFieldDefinition fieldDefinition, Object data,
      JexlContext jexlContext) {
    var rawType = (GraphQLObjectType) GraphQLTypeUtil.unwrapAll(fieldDefinition.getType());

    var envelopeValue = schema.getProperties()
        .entrySet()
        .stream()
        .collect(HashMap::new, (acc, entry) -> {
          var property = entry.getKey();
          var dwsProperty = resolveDwsName(entry.getValue(), property);
          var nestedSchema = entry.getValue();
          var nestedFieldDefinition = rawType.getFieldDefinition(dwsProperty);
          Object nestedValue;

          if (nestedFieldDefinition == null || isEnvelope(nestedSchema)) {
            nestedValue = mapSchema(nestedSchema, fieldDefinition, data, jexlContext);
          } else {
            if (!(data instanceof Map)) {
              throw illegalStateException("Data is not compatible with object schema.");
            }

            var dataMap = (Map<String, Object>) data;
            var childData = dataMap.get(dwsProperty);
            addParentData(dataMap, childData);
            nestedValue = mapSchema(nestedSchema, nestedFieldDefinition, childData, jexlContext);
          }

          if ((schema.getRequired() != null && schema.getRequired()
              .contains(property)) || !valueInEnvelopeIsEmpty(nestedValue)) {
            acc.put(property, nestedValue);
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
      return valueMap.isEmpty() || valueMap.values()
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

    var childData = data.get(name);
    addParentData(data, childData);
    return mapSchema(schema, fieldDefinition, childData, jexlContext);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void addParentData(Map<String, Object> data, Object childData) {
    if (childData == null) {
      return;
    }
    if (childData instanceof Map) {
      ((Map<String, Object>) childData).put("_parent", data);
    } else if (childData instanceof List childDataList && !childDataList.isEmpty()
        && childDataList.get(0) instanceof Map) {
      childDataList.forEach(item -> ((Map) item).put("_parent", data));
    }
  }

  @SuppressWarnings("unchecked")
  private Collection<Object> mapArraySchema(ArraySchema schema, GraphQLFieldDefinition fieldDefinition, Object data,
      JexlContext jexlContext) {

    var rawType = GraphQLTypeUtil.unwrapAll(fieldDefinition.getType());

    if (typeMappers.containsKey(rawType.getName())) {
      return (Collection<Object>) typeMappers.get(rawType.getName())
          .fieldToBody(data, schema);
    }

    if (MapperUtils.isPageableField(fieldDefinition)) {
      if (!(data instanceof Map)) {
        throw illegalStateException("Data is not compatible with pageable array schema.");
      }

      var dataMap = (Map<String, Object>) data;
      var items = dataMap.get(PagingConstants.NODES_FIELD_NAME);

      if (!(items instanceof Collection)) {
        throw illegalStateException("Data is not compatible with array schema.");
      }

      return ((Collection<Object>) items).stream()
          .map(item -> mapSchema(schema.getItems(),
              ((GraphQLObjectType) rawType).getFieldDefinition(PagingConstants.NODES_FIELD_NAME), item, jexlContext))
          .toList();
    }

    if (!(data instanceof Collection)) {
      throw illegalStateException("Data is not compatible with array schema.");
    }

    return ((Collection<Object>) data).stream()
        .map(item -> mapSchema(schema.getItems(), fieldDefinition, item, jexlContext))
        .toList();
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

    return newContext;
  }

  private Object evaluateScalarData(Schema<?> schema, Object data, JexlContext jexlContext) {
    var defaultValue = schema.getDefault();

    var optionalJexlExpression = getJexlExpression(schema);

    if (optionalJexlExpression.isPresent()) {
      return evaluateJexlExpression(optionalJexlExpression.get(), jexlHelper, jexlContext, Object.class)
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

    var schema = operationContext.getResponse()
        .getContent()
        .get(mediaTypeString)
        .getSchema();

    return schema != null && MEDIA_TYPE_PATTERN.matcher(mediaTypeString)
        .matches();
  }
}

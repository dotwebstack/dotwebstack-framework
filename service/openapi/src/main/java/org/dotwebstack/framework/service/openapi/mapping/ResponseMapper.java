package org.dotwebstack.framework.service.openapi.mapping;

import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.mappingException;
import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.noResultFoundException;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.ARRAY_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.OBJECT_TYPE;
import static org.dotwebstack.framework.service.openapi.mapping.ResponseMapperHelper.isRequiredAndNullOrEmpty;
import static org.dotwebstack.framework.service.openapi.response.ResponseWriteContextHelper.createResponseContextFromChildData;
import static org.dotwebstack.framework.service.openapi.response.ResponseWriteContextHelper.createResponseWriteContextFromChildSchema;
import static org.dotwebstack.framework.service.openapi.response.ResponseWriteContextHelper.unwrapChildSchema;
import static org.dotwebstack.framework.service.openapi.response.ResponseWriteContextHelper.unwrapItemSchema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.MapContext;
import org.dotwebstack.framework.core.helpers.JexlHelper;
import org.dotwebstack.framework.service.openapi.conversion.TypeConverterRouter;
import org.dotwebstack.framework.service.openapi.exception.NoResultFoundException;
import org.dotwebstack.framework.service.openapi.response.ResponseWriteContext;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;

@Component
public class ResponseMapper {

  private final ObjectMapper objectMapper;

  private final JexlHelper jexlHelper;

  private final EnvironmentProperties properties;

  private final TypeConverterRouter typeConverterRouter;

  public ResponseMapper(Jackson2ObjectMapperBuilder objectMapperBuilder, JexlEngine jexlEngine,
      EnvironmentProperties properties, TypeConverterRouter typeConverterRouter) {
    this.objectMapper = objectMapperBuilder.build();
    this.jexlHelper = new JexlHelper(jexlEngine);
    this.properties = properties;
    this.typeConverterRouter = typeConverterRouter;
  }

  public String toJson(@NonNull ResponseWriteContext writeContext)
      throws JsonProcessingException, NoResultFoundException {
    Object response = mapDataToResponse(writeContext);
    if (Objects.isNull(response)) {
      throw noResultFoundException("Did not find data for your response.");
    }
    return toJson(response);
  }

  private String toJson(Object object) throws JsonProcessingException {
    return this.objectMapper.writer()
        .writeValueAsString(object);
  }

  private Object mapDataToResponse(@NonNull ResponseWriteContext writeContext) {
    switch (writeContext.getSchema()
        .getType()) {
      case ARRAY_TYPE:
        return mapArrayDataToResponse(writeContext);
      case OBJECT_TYPE:
        if (writeContext.getSchema()
            .isEnvelope()) {
          return mapEnvelopeObjectToResponse(writeContext);
        }
        return mapObjectDataToResponse(writeContext);
      default:
        return mapScalarDataToResponse(writeContext);
    }
  }

  @SuppressWarnings("unchecked")
  private Object mapArrayDataToResponse(ResponseWriteContext parentContext) {
    if (Objects.isNull(parentContext.getData())) {
      return Collections.emptyList();
    }

    if (parentContext.getData() instanceof List) {
      return ((List<Object>) parentContext.getData()).stream()
          .map(childData -> mapDataToResponse(createResponseContextFromChildData(parentContext, childData)))
          .collect(Collectors.toList());
    }
    return mapDataToResponse(unwrapItemSchema(parentContext));
  }

  private Object mapObjectDataToResponse(@NonNull ResponseWriteContext parentContext) {
    if (Objects.isNull(parentContext.getData())) {
      return null;
    }

    if (Objects.nonNull(parentContext.getSchema()
        .getDwsType())) {
      return parentContext.getData();
    }

    Map<String, Object> result = new HashMap<>();
    parentContext.getSchema()
        .getChildren()
        .forEach(childSchema -> {
          ResponseWriteContext writeContext = createResponseWriteContextFromChildSchema(parentContext, childSchema);
          Object object = mapObject(writeContext, mapDataToResponse(writeContext));
          if (Objects.nonNull(object)) {
            result.put(childSchema.getIdentifier(), convertType(writeContext, object));
          }
        });
    return result;
  }

  private Object mapScalarDataToResponse(@NonNull ResponseWriteContext writeContext) {
    if (Objects.isNull(writeContext.getSchema()
        .getDwsTemplate())) {
      return writeContext.getData();
    }

    Optional<String> evaluated = evaluateJexl(writeContext);
    if (evaluated.isPresent()) {
      return evaluated.get();
    }

    if (writeContext.isSchemaRequiredNonNillable()) {
      throw mappingException(String.format(
          "Could not create response: required and non-nillable property '%s' template evaluation returned null.",
          writeContext.getSchema()
              .getIdentifier()));
    }

    return null;
  }

  @SuppressWarnings("rawtypes")
  private Object mapEnvelopeObjectToResponse(ResponseWriteContext parentContext) {
    Map<String, Object> result = new HashMap<>();
    ResponseWriteContext writeContext = unwrapChildSchema(parentContext);
    Object object = mapDataToResponse(writeContext);
    result.put(writeContext.getSchema()
        .getIdentifier(), object);
    return result;
  }


  private Object convertType(ResponseWriteContext writeContext, Object item) {
    return Objects.nonNull(writeContext.getSchema()
        .getDwsType()) ? typeConverterRouter.convert(item, writeContext.getParameters()) : item;
  }


  private Object mapObject(ResponseWriteContext writeContext, Object object) {
    if (isRequiredAndNullOrEmpty(writeContext, object)) {
      if (writeContext.getSchema()
          .isNillable()) {
        return null;
      } else {
        throw mappingException(
            "Could not map GraphQL response: Required and non-nillable "
                + "property '{}' was not returned in GraphQL response.",
            writeContext.getSchema()
                .getIdentifier());
      }
    }
    return object;
  }

  @SuppressWarnings("unchecked")
  private Optional<String> evaluateJexl(ResponseWriteContext writeContext) {
    MapContext context = new MapContext();

    // add object data to context
    StringBuilder builder = new StringBuilder("fields.");
    writeContext.getDataStack()
        .forEach(data -> {
          ((Map<String, Object>) data).entrySet()
              .stream()
              .filter(entry -> !(entry.getValue() instanceof Map))
              .forEach(entry -> context.set(builder.toString() + entry.getKey(), entry.getValue()));

          builder.append("_parent.");
        });

    // add properties data to context
    this.properties.getAllProperties()
        .forEach((key, value) -> context.set("env." + key, value));

    return jexlHelper.evaluateScript(writeContext.getSchema()
        .getDwsTemplate(), context, String.class);
  }
}

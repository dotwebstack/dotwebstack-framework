package org.dotwebstack.framework.service.openapi.mapping;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.mappingException;
import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.noResultFoundException;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.ARRAY_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.OBJECT_TYPE;
import static org.dotwebstack.framework.service.openapi.response.ResponseWriteContextHelper.unwrapData;
import static org.dotwebstack.framework.service.openapi.response.ResponseWriteContextHelper.unwrapSchema;
import static org.dotwebstack.framework.service.openapi.response.ResponseWriteContextHelper.unwrapSchemaAndData;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
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
import org.dotwebstack.framework.service.openapi.response.ResponseObject;
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
    Object response = mapDataToResponse(writeContext, new ArrayList<>());
    if (Objects.isNull(response)) {
      throw noResultFoundException("Did not find data for your response.");
    }
    return toJson(response);
  }

  private String toJson(Object object) throws JsonProcessingException {
    return this.objectMapper.writer()
        .writeValueAsString(object);
  }

  private Object mapDataToResponse(@NonNull ResponseWriteContext writeContext, List<Object> dataStack) {
    switch (writeContext.getSchema()
        .getType()) {
      case ARRAY_TYPE:
        return mapArrayDataToResponse(writeContext, dataStack);
      case OBJECT_TYPE:
        return mapObjectDataToResponse(writeContext, dataStack);
      default:
        return mapScalarDataToResponse(writeContext, dataStack);
    }
  }

  private Object mapScalarDataToResponse(@NonNull ResponseWriteContext writeContext, List<Object> dataStack) {
    if (Objects.nonNull(writeContext.getSchema()
        .getDwsTemplate())) {
      Optional<String> evaluated = evaluateJexl(writeContext.getSchema()
          .getDwsTemplate(), dataStack);
      if (!evaluated.isPresent() && writeContext.isSchemaRequiredNonNillable()) {
        throw new MappingException(String.format(
            "Could not create response: required and non-nillable property '%s' template evaluation returned null.",
            writeContext.getSchema()
                .getIdentifier()));
      } else if (evaluated.isPresent()) {
        return evaluated.get();
      }
      return null;
    }
    return writeContext.getData();
  }

  @SuppressWarnings("unchecked")
  private Object mapObjectDataToResponse(@NonNull ResponseWriteContext parentContext, List<Object> dataStack) {
    if (Objects.isNull(parentContext.getData())) {
      return null;
    }

    Map<String, Object> result = new HashMap<>();
    parentContext.getSchema()
        .getChildren()
        .forEach(child -> {
          ResponseWriteContext writeContext = ResponseWriteContext.builder()
              .schema(child)
              .data(parentContext.getData())
              .parameters(parentContext.getParameters())
              .build();

          Object object;
          if (child.isEnvelope()) {
            object = mapEnvelopeObject(writeContext, dataStack);
            if (Objects.nonNull(object)) {
              result.put(child.getIdentifier(), convertType(writeContext, object));
            }
          } else {
            if (writeContext.getData() instanceof Map) {
              dataStack.add(0, writeContext.getData());
              object = mapObject(writeContext, dataStack);
              if (!(Objects.isNull(object))) {
                result.put(child.getIdentifier(), convertType(writeContext, object));
              }
              dataStack.remove(0);
            } else if (writeContext.getData() instanceof List) {
              ((List) writeContext.getData()).stream()
                  .forEach(item -> {
                    ResponseWriteContext itemContext = ResponseWriteContext.builder()
                        .schema(writeContext.getSchema())
                        .data(item)
                        .parameters(writeContext.getParameters())
                        .build();

                    dataStack.add(0, item);
                    Object itemObject = mapObject(itemContext, dataStack);
                    if (!(Objects.isNull(itemObject))) {
                      result.put(child.getIdentifier(), convertType(itemContext, itemObject));
                    }
                    dataStack.remove(0);
                  });
            } else {
              throw invalidConfigurationException("Cannot map {} to response, it is of the wrong type",
                  child.getIdentifier());
            }
          }
        });
    return result;
  }

  private Object convertType(ResponseWriteContext writeContext, Object item) {
    return Objects.nonNull(writeContext.getSchema()
        .getDwsType()) ? typeConverterRouter.convert(item, writeContext.getParameters()) : item;
  }

  @SuppressWarnings("unchecked")
  private Object mapArrayDataToResponse(@NonNull ResponseWriteContext writeContext, List<Object> dataStack) {
    if (Objects.isNull(writeContext.getData())) {
      return Collections.emptyList();
    }

    ResponseObject childResponseObject = writeContext.getSchema()
        .getItems()
        .get(0);

    return ((List<Object>) writeContext.getData()).stream()
        .map(childData -> {
          ResponseWriteContext childWriteContext = ResponseWriteContext.builder()
              .schema(childResponseObject)
              .data(childData)
              .parameters(writeContext.getParameters())
              .build();
          return mapDataToResponse(childWriteContext, dataStack);
        })
        .collect(Collectors.toList());
  }

  private Object mapObject(ResponseWriteContext parentContext, List<Object> dataStack) {
    ResponseWriteContext writeContext = unwrapData(parentContext);

    Object object = mapDataToResponse(writeContext, dataStack);
    if (isRequiredAndNullOrEmpty(writeContext, object)) {
      if (writeContext.getSchema()
          .isNillable()) {
        return null;
      }
      throw mappingException(
          "Could not map GraphQL response: Required and non-nillable "
              + "property '{}' was not returned in GraphQL response.",
          writeContext.getSchema()
              .getIdentifier());
    }

    return object;
  }

  @SuppressWarnings("unchecked")
  private Optional<String> evaluateJexl(String dwsTemplate, List<Object> dataStack) {
    MapContext context = new MapContext();

    // add object data to context
    StringBuilder builder = new StringBuilder("fields.");
    dataStack.forEach(data -> {
      ((Map<String, Object>) data).entrySet()
          .stream()
          .filter(entry -> !(entry.getValue() instanceof Map))
          .forEach(entry -> context.set(builder.toString() + entry.getKey(), entry.getValue()));

      builder.append("_parent.");
    });

    // add properties data to context
    this.properties.getAllProperties()
        .forEach((key, value) -> context.set("env." + key, value));

    return jexlHelper.evaluateExpression(dwsTemplate, context, String.class);
  }

  private boolean isRequiredAndNullOrEmpty(ResponseWriteContext writeContext, Object object) {
    return writeContext.getSchema()
        .isRequired() && ((Objects.isNull(object)) || isEmptyList(writeContext.getSchema(), object));
  }

  private boolean isEmptyList(ResponseObject responseObject, Object object) {
    if (responseObject.isNillable() && object instanceof List) {
      return ((List) object).isEmpty();
    }
    return false;
  }

  private boolean isFilledList(Object object) {
    if (object instanceof List) {
      return !((List) object).isEmpty();
    }
    return false;
  }

  @SuppressWarnings("rawtypes")
  private Object mapEnvelopeObject(ResponseWriteContext parentContext, List<Object> dataStack) {
    if (Objects.nonNull(parentContext.getData())) {
      if (isFilledList(parentContext.getData())) {
        return mapDataToResponse(unwrapSchema(parentContext), dataStack);
      }

      ResponseObject embedded = parentContext.getSchema()
          .getChildren()
          .get(0);
      if (parentContext.getData() instanceof Map
          && ((Map) parentContext.getData()).containsKey(embedded.getIdentifier())) {
        List childData = (List) ((Map) parentContext.getData()).get(embedded.getIdentifier());
        if (!childData.isEmpty()) {
          return mapDataToResponse(unwrapSchemaAndData(parentContext), dataStack);
        }
      }
    }
    throw invalidConfigurationException("Unable to map envelope object '{}'", parentContext.getSchema()
        .getIdentifier());
  }
}

package org.dotwebstack.framework.service.openapi.mapping;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.mappingException;
import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.noResultFoundException;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.ARRAY_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.OBJECT_TYPE;

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
import org.dotwebstack.framework.service.openapi.exception.NoResultFoundException;
import org.dotwebstack.framework.service.openapi.response.ResponseObject;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;

@Component
public class ResponseMapper {

  private final ObjectMapper objectMapper;

  private final JexlHelper jexlHelper;

  private final EnvironmentProperties properties;

  public ResponseMapper(Jackson2ObjectMapperBuilder objectMapperBuilder, JexlEngine jexlEngine,
      EnvironmentProperties properties) {
    this.objectMapper = objectMapperBuilder.build();
    this.jexlHelper = new JexlHelper(jexlEngine);
    this.properties = properties;
  }

  public String toJson(@NonNull ResponseObject responseObject, Object data)
      throws JsonProcessingException, NoResultFoundException {
    Object response = mapDataToResponse(responseObject, data, new ArrayList<>());
    if (Objects.isNull(response)) {
      throw noResultFoundException("Did not find data for your response.");
    }
    return toJson(response);
  }

  public String toJson(Object object) throws JsonProcessingException {
    return this.objectMapper.writer()
        .writeValueAsString(object);
  }

  private Object mapDataToResponse(@NonNull ResponseObject responseObject, Object data, List<Object> dataStack) {
    switch (responseObject.getType()) {
      case ARRAY_TYPE:
        return mapArrayDataToResponse(responseObject, data, dataStack);
      case OBJECT_TYPE:
        return mapObjectDataToResponse(responseObject, data, dataStack);
      default:
        return mapScalarDataToResponse(responseObject, data, dataStack);
    }
  }

  private Object mapScalarDataToResponse(@NonNull ResponseObject responseObject, Object data, List<Object> dataStack) {
    if (!Objects.isNull(responseObject.getDwsTemplate())) {
      Optional<String> evaluated = evaluateJexl(responseObject.getDwsTemplate(), dataStack);
      if (!evaluated.isPresent() && responseObject.isRequired() && !responseObject.isNillable()) {
        throw new MappingException(String.format(
            "Could not create response: required and non-nillable property '%s' template evaluation returned null.",
            responseObject.getIdentifier()));
      } else if (evaluated.isPresent()) {
        return evaluated.get();
      }
      return null;
    }
    return data;
  }

  @SuppressWarnings("unchecked")
  private Object mapObjectDataToResponse(@NonNull ResponseObject responseObject, Object data, List<Object> dataStack) {
    if (Objects.isNull(data)) {
      return null;
    }

    Map<String, Object> result = new HashMap<>();
    responseObject.getChildren()
        .forEach(child -> {
          Object object;
          if (child.isEnvelope()) {
            object = mapEnvelopeObject(data, child, dataStack);
            if (!Objects.isNull(object)) {
              result.put(child.getIdentifier(), object);
            }
          } else {
            if (data instanceof Map) {
              dataStack.add(0, data);
              object = mapObject((Map<String, Object>) data, child, dataStack);
              if (!(Objects.isNull(object))) {
                result.put(child.getIdentifier(), object);
              }
              dataStack.remove(0);
            } else if (data instanceof List) {
              ((List) data).stream()
                  .forEach(item -> {
                    dataStack.add(0, item);
                    Object itemObject = mapObject((Map<String, Object>) item, child, dataStack);
                    if (!(Objects.isNull(itemObject))) {
                      result.put(child.getIdentifier(), itemObject);
                    }
                    dataStack.remove(0);
                  });
            } else {
              throw invalidConfigurationException("");
            }
          }
        });
    return result;
  }

  @SuppressWarnings("unchecked")
  private Object mapArrayDataToResponse(@NonNull ResponseObject responseObject, Object data, List<Object> dataStack) {
    if (Objects.isNull(data)) {
      return Collections.emptyList();
    }

    ResponseObject childResponseObject = responseObject.getItems()
        .get(0);

    return ((List<Object>) data).stream()
        .map(object -> mapDataToResponse(childResponseObject, object, dataStack))
        .collect(Collectors.toList());
  }

  private Object mapObject(Map<String, Object> data, ResponseObject child, List<Object> dataStack) {
    Object object = mapDataToResponse(child, data.get(child.getIdentifier()), dataStack);
    if ((child.isRequired() && ((Objects.isNull(object)) || (child.isNillable() && isEmptyList(object))))) {
      if (child.isNillable()) {
        return null;
      } else {
        throw mappingException("Could not map GraphQL response: Required and non-nillable "
            + "property '{}' was not returned in GraphQL response.", child.getIdentifier());
      }
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

  private boolean isEmptyList(Object object) {
    if (object instanceof List) {
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
  private Object mapEnvelopeObject(Object data, ResponseObject child, List<Object> dataStack) {
    ResponseObject embedded = child.getChildren()
        .get(0);

    if (Objects.nonNull(data)) {
      if (isFilledList(data)) {
        return mapDataToResponse(embedded, data, dataStack);
      }

      if (data instanceof Map && ((Map) data).containsKey(embedded.getIdentifier())) {
        List childData = (List) ((Map) data).get(embedded.getIdentifier());
        if (!childData.isEmpty()) {
          return mapDataToResponse(embedded, childData, dataStack);
        }
      }
    }
    return null;
  }
}

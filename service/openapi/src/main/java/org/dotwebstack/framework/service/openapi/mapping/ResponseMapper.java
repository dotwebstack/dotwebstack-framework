package org.dotwebstack.framework.service.openapi.mapping;

import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.noResultFoundException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

  @SuppressWarnings("unchecked")
  private Object mapDataToResponse(@NonNull ResponseObject responseObject, Object data, List<Object> dataStack) {
    switch (responseObject.getType()) {
      case "array":
        if (data == null) {
          return Collections.emptyList();
        }
        ResponseObject childResponseObject = responseObject.getItems()
            .get(0);
        return ((List<Object>) data).stream()
            .map(object -> mapDataToResponse(childResponseObject, object, dataStack))
            .collect(Collectors.toList());
      case "object":
        if (data == null) {
          return null;
        }
        Map<String, Object> result = new HashMap<>();

        responseObject.getChildren()
            .forEach(child -> {
              dataStack.add(0, data);
              Object object =
                  mapDataToResponse(child, ((Map<String, Object>) data).get(child.getIdentifier()), dataStack);
              dataStack.remove(0);
              if (!child.isRequired() && object == null) {
                // property is not required and not returned: don't add to response.
              } else if (child.isRequired() && child.isNillable() && object == null) {
                result.put(child.getIdentifier(), null);
              } else if (child.isRequired() && !child.isNillable() && object == null) {
                throw new MappingException(String.format("Could not map GraphQL response: Required and non-nillable "
                    + "property '%s' was not return in GraphQL response.", child.getIdentifier()));
              } else {
                result.put(child.getIdentifier(), object);
              }
            });
        return result;
      default:
        if (!Objects.isNull(responseObject.getDwsTemplate())) {
          return executeJexl(responseObject.getDwsTemplate(), dataStack);
        } else {
          return data;
        }
    }
  }

  @SuppressWarnings("unchecked")
  protected Object executeJexl(String dwsTemplate, List<Object> dataStack) {

    MapContext context = new MapContext();

    // add object data to context
    String prefix = "fields.";
    for (Object data : dataStack) {
      String finalPrefix = prefix;
      ((Map<String, Object>) data).entrySet()
          .stream()
          .filter(e -> !(e.getValue() instanceof Map))
          .forEach(e -> context.set(finalPrefix + e.getKey(), e.getValue()));

      prefix += "_parent.";
    }

    // add properties data to context
    this.properties.getAllProperties()
        .entrySet()
        .forEach(e -> context.set("env." + e.getKey(), e.getValue()));

    return jexlHelper.evaluateExpression(dwsTemplate, context, String.class)
        .get();
  }
}

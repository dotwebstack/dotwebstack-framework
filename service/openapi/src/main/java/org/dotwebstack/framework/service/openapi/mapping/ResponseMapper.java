package org.dotwebstack.framework.service.openapi.mapping;

import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.mappingException;
import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.noResultFoundException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.dotwebstack.framework.service.openapi.exception.NoResultFoundException;
import org.dotwebstack.framework.service.openapi.response.ResponseObject;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;

@Component
public class ResponseMapper {

  private final ObjectMapper objectMapper;

  public ResponseMapper(Jackson2ObjectMapperBuilder objectMapperBuilder) {
    this.objectMapper = objectMapperBuilder.build();
  }

  public String toJson(@NonNull ResponseObject responseObject, Object data)
      throws JsonProcessingException, NoResultFoundException {
    Object response = mapDataToResponse(responseObject, data);
    if (Objects.isNull(response)) {
      throw noResultFoundException("Did not find data for your response");
    }
    return toJson(response);
  }

  public String toJson(Object object) throws JsonProcessingException {
    return this.objectMapper.writer()
        .writeValueAsString(object);
  }

  @SuppressWarnings("unchecked")
  private Object mapDataToResponse(@NonNull ResponseObject responseObject, Object data) {
    switch (responseObject.getType()) {
      case "array":
        if (Objects.isNull(data)) {
          return Collections.emptyList();
        }

        ResponseObject childResponseObject = responseObject.getItems()
            .get(0);

        return ((List<Object>) data).stream()
            .map(object -> mapDataToResponse(childResponseObject, object))
            .collect(Collectors.toList());
      case "object":
        if (Objects.isNull(data)) {
          return null;
        }

        Map<String, Object> result = new HashMap<>();
        responseObject.getChildren()
            .forEach(child -> {
              Object object;
              if (child.isEnvelope()) {
                object = mapEnvelopeObject(data, child);
                if (!Objects.isNull(object)) {
                  result.put(child.getIdentifier(), object);
                }
              } else {
                object = mapObject((Map<String, Object>) data, child);
                if (!(Objects.isNull(object) && child.isNillable())) {
                  result.put(child.getIdentifier(), object);
                }
              }
            });
        return result;
      default:
        return data;
    }
  }

  private Object mapObject(Map<String, Object> data, ResponseObject child) {
    Object object = mapDataToResponse(child, data.get(child.getIdentifier()));
    if (child.isRequired() && Objects.isNull(object)) {
      if (child.isNillable()) {
        return null;
      } else {
        throw mappingException("Could not map GraphQL response: Required and non-nillable "
            + "property '{}}' was not return in GraphQL response.", child.getIdentifier());
      }
    }

    return object;
  }

  @SuppressWarnings("rawtypes")
  private Object mapEnvelopeObject(Object data, ResponseObject child) {
    ResponseObject embedded = child.getChildren()
        .get(0);

    if (Objects.nonNull(data)) {
      if (data instanceof List && !((List) data).isEmpty()) {
        return mapDataToResponse(embedded, data);
      }

      if (data instanceof Map && ((Map) data).containsKey(embedded.getIdentifier())) {
        List childData = (List) ((Map) data).get(embedded.getIdentifier());
        if (!childData.isEmpty()) {
          return mapDataToResponse(embedded, childData);
        }
      }
    }
    return null;
  }
}

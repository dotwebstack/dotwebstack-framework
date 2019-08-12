package org.dotwebstack.framework.service.openapi.mapping;

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
        if (data == null) {
          return Collections.emptyList();
        }
        ResponseObject childResponseObject = responseObject.getItems()
            .get(0);
        return ((List<Object>) data).stream()
            .map(object -> mapDataToResponse(childResponseObject, object))
            .collect(Collectors.toList());
      case "object":
        if (data == null) {
          return null;
        }
        Map<String, Object> result = new HashMap<>();

        responseObject.getChildren()
            .forEach(child -> {
              Object object = mapDataToResponse(child, ((Map<String, Object>) data).get(child.getIdentifier()));
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
        return data;
    }
  }
}

package org.dotwebstack.framework.service.openapi.jexl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dotwebstack.framework.core.jexl.JexlFunction;
import org.springframework.stereotype.Component;

@Component
public class JsonFunctions implements JexlFunction {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Override
  public String getNamespace() {
    return "json";
  }

  @SuppressWarnings("unused")
  public String asString(Object object) {
    if (object == null) {
      return null;
    }
    try {
      return OBJECT_MAPPER.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException(String.format("Object %s could not be converted to json string", object), e);
    }
  }
}

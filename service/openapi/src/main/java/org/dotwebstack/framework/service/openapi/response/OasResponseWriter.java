package org.dotwebstack.framework.service.openapi.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;
import lombok.ToString;

import java.util.List;

public class OasResponseWriter {
  public static String toString(OasResponse response){
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
      return objectMapper.writeValueAsString(response.getRoot());
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return null;
    }
  }
}


package org.dotwebstack.framework.frontend.http.jackson;

import com.bedatadriven.jackson.datatype.jts.JtsModule3D;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import javax.ws.rs.ext.ContextResolver;

public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {

  private ObjectMapper objectMapper;

  public ObjectMapperProvider() {
    objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    objectMapper.registerModule(new GuavaModule());
    objectMapper.registerModule(new JtsModule3D());
  }

  @Override
  public ObjectMapper getContext(Class<?> type) {
    return objectMapper;
  }

}

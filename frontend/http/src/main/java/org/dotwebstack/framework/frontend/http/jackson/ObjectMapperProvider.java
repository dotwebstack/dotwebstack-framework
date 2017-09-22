package org.dotwebstack.framework.frontend.http.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import javax.ws.rs.ext.ContextResolver;
import org.eclipse.rdf4j.query.TupleQueryResult;

public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {

  private ObjectMapper objectMapper;

  public ObjectMapperProvider() {
    objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    objectMapper.registerModule(new GuavaModule());
    registerSerializers();
  }

  @Override
  public ObjectMapper getContext(Class<?> type) {
    return objectMapper;
  }

  private void registerSerializers() {
    SimpleModule module = new SimpleModule();
    module.addSerializer(TupleQueryResult.class, new TupleQueryResultSerializer());
    objectMapper.registerModule(module);
  }

}

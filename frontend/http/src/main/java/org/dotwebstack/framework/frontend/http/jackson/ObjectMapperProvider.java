package org.dotwebstack.framework.frontend.http.jackson;

import java.io.IOException;
import java.util.Optional;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import javax.ws.rs.ext.ContextResolver;

public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {

  private ObjectMapper objectMapper;

  public ObjectMapperProvider() {
    objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    objectMapper.registerModule(new GuavaModule());
    objectMapper.registerModule(
            new SimpleModule().addSerializer(new StdSerializer<Optional<?>>(Optional.class, false) {

              private static final long serialVersionUID = 897887164159829640L;

              @Override
              public void serialize(Optional<?> value, JsonGenerator gen,
                                    SerializerProvider provider) throws IOException {
                if (value.isPresent()) {
                  provider.defaultSerializeValue(value.get(), gen);
                } else {
                  provider.defaultSerializeValue(null, gen);
                }
              }
            }));
  }

  @Override
  public ObjectMapper getContext(Class<?> type) {
    return objectMapper;
  }

}

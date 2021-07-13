package org.dotwebstack.framework.core.graphql.client;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import graphql.ExecutionResult;
import graphql.ExecutionResultImpl;
import graphql.GraphQLError;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ExecutionResultDeserializer extends StdDeserializer<ExecutionResult> {

  private static final long serialVersionUID = 1L;

  public ExecutionResultDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public ExecutionResult deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
    JsonToken currentToken;
    Object data = null;
    List<? extends GraphQLError> errors = List.of();
    Map<Object, Object> extensions = null;

    while ((currentToken = jp.nextValue()) != null) {
      switch (currentToken) {
        case START_OBJECT:
          switch (jp.getCurrentName()) {
            case "data":
              data = jp.readValueAs(Map.class);
              break;
            case "errors":
              break;
            default:
              break;
          }
          break;
        default:
          break;
      }
    }
    return new ExecutionResultImpl(data, errors, extensions);
  }
}

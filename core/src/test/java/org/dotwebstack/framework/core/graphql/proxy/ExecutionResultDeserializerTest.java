package org.dotwebstack.framework.core.graphql.proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import graphql.ExecutionResult;
import graphql.ExecutionResultImpl;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.language.SourceLocation;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ExecutionResultDeserializerTest {

  private static ObjectMapper OBJECT_MAPPER;

  @BeforeAll
  static void init() {
    OBJECT_MAPPER = new ObjectMapper();
    SimpleModule sm = new SimpleModule("Graphql");
    sm.addDeserializer(ExecutionResult.class, new ExecutionResultDeserializer(ExecutionResult.class));
    OBJECT_MAPPER.registerModule(sm);
  }

  @Test
  void deserialize_returnsExpected_forSuccessResponse() throws JsonProcessingException {
    Object data = Map.of("key", "value", "key2", List.of("1", "2"));
    ExecutionResult result = new ExecutionResultImpl(data, null, null);
    String json = OBJECT_MAPPER.writeValueAsString(result);

    ExecutionResult readback = OBJECT_MAPPER.readValue(json, ExecutionResult.class);

    assertEquals(result.toSpecification(), readback.toSpecification());
  }

  @Test
  void deserialize_returnsExpected_forErrorResponse() throws JsonProcessingException {
    SourceLocation location1 = new SourceLocation(1, 2);
    List<SourceLocation> locations = List.of(location1);
    GraphQLError error1 = GraphqlErrorBuilder.newError()
        .message("message")
        .locations(locations)
        .extensions(Map.of("k1", "v1"))
        .build();
    List<? extends GraphQLError> errors = List.of(error1);
    ExecutionResult result = new ExecutionResultImpl(null, errors, null);
    String json = OBJECT_MAPPER.writeValueAsString(result);

    ExecutionResult readback = OBJECT_MAPPER.readValue(json, ExecutionResult.class);

    assertEquals(result.toSpecification(), readback.toSpecification());
  }

}

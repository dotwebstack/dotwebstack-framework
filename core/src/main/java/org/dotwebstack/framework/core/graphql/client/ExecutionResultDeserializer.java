package org.dotwebstack.framework.core.graphql.client;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import graphql.ExecutionResult;
import graphql.ExecutionResultImpl;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.language.SourceLocation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.dotwebstack.framework.core.InternalServerErrorException;

public class ExecutionResultDeserializer extends StdDeserializer<ExecutionResult> {

  private static final long serialVersionUID = 1L;

  public ExecutionResultDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public ExecutionResult deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
    Object data = null;
    List<? extends GraphQLError> errors = List.of();

    while ((jsonParser.nextValue()) != null) {
      String name = jsonParser.getCurrentName();
      if (name != null) {
        switch (name) {
          case "data":
            data = jsonParser.readValueAs(Map.class);
            break;
          case "errors":
            errors = deserializeArray(jsonParser, new ErrorDeserializer());
            break;
          default:
            break;
        }
      }
    }
    return new ExecutionResultImpl(data, errors, null);
  }

  private static class ErrorDeserializer implements Function<JsonParser, GraphQLError> {

    @SuppressWarnings("unchecked")
    @Override
    public GraphQLError apply(JsonParser jsonParser) {
      GraphqlErrorBuilder builder = GraphqlErrorBuilder.newError();
      try {
        while ((jsonParser.nextValue()) != null) {
          String name = jsonParser.getCurrentName();
          if (name != null) {
            switch (name) {
              case "message":
                builder.message(jsonParser.getText());
                break;
              case "locations":
                builder.locations(deserializeArray(jsonParser, new LocationDeserializer()));
                break;
              case "extensions":
                builder.extensions(jsonParser.readValueAs(Map.class));
                break;
              default:
                break;
            }
          }
        }
        return builder.build();
      } catch (IOException e) {
        throw new InternalServerErrorException("Could not deserialize error", e);
      }
    }

    private static class LocationDeserializer implements Function<JsonParser, SourceLocation> {
      @Override
      public SourceLocation apply(JsonParser jsonParser) {
        try {
          int line = 0;
          int column = 0;
          String sourceName = null;

          while ((jsonParser.nextValue()) != null) {
            String name = jsonParser.getCurrentName();
            if (name != null) {
              switch (name) {
                case "line":
                  line = jsonParser.getIntValue();
                  break;
                case "column":
                  column = jsonParser.getIntValue();
                  break;
                case "sourceName":
                  sourceName = jsonParser.getText();
                  break;
                default:
                  break;
              }
            }
          }
          return new SourceLocation(line, column, sourceName);
        } catch (IOException e) {
          throw new InternalServerErrorException("Could not deserialize error.locations", e);
        }
      }
    }
  }

  private static <T> List<T> deserializeArray(JsonParser jp, Function<JsonParser, T> converter) throws IOException {
    List<T> result = new ArrayList<>();
    JsonToken currentToken;
    while ((currentToken = jp.nextValue()) != null) {
      if (currentToken == JsonToken.START_OBJECT) {
        result.add(converter.apply(jp));
      } else if (currentToken == JsonToken.END_ARRAY) {
        break;
      }
    }
    return result;
  }
}

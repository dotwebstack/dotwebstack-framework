package org.dotwebstack.framework.backend.json.query;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.language.Field;
import graphql.schema.DataFetchingEnvironment;
import java.util.ArrayList;
import org.dotwebstack.framework.backend.json.TestHelper;
import org.dotwebstack.framework.backend.json.converters.JsonConverterRouter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class JsonValueFetcherTest {

  @Mock
  private DataFetchingEnvironment environment;

  private ObjectMapper objectMapper = new ObjectMapper();

  private JsonConverterRouter jsonConverterRouter = new JsonConverterRouter();

  private JsonValueFetcher jsonValueFetcher;

  @BeforeEach
  void setup() throws JsonProcessingException {
    jsonValueFetcher = new JsonValueFetcher(jsonConverterRouter);

    JsonNode jsonNode = objectMapper.readTree(TestHelper.getSingleBrewery());
    JsonSolution jsonSolution = new JsonSolution(jsonNode);

    when(environment.getSource()).thenReturn(jsonSolution);
  }

  @Test
  void getIdentifierFromBreweryTest() throws Exception {
    // Arrange
    when(environment.getField()).thenReturn(Field.newField()
        .name("identifier")
        .build());

    // Act
    Object result = jsonValueFetcher.get(environment);

    // Assert
    assertThat(result.toString(), is(equalTo("1")));
  }

  @Test
  void getOwnersFromBreweryTest() throws Exception {
    // Arrange
    when(environment.getField()).thenReturn(Field.newField()
        .name("owners")
        .build());

    // Act
    ArrayList<?> result = (ArrayList<?>) jsonValueFetcher.get(environment);

    // Assert
    assertThat(result.size(), is(4));
  }

  @Test
  void returnNullWhenFieldIsNotFoundTest() throws Exception {
    // Arrange
    when(environment.getField()).thenReturn(Field.newField()
        .name("unknownField")
        .build());

    // Act
    Object result = jsonValueFetcher.get(environment);

    // Assert
    assertThat(result, is(equalTo(null)));
  }

  @Test
  void supportsJsonNodeTest() {
    // Act
    boolean supportsJsonNode = jsonValueFetcher.supports(environment);

    // Assert
    assertThat(supportsJsonNode, is(true));
  }

}

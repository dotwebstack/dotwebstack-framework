package org.dotwebstack.framework.backend.json.query;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.Scalars;
import graphql.language.Field;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import java.util.ArrayList;
import org.dotwebstack.framework.backend.json.converters.JsonConverterRouter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JsonValueFetcherTest {

  @Mock
  private DataFetchingEnvironment environmentMock;

  private final ObjectMapper objectMapper = new ObjectMapper();

  private final JsonConverterRouter jsonConverterRouter = new JsonConverterRouter();

  private JsonValueFetcher jsonValueFetcher;

  @BeforeEach
  void setup() throws JsonProcessingException {
    jsonValueFetcher = new JsonValueFetcher(jsonConverterRouter);

    JsonNode jsonNode = objectMapper.readTree(getSingleBreweryAsJson());
    JsonSolution jsonSolution = new JsonSolution(jsonNode);

    when(environmentMock.getSource()).thenReturn(jsonSolution);
  }

  @Test
  void getIdentifierFromBreweryTest() throws Exception {
    // Arrange
    when(environmentMock.getField()).thenReturn(Field.newField()
        .name("identifier")
        .build());

    mockFieldDefinition();

    // Act
    Object result = jsonValueFetcher.get(environmentMock);

    // Assert
    assertThat(result.toString(), is(equalTo("1")));
  }

  @Test
  void getOwnersFromBreweryTest() throws Exception {
    // Arrange
    when(environmentMock.getField()).thenReturn(Field.newField()
        .name("owners")
        .build());

    mockFieldDefinition();

    // Act
    ArrayList<?> result = (ArrayList<?>) jsonValueFetcher.get(environmentMock);

    // Assert
    assertThat(result.size(), is(4));
  }

  private void mockFieldDefinition() {
    GraphQLFieldDefinition fieldDefinition = mock(GraphQLFieldDefinition.class);
    when(fieldDefinition.getType()).thenReturn(Scalars.GraphQLString);

    when(environmentMock.getFieldDefinition()).thenReturn(fieldDefinition);
  }

  @Test
  void returnNullWhenFieldIsNotFoundTest() throws Exception {
    // Arrange
    when(environmentMock.getField()).thenReturn(Field.newField()
        .name("unknownField")
        .build());

    // Act
    Object result = jsonValueFetcher.get(environmentMock);

    // Assert
    assertThat(result, is(equalTo(null)));
  }

  @Test
  void supportsJsonNodeTest() {
    // Act
    boolean supportsJsonNode = jsonValueFetcher.supports(environmentMock);

    // Assert
    assertThat(supportsJsonNode, is(true));
  }

  private String getSingleBreweryAsJson() {
    return "{\n" + "    \"identifier\": 1,\n" + "    \"brewmasters\": \"Jeroen van Hees\",\n"
        + "    \"founded\": \"2014-05-03\",\n" + "    \"name\": \"De Brouwerij\",\n" + "    \"beerCount\": 3,\n"
        + "    \"group\": \"Onafhankelijk\",\n" + "    \"beers\": [\n" + "      {\n" + "        \"identifier\": 1,\n"
        + "        \"brewery\": 1,\n" + "        \"name\": \"Alfa Edel Pils\"\n" + "      },\n" + "      {\n"
        + "        \"identifier\": 2,\n" + "        \"brewery\": 1,\n" + "        \"name\": \"Alfa Radler\"\n"
        + "      }\n" + "    ],\n" + "    \"owners\": [\n" + "      \"J.v.Hees\",\n" + "      \"I.Verhoef\",\n"
        + "      \"L.du Clou\",\n" + "      \"M.Kuijpers\"\n" + "    ]\n" + "  }\n";
  }

}

package org.dotwebstack.framework.backend.json.query;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.dotwebstack.framework.backend.json.directives.JsonDirectives;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ResourceLoader;

@ExtendWith(MockitoExtension.class)
public class JsonQueryFetcherTest {

  @Mock
  private DataFetchingEnvironment environmentMock;

  @Mock
  private ResourceLoader resourceLoaderMock;

  @Mock
  private GraphQLFieldDefinition graphQlFieldDefinitionMock;

  @Mock
  private GraphQLDirective graphQlDirectiveMock;

  @Mock
  private GraphQLArgument graphQlFileArgumentMock;

  @Mock
  private GraphQLArgument graphQlPathArgumentMock;

  private JsonQueryFetcher jsonQueryFetcher;

  @BeforeEach
  void setup() {
    Map<String, Object> arguments = new HashMap<>();
    arguments.put("identifier", 1);

    when(graphQlDirectiveMock.getArgument(JsonDirectives.ARGS_FILE)).thenReturn(graphQlFileArgumentMock);
    when(graphQlDirectiveMock.getArgument(JsonDirectives.ARGS_PATH)).thenReturn(graphQlPathArgumentMock);

    when(graphQlFieldDefinitionMock.getDirective(JsonDirectives.JSON_NAME)).thenReturn(graphQlDirectiveMock);

    when(environmentMock.getArguments()).thenReturn(arguments);
    when(environmentMock.getFieldDefinition()).thenReturn(graphQlFieldDefinitionMock);

    JsonDataService jsonDataService = new JsonDataService(resourceLoaderMock);
    jsonQueryFetcher = new JsonQueryFetcher(jsonDataService);
  }

  @Test
  void getBeersReturnsArrayListTest() throws Exception {
    // Arrange
    GraphQLList graphQlObjectTypeMock = mock(GraphQLList.class);

    when(graphQlFileArgumentMock.getValue()).thenReturn("test.json");
    when(graphQlPathArgumentMock.getValue()).thenReturn("$.beers");
    when(environmentMock.getFieldType()).thenReturn(graphQlObjectTypeMock);

    // Act
    ArrayList<?> result = (ArrayList<?>) jsonQueryFetcher.get(environmentMock);

    // Assert
    assertThat(result.size(), equalTo(2));
  }

  @Test
  void getBeerByIdReturnsJsonSolutionTest() throws Exception {
    // Arrange
    GraphQLNonNull graphQlObjectTypeMock = mock(GraphQLNonNull.class);

    when(graphQlFileArgumentMock.getValue()).thenReturn("test.json");
    when(graphQlPathArgumentMock.getValue()).thenReturn("$.beers[?]");
    when(environmentMock.getFieldType()).thenReturn(graphQlObjectTypeMock);

    // Act
    JsonSolution result = (JsonSolution) jsonQueryFetcher.get(environmentMock);

    // Assert
    String beerResult = "{\"identifier\":1,\"brewery\":1,\"name\":\"Alfa Edel Pils\"}";
    assertThat(result.getJsonNode()
        .toString(), equalTo(beerResult));
  }
}

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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.backend.json.directives.JsonDirectives;
import org.dotwebstack.framework.backend.json.directives.PredicateDirectives;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ResourceLoader;

@ExtendWith(MockitoExtension.class)
class JsonQueryFetcherTest {

  @Mock
  private DataFetchingEnvironment environmentMock;

  @Mock
  private ResourceLoader resourceLoaderMock;

  @Mock
  private GraphQLFieldDefinition graphQlFieldDefinitionMock;

  @Mock
  private GraphQLDirective jsonGraphQlDirectiveMock;

  @Mock
  private GraphQLArgument graphQlFileArgumentMock;

  @Mock
  private GraphQLArgument graphQlPathArgumentMock;

  @Mock
  private GraphQLArgument graphQlExcludedArgumentMock;

  private JsonQueryFetcher jsonQueryFetcher;

  @BeforeEach
  void setup() {
    when(jsonGraphQlDirectiveMock.getArgument(JsonDirectives.ARGS_FILE)).thenReturn(graphQlFileArgumentMock);
    when(jsonGraphQlDirectiveMock.getArgument(JsonDirectives.ARGS_PATH)).thenReturn(graphQlPathArgumentMock);

    when(graphQlFieldDefinitionMock.getDirective(JsonDirectives.JSON_NAME)).thenReturn(jsonGraphQlDirectiveMock);

    when(environmentMock.getFieldDefinition()).thenReturn(graphQlFieldDefinitionMock);

    JsonDataService jsonDataService = new JsonDataService(resourceLoaderMock);
    jsonQueryFetcher = new JsonQueryFetcher(jsonDataService);
  }

  @Test
  void getBeersReturnsArrayListTest() throws Exception {
    // Arrange
    GraphQLList graphQlObjectTypeMock = mock(GraphQLList.class);

    when(graphQlFileArgumentMock.getValue()).thenReturn("test.json");
    when(graphQlPathArgumentMock.getValue()).thenReturn("$..beers");

    GraphQLArgument graphQlArgument = mock(GraphQLArgument.class);
    GraphQLDirective predicateDirective = mock(GraphQLDirective.class);
    GraphQLArgument propertyArgument = mock(GraphQLArgument.class);

    when(predicateDirective.getArgument(PredicateDirectives.ARGS_PROPERTY)).thenReturn(propertyArgument);
    when(graphQlArgument.getDirective(PredicateDirectives.PREDICATE_NAME)).thenReturn(predicateDirective);

    when(graphQlFieldDefinitionMock.getArguments()).thenReturn(Collections.singletonList(graphQlArgument));
    when(environmentMock.getFieldType()).thenReturn(graphQlObjectTypeMock);

    // Act
    ArrayList<?> result = (ArrayList<?>) jsonQueryFetcher.get(environmentMock);

    // Assert
    assertThat(result.size(), equalTo(2));
  }

  @Test
  void getBeerByIdReturnsJsonSolutionTest() throws Exception {
    // Arrange
    Map<String, Object> arguments = new HashMap<>();
    arguments.put("identifier", 1);

    when(environmentMock.getArguments()).thenReturn(arguments);
    when(graphQlFileArgumentMock.getValue()).thenReturn("test.json");
    when(graphQlPathArgumentMock.getValue()).thenReturn("$..beers[?]");

    GraphQLNonNull graphQlObjectTypeMock = mock(GraphQLNonNull.class);
    GraphQLArgument graphQlArgument = mock(GraphQLArgument.class);
    GraphQLDirective predicateDirective = mock(GraphQLDirective.class);
    GraphQLArgument propertyArgument = mock(GraphQLArgument.class);

    when(graphQlArgument.getName()).thenReturn("identifier");
    when(propertyArgument.getValue()).thenReturn("identifier");
    when(predicateDirective.getArgument(PredicateDirectives.ARGS_PROPERTY)).thenReturn(propertyArgument);
    when(graphQlArgument.getDirective(PredicateDirectives.PREDICATE_NAME)).thenReturn(predicateDirective);

    when(graphQlFieldDefinitionMock.getArguments()).thenReturn(Collections.singletonList(graphQlArgument));
    when(environmentMock.getFieldType()).thenReturn(graphQlObjectTypeMock);

    // Act
    JsonSolution result = (JsonSolution) jsonQueryFetcher.get(environmentMock);

    // Assert
    String beerResult = "{\"identifier\":1,\"brewery\":1,\"name\":\"Alfa Edel Pils\",\"_metadata\":\"metadata\"}";
    assertThat(result.getJsonNode()
        .toString(), equalTo(beerResult));
  }

  @Test
  void getBeerByIdReturnsJsonSolutionWithExcludedFieldsTest() throws Exception {
    // Arrange
    Map<String, Object> arguments = new HashMap<>();
    arguments.put("identifier", 1);

    when(jsonGraphQlDirectiveMock.getArgument(JsonDirectives.ARGS_EXCLUDE)).thenReturn(graphQlExcludedArgumentMock);

    when(environmentMock.getArguments()).thenReturn(arguments);
    when(graphQlFileArgumentMock.getValue()).thenReturn("test.json");
    when(graphQlPathArgumentMock.getValue()).thenReturn("$..beers[?]");
    when(graphQlExcludedArgumentMock.getValue()).thenReturn(List.of("_metadata"));

    GraphQLNonNull graphQlObjectTypeMock = mock(GraphQLNonNull.class);
    GraphQLArgument graphQlArgument = mock(GraphQLArgument.class);
    GraphQLDirective predicateDirective = mock(GraphQLDirective.class);
    GraphQLArgument propertyArgument = mock(GraphQLArgument.class);

    when(graphQlArgument.getName()).thenReturn("identifier");
    when(propertyArgument.getValue()).thenReturn("identifier");
    when(predicateDirective.getArgument(PredicateDirectives.ARGS_PROPERTY)).thenReturn(propertyArgument);
    when(graphQlArgument.getDirective(PredicateDirectives.PREDICATE_NAME)).thenReturn(predicateDirective);

    when(graphQlFieldDefinitionMock.getArguments()).thenReturn(Collections.singletonList(graphQlArgument));
    when(environmentMock.getFieldType()).thenReturn(graphQlObjectTypeMock);

    // Act
    JsonSolution result = (JsonSolution) jsonQueryFetcher.get(environmentMock);

    // Assert
    String beerResult = "{\"identifier\":1,\"brewery\":1,\"name\":\"Alfa Edel Pils\"}";
    assertThat(result.getJsonNode()
        .toString(), equalTo(beerResult));
  }
}

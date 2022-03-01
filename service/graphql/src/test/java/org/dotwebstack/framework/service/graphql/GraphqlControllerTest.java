package org.dotwebstack.framework.service.graphql;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.ExceptionWhileDataFetching;
import graphql.ExecutionInput;
import graphql.ExecutionResultImpl;
import graphql.GraphQL;
import graphql.GraphQLError;
import graphql.execution.ResultPath;
import graphql.execution.UnknownOperationException;
import graphql.language.SourceLocation;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.dotwebstack.framework.core.RequestValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class GraphqlControllerTest {

  @Mock
  GraphQL graphQL;

  GraphqlController graphqlController;

  @BeforeEach
  void init() {
    this.graphqlController = new GraphqlController(graphQL);
  }

  @Test
  void handleGet_shouldReturnMono_default() {
    var query = "{beers{identifier_beer name}}";

    ExecutionResultImpl executionResult = ExecutionResultImpl.newExecutionResult()
        .data(Map.of("beers",
            List.of(Map.of("identifier_beer", "b0e7cf18-e3ce-439b-a63e-034c8452f59c", "name", "Beer 1"),
                Map.of("identifier_beer", "1295f4c1-846b-440c-b302-80bbc1f9f3a9", "name", "Beer 2"))))
        .build();

    when(graphQL.executeAsync(any(ExecutionInput.class)))
        .thenReturn(CompletableFuture.completedFuture(executionResult));
    Mono<Map<String, Object>> result = graphqlController.handleGet(query, null, null);

    StepVerifier.create(result)
        .assertNext(resultMap -> {
          Map<String, Object> data = getNestedObject(resultMap, "data");
          assertThat(data.containsKey("beers"), is(true));

          List<Map<String, Object>> beers = getNestedObjectList(List.of(data.get("beers")));

          assertThat(beers.size(), is(2));
          assertThat(beers.get(0)
              .get("name"), is("Beer 1"));
          assertThat(beers.get(1)
              .get("name"), is("Beer 2"));
        })
        .verifyComplete();
  }

  @Test
  void handleGet_shouldThrowException_whenVariablesIsNotJsonMap() {
    var query = "{beers{identifier_beer name}}";

    Throwable throwable =
        assertThrows(IllegalArgumentException.class, () -> graphqlController.handleGet(query, null, ""));

    assertThat(throwable.getMessage(), is("Could not convert variables GET parameter: expected a JSON map"));
  }

  @Test
  void handleGet_shouldThrowException_whenOperationNameIsEmptyString() {
    var query =
        "query beerCollection{beers{identifier_beer name}} query breweryCollection{breweries{identifier_brewery name}}";

    Throwable throwable =
        assertThrows(IllegalArgumentException.class, () -> graphqlController.handleGet(query, "", null));

    assertThat(throwable.getMessage(), is("Must provide operation name if query contains multiple operations."));
  }

  @Test
  void handleGet_shouldThrowException_forMultipleOperationsNameAndOperationNotProvided() {
    var query =
        "query beerCollection{beers{identifier_beer name}} query breweryCollection{breweries{identifier_brewery name}}";

    var exception = new UnknownOperationException("Must provide operation name if query contains multiple operations.");
    when(graphQL.executeAsync(any(ExecutionInput.class))).thenThrow(exception);

    Throwable throwable =
        assertThrows(UnknownOperationException.class, () -> graphqlController.handleGet(query, null, null));

    assertThat(throwable.getMessage(), is("Must provide operation name if query contains multiple operations."));
  }

  @Test
  void handleGet_shouldReturnInternalServerError_whenNonDotWebStackRuntimeExceptionIsThrown() {
    var query = "{beers{identifier_beer name}}";

    var executionResult = mock(ExecutionResultImpl.class);
    when(graphQL.executeAsync(any(ExecutionInput.class)))
        .thenReturn(CompletableFuture.completedFuture(executionResult));

    Mono<Map<String, Object>> result = graphqlController.handleGet(query, null, null);

    var graphQlError = new ExceptionWhileDataFetching(ResultPath.fromList(List.of("beers")), new NullPointerException(),
        SourceLocation.EMPTY);
    List<GraphQLError> errors = List.of(graphQlError);
    when(executionResult.getErrors()).thenReturn(errors);

    StepVerifier.create(result)
        .expectErrorMessage("An internal server error has occurred!")
        .verify();
  }

  @Test
  void handleGet_shouldReturnProblem_whenDotWebStackRuntimeExceptionIsThrown() {
    var query = "{beers{identifier_beer name}}";

    var executionResult = mock(ExecutionResultImpl.class);
    when(graphQL.executeAsync(any(ExecutionInput.class)))
        .thenReturn(CompletableFuture.completedFuture(executionResult));

    Mono<Map<String, Object>> result = graphqlController.handleGet(query, null, null);


    var graphQlError = new ExceptionWhileDataFetching(ResultPath.fromList(List.of("beers")),
        new RequestValidationException("Bad request"), SourceLocation.EMPTY);
    List<GraphQLError> errors = List.of(graphQlError);
    when(executionResult.getErrors()).thenReturn(errors);

    StepVerifier.create(result)
        .expectErrorMessage("Bad request")
        .verify();
  }


  @Test
  void handlePost_shouldReturnMono_applicationJson() {
    var body = Map.of("query", "{beers{identifier_beer name}}", "variables", Map.of());

    ExecutionResultImpl executionResult = ExecutionResultImpl.newExecutionResult()
        .data(Map.of("beers",
            List.of(Map.of("identifier_beer", "b0e7cf18-e3ce-439b-a63e-034c8452f59c", "name", "Beer 1"),
                Map.of("identifier_beer", "1295f4c1-846b-440c-b302-80bbc1f9f3a9", "name", "Beer 2"))))
        .build();

    when(graphQL.executeAsync(any(ExecutionInput.class)))
        .thenReturn(CompletableFuture.completedFuture(executionResult));
    Mono<Map<String, Object>> result = graphqlController.handlePost(body);

    StepVerifier.create(result)
        .assertNext(resultMap -> {
          Map<String, Object> data = getNestedObject(resultMap, "data");
          assertThat(data.containsKey("beers"), is(true));

          List<Map<String, Object>> beers = getNestedObjectList(List.of(data.get("beers")));

          assertThat(beers.size(), is(2));
          assertThat(beers.get(0)
              .get("name"), is("Beer 1"));
          assertThat(beers.get(1)
              .get("name"), is("Beer 2"));
        })
        .verifyComplete();
  }

  @Test
  void handlePost_shouldReturnMono_applicationGraphql() {
    var query = "{beers{identifier_beer name}}";

    ExecutionResultImpl executionResult = ExecutionResultImpl.newExecutionResult()
        .data(Map.of("beers",
            List.of(Map.of("identifier_beer", "b0e7cf18-e3ce-439b-a63e-034c8452f59c", "name", "Beer 1"),
                Map.of("identifier_beer", "1295f4c1-846b-440c-b302-80bbc1f9f3a9", "name", "Beer 2"))))
        .build();

    when(graphQL.executeAsync(any(ExecutionInput.class)))
        .thenReturn(CompletableFuture.completedFuture(executionResult));
    Mono<Map<String, Object>> result = graphqlController.handlePost(query);

    StepVerifier.create(result)
        .assertNext(resultMap -> {
          Map<String, Object> data = getNestedObject(resultMap, "data");
          assertThat(data.containsKey("beers"), is(true));

          List<Map<String, Object>> beers = getNestedObjectList(List.of(data.get("beers")));

          assertThat(beers.size(), is(2));
          assertThat(beers.get(0)
              .get("name"), is("Beer 1"));
          assertThat(beers.get(1)
              .get("name"), is("Beer 2"));
        })
        .verifyComplete();
  }

  @Test
  void handlePost_shouldThrowException_whenQueryIsMissing() {
    var body = Map.of("operationName", "", "variables", Map.of());

    Throwable throwable = assertThrows(IllegalArgumentException.class, () -> graphqlController.handlePost(body));

    assertThat(throwable.getMessage(), is("Required parameter 'query' is not present."));
  }

  @Test
  void handlePost_shouldThrowException_whenQueryIsEmptyString() {
    var body = Map.of("query", "", "operationName", "", "variables", Map.of());

    Throwable throwable = assertThrows(IllegalArgumentException.class, () -> graphqlController.handlePost(body));

    assertThat(throwable.getMessage(), is("Required parameter 'query' can not be empty."));
  }

  @Test
  void handlePost_shouldThrowException_whenOperationNameIsEmptyString() {
    var body = Map.of("query",
        "query beerCollection{beers{identifier_beer name}} "
            + "query breweryCollection{breweries{identifier_brewery name}}",
        "operationName", "", "variables", Map.of());

    Throwable throwable = assertThrows(IllegalArgumentException.class, () -> graphqlController.handlePost(body));

    assertThat(throwable.getMessage(), is("Must provide operation name if query contains multiple operations."));
  }

  @Test
  void handlePost_shouldThrowException_whenOperationNameIsNotProvided() {
    var body = Map.of("query",
        "query beerCollection {beers{identifier_beer name}} query breweryCollection {breweries{identifier_brewery}}",
        "variables", Map.of());

    var exception = new UnknownOperationException("Must provide operation name if query contains multiple operations.");
    when(graphQL.executeAsync(any(ExecutionInput.class))).thenThrow(exception);

    Throwable throwable = assertThrows(UnknownOperationException.class, () -> graphqlController.handlePost(body));

    assertThat(throwable.getMessage(), is("Must provide operation name if query contains multiple operations."));
  }

  @Test
  void handlePost_shouldReturnInternalServerError_whenNonDotWebStackRuntimeExceptionIsThrown() {
    var body = Map.of("query", "{beers{identifier_beer name}}", "variables", Map.of());

    var executionResult = mock(ExecutionResultImpl.class);
    when(graphQL.executeAsync(any(ExecutionInput.class)))
        .thenReturn(CompletableFuture.completedFuture(executionResult));

    var result = graphqlController.handlePost(body);

    var graphQlError = new ExceptionWhileDataFetching(ResultPath.fromList(List.of("beers")), new NullPointerException(),
        SourceLocation.EMPTY);
    List<GraphQLError> errors = List.of(graphQlError);
    when(executionResult.getErrors()).thenReturn(errors);

    StepVerifier.create(result)
        .expectErrorMessage("An internal server error has occurred!")
        .verify();
  }

  @Test
  void handlePost_shouldReturnProblem_whenDotWebStackRuntimeExceptionIsThrown() {
    var body = Map.of("query", "{beers{identifier_beer name}}", "variables", Map.of());

    var executionResult = mock(ExecutionResultImpl.class);
    when(graphQL.executeAsync(any(ExecutionInput.class)))
        .thenReturn(CompletableFuture.completedFuture(executionResult));

    var result = graphqlController.handlePost(body);

    var graphQlError = new ExceptionWhileDataFetching(ResultPath.fromList(List.of("beers")),
        new RequestValidationException("Bad request"), SourceLocation.EMPTY);
    List<GraphQLError> errors = List.of(graphQlError);
    when(executionResult.getErrors()).thenReturn(errors);

    StepVerifier.create(result)
        .expectErrorMessage("Bad request")
        .verify();
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> getNestedObject(Map<String, Object> data, String name) {
    return (Map<String, Object>) data.get(name);
  }

  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> getNestedObjectList(List<Object> data) {
    return (List<Map<String, Object>>) data.get(0);
  }
}

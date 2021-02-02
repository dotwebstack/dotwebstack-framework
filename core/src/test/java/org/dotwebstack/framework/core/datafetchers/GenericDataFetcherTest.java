package org.dotwebstack.framework.core.datafetchers;

import static graphql.schema.DataFetchingEnvironmentImpl.newDataFetchingEnvironment;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import graphql.execution.ExecutionStepInfo;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;


@ExtendWith(MockitoExtension.class)
class GenericDataFetcherTest {

  @Mock
  private GraphQLFieldDefinition graphQlFieldDefinitionMock;

  @Mock
  private AbstractTypeConfiguration<?> typeConfiguration;

  @Mock
  private DotWebStackConfiguration dotWebStackConfiguration;

  @Mock
  private BackendDataLoader backendDataLoader;

  private GenericDataFetcher genericDataFetcher;

  @Test
  void get_loadSingle_returnsFuture() {
    // Arrange
    genericDataFetcher = new GenericDataFetcher(dotWebStackConfiguration, List.of(backendDataLoader));

    when(dotWebStackConfiguration.getTypeMapping()).thenReturn(Map.of("Brewery", typeConfiguration));
    when(backendDataLoader.supports(typeConfiguration)).thenReturn(true);
    when(backendDataLoader.loadSingle(any(), any())).thenReturn(Mono.empty());

    DataFetchingFieldSelectionSet dataFetchingFieldSelectionSet = mock(DataFetchingFieldSelectionSet.class);

    ExecutionStepInfo executionStepInfo = mock(ExecutionStepInfo.class);

    GraphQLOutputType outputType = GraphQLObjectType.newObject()
        .name("Brewery")
        .build();

    DataFetchingEnvironment dataFetchingEnvironment = newDataFetchingEnvironment().executionStepInfo(executionStepInfo)
        .fieldType(outputType)
        .selectionSet(dataFetchingFieldSelectionSet)
        .fieldDefinition(graphQlFieldDefinitionMock)
        .build();

    // Act
    genericDataFetcher.get(dataFetchingEnvironment);

    // Assert
    verify(backendDataLoader).loadSingle(isNull(), any(LoadEnvironment.class));
  }
}

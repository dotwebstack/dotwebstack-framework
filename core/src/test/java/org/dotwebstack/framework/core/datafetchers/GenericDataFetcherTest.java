package org.dotwebstack.framework.core.datafetchers;

import static graphql.language.FieldDefinition.newFieldDefinition;
import static graphql.language.OperationDefinition.Operation.QUERY;
import static graphql.language.OperationDefinition.Operation.SUBSCRIPTION;
import static graphql.language.OperationDefinition.newOperationDefinition;
import static graphql.language.TypeName.newTypeName;
import static graphql.schema.DataFetchingEnvironmentImpl.newDataFetchingEnvironment;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import graphql.Scalars;
import graphql.execution.DataFetcherResult;
import graphql.execution.ExecutionStepInfo;
import graphql.execution.ResultPath;
import graphql.language.FieldDefinition;
import graphql.language.OperationDefinition;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.SelectedField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.dataloader.DataLoaderRegistry;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
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

  @Mock
  private ExecutionStepInfo executionStepInfo;

  private GenericDataFetcher genericDataFetcher;

  @BeforeEach
  void doBeforeEach() {
    when(dotWebStackConfiguration.getTypeMapping()).thenReturn(Map.of("Brewery", typeConfiguration));
    when(graphQlFieldDefinitionMock.getName()).thenReturn("brewery");

    genericDataFetcher = new GenericDataFetcher(dotWebStackConfiguration, List.of(backendDataLoader));
  }

  @Test
  @SuppressWarnings("unchecked")
  void get_returnsFuture_forLoadSingleQueryOperation() throws Exception {
    Map<String, Object> data = Map.of("identifier", "id-1");

    when(backendDataLoader.loadSingle(any(), any())).thenReturn(Mono.just(data));
    when(backendDataLoader.supports(typeConfiguration)).thenReturn(true);

    GraphQLOutputType outputType = createBreweryType();

    DataFetchingEnvironment dataFetchingEnvironment = createDataFetchingEnvironment(outputType, QUERY);

    Object future = genericDataFetcher.get(dataFetchingEnvironment);

    assertThat(future, instanceOf(Future.class));

    DataFetcherResult<Map<String, Object>> result = (DataFetcherResult<Map<String, Object>>) ((Future<?>) future).get();

    assertThat(Optional.of(result)
        .map(DataFetcherResult::getData)
        .orElseThrow()
        .entrySet(), equalTo(data.entrySet()));

    verify(backendDataLoader).loadSingle(isNull(), any(LoadEnvironment.class));
  }

  @Test
  @SuppressWarnings("unchecked")
  void get_returnsFuture_ForLoadManyQueryOperation() throws Exception {
    List<Map<String, Object>> data = List.of(Map.of("identifier", "id-1"), Map.of("identifier", "id-2"));
    GraphQLOutputType outputType = GraphQLList.list(createBreweryType());
    DataFetchingEnvironment dataFetchingEnvironment = createDataFetchingEnvironment(outputType, QUERY);

    when(backendDataLoader.supports(typeConfiguration)).thenReturn(true);
    when(backendDataLoader.loadMany(any(), any())).thenReturn(Flux.fromIterable(data));

    Object future = genericDataFetcher.get(dataFetchingEnvironment);

    assertThat(future, instanceOf(Future.class));

    List<DataFetcherResult<Map<String, Object>>> result =
        (List<DataFetcherResult<Map<String, Object>>>) ((Future<?>) future).get();

    assertThat(result.stream()
        .map(DataFetcherResult::getData)
        .collect(Collectors.toList()), equalTo(data));

    verify(backendDataLoader).loadMany(isNull(), any(LoadEnvironment.class));
  }

  @Test
  void get_returnsFlux_forLoadManySubscriptionOperation() {
    GraphQLOutputType outputType = GraphQLList.list(createBreweryType());
    DataFetchingEnvironment dataFetchingEnvironment = createDataFetchingEnvironment(outputType, SUBSCRIPTION);

    when(backendDataLoader.supports(typeConfiguration)).thenReturn(true);
    when(backendDataLoader.loadMany(any(), any())).thenReturn(Flux.empty());

    Object result = genericDataFetcher.get(dataFetchingEnvironment);

    assertThat(result, instanceOf(Flux.class));

    verify(backendDataLoader).loadMany(isNull(), any(LoadEnvironment.class));
  }

  @Test
  @SuppressWarnings("unchecked")
  void get_returnsDatafetcherResult_ForEagerLoadedNestedQueryOperation() {
    Map<String, Object> data = Map.of("identifier", "id-1");
    GraphQLOutputType outputType = GraphQLList.list(createBreweryType());
    Map<String, Object> source = Map.of("brewery", data);
    DataFetchingEnvironment dataFetchingEnvironment = createDataFetchingEnvironment(outputType, QUERY, source);

    when(dataFetchingEnvironment.getExecutionStepInfo()
        .getFieldDefinition()).thenReturn(graphQlFieldDefinitionMock);

    DataFetcherResult<Map<String, Object>> result =
        (DataFetcherResult<Map<String, Object>>) genericDataFetcher.get(dataFetchingEnvironment);

    assertThat(result, notNullValue());
    assertThat(result.getData()
        .entrySet(), equalTo(data.entrySet()));
  }

  @Test
  void get_returnsDatafetcherResult_ForBatchLoadSingleQueryOperation() {
    GraphQLOutputType outputType = createBreweryType();

    when(backendDataLoader.supports(typeConfiguration)).thenReturn(true);
    when(executionStepInfo.getFieldDefinition()).thenReturn(graphQlFieldDefinitionMock);
    when(executionStepInfo.getPath()).thenReturn(ResultPath.parse("/my/brewery"));
    when(executionStepInfo.getUnwrappedNonNullType()).thenReturn(outputType);

    DataFetchingEnvironment dataFetchingEnvironment = createDataFetchingEnvironment(outputType, QUERY, Map.of());

    Future<?> future = (Future<?>) genericDataFetcher.get(dataFetchingEnvironment);

    assertThat(future, instanceOf(Future.class));
  }

  @Test
  @SuppressWarnings("unchecked")
  void get_returnsResultWithRdfUri_forLoadSingleQueryOperation() throws Exception {
    Map<String, Object> data = new HashMap<>() {
      {
        put("identifier", "id-1");
      }
    };
    when(typeConfiguration.getUriTemplate()).thenReturn("http://registrations.org/brewery/${identifier}");
    when(backendDataLoader.loadSingle(any(), any())).thenReturn(Mono.just(data));
    when(backendDataLoader.supports(typeConfiguration)).thenReturn(true);

    GraphQLOutputType outputType = createBreweryType();

    DataFetchingFieldSelectionSet mockDataFetchingFieldSelectionSet =
        mockDataFetchingFieldSelectionSet(Arrays.asList("_id"));
    DataFetchingEnvironment dataFetchingEnvironment =
        createDataFetchingEnvironment(outputType, QUERY, null, mockDataFetchingFieldSelectionSet);

    Object future = genericDataFetcher.get(dataFetchingEnvironment);

    assertThat(future, instanceOf(Future.class));

    DataFetcherResult<Map<String, Object>> result = (DataFetcherResult<Map<String, Object>>) ((Future<?>) future).get();

    assertThat(result, notNullValue());
    Map<String, Object> resultData = result.getData();
    assertTrue(resultData.containsKey("_id"));

    verify(backendDataLoader).loadSingle(isNull(), any(LoadEnvironment.class));
  }

  private DataFetchingEnvironment createDataFetchingEnvironment(GraphQLOutputType outputType,
      OperationDefinition.Operation operation) {
    return createDataFetchingEnvironment(outputType, operation, null);
  }

  private DataFetchingEnvironment createDataFetchingEnvironment(GraphQLOutputType outputType,
      OperationDefinition.Operation operation, Object source) {
    return createDataFetchingEnvironment(outputType, operation, source, mock(DataFetchingFieldSelectionSet.class));
  }

  private DataFetchingEnvironment createDataFetchingEnvironment(GraphQLOutputType outputType,
      OperationDefinition.Operation operation, Object source,
      DataFetchingFieldSelectionSet dataFetchingFieldSelectionSet) {
    return newDataFetchingEnvironment().executionStepInfo(executionStepInfo)
        .dataLoaderRegistry(new DataLoaderRegistry())
        .fieldType(outputType)
        .selectionSet(dataFetchingFieldSelectionSet)
        .fieldDefinition(graphQlFieldDefinitionMock)
        .operationDefinition(newOperationDefinition().operation(operation)
            .build())
        .source(source)
        .localContext(LocalDataFetcherContext.builder()
            .keyConditionFn((s, stringObjectMap) -> mock(KeyCondition.class))
            .build())
        .build();
  }

  private DataFetchingFieldSelectionSet mockDataFetchingFieldSelectionSet(List<String> rdfUriFieldNames) {
    DataFetchingFieldSelectionSet mockedDataFetchingFieldSelectionSet = mock(DataFetchingFieldSelectionSet.class);
    List<SelectedField> selectedFields = new ArrayList<>();
    rdfUriFieldNames.forEach(fieldName -> {
      SelectedField selectedField = mockRdfuriSelectedField();
      selectedFields.add(selectedField);
      when(mockedDataFetchingFieldSelectionSet.contains(fieldName)).thenReturn(true);
    });
    when(mockedDataFetchingFieldSelectionSet.getFields(anyString())).thenReturn(selectedFields);

    return mockedDataFetchingFieldSelectionSet;
  }

  private SelectedField mockRdfuriSelectedField() {
    SelectedField selectedField = mock(SelectedField.class);
    FieldDefinition def = newFieldDefinition().name("_id")
        .type(newTypeName(Scalars.GraphQLString.getName()).build())
        .build();

    GraphQLFieldDefinition qlDef = GraphQLFieldDefinition.newFieldDefinition()
        .name("_id")
        .definition(def)
        .type(Scalars.GraphQLString)
        .build();
    when(selectedField.getFieldDefinition()).thenReturn(qlDef);
    return selectedField;
  }

  private GraphQLOutputType createBreweryType() {
    return GraphQLObjectType.newObject()
        .name("Brewery")
        .build();
  }
}

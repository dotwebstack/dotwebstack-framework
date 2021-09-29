package org.dotwebstack.framework.backend.rdf4j;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import graphql.execution.ExecutionStepInfo;
import graphql.schema.DataFetchingFieldSelectionSet;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.dotwebstack.framework.backend.rdf4j.config.Rdf4jTypeConfiguration;
import org.dotwebstack.framework.backend.rdf4j.query.QueryBuilder;
import org.dotwebstack.framework.backend.rdf4j.query.QueryHolder;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.datafetchers.FieldKeyCondition;
import org.dotwebstack.framework.core.datafetchers.KeyCondition;
import org.dotwebstack.framework.core.datafetchers.LoadEnvironment;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.impl.IteratingTupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class Rdf4jDataLoaderTest {

  @Mock
  private Repository repository;

  @Mock
  private DotWebStackConfiguration dotWebStackConfiguration;

  @Mock
  private QueryBuilder queryBuilder;

  private Rdf4jDataLoader rdf4jDataLoader;

  @BeforeEach
  void init() {
    rdf4jDataLoader = new Rdf4jDataLoader(dotWebStackConfiguration, queryBuilder, repository);
  }

  @Test
  void batchLoadSingle_ThrowsException_ForEveryCall() {
    assertThrows(UnsupportedOperationException.class, () -> rdf4jDataLoader.batchLoadSingle(null, null));
  }

  @Test
  void loadSingle_returnsData_forExistingEntity() {
    mockQueryContext(mock(BindingSet.class));

    Map<String, Object> data = Map.of("x1", "id-1", "x2", "Brewery 1");

    FieldKeyCondition keyCondition = mock(FieldKeyCondition.class);

    QueryHolder queryHolder = QueryHolder.builder()
        .query("")
        .mapAssembler(binding -> data)
        .build();

    when(queryBuilder.build(any(Rdf4jTypeConfiguration.class), any(DataFetchingFieldSelectionSet.class),
        any(KeyCondition.class))).thenReturn(queryHolder);

    LoadEnvironment loadEnvironment = mockLoadEnvironment();

    when(dotWebStackConfiguration.getTypeConfiguration(loadEnvironment)).thenReturn(mock(Rdf4jTypeConfiguration.class));

    Map<String, Object> result = rdf4jDataLoader.loadSingle(keyCondition, loadEnvironment)
        .block(Duration.ofSeconds(5));

    assertThat(result, notNullValue());
    assertThat(data.entrySet(), equalTo(result.entrySet()));

    verify(repository, times(1)).getConnection();
    verify(queryBuilder, times(1)).build(any(Rdf4jTypeConfiguration.class), any(DataFetchingFieldSelectionSet.class),
        any(KeyCondition.class));
  }

  @Test
  void loadMany_returnsData_forExistingEntity() {
    mockQueryContext(mock(BindingSet.class), mock(BindingSet.class));

    List<Map<String, Object>> data =
        List.of(Map.of("x1", "id-1", "x2", "Brewery 1"), Map.of("x1", "id-2", "x2", "Brewery 2"));

    FieldKeyCondition keyCondition = mock(FieldKeyCondition.class);

    QueryHolder queryHolder = QueryHolder.builder()
        .query("")
        .mapAssembler(new Function<>() {
          private int count = 0;

          @Override
          public Map<String, Object> apply(BindingSet bindings) {
            Map<String, Object> result = data.get(count);
            count++;
            return result;
          }
        })
        .build();

    when(queryBuilder.build(any(Rdf4jTypeConfiguration.class), any(DataFetchingFieldSelectionSet.class),
        any(KeyCondition.class))).thenReturn(queryHolder);

    LoadEnvironment loadEnvironment = mockLoadEnvironment();

    when(dotWebStackConfiguration.getTypeConfiguration(loadEnvironment)).thenReturn(mock(Rdf4jTypeConfiguration.class));

    List<Map<String, Object>> result = rdf4jDataLoader.loadMany(keyCondition, loadEnvironment)
        .toStream()
        .collect(Collectors.toList());

    assertThat(result, notNullValue());
    assertThat(result.size(), equalTo(2));

    verify(repository, times(1)).getConnection();
    verify(queryBuilder, times(1)).build(any(Rdf4jTypeConfiguration.class), any(DataFetchingFieldSelectionSet.class),
        any(KeyCondition.class));
  }

  private LoadEnvironment mockLoadEnvironment() {
    return LoadEnvironment.builder()
        .executionStepInfo(mock(ExecutionStepInfo.class))
        .selectionSet(mock(DataFetchingFieldSelectionSet.class))
        .build();
  }

  private void mockQueryContext(BindingSet... bindingSets) {

    IteratingTupleQueryResult tupleQueryResult =
        new IteratingTupleQueryResult(List.of("x1"), Arrays.asList(bindingSets));

    TupleQuery tupleQuery = mock(TupleQuery.class);

    when(tupleQuery.evaluate()).thenReturn(tupleQueryResult);

    RepositoryConnection repositoryConnection = mock(RepositoryConnection.class);

    when(repositoryConnection.prepareTupleQuery(anyString())).thenReturn(tupleQuery);
    when(repository.getConnection()).thenReturn(repositoryConnection);
  }
}

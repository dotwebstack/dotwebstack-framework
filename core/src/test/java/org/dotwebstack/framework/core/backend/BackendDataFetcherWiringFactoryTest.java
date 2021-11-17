package org.dotwebstack.framework.core.backend;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.schema.GraphQLNamedOutputType;
import graphql.schema.idl.FieldWiringEnvironment;
import java.util.List;
import java.util.NoSuchElementException;
import org.dotwebstack.framework.core.config.SchemaReader;
import org.dotwebstack.framework.core.model.Schema;
import org.dotwebstack.framework.core.testhelpers.TestBackendLoaderFactory;
import org.dotwebstack.framework.core.testhelpers.TestBackendModule;
import org.dotwebstack.framework.core.testhelpers.TestHelper;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;

@ExtendWith(MockitoExtension.class)
class BackendDataFetcherWiringFactoryTest {

  @Mock
  private BackendRequestFactory requestFactory;

  @Mock
  private FieldWiringEnvironment environment;

  @Mock
  private BackendExecutionStepInfo backendExecutionStepInfo;

  @Mock
  private DatabaseClient databaseClient;

  private BackendDataFetcherWiringFactory dataFetcher;

  @BeforeEach
  void doBeforeEach() {
    BackendModule<?> backendModule = new TestBackendModule(new TestBackendLoaderFactory(databaseClient));
    SchemaReader schemaReader = new SchemaReader(TestHelper.createSimpleObjectMapper());
    Schema schema = schemaReader.read("dotwebstack/dotwebstack-objecttypes-complex-fields.yaml");
    dataFetcher =
        new BackendDataFetcherWiringFactory(backendModule, requestFactory, schema, backendExecutionStepInfo, List.of());
  }

  @Test
  void providesDataFetcher_returnsFalse_ifTypeNameIsNotPresented() {
    var environment = mock(FieldWiringEnvironment.class);

    var result = dataFetcher.providesDataFetcher(environment);
    assertFalse(result);
  }

  @Test
  void getDataFetcher_throwsException_ifTypeNameIsEmpty() {
    var environment = mock(FieldWiringEnvironment.class);

    var exception = assertThrows(NoSuchElementException.class, () -> dataFetcher.getDataFetcher(environment));

    assertThat(exception.getMessage(), is("No value present"));
  }

  @Test
  void getDataFetcher_returnsDataFetcher_ifTypeNamePresented() {
    GraphQLNamedOutputType typeMock = mock(GraphQLNamedOutputType.class);
    lenient().when(typeMock.getName())
        .thenReturn("Brewery");

    when(environment.getFieldType()).thenReturn(typeMock);

    var result = dataFetcher.getDataFetcher(environment);
    assertThat(result, CoreMatchers.is(notNullValue()));
    assertTrue(result instanceof BackendDataFetcher);
  }
}

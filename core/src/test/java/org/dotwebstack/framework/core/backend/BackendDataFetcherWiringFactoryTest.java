package org.dotwebstack.framework.core.backend;

import static graphql.language.FieldDefinition.newFieldDefinition;
import static org.dotwebstack.framework.core.graphql.GraphQlConstants.COUNTER_OVER;
import static org.dotwebstack.framework.core.graphql.GraphQlConstants.IS_COUNTER_TYPE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.language.FieldDefinition;
import graphql.language.Type;
import graphql.schema.GraphQLNamedOutputType;
import graphql.schema.idl.FieldWiringEnvironment;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.dotwebstack.framework.core.config.SchemaReader;
import org.dotwebstack.framework.core.model.Schema;
import org.dotwebstack.framework.core.testhelpers.TestBackendLoaderFactory;
import org.dotwebstack.framework.core.testhelpers.TestBackendModule;
import org.dotwebstack.framework.core.testhelpers.TestHelper;
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
    dataFetcher = new BackendDataFetcherWiringFactory(backendModule, requestFactory, schema, backendExecutionStepInfo,
        List.of(), null);

    lenient().when(environment.getFieldDefinition())
        .thenReturn(newFieldDefinition().build());
  }


  @Test
  void providesDataFetcher_throwsException_ifTypeNameIsNotPresent() {
    var exception = assertThrows(NoSuchElementException.class, () -> dataFetcher.providesDataFetcher(environment));

    assertThat(exception.getMessage(), is("No value present"));
  }

  @Test
  void providesDataFetcher_throwsException_ifTypeNameIsEmpty() {
    var typeMock = getTypeMock("");
    when(environment.getFieldType()).thenReturn(typeMock);

    var exception = assertThrows(IllegalStateException.class, () -> dataFetcher.providesDataFetcher(environment));

    assertThat(exception.getMessage(), is("Unknown ObjectType: "));
  }

  @Test
  void providesDataFetcher_returnsTrue_ifTypeNameEqualsAggregate() {
    var typeMock = getTypeMock("Aggregate");

    when(environment.getFieldType()).thenReturn(typeMock);

    var result = dataFetcher.providesDataFetcher(environment);
    assertThat(result, is(true));
  }

  @Test
  void providesDataFetcher_returnsTrue_ifTypeNameIsPresent() {
    var typeMock = getTypeMock("Beer");
    var fieldDefinition = getFieldDefinition("beer", null);

    when(environment.getFieldType()).thenReturn(typeMock);
    when(environment.getFieldDefinition()).thenReturn(fieldDefinition);

    var result = dataFetcher.providesDataFetcher(environment);
    assertThat(result, is(true));
  }

  @Test
  void getDataFetcher_throwsException_ifTypeNameIsNotPresent() {
    var exception = assertThrows(NoSuchElementException.class, () -> dataFetcher.getDataFetcher(environment));

    assertThat(exception.getMessage(), is("No value present"));
  }

  @Test
  void getDataFetcher_throwsException_ifTypeNameIsEmpty() {
    var typeMock = getTypeMock("");
    when(environment.getFieldType()).thenReturn(typeMock);

    var exception = assertThrows(IllegalStateException.class, () -> dataFetcher.getDataFetcher(environment));

    assertThat(exception.getMessage(), is("Unknown ObjectType: "));
  }

  @Test
  void getDataFetcher_returnsDataFetcher_ifTypeNamePresented() {
    var typeMock = getTypeMock("Brewery");
    var fieldDefinition = getFieldDefinition("beer", null);

    when(environment.getFieldType()).thenReturn(typeMock);
    when(environment.getFieldDefinition()).thenReturn(fieldDefinition);

    var result = dataFetcher.getDataFetcher(environment);
    assertThat(result, is(notNullValue()));
    assertThat(result.getClass(), is(BackendDataFetcher.class));
  }

  @Test
  void getDataFetcher_returnsDataFetcher_ifTypeNameEqualsAggregate() {
    var typeMock = getTypeMock("Aggregate");

    var fieldDefinition = getFieldDefinition("aggregate", null);

    when(environment.getFieldType()).thenReturn(typeMock);
    when(environment.getFieldDefinition()).thenReturn(fieldDefinition);

    var result = dataFetcher.getDataFetcher(environment);
    assertThat(result, is(notNullValue()));
    assertThat(result.getClass(), is(BackendDataFetcher.class));
  }

  @Test
  void getDataFetcher_returnsDataFetcher_forCounter() {
    var typeMock = getTypeMock("Counter");
    var additionalData = Map.of(IS_COUNTER_TYPE, Boolean.TRUE.toString(),
        COUNTER_OVER, "Brewery");
    var fieldDefinition = getFieldDefinition("breweriesCounter", additionalData);

    when(environment.getFieldType()).thenReturn(typeMock);
    when(environment.getFieldDefinition()).thenReturn(fieldDefinition);

    var result = dataFetcher.getDataFetcher(environment);
    assertThat(result, is(notNullValue()));
    assertThat(result.getClass(), is(BackendDataFetcher.class));
  }

  private GraphQLNamedOutputType getTypeMock(String typeName) {
    var typeMock = mock(GraphQLNamedOutputType.class);
    lenient().when(typeMock.getName())
        .thenReturn(typeName);
    return typeMock;
  }

  private FieldDefinition getFieldDefinition(String name, Map<String, String> additionalObjData) {
    if (additionalObjData == null) {
      additionalObjData = Map.of();
    }
    var fieldDefinition = mock(FieldDefinition.class);
    var typeMock = mock(Type.class);
      lenient().when(typeMock.getAdditionalData())
          .thenReturn(additionalObjData);

    lenient().when(fieldDefinition.getName())
        .thenReturn(name);
    lenient().when(fieldDefinition.getType())
        .thenReturn(typeMock);
    return fieldDefinition;
  }
}

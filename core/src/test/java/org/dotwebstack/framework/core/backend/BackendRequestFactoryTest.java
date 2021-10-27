package org.dotwebstack.framework.core.backend;

import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.STRING_JOIN_FIELD;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.execution.ExecutionStepInfo;
import graphql.execution.MergedField;
import graphql.execution.ResultPath;
import graphql.schema.DataFetchingEnvironmentImpl;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.SelectedField;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.core.config.SchemaReader;
import org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.RequestContext;
import org.dotwebstack.framework.core.scalars.DateSupplier;
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
class BackendRequestFactoryTest {

  @Mock
  private DatabaseClient databaseClient;

  private SchemaReader schemaReader;

  private BackendRequestFactory backendRequestFactory;

  @BeforeEach
  void doBefore() {
    schemaReader = new SchemaReader(TestHelper.createSimpleObjectMapper());
  }

  @Test
  void createCollectionRequest_returnsCollectionRequest_Brewery() {
    BackendModule<?> backendModule = new TestBackendModule(new TestBackendLoaderFactory(databaseClient));
    var testHelper = new TestHelper(backendModule);
    var schema = testHelper.getSchema("dotwebstack/dotwebstack-queries-with-filters-sortable-by.yaml");
    backendModule.init(schema.getObjectTypes());

    GraphQLObjectType namedType = mock(GraphQLObjectType.class);
    when(namedType.getName()).thenReturn("Aggregate");

    SelectedField selectedField = mock(SelectedField.class);
    when(selectedField.getArguments()).thenReturn(Map.of(AggregateConstants.FIELD_ARGUMENT, "name"));
    when(selectedField.getName()).thenReturn(STRING_JOIN_FIELD);

    DataFetchingFieldSelectionSet selectionSet = mock(DataFetchingFieldSelectionSet.class);
    when(selectionSet.getImmediateFields()).thenReturn(List.of(selectedField));


    SelectedField selectedFieldParent = mock(SelectedField.class);
    when(selectedFieldParent.getName()).thenReturn("beerAgg");
    GraphQLList graphQlList = mock(GraphQLList.class);
    when(graphQlList.getWrappedType()).thenReturn(namedType);
    when(selectedFieldParent.getType()).thenReturn(graphQlList);
    when(selectedFieldParent.getSelectionSet()).thenReturn(selectionSet);

    var selectionSetParent = mock(DataFetchingFieldSelectionSet.class);
    when(selectionSetParent.getImmediateFields()).thenReturn(List.of(selectedFieldParent));

    var executionStepInfo = initExecutionStepInfoMock();
    backendRequestFactory = new BackendRequestFactory(schema, new BackendExecutionStepInfo(schema));
    var result = backendRequestFactory.createCollectionRequest(executionStepInfo, selectionSetParent);

    assertThat(result, CoreMatchers.is(notNullValue()));
    assertTrue(result instanceof CollectionRequest);
    assertThat(result.getObjectRequest()
        .getObjectType()
        .getName(), is("Brewery"));
    assertTrue(!result.getFilterCriterias()
        .isEmpty());
    assertThat(result.getSortCriterias()
        .get(0)
        .getFieldPath()
        .get(0)
        .getName(), is("name"));
  }


  @Test
  void createRequestContext_returnsRequestContext_whithObjectFieldType_Address() {
    LocalDate date = LocalDate.of(2021, 1, 1);
    Map<String, Object> data = new HashMap<>();
    data.put("key", new DateSupplier(false, date));
    Map<String, Object> source = Map.of("arg", Map.of("arg1", data));

    DataFetchingEnvironmentImpl.Builder envBuilder = new DataFetchingEnvironmentImpl.Builder();
    envBuilder.source(source);

    ExecutionStepInfo executionStepInfo = mock(ExecutionStepInfo.class);
    GraphQLObjectType objectType = mock(GraphQLObjectType.class);
    when(objectType.getName()).thenReturn("Brewery");

    when(executionStepInfo.getObjectType()).thenReturn(objectType);

    MergedField mergedField = mock(MergedField.class);
    when(mergedField.getName()).thenReturn("addresses");
    when(executionStepInfo.getField()).thenReturn(mergedField);

    envBuilder.executionStepInfo(executionStepInfo);

    var schema = schemaReader.read("dotwebstack/dotwebstack-objecttypes.yaml");

    backendRequestFactory = new BackendRequestFactory(schema, new BackendExecutionStepInfo(schema));

    var result = backendRequestFactory.createRequestContext(envBuilder.build());
    assertThat(result, CoreMatchers.is(notNullValue()));
    assertTrue(result instanceof RequestContext);
    assertThat(result.getObjectField()
        .getName(), is("addresses"));
    assertThat(result.getObjectField()
        .getType(), is("Address"));
  }

  private ExecutionStepInfo initExecutionStepInfoMock() {
    var executionStepInfo = mock(ExecutionStepInfo.class);
    var resultPath = ResultPath.rootPath()
        .segment("a");
    lenient().when(executionStepInfo.getPath())
        .thenReturn(resultPath);
    var objectType = mock(GraphQLObjectType.class);
    lenient().when(objectType.getName())
        .thenReturn("Brewery");

    lenient().when(executionStepInfo.getType())
        .thenReturn(objectType);

    var date = LocalDate.of(2021, 1, 1);
    Map<String, Object> data = new HashMap<>();
    Map<String, Object> argument = Map.of("name", "name");
    data.put("key", new DateSupplier(false, date));
    Map<String, Object> arguments = new HashMap<>();
    arguments.put("arg", Map.of("arg1", data));
    arguments.put("filter", argument);
    lenient().when(executionStepInfo.getArguments())
        .thenReturn(arguments);
    lenient().when(executionStepInfo.getArgument(eq("filter")))
        .thenReturn(argument);
    lenient().when(executionStepInfo.getArgument(eq("sort")))
        .thenReturn("NAME");

    var fieldDefinitionBuilder = GraphQLFieldDefinition.newFieldDefinition();
    List<GraphQLArgument> argumentList = new ArrayList<>();
    fieldDefinitionBuilder.arguments(argumentList);
    fieldDefinitionBuilder.name("a");
    fieldDefinitionBuilder.description("any");
    fieldDefinitionBuilder.type(mock(GraphQLOutputType.class));
    when(executionStepInfo.getFieldDefinition()).thenReturn(fieldDefinitionBuilder.build());

    return executionStepInfo;
  }
}
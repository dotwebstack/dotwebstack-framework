package org.dotwebstack.framework.core.backend;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.STRING_JOIN_FIELD;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.Scalars;
import graphql.execution.ExecutionStepInfo;
import graphql.execution.MergedField;
import graphql.execution.ResultPath;
import graphql.language.FieldDefinition;
import graphql.schema.DataFetchingEnvironmentImpl;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.SelectedField;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants;
import org.dotwebstack.framework.core.graphql.GraphQlConstants;
import org.dotwebstack.framework.core.scalars.DateSupplier;
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
class BackendRequestFactoryTest {

  @Mock
  private DatabaseClient databaseClient;

  private TestHelper testHelper;

  @BeforeEach
  void setUp() {
    var backendModule = new TestBackendModule(new TestBackendLoaderFactory(databaseClient));
    testHelper = new TestHelper(backendModule);
  }

  @Test
  void createObjectRequest_returnsObjectRequest_forBeerWithKey() {
    var schema = testHelper.loadSchema("dotwebstack/dotwebstack-objecttypes.yaml");
    var graphQlSchema = TestHelper.schemaToGraphQl(schema);

    var breweryFieldDefinition = graphQlSchema.getQueryType()
        .getFieldDefinition("brewery");

    var beerFieldDefinition = graphQlSchema.getObjectType("Brewery")
        .getFieldDefinition("beer");

    GraphQLObjectType objectType = mock(GraphQLObjectType.class);
    var executionStepInfo = ExecutionStepInfo.newExecutionStepInfo()
        .fieldDefinition(breweryFieldDefinition)
        .fieldContainer(objectType)
        .type(breweryFieldDefinition.getType())
        .build();

    var brewerySelectionSet = mock(DataFetchingFieldSelectionSet.class);
    var beerSelectionSet = mock(DataFetchingFieldSelectionSet.class);
    var beerField = mock(SelectedField.class);

    when(beerField.getType()).thenReturn(beerFieldDefinition.getType());
    when(beerField.getFieldDefinitions()).thenReturn(List.of(beerFieldDefinition));
    when(beerField.getSelectionSet()).thenReturn(beerSelectionSet);
    when(beerField.getArguments()).thenReturn(Map.of("identifier", "foo"));
    when(brewerySelectionSet.getImmediateFields()).thenReturn(List.of(beerField));

    var backendRequestFactory = new BackendRequestFactory(schema, new BackendExecutionStepInfo());
    var objectRequest = backendRequestFactory.createObjectRequest(executionStepInfo, brewerySelectionSet);

    assertThat(objectRequest, is(notNullValue()));

    var objectFields = objectRequest.getObjectFields();
    assertThat(objectFields.size(), is(1));

    var beerObjectRequest = objectFields.values()
        .stream()
        .findFirst()
        .orElseThrow();

    var keyCriteria = beerObjectRequest.getKeyCriterias();
    assertThat(keyCriteria.size(), is(1));

    var fieldPath = keyCriteria.get(0)
        .getFieldPath();
    assertThat(fieldPath.size(), is(1));
    assertThat(fieldPath.get(0)
        .getName(), is("identifier"));
    assertThat(keyCriteria.get(0)
        .getValue(), is("foo"));
  }

  @Test
  void createObjectRequest_returnsObjectRequestWithKeyCriteria_forBreweryWithKey() {
    var schema = testHelper.loadSchema("dotwebstack/dotwebstack-objecttypes.yaml");
    var graphQlSchema = TestHelper.schemaToGraphQl(schema);

    var breweryFieldDefinition = graphQlSchema.getQueryType()
        .getFieldDefinition("brewery");

    GraphQLObjectType objectType = mock(GraphQLObjectType.class);
    var executionStepInfo = ExecutionStepInfo.newExecutionStepInfo()
        .fieldDefinition(breweryFieldDefinition)
        .fieldContainer(objectType)
        .type(breweryFieldDefinition.getType())
        .arguments(Map.of("identifier", "id-1"))
        .build();

    var brewerySelectionSet = mock(DataFetchingFieldSelectionSet.class);

    var backendRequestFactory = new BackendRequestFactory(schema, new BackendExecutionStepInfo());

    var objectRequest = backendRequestFactory.createObjectRequest(executionStepInfo, brewerySelectionSet);
    assertThat(objectRequest.getKeyCriterias(), is(notNullValue()));

    var keyCriteria = objectRequest.getKeyCriterias();
    assertThat(keyCriteria.size(), is(1));

    var fieldPath = keyCriteria.get(0)
        .getFieldPath();
    assertThat(fieldPath.size(), is(1));
    assertThat(fieldPath.get(0)
        .getName(), is("identifier"));
    assertThat(fieldPath.get(0)
        .getObjectType()
        .getName(), is("Brewery"));
    assertThat(keyCriteria.get(0)
        .getValue(), is("id-1"));
  }

  @Test
  void createCollectionRequest_returnsCollectionRequest_Brewery() {
    var schema = testHelper.loadSchema("dotwebstack/dotwebstack-queries-with-filters-sortable-by.yaml");

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
    var backendRequestFactory = new BackendRequestFactory(schema, new BackendExecutionStepInfo());

    backendRequestFactory = new BackendRequestFactory(schema, new BackendExecutionStepInfo());
    var result = backendRequestFactory.createCollectionRequest(executionStepInfo, selectionSetParent);

    assertThat(result, is(notNullValue()));
    assertThat(result.getObjectRequest()
        .getObjectType()
        .getName(), is("Brewery"));
    assertThat(result.getFilterCriteria(), is(notNullValue()));
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

    envBuilder.fieldDefinition(newFieldDefinition().name("field")
        .type(Scalars.GraphQLID)
        .definition(FieldDefinition.newFieldDefinition()
            .additionalData(GraphQlConstants.IS_PAGING_NODE, Boolean.TRUE.toString())
            .build())
        .build());
    envBuilder.fieldDefinition(newFieldDefinition().name("field")
        .type(Scalars.GraphQLID)
        .definition(FieldDefinition.newFieldDefinition()
            .additionalData(GraphQlConstants.IS_PAGING_NODE, Boolean.TRUE.toString())
            .build())
        .build());

    var schema = testHelper.loadSchema("dotwebstack/dotwebstack-objecttypes.yaml");
    var backendRequestFactory = new BackendRequestFactory(schema, new BackendExecutionStepInfo());

    var result = backendRequestFactory.createRequestContext(envBuilder.build());

    assertThat(result, is(notNullValue()));
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
    Map<String, Object> argument = Map.of("name", Map.of("eq", "name-value"));
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

    var fieldDefinitionBuilder = newFieldDefinition();
    List<GraphQLArgument> argumentList = new ArrayList<>();
    fieldDefinitionBuilder.arguments(argumentList);
    fieldDefinitionBuilder.name("a");
    fieldDefinitionBuilder.description("any");
    fieldDefinitionBuilder.type(mock(GraphQLOutputType.class));
    when(executionStepInfo.getFieldDefinition()).thenReturn(fieldDefinitionBuilder.build());

    return executionStepInfo;
  }
}

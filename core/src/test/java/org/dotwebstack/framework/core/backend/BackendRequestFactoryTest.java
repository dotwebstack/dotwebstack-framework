package org.dotwebstack.framework.core.backend;

import static graphql.execution.ExecutionStepInfo.newExecutionStepInfo;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.STRING_JOIN_FIELD;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.Scalars;
import graphql.collect.ImmutableMapWithNullValues;
import graphql.execution.ExecutionStepInfo;
import graphql.execution.MergedField;
import graphql.execution.ResultPath;
import graphql.language.Field;
import graphql.language.FieldDefinition;
import graphql.language.NodeChildrenContainer;
import graphql.language.Selection;
import graphql.language.SelectionSet;
import graphql.language.TypeName;
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
import java.util.Set;
import org.dotwebstack.framework.core.CustomValueFetcherDispatcher;
import org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants;
import org.dotwebstack.framework.core.graphql.GraphQlConstants;
import org.dotwebstack.framework.core.model.Query;
import org.dotwebstack.framework.core.model.Schema;
import org.dotwebstack.framework.core.query.model.FieldRequest;
import org.dotwebstack.framework.core.query.model.SingleObjectRequest;
import org.dotwebstack.framework.core.query.model.UnionObjectRequest;
import org.dotwebstack.framework.core.scalars.DateSupplier;
import org.dotwebstack.framework.core.testhelpers.TestBackendLoaderFactory;
import org.dotwebstack.framework.core.testhelpers.TestBackendModule;
import org.dotwebstack.framework.core.testhelpers.TestHelper;
import org.dotwebstack.framework.core.testhelpers.TestObjectField;
import org.dotwebstack.framework.core.testhelpers.TestObjectType;
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

  private ExecutionStepInfo queryExecutionStepInfo;

  @BeforeEach
  void setUp() {
    var backendModule = new TestBackendModule(new TestBackendLoaderFactory(databaseClient));
    testHelper = new TestHelper(backendModule);
    queryExecutionStepInfo = newExecutionStepInfo().type(newObject().name("Query")
        .build())
        .build();
  }

  @Test
  void createObjectRequest_returnsObjectRequest_forBeerWithCustomValue() {
    var objectType = new TestObjectType();
    objectType.setName("TestObject");

    var nameField = new TestObjectField();
    nameField.setName("name");
    nameField.setType("String");
    objectType.getFields()
        .put("name", nameField);

    var shortNameField = new TestObjectField();
    shortNameField.setName("shortName");
    shortNameField.setType("String");
    shortNameField.setValueFetcher("shortname-valuefetcher");
    objectType.getFields()
        .put("shortName", shortNameField);

    var schema = new Schema();
    schema.getObjectTypes()
        .put("TestObject", objectType);

    var testQuery = new Query();
    testQuery.setType("TestObject");
    schema.getQueries()
        .put("testQuery", testQuery);

    var graphQlSchema = TestHelper.schemaToGraphQl(schema);

    var fieldDefinition = graphQlSchema.getQueryType()
        .getFieldDefinition("testQuery");

    GraphQLObjectType objectType2 = mock(GraphQLObjectType.class);
    var executionStepInfo = newExecutionStepInfo().fieldDefinition(fieldDefinition)
        .fieldContainer(objectType2)
        .type(fieldDefinition.getType())
        .parentInfo(queryExecutionStepInfo)
        .build();

    var selectionSet = mock(DataFetchingFieldSelectionSet.class);

    var selectedField = mock(SelectedField.class);
    when(selectedField.getFieldDefinitions()).thenReturn(List.of(newFieldDefinition().type(Scalars.GraphQLString)
        .definition(FieldDefinition.newFieldDefinition()
            .additionalData(GraphQlConstants.CUSTOM_FIELD_VALUEFETCHER, "shortname-valuefetcher")
            .build())
        .name("shortName")
        .build()));
    when(selectedField.getName()).thenReturn("shortName");
    when(selectedField.getFullyQualifiedName()).thenReturn("TestObject.shortName");
    when(selectedField.getType()).thenReturn(Scalars.GraphQLString);

    when(selectionSet.getImmediateFields()).thenReturn(List.of(selectedField));

    var customValueFetcherDispatcher = mock(CustomValueFetcherDispatcher.class);
    when(customValueFetcherDispatcher.getSourceFieldNames("shortname-valuefetcher")).thenReturn(Set.of("name"));

    var backendRequestFactory =
        new BackendRequestFactory(schema, new BackendExecutionStepInfo(), customValueFetcherDispatcher);
    var objectRequest =
        (SingleObjectRequest) backendRequestFactory.createObjectRequest(executionStepInfo, selectionSet);

    assertThat(objectRequest, is(notNullValue()));
    assertThat(objectRequest.getScalarFields(), equalTo(List.of(FieldRequest.builder()
        .name("name")
        .resultKey("name")
        .build())));
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
    var executionStepInfo = newExecutionStepInfo().fieldDefinition(breweryFieldDefinition)
        .fieldContainer(objectType)
        .type(breweryFieldDefinition.getType())
        .parentInfo(queryExecutionStepInfo)
        .build();

    var brewerySelectionSet = mock(DataFetchingFieldSelectionSet.class);
    var beerSelectionSet = mock(DataFetchingFieldSelectionSet.class);
    var beerField = mock(SelectedField.class);

    when(beerField.getType()).thenReturn(beerFieldDefinition.getType());
    when(beerField.getFieldDefinitions()).thenReturn(List.of(beerFieldDefinition));
    when(beerField.getSelectionSet()).thenReturn(beerSelectionSet);
    when(beerField.getArguments()).thenReturn(Map.of("identifier", "foo"));
    when(brewerySelectionSet.getImmediateFields()).thenReturn(List.of(beerField));

    var backendRequestFactory = new BackendRequestFactory(schema, new BackendExecutionStepInfo(), null);
    var objectRequest =
        (SingleObjectRequest) backendRequestFactory.createObjectRequest(executionStepInfo, brewerySelectionSet);

    assertThat(objectRequest, is(notNullValue()));

    var objectFields = objectRequest.getObjectFields();
    assertThat(objectFields.size(), is(1));

    var beerObjectRequest = objectFields.values()
        .stream()
        .map(SingleObjectRequest.class::cast)
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
    var executionStepInfo = newExecutionStepInfo().fieldDefinition(breweryFieldDefinition)
        .fieldContainer(objectType)
        .type(breweryFieldDefinition.getType())
        .arguments(() -> ImmutableMapWithNullValues.copyOf(Map.of("identifier", "id-1")))
        .parentInfo(queryExecutionStepInfo)
        .build();

    var brewerySelectionSet = mock(DataFetchingFieldSelectionSet.class);

    var backendRequestFactory = new BackendRequestFactory(schema, new BackendExecutionStepInfo(), null);

    var objectRequest =
        (SingleObjectRequest) backendRequestFactory.createObjectRequest(executionStepInfo, brewerySelectionSet);
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
  void createObjectRequest_returnsObjectRequestWithKeyCriteriaAndSelections_forInterfaceWithKey() {
    var pathToConfigFile = "dotwebstack/dotwebstack-objecttypes-with-interfaces.yaml";
    var schema = TestHelper.loadSchemaWithDefaultBackendModule(pathToConfigFile);
    var graphQlSchema = TestHelper.schemaToGraphQlWithDefaultTypeResolvers(pathToConfigFile);

    var baseObjectFieldDefinition = graphQlSchema.getQueryType()
        .getFieldDefinition("baseObject");

    var mergedField = mock(MergedField.class);
    var field = mock(Field.class);
    var selectionSet = mock(SelectionSet.class);
    var brewery = createGraphqlSelection("Brewery");
    var address = createGraphqlSelection("Address");
    var beer = createGraphqlSelection("Beer");
    var somethingElse = createGraphqlSelection(null);

    when(selectionSet.getSelections()).thenReturn(List.of(brewery, address, beer, somethingElse));
    when(field.getSelectionSet()).thenReturn(selectionSet);
    when(mergedField.getFields()).thenReturn(List.of(field));

    var objectType = mock(GraphQLObjectType.class);
    var executionStepInfo = newExecutionStepInfo().fieldDefinition(baseObjectFieldDefinition)
        .fieldContainer(objectType)
        .field(mergedField)
        .type(baseObjectFieldDefinition.getType())
        .arguments(() -> ImmutableMapWithNullValues.copyOf(Map.of("identifier", "id-1")))
        .parentInfo(queryExecutionStepInfo)
        .build();

    var brewerySelectionSet = mock(DataFetchingFieldSelectionSet.class);
    var backendRequestFactory = new BackendRequestFactory(schema, new BackendExecutionStepInfo(), null);
    var objectRequest = backendRequestFactory.createObjectRequest(executionStepInfo, brewerySelectionSet);

    assertThat(objectRequest, is(notNullValue()));
    assertThat(objectRequest, instanceOf(UnionObjectRequest.class));

    var unionObjectRequest = (UnionObjectRequest) objectRequest;

    assertThat(unionObjectRequest.getObjectRequests()
        .size(), is(3));
    unionObjectRequest.getObjectRequests()
        .forEach(singleObjectRequest -> {
          assertThat(singleObjectRequest.getKeyCriterias(), is(notNullValue()));
          var keyCriteria = singleObjectRequest.getKeyCriterias();
          assertThat(keyCriteria.size(), is(1));
          var fieldPath = keyCriteria.get(0)
              .getFieldPath();
          assertThat(fieldPath.size(), is(1));
          assertThat(fieldPath.get(0)
              .getName(), is("identifier"));
          assertThat(fieldPath.get(0)
              .getObjectType()
              .getName(), is("BaseObject"));
          assertThat(keyCriteria.get(0)
              .getValue(), is("id-1"));
        });
  }

  @Test
  void createObjectRequest_returnsObjectRequestWithKeyCriteriaWithoutSelections_forInterfaceWithKey() {
    var pathToConfigFile = "dotwebstack/dotwebstack-objecttypes-with-interfaces.yaml";
    var schema = TestHelper.loadSchemaWithDefaultBackendModule(pathToConfigFile);
    var graphQlSchema = TestHelper.schemaToGraphQlWithDefaultTypeResolvers(pathToConfigFile);

    var baseObjectFieldDefinition = graphQlSchema.getQueryType()
        .getFieldDefinition("baseObject");

    var objectType = mock(GraphQLObjectType.class);
    var executionStepInfo = newExecutionStepInfo().fieldDefinition(baseObjectFieldDefinition)
        .fieldContainer(objectType)
        .type(baseObjectFieldDefinition.getType())
        .arguments(() -> ImmutableMapWithNullValues.copyOf(Map.of("identifier", "id-1")))
        .parentInfo(queryExecutionStepInfo)
        .build();

    var brewerySelectionSet = mock(DataFetchingFieldSelectionSet.class);
    var backendRequestFactory = new BackendRequestFactory(schema, new BackendExecutionStepInfo(), null);
    var objectRequest = backendRequestFactory.createObjectRequest(executionStepInfo, brewerySelectionSet);

    assertThat(objectRequest, is(notNullValue()));
    assertThat(objectRequest, instanceOf(UnionObjectRequest.class));

    var unionObjectRequest = (UnionObjectRequest) objectRequest;

    assertThat(unionObjectRequest.getObjectRequests()
        .size(), is(3));
    unionObjectRequest.getObjectRequests()
        .forEach(singleObjectRequest -> {
          assertThat(singleObjectRequest.getKeyCriterias(), is(notNullValue()));
          var keyCriteria = singleObjectRequest.getKeyCriterias();
          assertThat(keyCriteria.size(), is(1));
          var fieldPath = keyCriteria.get(0)
              .getFieldPath();
          assertThat(fieldPath.size(), is(1));
          assertThat(fieldPath.get(0)
              .getName(), is("identifier"));
          assertThat(fieldPath.get(0)
              .getObjectType()
              .getName(), is("BaseObject"));
          assertThat(keyCriteria.get(0)
              .getValue(), is("id-1"));
        });
  }

  private Selection createGraphqlSelection(String name) {
    var selection = mock(Selection.class);
    var nodeChildrenContainer = mock(NodeChildrenContainer.class);
    if (name != null) {
      var typeName = mock(TypeName.class);
      when(typeName.getName()).thenReturn(name);
      when(nodeChildrenContainer.getChildren("typeCondition")).thenReturn(List.of(typeName));
    } else {
      when(nodeChildrenContainer.getChildren("typeCondition")).thenReturn(List.of());
    }
    when(selection.getNamedChildren()).thenReturn(nodeChildrenContainer);

    return selection;
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
    var backendRequestFactory = new BackendRequestFactory(schema, new BackendExecutionStepInfo(), null);

    backendRequestFactory = new BackendRequestFactory(schema, new BackendExecutionStepInfo(), null);
    var result = backendRequestFactory.createCollectionRequest(executionStepInfo, selectionSetParent);

    assertThat(result, is(notNullValue()));
    assertThat(((SingleObjectRequest) result.getObjectRequest()).getObjectType()
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
    var backendRequestFactory = new BackendRequestFactory(schema, new BackendExecutionStepInfo(), null);

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
        .segment("brewery");
    lenient().when(executionStepInfo.getPath())
        .thenReturn(resultPath);

    var objectType = newObject().name("Brewery")
        .build();
    var listType = GraphQLList.list(objectType);

    lenient().when(executionStepInfo.getType())
        .thenReturn(listType);

    var queryType = newObject().name("Query")
        .build();
    lenient().when(executionStepInfo.getObjectType())
        .thenReturn(queryType);

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
    fieldDefinitionBuilder.name("brewery");
    fieldDefinitionBuilder.description("any");
    fieldDefinitionBuilder.type(mock(GraphQLOutputType.class));
    fieldDefinitionBuilder.definition(FieldDefinition.newFieldDefinition()
        .build());

    when(executionStepInfo.getFieldDefinition()).thenReturn(fieldDefinitionBuilder.build());
    when(executionStepInfo.getParent()).thenReturn(queryExecutionStepInfo);

    return executionStepInfo;
  }
}

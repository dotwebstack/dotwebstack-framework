package org.dotwebstack.framework.core.backend;

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
import graphql.schema.DataFetchingFieldSelectionSetImpl;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.core.TestHelper;
import org.dotwebstack.framework.core.config.SchemaReader;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.RequestContext;
import org.dotwebstack.framework.core.scalars.DateSupplier;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BackendRequestFactoryTest {

  private SchemaReader schemaReader;

  private BackendRequestFactory backendRequestFactory;

  @BeforeEach
  void doBefore() {
    schemaReader = new SchemaReader(TestHelper.createObjectMapper());
  }

  @Test
  void createCollectionRequest_returnsCollectionRequest_Brewery() {
    DataFetchingEnvironmentImpl.Builder envBuilder = new DataFetchingEnvironmentImpl.Builder();
    var typeMock = mock(GraphQLObjectType.class);
    lenient().when(typeMock.getName())
        .thenReturn("Brewery");
    envBuilder.fieldType(typeMock);

    var fieldSelectionSet = mock(DataFetchingFieldSelectionSetImpl.class);
    envBuilder.selectionSet(fieldSelectionSet);

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

    envBuilder.executionStepInfo(executionStepInfo);
    var selectionSetMock = mock(DataFetchingFieldSelectionSet.class);

    var schema = schemaReader.read("dotwebstack/dotwebstack-queries-with-filters-sortable-by.yaml");

    backendRequestFactory = new BackendRequestFactory(schema);

    var result = backendRequestFactory.createCollectionRequest(executionStepInfo, selectionSetMock);
    assertThat(result, CoreMatchers.is(notNullValue()));
    assertTrue(result instanceof CollectionRequest);
    assertThat(result.getObjectRequest()
        .getObjectType()
        .getName(), is("Brewery"));
    assertTrue(!result.getFilterCriterias()
        .isEmpty());
    assertThat(result.getSortCriterias()
        .get(0)
        .getFields()
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

    backendRequestFactory = new BackendRequestFactory(schema);

    var result = backendRequestFactory.createRequestContext(envBuilder.build());
    assertThat(result, CoreMatchers.is(notNullValue()));
    assertTrue(result instanceof RequestContext);
    assertThat(result.getObjectField()
        .getName(), is("addresses"));
    assertThat(result.getObjectField()
        .getType(), is("Address"));
  }

  @Test
  void getExecutionStepInfo_returnsExecutionStepInfo_ifPaging() {
    var envBuilder = new DataFetchingEnvironmentImpl.Builder();
    var listType = mock(GraphQLList.class);
    envBuilder.fieldType(listType);

    var executionStepInfo = mock(ExecutionStepInfo.class);
    var executionStepInfoParent = mock(ExecutionStepInfo.class);
    lenient().when(executionStepInfoParent.hasParent())
        .thenReturn(false);
    var objectType = mock(GraphQLObjectType.class);
    lenient().when(objectType.getName())
        .thenReturn("anyName");
    lenient().when(executionStepInfoParent.getObjectType())
        .thenReturn(objectType);
    when(executionStepInfo.getParent()).thenReturn(executionStepInfoParent);

    envBuilder.executionStepInfo(executionStepInfo);

    var schema = schemaReader.read("dotwebstack/dotwebstack-queries-with-paging.yaml");

    backendRequestFactory = new BackendRequestFactory(schema);

    var result = backendRequestFactory.getExecutionStepInfo(envBuilder.build());
    assertThat(result, CoreMatchers.is(notNullValue()));
    assertTrue(result instanceof ExecutionStepInfo);
    assertThat(result.getObjectType()
        .getName(), is("anyName"));
  }
}

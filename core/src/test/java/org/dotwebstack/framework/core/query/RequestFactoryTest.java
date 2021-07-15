package org.dotwebstack.framework.core.query;

import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInputObjectType.newInputObject;
import static graphql.schema.GraphQLObjectType.newObject;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.COUNT_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FIELD_ARGUMENT;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FLOAT_AVG_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FLOAT_MAX_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FLOAT_MIN_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FLOAT_SUM_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.INT_AVG_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.INT_MAX_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.INT_MIN_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.INT_SUM_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.STRING_JOIN_FIELD;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import graphql.execution.ExecutionStepInfo;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.SelectedField;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;
import org.dotwebstack.framework.core.config.ContextConfiguration;
import org.dotwebstack.framework.core.config.ContextFieldConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.Feature;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.SortConstants;
import org.dotwebstack.framework.core.datafetchers.filter.FilterConstants;
import org.dotwebstack.framework.core.datafetchers.filter.FilterCriteriaParserFactory;
import org.dotwebstack.framework.core.datafetchers.paging.PagingDataFetcherContext;
import org.dotwebstack.framework.core.query.model.AggregateObjectFieldConfiguration;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.ContextCriteria;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.PagingCriteria;
import org.dotwebstack.framework.core.query.model.ScalarType;
import org.dotwebstack.framework.core.query.model.SortCriteria;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RequestFactoryTest {

  private static final String FIELD_IDENTIFIER = "identifier";

  private static final String FIELD_BREWERY = "brewery";

  private static final String FIELD_KEYCRITERIA = "fieldName";

  private static final String SORT_KEYCRITERIA = "sort";

  private static final String FILTER_KEYCRITERIA = "filter";

  private static final String FIELD_HISTORY = "history";

  private static final String FIELD_AGGREGATE = "aggregateField";

  private static final String SCALAR_FIELDCONFIGURATION = "scalar";

  private static final String AGGREGATE_FIELDCONFIGURATION = "aggregate";

  private static final String NESTEDOBJECT_FIELDCONFIGURATION = "nestedObject";

  private static final String OBJECT_FIELDCONFIGURATION = "object";

  private static final String COLLECTIONOBJECT_FIELDCONFIGURATION = "list";

  private static final String NUMERIC = "numeric";

  private static final String TEXT = "text";

  private RequestFactory requestFactory;

  @SuppressWarnings("rawtypes")
  private TypeConfiguration typeConfiguration;

  private DataFetchingFieldSelectionSet selectionSet;

  private String fieldPathPrefix;

  private TestFieldConfiguration identifierFieldConfiguration;

  private List<SelectedField> selectedFields;

  @Mock
  private DotWebStackConfiguration dotWebStackConfiguration;

  @Mock
  private FilterCriteriaParserFactory filterCriteriaParserFactory;

  @Mock
  private TypeDefinitionRegistry typeDefinitionRegistry;

  @Mock
  private DataFetchingEnvironment environment;

  @Mock
  private ExecutionStepInfo executionStepInfo;

  @BeforeEach
  void beforeEach() {
    requestFactory = new RequestFactory(dotWebStackConfiguration, filterCriteriaParserFactory, typeDefinitionRegistry);
    typeConfiguration = mock(TypeConfiguration.class);
    selectionSet = mock(DataFetchingFieldSelectionSet.class);
    fieldPathPrefix = "";
    identifierFieldConfiguration = getTestFieldConfiguration(FIELD_IDENTIFIER, SCALAR_FIELDCONFIGURATION);

    selectedFields = new ArrayList<>();
    when(environment.getSelectionSet()).thenReturn(selectionSet);
    when(selectionSet.getFields(fieldPathPrefix.concat("*.*"))).thenReturn(selectedFields);
  }

  @Test
  void createCollectionRequest_returnsCollectionRequest_forScalarField() {
    selectedFields.add(mockSelectedField(FIELD_IDENTIFIER));

    Map<String, TestFieldConfiguration> fields = Map.of(FIELD_IDENTIFIER, identifierFieldConfiguration);

    when(typeConfiguration.getFields()).thenReturn(fields);

    when(environment.getExecutionStepInfo()).thenReturn(executionStepInfo);
    when(executionStepInfo.getFieldDefinition()).thenReturn(newFieldDefinition().name("testQuery")
        .type(newObject().name("ReturnObject")
            .build())
        .argument(newArgument().name(FilterConstants.FILTER_ARGUMENT_NAME)
            .type(newInputObject().name(FilterConstants.STRING_FILTER_INPUT_OBJECT_TYPE)
                .build())
            .build())
        .build());

    var collectionRequest = requestFactory.createCollectionRequest(typeConfiguration, environment);

    assertCollectionRequest(collectionRequest);
  }

  @Test
  void createCollectionRequest_returnsCollectionRequest_forSortedScalarField() {
    selectedFields.add(mockSelectedField(FIELD_IDENTIFIER));

    var sortCriteria = mock(SortCriteria.class);

    when(typeConfiguration.getSortCriterias()).thenReturn(Map.of("IDENTIFICATIE", List.of(sortCriteria)));

    var fields = Map.of(FIELD_IDENTIFIER, identifierFieldConfiguration);

    when(typeConfiguration.getFields()).thenReturn(fields);

    when(executionStepInfo.getArguments()).thenReturn(Map.of("sort", "IDENTIFICATIE"));

    when(environment.getExecutionStepInfo()).thenReturn(executionStepInfo);
    when(executionStepInfo.getFieldDefinition()).thenReturn(newFieldDefinition().name("testQuery")
        .type(newObject().name("ReturnObject")
            .build())
        .argument(newArgument().name(SortConstants.SORT_ARGUMENT_NAME)
            .type(GraphQLEnumType.newEnum()
                .name("TestOrder")
                .build())
            .build())
        .build());

    var collectionRequest = requestFactory.createCollectionRequest(typeConfiguration, environment);

    assertCollectionRequest(collectionRequest);
    assertThat(collectionRequest.getSortCriterias(), notNullValue());
    assertThat(collectionRequest.getSortCriterias(), equalTo(List.of(sortCriteria)));
  }

  @Test
  void createCollectionRequest_returnsCollectionRequest_forPaging() {
    when(dotWebStackConfiguration.isFeatureEnabled(Feature.PAGING)).thenReturn(true);

    when(environment.getLocalContext()).thenReturn(PagingDataFetcherContext.builder()
        .first(10)
        .offset(0)
        .build());

    when(environment.getExecutionStepInfo()).thenReturn(executionStepInfo);

    selectedFields.add(mockSelectedField(FIELD_IDENTIFIER));

    var fields = Map.of(FIELD_IDENTIFIER, identifierFieldConfiguration);

    when(typeConfiguration.getFields()).thenReturn(fields);

    when(environment.getArguments()).thenReturn(Map.of());

    var fieldDefinition = newFieldDefinition().name("testQuery")
        .type(newObject().name("ReturnObject")
            .build())
        .build();

    when(environment.getExecutionStepInfo()).thenReturn(executionStepInfo);
    when(environment.getExecutionStepInfo()
        .getParent()).thenReturn(executionStepInfo);
    when(executionStepInfo.getFieldDefinition()).thenReturn(fieldDefinition);

    var collectionRequest = requestFactory.createCollectionRequest(typeConfiguration, environment);

    assertCollectionRequest(collectionRequest);
    assertThat(collectionRequest.getPagingCriteria(), equalTo(PagingCriteria.builder()
        .offset(0)
        .first(10)
        .build()));

    verify(executionStepInfo, times(2)).getParent();
  }

  @Test
  void createObjectRequest_returnsObjectRequest_withScalarField() {
    selectedFields.add(mockSelectedField(FIELD_IDENTIFIER));

    Map<String, TestFieldConfiguration> fields = Map.of(FIELD_IDENTIFIER, identifierFieldConfiguration);

    when(environment.getSelectionSet()).thenReturn(selectionSet);
    when(selectionSet.getFields(fieldPathPrefix.concat("*.*"))).thenReturn(selectedFields);

    when(typeConfiguration.getFields()).thenReturn(fields);

    var objectRequest = requestFactory.createObjectRequest(typeConfiguration, environment);

    assertIdentifierScalarConfiguration(objectRequest);
  }

  @Test
  void createObjectRequest_returnsObjectRequest_withContextAndScalarField() {
    ContextFieldConfiguration contextFieldConfiguration = new ContextFieldConfiguration();
    contextFieldConfiguration.setType("Date");
    contextFieldConfiguration.setDefaultValue("NOW");

    ContextConfiguration contextConfiguration = new ContextConfiguration();
    contextConfiguration.setFields(Map.of("validOn", contextFieldConfiguration));

    when(dotWebStackConfiguration.getContext()).thenReturn(contextConfiguration);

    ExecutionStepInfo executionStepInfo = mock(ExecutionStepInfo.class);
    when(executionStepInfo.getArguments()).thenReturn(Map.of("context", Map.of("validOn", LocalDate.of(2020, 1, 1))));

    when(environment.getExecutionStepInfo()).thenReturn(executionStepInfo);

    selectedFields.add(mockSelectedField(FIELD_IDENTIFIER));

    Map<String, TestFieldConfiguration> fields = Map.of(FIELD_IDENTIFIER, identifierFieldConfiguration);

    when(environment.getSelectionSet()).thenReturn(selectionSet);
    when(selectionSet.getFields(fieldPathPrefix.concat("*.*"))).thenReturn(selectedFields);

    when(typeConfiguration.getFields()).thenReturn(fields);

    var objectRequest = requestFactory.createObjectRequest(typeConfiguration, environment);

    assertThat(objectRequest.getContextCriteria(), IsIterableContainingInOrder.contains(ContextCriteria.builder()
        .field("validOn")
        .value(LocalDate.of(2020, 1, 1))
        .build()));
  }

  @Test
  void createObjectRequest_returnsObjectRequest_withKeyCriteria() {
    selectedFields.add(mockSelectedField(FIELD_IDENTIFIER));

    Map<String, TestFieldConfiguration> fields = Map.of(FIELD_IDENTIFIER, identifierFieldConfiguration);

    when(environment.getSelectionSet()).thenReturn(selectionSet);
    when(selectionSet.getFields(fieldPathPrefix.concat("*.*"))).thenReturn(selectedFields);

    when(typeConfiguration.getFields()).thenReturn(fields);

    Map<String, Object> arguments = Map.of(FIELD_KEYCRITERIA, "1234-5678");
    when(environment.getArguments()).thenReturn(arguments);

    var objectRequest = requestFactory.createObjectRequest(typeConfiguration, environment);

    assertIdentifierScalarConfiguration(objectRequest);
    assertKeyCriterias(objectRequest);
  }

  @Test
  void createObjectRequest_returnsObjectRequestWithOneKeyCriteria_forArgumentsWithSortAndFilter() {
    selectedFields.add(mockSelectedField(FIELD_IDENTIFIER));

    Map<String, TestFieldConfiguration> fields = Map.of(FIELD_IDENTIFIER, identifierFieldConfiguration);

    when(environment.getSelectionSet()).thenReturn(selectionSet);
    when(selectionSet.getFields(fieldPathPrefix.concat("*.*"))).thenReturn(selectedFields);

    when(typeConfiguration.getFields()).thenReturn(fields);

    Map<String, Object> arguments =
        Map.of(FIELD_KEYCRITERIA, "1234-5678", SORT_KEYCRITERIA, "testSort", FILTER_KEYCRITERIA, "testFilter");
    when(environment.getArguments()).thenReturn(arguments);

    var objectRequest = requestFactory.createObjectRequest(typeConfiguration, environment);

    assertIdentifierScalarConfiguration(objectRequest);
    assertKeyCriterias(objectRequest);
  }

  @Test
  void createObjectRequest_returnsObjectRequest_forObjectField() {
    selectedFields.add(mockSelectedField(FIELD_IDENTIFIER));
    selectedFields.add(mockSelectedFieldWithQualifiedName(FIELD_BREWERY));

    var breweryFieldConfiguration = getTestFieldConfiguration(FIELD_BREWERY, OBJECT_FIELDCONFIGURATION);

    Map<String, TestFieldConfiguration> fields =
        Map.of(FIELD_IDENTIFIER, identifierFieldConfiguration, FIELD_BREWERY, breweryFieldConfiguration);

    when(environment.getSelectionSet()).thenReturn(selectionSet);
    when(selectionSet.getFields(fieldPathPrefix.concat("*.*"))).thenReturn(selectedFields);

    when(typeConfiguration.getFields()).thenReturn(fields);

    var objectRequest = requestFactory.createObjectRequest(typeConfiguration, environment);

    assertIdentifierScalarConfiguration(objectRequest);
    assertObjectFieldConfiguration(objectRequest);
  }

  @Test
  void createObjectRequest_returnsObjectRequest_forCollectionObjectField() {
    selectedFields.add(mockSelectedField(FIELD_IDENTIFIER));
    selectedFields.add(mockSelectedFieldWithQualifiedName(FIELD_BREWERY));

    var breweryFieldConfiguration = getTestFieldConfiguration(FIELD_BREWERY, COLLECTIONOBJECT_FIELDCONFIGURATION);

    Map<String, TestFieldConfiguration> fields =
        Map.of(FIELD_IDENTIFIER, identifierFieldConfiguration, FIELD_BREWERY, breweryFieldConfiguration);

    when(environment.getSelectionSet()).thenReturn(selectionSet);
    when(selectionSet.getFields(fieldPathPrefix.concat("*.*"))).thenReturn(selectedFields);

    when(typeConfiguration.getFields()).thenReturn(fields);

    var objectRequest = requestFactory.createObjectRequest(typeConfiguration, environment);

    assertIdentifierScalarConfiguration(objectRequest);
    assertCollectionObjectFieldConfiguration(objectRequest);
  }

  @Test
  void createObjectRequest_returnsObjectRequest_forNestedObjectField() {
    selectedFields.add(mockSelectedField(FIELD_IDENTIFIER));
    selectedFields.add(mockSelectedFieldWithQualifiedName(FIELD_HISTORY));

    var historyFieldConfiguration = getTestFieldConfiguration(FIELD_HISTORY, NESTEDOBJECT_FIELDCONFIGURATION);

    Map<String, TestFieldConfiguration> fields =
        Map.of(FIELD_IDENTIFIER, identifierFieldConfiguration, FIELD_HISTORY, historyFieldConfiguration);

    when(environment.getSelectionSet()).thenReturn(selectionSet);
    when(selectionSet.getFields(fieldPathPrefix.concat("*.*"))).thenReturn(selectedFields);

    when(typeConfiguration.getFields()).thenReturn(fields);

    var objectRequest = requestFactory.createObjectRequest(typeConfiguration, environment);

    assertIdentifierScalarConfiguration(objectRequest);
    assertNestedObjectFieldConfiguration(objectRequest);
  }

  @ParameterizedTest
  @MethodSource("aggregateTypes")
  void createObjectRequest_returnsObjectRequest_forSupportedAggregateFields(String aggregateFunction,
      ScalarType scalarType, String numericOrText) {
    selectedFields.add(mockSelectedField(FIELD_IDENTIFIER));
    selectedFields.add(mockSelectedFieldWithQualifiedName(FIELD_AGGREGATE));

    var aggregateTypeConfiguration = mock(TypeConfiguration.class);
    var aggregateFieldConfiguration = getTestFieldConfigurationWithTypeConfiguration(FIELD_AGGREGATE,
        AGGREGATE_FIELDCONFIGURATION, aggregateTypeConfiguration);

    Map<String, TestFieldConfiguration> fields =
        Map.of(FIELD_IDENTIFIER, identifierFieldConfiguration, FIELD_AGGREGATE, aggregateFieldConfiguration);

    when(typeConfiguration.getFields()).thenReturn(fields);

    var aggregateSelectedFields = List.of(mockSelectedAggregateField(aggregateFunction));

    var soldPerYearFieldConfiguration = getTestFieldConfiguration("soldPerYear", SCALAR_FIELDCONFIGURATION);

    switch (numericOrText) {
      case NUMERIC:
        soldPerYearFieldConfiguration.setNumeric(true);
        break;
      case TEXT:
        soldPerYearFieldConfiguration.setText(true);
        break;
      default:
        break;
    }

    Map<String, TestFieldConfiguration> aggregateFields = Map.of("soldPerYear", soldPerYearFieldConfiguration);

    when(selectionSet.getFields("aggregateField/*.*")).thenReturn(aggregateSelectedFields);
    when(aggregateTypeConfiguration.getFields()).thenReturn(aggregateFields);

    var objectRequest = requestFactory.createObjectRequest(typeConfiguration, environment);

    assertIdentifierScalarConfiguration(objectRequest);
    assertAggregateFieldConfiguration(objectRequest, aggregateFunction, scalarType);
  }

  @Test
  void createObjectRequest_throwsException_forUnsupportedAggregateField() {
    selectedFields.add(mockSelectedField(FIELD_IDENTIFIER));
    selectedFields.add(mockSelectedFieldWithQualifiedName(FIELD_AGGREGATE));

    var aggregateTypeConfiguration = mock(TypeConfiguration.class);
    var aggregateFieldConfiguration = getTestFieldConfigurationWithTypeConfiguration(FIELD_AGGREGATE,
        AGGREGATE_FIELDCONFIGURATION, aggregateTypeConfiguration);

    Map<String, TestFieldConfiguration> fields =
        Map.of(FIELD_IDENTIFIER, identifierFieldConfiguration, FIELD_AGGREGATE, aggregateFieldConfiguration);

    when(typeConfiguration.getFields()).thenReturn(fields);

    var aggregateSelectedFields = List.of(mockSelectedAggregateField("intRange"));

    var soldPerYearFieldConfiguration = getTestFieldConfiguration("soldPerYear", SCALAR_FIELDCONFIGURATION);

    Map<String, TestFieldConfiguration> aggregateFields = Map.of("soldPerYear", soldPerYearFieldConfiguration);

    when(selectionSet.getFields("aggregateField/*.*")).thenReturn(aggregateSelectedFields);
    when(aggregateTypeConfiguration.getFields()).thenReturn(aggregateFields);

    IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
        () -> requestFactory.createObjectRequest(typeConfiguration, environment));
    assertThat(thrown.getMessage(), is("Aggregate function intRange is not supported"));
  }

  private static Stream<Arguments> aggregateTypes() {
    return Stream.of(arguments(COUNT_FIELD, ScalarType.INT, ""), arguments(INT_AVG_FIELD, ScalarType.INT, NUMERIC),
        arguments(INT_MAX_FIELD, ScalarType.INT, NUMERIC), arguments(INT_MIN_FIELD, ScalarType.INT, NUMERIC),
        arguments(INT_SUM_FIELD, ScalarType.INT, NUMERIC), arguments(FLOAT_AVG_FIELD, ScalarType.FLOAT, NUMERIC),
        arguments(FLOAT_MIN_FIELD, ScalarType.FLOAT, NUMERIC), arguments(FLOAT_MAX_FIELD, ScalarType.FLOAT, NUMERIC),
        arguments(FLOAT_SUM_FIELD, ScalarType.FLOAT, NUMERIC), arguments(STRING_JOIN_FIELD, ScalarType.STRING, TEXT));
  }

  private void assertCollectionRequest(CollectionRequest collectionRequest) {
    assertIdentifierScalarConfiguration(collectionRequest.getObjectRequest());
  }

  private void assertIdentifierScalarConfiguration(ObjectRequest objectRequest) {
    var scalarField = objectRequest.getScalarFields()
        .stream()
        .filter(sf -> sf.getField()
            .getName()
            .equals(FIELD_IDENTIFIER))
        .findFirst()
        .orElseThrow();
    assertThat(scalarField.getName(), is(FIELD_IDENTIFIER));

    assertFieldTypes((TestFieldConfiguration) scalarField.getField(), true, false, false, false, false);

  }

  private void assertKeyCriterias(ObjectRequest objectRequest) {
    var keyCriterias = objectRequest.getKeyCriteria()
        .stream()
        .filter(keyCriteria -> keyCriteria.getValues()
            .containsKey(FIELD_KEYCRITERIA))
        .findFirst()
        .orElseThrow();
    assertThat(keyCriterias.getValues()
        .get(FIELD_KEYCRITERIA), is("1234-5678"));
    assertThat(objectRequest.getKeyCriteria()
        .size(), is(1));
  }

  private void assertObjectFieldConfiguration(ObjectRequest objectRequest) {
    var objectFieldConfiguration = objectRequest.getObjectFields()
        .stream()
        .filter(fieldConfiguration -> fieldConfiguration.getField()
            .getName()
            .equals(FIELD_BREWERY))
        .findFirst()
        .orElseThrow()
        .getField();
    assertThat(objectFieldConfiguration.getName(), is(FIELD_BREWERY));

    assertFieldTypes((TestFieldConfiguration) objectFieldConfiguration, false, false, false, true, false);
  }

  private void assertCollectionObjectFieldConfiguration(ObjectRequest objectRequest) {
    var collectionObjectFieldConfiguration = objectRequest.getCollectionObjectFields()
        .stream()
        .filter(fieldConfiguration -> fieldConfiguration.getField()
            .getName()
            .equals(FIELD_BREWERY))
        .findFirst()
        .orElseThrow()
        .getField();
    assertThat(collectionObjectFieldConfiguration.getName(), is(FIELD_BREWERY));

    assertFieldTypes((TestFieldConfiguration) collectionObjectFieldConfiguration, false, false, false, true, true);
  }

  private void assertNestedObjectFieldConfiguration(ObjectRequest objectRequest) {
    var nestedObjectFieldConfiguration = objectRequest.getNestedObjectFields()
        .stream()
        .filter(fieldConfiguration -> fieldConfiguration.getField()
            .getName()
            .equals(FIELD_HISTORY))
        .findFirst()
        .orElseThrow()
        .getField();
    assertThat(nestedObjectFieldConfiguration.getName(), is(FIELD_HISTORY));

    assertFieldTypes((TestFieldConfiguration) nestedObjectFieldConfiguration, false, false, true, false, false);
  }

  private void assertAggregateFieldConfiguration(ObjectRequest objectRequest, String aggregateFunction,
      ScalarType scalarType) {
    var aggregateFieldConfiguration = objectRequest.getAggregateObjectFields()
        .stream()
        .filter(fieldConfiguration -> fieldConfiguration.getField()
            .getName()
            .equals(FIELD_AGGREGATE))
        .findFirst()
        .orElseThrow()
        .getField();
    assertThat(aggregateFieldConfiguration.getName(), is(FIELD_AGGREGATE));

    assertFieldTypes((TestFieldConfiguration) aggregateFieldConfiguration, false, true, false, false, false);

    var aggregateFieldsResult = objectRequest.getAggregateObjectFields()
        .stream()
        .map(AggregateObjectFieldConfiguration::getAggregateFields)
        .findFirst()
        .orElseThrow();
    assertThat(aggregateFieldsResult.size(), is(1));

    assertThat(aggregateFieldsResult.get(0)
        .getAlias(), is(aggregateFunction));
    assertThat(aggregateFieldsResult.get(0)
        .getType(), is(scalarType));

    assertThat(aggregateFieldsResult.get(0)
        .getField()
        .getName(), is("soldPerYear"));
  }

  private void assertFieldTypes(TestFieldConfiguration testFieldConfiguration, boolean isScalar, boolean isAggregate,
      boolean isNestedObjectField, boolean isObjectField, boolean isList) {
    assertThat(testFieldConfiguration.isScalarField(), is(isScalar));
    assertThat(testFieldConfiguration.isAggregateField(), is(isAggregate));
    assertThat(testFieldConfiguration.isNestedObjectField(), is(isNestedObjectField));
    assertThat(testFieldConfiguration.isObjectField(), is(isObjectField));
    assertThat(testFieldConfiguration.isList(), is(isList));
  }

  private TestFieldConfiguration getTestFieldConfiguration(String name, String fieldType) {
    return getTestFieldConfigurationWithTypeConfiguration(name, fieldType, mock(TypeConfiguration.class));
  }

  @SuppressWarnings("rawtypes")
  private TestFieldConfiguration getTestFieldConfigurationWithTypeConfiguration(String name, String fieldType,
      TypeConfiguration typeConfiguration) {
    var fieldConfiguration = new TestFieldConfiguration();
    fieldConfiguration.setName(name);

    switch (fieldType) {
      case SCALAR_FIELDCONFIGURATION:
        fieldConfiguration.setScalarField(true);
        break;
      case AGGREGATE_FIELDCONFIGURATION:
        fieldConfiguration.setAggregateField(true);
        break;
      case NESTEDOBJECT_FIELDCONFIGURATION:
        fieldConfiguration.setNestedObjectField(true);
        break;
      case OBJECT_FIELDCONFIGURATION:
        fieldConfiguration.setObjectField(true);
        break;
      case COLLECTIONOBJECT_FIELDCONFIGURATION:
        fieldConfiguration.setObjectField(true);
        fieldConfiguration.setList(true);
        break;
      default:
        break;
    }

    fieldConfiguration.setTypeConfiguration(typeConfiguration);

    return fieldConfiguration;
  }

  private SelectedField mockSelectedFieldWithQualifiedName(String name) {
    SelectedField selectedField = mockSelectedField(name);
    when(selectedField.getFullyQualifiedName()).thenReturn(name);

    return selectedField;
  }

  private SelectedField mockSelectedAggregateField(String name) {
    SelectedField selectedField = mockSelectedField(name);

    Map<String, Object> arguments = Map.of(FIELD_ARGUMENT, "soldPerYear");
    when(selectedField.getArguments()).thenReturn(arguments);

    return selectedField;
  }

  private SelectedField mockSelectedField(String name) {
    var selectedField = mock(SelectedField.class);
    when(selectedField.getName()).thenReturn(name);

    var fieldDefinition = mock(GraphQLFieldDefinition.class);

    var type = mock(GraphQLObjectType.class);
    when(type.getName()).thenReturn("testType");

    when(fieldDefinition.getType()).thenReturn(type);

    when(selectedField.getFieldDefinition()).thenReturn(fieldDefinition);

    return selectedField;
  }

  @Data
  @EqualsAndHashCode(callSuper = true)
  static class TestFieldConfiguration extends AbstractFieldConfiguration {
    private boolean isScalarField = false;

    private boolean isObjectField = false;

    private boolean isNestedObjectField = false;

    private boolean isAggregateField = false;

    private TypeConfiguration<?> typeConfiguration;

    @Override
    public boolean isScalarField() {
      return isScalarField;
    }

    @Override
    public boolean isObjectField() {
      return isObjectField;
    }

    @Override
    public boolean isNestedObjectField() {
      return isNestedObjectField;
    }

    @Override
    public boolean isAggregateField() {
      return isAggregateField;
    }
  }
}

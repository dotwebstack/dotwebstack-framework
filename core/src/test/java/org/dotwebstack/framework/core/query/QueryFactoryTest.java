package org.dotwebstack.framework.core.query;

import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FIELD_ARGUMENT;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FLOAT_MIN_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.INT_AVG_FIELD;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.SelectedField;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.query.model.AggregateObjectFieldConfiguration;
import org.dotwebstack.framework.core.query.model.ObjectQuery;
import org.dotwebstack.framework.core.query.model.ScalarType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QueryFactoryTest {

  private static final String FIELD_IDENTIFIER = "identifier";

  private static final String FIELD_BREWERY = "brewery";

  private static final String FIELD_KEYCRITERIA = "fieldName";

  private static final String FIELD_HISTORY = "history";

  private static final String FIELD_AGGREGATE = "aggregateField";

  private static final String SCALAR_FIELDCONFIGURATION = "scalar";

  private static final String AGGREGATE_FIELDCONFIGURATION = "aggregate";

  private static final String NESTEDOBJECT_FIELDCONFIGURATION = "nestedObject";

  private static final String OBJECT_FIELDCONFIGURATION = "object";

  private QueryFactory queryFactory;

  @SuppressWarnings("rawtypes")
  private TypeConfiguration typeConfiguration;

  private DataFetchingFieldSelectionSet selectionSet;

  private String fieldPathPrefix;

  private TestFieldConfiguration identifierFieldConfiguration;

  private List<SelectedField> selectedFields;

  @Mock
  private DataFetchingEnvironment environment;

  @BeforeEach
  void beforeEach() {
    queryFactory = new QueryFactory();
    typeConfiguration = mock(TypeConfiguration.class);
    selectionSet = mock(DataFetchingFieldSelectionSet.class);
    fieldPathPrefix = "";
    identifierFieldConfiguration = getTestFieldConfiguration(FIELD_IDENTIFIER, SCALAR_FIELDCONFIGURATION);

    selectedFields = new ArrayList<>();
    when(environment.getSelectionSet()).thenReturn(selectionSet);
    when(selectionSet.getFields(fieldPathPrefix.concat("*.*"))).thenReturn(selectedFields);

  }

  @Test
  void createCollectionQuery_defaultWithScalarField() {
    selectedFields.add(mockSelectedField(FIELD_IDENTIFIER));

    Map<String, TestFieldConfiguration> fields = Map.of(FIELD_IDENTIFIER, identifierFieldConfiguration);

    when(typeConfiguration.getFields()).thenReturn(fields);

    var collectionQuery = queryFactory.createCollectionQuery(typeConfiguration, environment, true);

    assertCollectionQuery(collectionQuery);
  }

  @Test
  void createObjectQuery_defaultWithScalarField() {
    selectedFields.add(mockSelectedField(FIELD_IDENTIFIER));

    Map<String, TestFieldConfiguration> fields = Map.of(FIELD_IDENTIFIER, identifierFieldConfiguration);

    when(environment.getSelectionSet()).thenReturn(selectionSet);
    when(selectionSet.getFields(fieldPathPrefix.concat("*.*"))).thenReturn(selectedFields);

    when(typeConfiguration.getFields()).thenReturn(fields);

    var objectQuery = queryFactory.createObjectQuery(typeConfiguration, environment);

    assertIdentifierScalarConfiguration(objectQuery);
  }

  @Test
  void createObjectQuery_withKeyCriteria() {
    selectedFields.add(mockSelectedField(FIELD_IDENTIFIER));

    Map<String, TestFieldConfiguration> fields = Map.of(FIELD_IDENTIFIER, identifierFieldConfiguration);

    when(environment.getSelectionSet()).thenReturn(selectionSet);
    when(selectionSet.getFields(fieldPathPrefix.concat("*.*"))).thenReturn(selectedFields);

    when(typeConfiguration.getFields()).thenReturn(fields);

    Map<String, Object> arguments = Map.of(FIELD_KEYCRITERIA, "1234-5678");
    when(environment.getArguments()).thenReturn(arguments);

    var objectQuery = queryFactory.createObjectQuery(typeConfiguration, environment);

    assertIdentifierScalarConfiguration(objectQuery);
    assertKeyCriterias(objectQuery);
  }

  @Test
  void createObjectQuery_withObjectField() {
    selectedFields.add(mockSelectedField(FIELD_IDENTIFIER));
    selectedFields.add(mockSelectedFieldWithQualifiedName(FIELD_BREWERY));

    var breweryFieldConfiguration = getTestFieldConfiguration(FIELD_BREWERY, OBJECT_FIELDCONFIGURATION);

    Map<String, TestFieldConfiguration> fields =
        Map.of(FIELD_IDENTIFIER, identifierFieldConfiguration, FIELD_BREWERY, breweryFieldConfiguration);

    when(environment.getSelectionSet()).thenReturn(selectionSet);
    when(selectionSet.getFields(fieldPathPrefix.concat("*.*"))).thenReturn(selectedFields);

    when(typeConfiguration.getFields()).thenReturn(fields);

    var objectQuery = queryFactory.createObjectQuery(typeConfiguration, environment);

    assertIdentifierScalarConfiguration(objectQuery);
    assertObjectFieldConfiguration(objectQuery);
  }

  @Test
  void createObjectQuery_withNestedObjectField() {
    selectedFields.add(mockSelectedField(FIELD_IDENTIFIER));
    selectedFields.add(mockSelectedFieldWithQualifiedName(FIELD_HISTORY));

    var historyFieldConfiguration = getTestFieldConfiguration(FIELD_HISTORY, NESTEDOBJECT_FIELDCONFIGURATION);

    Map<String, TestFieldConfiguration> fields =
        Map.of(FIELD_IDENTIFIER, identifierFieldConfiguration, FIELD_HISTORY, historyFieldConfiguration);

    when(environment.getSelectionSet()).thenReturn(selectionSet);
    when(selectionSet.getFields(fieldPathPrefix.concat("*.*"))).thenReturn(selectedFields);

    when(typeConfiguration.getFields()).thenReturn(fields);

    var objectQuery = queryFactory.createObjectQuery(typeConfiguration, environment);

    assertIdentifierScalarConfiguration(objectQuery);
    assertNestedObjectFieldConfiguration(objectQuery);
  }

  @Test
  void createObjectQuery_withAggregateField() {
    selectedFields.add(mockSelectedField(FIELD_IDENTIFIER));
    selectedFields.add(mockSelectedFieldWithQualifiedName(FIELD_AGGREGATE));

    var aggregateTypeConfiguration = mock(TypeConfiguration.class);
    var aggregateFieldConfiguration = getTestFieldConfigurationWithTypeConfiguration(FIELD_AGGREGATE,
        AGGREGATE_FIELDCONFIGURATION, aggregateTypeConfiguration);

    Map<String, TestFieldConfiguration> fields =
        Map.of(FIELD_IDENTIFIER, identifierFieldConfiguration, FIELD_AGGREGATE, aggregateFieldConfiguration);

    when(typeConfiguration.getFields()).thenReturn(fields);

    var aggregateSelectedFields =
        List.of(mockSelectedAggregateField(INT_AVG_FIELD), mockSelectedAggregateField(FLOAT_MIN_FIELD));

    var soldPerYearFieldConfiguration = getTestFieldConfiguration("soldPerYear", SCALAR_FIELDCONFIGURATION);

    Map<String, TestFieldConfiguration> aggregateFields = Map.of("soldPerYear", soldPerYearFieldConfiguration);

    when(selectionSet.getFields("aggregateField/*.*")).thenReturn(aggregateSelectedFields);
    when(aggregateTypeConfiguration.getFields()).thenReturn(aggregateFields);

    var objectQuery = queryFactory.createObjectQuery(typeConfiguration, environment);

    assertIdentifierScalarConfiguration(objectQuery);
    assertAggregateFieldConfiguration(objectQuery);
  }

  private void assertCollectionQuery(org.dotwebstack.framework.core.query.model.CollectionQuery collectionQuery) {
    assertThat(collectionQuery.getPagingCriteria()
        .getPage(), is(0));
    assertThat(collectionQuery.getPagingCriteria()
        .getPageSize(), is(10));

    assertIdentifierScalarConfiguration(collectionQuery.getObjectQuery());
  }

  private void assertIdentifierScalarConfiguration(ObjectQuery objectQuery) {
    var scalarFieldConfigurationName = objectQuery.getScalarFields()
        .stream()
        .filter(fieldConfiguration -> fieldConfiguration.getName()
            .equals(FIELD_IDENTIFIER))
        .findFirst()
        .orElseThrow()
        .getName();
    assertThat(scalarFieldConfigurationName, is(FIELD_IDENTIFIER));
  }

  private void assertKeyCriterias(ObjectQuery objectQuery) {
    var keyCriterias = objectQuery.getKeyCriteria()
        .stream()
        .filter(keyCriteria -> keyCriteria.getValues()
            .containsKey(FIELD_KEYCRITERIA))
        .findFirst()
        .orElseThrow();
    assertThat(keyCriterias.getValues()
        .get(FIELD_KEYCRITERIA), is("1234-5678"));
  }

  private void assertObjectFieldConfiguration(ObjectQuery objectQuery) {
    var objectFieldConfigurationName = objectQuery.getObjectFields()
        .stream()
        .filter(fieldConfiguration -> fieldConfiguration.getField()
            .getName()
            .equals(FIELD_BREWERY))
        .findFirst()
        .orElseThrow()
        .getField()
        .getName();
    assertThat(objectFieldConfigurationName, is(FIELD_BREWERY));
  }

  private void assertNestedObjectFieldConfiguration(ObjectQuery objectQuery) {
    var nestedObjectFieldConfigurationName = objectQuery.getNestedObjectFields()
        .stream()
        .filter(fieldConfiguration -> fieldConfiguration.getField()
            .getName()
            .equals(FIELD_HISTORY))
        .findFirst()
        .orElseThrow()
        .getField()
        .getName();
    assertThat(nestedObjectFieldConfigurationName, is(FIELD_HISTORY));
  }

  private void assertAggregateFieldConfiguration(ObjectQuery objectQuery) {
    var aggregateFieldConfigurationName = objectQuery.getAggregateObjectFields()
        .stream()
        .filter(fieldConfiguration -> fieldConfiguration.getField()
            .getName()
            .equals(FIELD_AGGREGATE))
        .findFirst()
        .orElseThrow()
        .getField()
        .getName();
    assertThat(aggregateFieldConfigurationName, is(FIELD_AGGREGATE));

    var aggregateFieldsResult = objectQuery.getAggregateObjectFields()
        .stream()
        .map(AggregateObjectFieldConfiguration::getAggregateFields)
        .findFirst()
        .orElseThrow();
    assertThat(aggregateFieldsResult.size(), is(2));

    assertThat(aggregateFieldsResult.get(0)
        .getAlias(), is(INT_AVG_FIELD));
    assertThat(aggregateFieldsResult.get(0)
        .getType(), is(ScalarType.INT));
    assertThat(aggregateFieldsResult.get(1)
        .getAlias(), is(FLOAT_MIN_FIELD));
    assertThat(aggregateFieldsResult.get(1)
        .getType(), is(ScalarType.FLOAT));

    assertThat(aggregateFieldsResult.get(0)
        .getField()
        .getName(), is("soldPerYear"));
    assertThat(aggregateFieldsResult.get(1)
        .getField()
        .getName(), is("soldPerYear"));
  }

  private TestFieldConfiguration getTestFieldConfiguration(String name, String fieldType) {
    return getTestFieldConfigurationWithTypeConfiguration(name, fieldType, null);
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
      default:
        break;
    }

    fieldConfiguration.typeConfiguration = typeConfiguration;

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
    SelectedField selectedField = mock(SelectedField.class);
    when(selectedField.getName()).thenReturn(name);

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

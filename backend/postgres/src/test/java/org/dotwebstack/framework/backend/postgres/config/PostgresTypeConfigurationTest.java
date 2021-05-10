package org.dotwebstack.framework.backend.postgres.config;

import static graphql.language.FieldDefinition.newFieldDefinition;
import static graphql.language.ObjectTypeDefinition.newObjectTypeDefinition;
import static graphql.language.TypeName.newTypeName;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import graphql.Scalars;
import graphql.language.ObjectTypeDefinition;
import graphql.schema.DataFetchingEnvironment;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.KeyConfiguration;
import org.dotwebstack.framework.core.datafetchers.KeyCondition;
import org.dotwebstack.framework.core.datafetchers.MappedByKeyCondition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostgresTypeConfigurationTest {

  private static final String FIELD_IDENTIFIER = "identifier";

  private static final String FIELD_NAME = "name";

  private static final String FIELD_PART_OF = "partOf";

  private static final String BEER_TYPE_NAME = "Beer";

  @Mock
  private DotWebStackConfiguration dotWebStackConfiguration;

  @Mock
  Map<String, AbstractTypeConfiguration<?>> objectTypesMock;

  @BeforeEach
  public void beforeEach() {
    dotWebStackConfigurationMock();
  }

  @Test
  @SuppressWarnings({"rawtypes", "unchecked"})
  void init_shouldWork_withValidConfiguration() {
    JoinColumn joinColumn = createJoinColumnWithReferencedField("beer_identifier", "identifier_beer");
    JoinColumn inversedJoinColumn = createJoinColumnWithReferencedColumn("ingredient_code", "code");

    PostgresTypeConfiguration typeConfiguration = createTypeConfiguration(joinColumn, inversedJoinColumn);

    ObjectTypeDefinition objectTypeDefinition = createObjectTypeDefinition();
    AbstractTypeConfiguration postgresTypeConfiguration = new PostgresTypeConfiguration();

    when(objectTypesMock.get(BEER_TYPE_NAME)).thenReturn(postgresTypeConfiguration);

    assertDoesNotThrow(() -> typeConfiguration.init(dotWebStackConfiguration, objectTypeDefinition));
  }

  @Test
  void init_shouldWork_withAggregationOfConfiguration() {
    JoinColumn joinColumn = createJoinColumnWithReferencedField("beer_identifier", "identifier_beer");
    JoinColumn inversedJoinColumn = createJoinColumnWithReferencedColumn("ingredient_code", "code");

    PostgresTypeConfiguration typeConfiguration = createTypeConfiguration(joinColumn, inversedJoinColumn, "Beers");

    ObjectTypeDefinition objectTypeDefinition = createObjectTypeDefinition();

    assertDoesNotThrow(() -> typeConfiguration.init(dotWebStackConfiguration, objectTypeDefinition));
  }

  @Test
  void init_shouldThrowException_whenReferencedFieldAndReferencedColumnAreNull() {
    JoinColumn joinColumn = createJoinColumnWithReferencedField("beer_identifier", "identifier_beer");
    JoinColumn inverseJoinColumn = createJoinColumnWithReferencedFieldAndColumn("ingredient_code", null, null);

    PostgresTypeConfiguration typeConfiguration = createTypeConfiguration(joinColumn, inverseJoinColumn);

    ObjectTypeDefinition objectTypeDefinition = createObjectTypeDefinition();

    InvalidConfigurationException thrown = assertThrows(InvalidConfigurationException.class,
        () -> typeConfiguration.init(dotWebStackConfiguration, objectTypeDefinition));

    assertThat(thrown.getMessage(),
        is("The field 'referencedField' or 'referencedColumn' must have a value in field 'partOf'."));
  }

  @Test
  void init_shouldThrowException_whenReferencedFieldAndReferencedColumnBothHaveValues() {
    JoinColumn joinColumn = createJoinColumnWithReferencedField("beer_identifier", "identifier_beer");
    JoinColumn inverseJoinColumn = createJoinColumnWithReferencedFieldAndColumn("ingredient_code", "code", "code");

    PostgresTypeConfiguration typeConfiguration = createTypeConfiguration(joinColumn, inverseJoinColumn);

    ObjectTypeDefinition objectTypeDefinition = createObjectTypeDefinition();

    InvalidConfigurationException thrown = assertThrows(InvalidConfigurationException.class,
        () -> typeConfiguration.init(dotWebStackConfiguration, objectTypeDefinition));

    assertThat(thrown.getMessage(),
        is("The field 'referencedField' or 'referencedColumn' must have a value in field 'partOf'."));
  }

  @Test
  @SuppressWarnings({"rawtypes", "unchecked"})
  void init_shouldThrowException_whenTargetTypeConfigurationDoesNotMatchPostgresTypeConfiguration() {
    JoinColumn joinColumn = createJoinColumnWithReferencedField("beer_identifier", "identifier_beer");
    JoinColumn inverseJoinColumn = createJoinColumnWithReferencedColumn("ingredient_code", "code");

    PostgresTypeConfiguration typeConfiguration = createTypeConfiguration(joinColumn, inverseJoinColumn);

    ObjectTypeDefinition objectTypeDefinition = createObjectTypeDefinition();

    AbstractTypeConfiguration testTypeConfiguration = new TestTypeConfiguration();

    when(objectTypesMock.get(BEER_TYPE_NAME)).thenReturn(testTypeConfiguration);

    InvalidConfigurationException thrown = assertThrows(InvalidConfigurationException.class,
        () -> typeConfiguration.init(dotWebStackConfiguration, objectTypeDefinition));

    assertThat(thrown.getMessage(),
        is(String.format("Target objectType must be an 'PostgresTypeConfiguration' but is an '%s'.",
            testTypeConfiguration.getClass())));
  }

  private ObjectTypeDefinition createObjectTypeDefinition() {
    return newObjectTypeDefinition().name("Ingredient")
        .fieldDefinition(newFieldDefinition().name(FIELD_IDENTIFIER)
            .type(newTypeName(Scalars.GraphQLString.getName()).build())
            .build())
        .fieldDefinition(newFieldDefinition().name(FIELD_NAME)
            .type(newTypeName(Scalars.GraphQLString.getName()).build())
            .build())
        .fieldDefinition(newFieldDefinition().name(FIELD_PART_OF)
            .type(newTypeName(BEER_TYPE_NAME).build())
            .build())
        .build();
  }

  private JoinColumn createJoinColumnWithReferencedField(String name, String fieldName) {
    JoinColumn joinColumn = new JoinColumn();
    joinColumn.setName(name);
    joinColumn.setReferencedField(fieldName);

    return joinColumn;
  }

  private JoinColumn createJoinColumnWithReferencedColumn(String name, String columnName) {
    JoinColumn joinColumn = new JoinColumn();
    joinColumn.setName(name);
    joinColumn.setReferencedColumn(columnName);

    return joinColumn;
  }

  private JoinColumn createJoinColumnWithReferencedFieldAndColumn(String name, String fieldName, String columnName) {
    JoinColumn joinColumn = new JoinColumn();
    joinColumn.setName(name);
    joinColumn.setReferencedField(fieldName);
    joinColumn.setReferencedColumn(columnName);

    return joinColumn;
  }

  private PostgresTypeConfiguration createTypeConfiguration(JoinColumn joinColumn, JoinColumn inverseJoinColumn) {
    return createTypeConfiguration(joinColumn, inverseJoinColumn, null);
  }

  private PostgresTypeConfiguration createTypeConfiguration(JoinColumn joinColumn, JoinColumn inverseJoinColumn,
      String aggregationOf) {
    PostgresTypeConfiguration typeConfiguration = new PostgresTypeConfiguration();
    KeyConfiguration keyConfiguration = new KeyConfiguration();
    keyConfiguration.setField(FIELD_IDENTIFIER);
    typeConfiguration.setKeys(List.of(keyConfiguration));

    JoinTable joinTable = createJoinTable(joinColumn, inverseJoinColumn);
    PostgresFieldConfiguration fieldConfiguration = createPostgresFieldConfiguration(joinTable);

    if (StringUtils.isNoneBlank(aggregationOf)) {
      fieldConfiguration.setAggregationOf(aggregationOf);
    }

    Map<String, PostgresFieldConfiguration> fieldsMap =
        new HashMap<>(Map.of(FIELD_IDENTIFIER, new PostgresFieldConfiguration(), FIELD_PART_OF, fieldConfiguration));

    typeConfiguration.setFields(fieldsMap);

    typeConfiguration.setTable("db.ingredient");

    return typeConfiguration;
  }

  private JoinTable createJoinTable(JoinColumn joinColumn, JoinColumn inverseJoinColumn) {
    JoinTable joinTable = new JoinTable();
    joinTable.setName("db.beer_ingredient");

    if (joinColumn != null) {
      joinTable.setJoinColumns(List.of(joinColumn));
    }

    if (inverseJoinColumn != null) {
      joinTable.setJoinColumns(List.of(inverseJoinColumn));
    }

    return joinTable;
  }

  private PostgresFieldConfiguration createPostgresFieldConfiguration(JoinTable joinTable) {
    PostgresFieldConfiguration fieldConfiguration = new PostgresFieldConfiguration();

    fieldConfiguration.setJoinTable(joinTable);

    return fieldConfiguration;
  }

  static class TestFieldConfiguration extends AbstractFieldConfiguration {
  }

  static class TestTypeConfiguration extends AbstractTypeConfiguration<TestFieldConfiguration> {
    @Override
    public KeyCondition getKeyCondition(DataFetchingEnvironment environment) {
      return null;
    }

    @Override
    public KeyCondition getKeyCondition(String fieldName, Map<String, Object> source) {
      return null;
    }

    @Override
    public KeyCondition invertKeyCondition(MappedByKeyCondition mappedByKeyCondition, Map<String, Object> source) {
      return null;
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void dotWebStackConfigurationMock() {
    when(dotWebStackConfiguration.getObjectTypes()).thenReturn(objectTypesMock);
    when(objectTypesMock.get(null)).thenReturn(null);
  }
}

package org.dotwebstack.framework.backend.postgres.config;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.KeyConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostgresTypeConfigurationTest {

  private static final String FIELD_IDENTIFIER = "identifier";

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

    AbstractTypeConfiguration postgresTypeConfiguration = new PostgresTypeConfiguration();

    lenient().when(objectTypesMock.get(BEER_TYPE_NAME))
        .thenReturn(postgresTypeConfiguration);

    assertDoesNotThrow(() -> typeConfiguration.init(dotWebStackConfiguration));
  }

  @Test
  void init_shouldWork_withAggregationOfConfiguration() {
    JoinColumn joinColumn = createJoinColumnWithReferencedField("beer_identifier", "identifier_beer");
    JoinColumn inversedJoinColumn = createJoinColumnWithReferencedColumn("ingredient_code", "code");

    PostgresTypeConfiguration typeConfiguration =
        createTypeConfiguration(joinColumn, inversedJoinColumn, BEER_TYPE_NAME);

    assertDoesNotThrow(() -> typeConfiguration.init(dotWebStackConfiguration));
  }

  @Test
  void init_shouldThrowException_whenReferencedFieldAndReferencedColumnAreNull() {
    JoinColumn joinColumn = createJoinColumnWithReferencedField("beer_identifier", "identifier_beer");
    JoinColumn inverseJoinColumn = createJoinColumnWithReferencedFieldAndColumn("ingredient_code", null, null);

    PostgresTypeConfiguration typeConfiguration = createTypeConfiguration(joinColumn, inverseJoinColumn);

    InvalidConfigurationException thrown =
        assertThrows(InvalidConfigurationException.class, () -> typeConfiguration.init(dotWebStackConfiguration));

    assertThat(thrown.getMessage(),
        is("The field 'referencedField' or 'referencedColumn' must have a value in field 'partOf'."));
  }

  @Test
  void init_shouldThrowException_whenReferencedFieldAndReferencedColumnBothHaveValues() {
    JoinColumn joinColumn = createJoinColumnWithReferencedField("beer_identifier", "identifier_beer");
    JoinColumn inverseJoinColumn = createJoinColumnWithReferencedFieldAndColumn("ingredient_code", "code", "code");

    PostgresTypeConfiguration typeConfiguration = createTypeConfiguration(joinColumn, inverseJoinColumn);

    InvalidConfigurationException thrown =
        assertThrows(InvalidConfigurationException.class, () -> typeConfiguration.init(dotWebStackConfiguration));

    assertThat(thrown.getMessage(),
        is("The field 'referencedField' or 'referencedColumn' must have a value in field 'partOf'."));
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
    fieldConfiguration.setType("Beer");

    if (StringUtils.isNoneBlank(aggregationOf)) {
      fieldConfiguration.setAggregationOf(aggregationOf);
    }

    PostgresFieldConfiguration stringFieldConfiguration = new PostgresFieldConfiguration();
    stringFieldConfiguration.setType("String");

    Map<String, PostgresFieldConfiguration> fieldsMap =
        new HashMap<>(Map.of(FIELD_IDENTIFIER, stringFieldConfiguration, FIELD_PART_OF, fieldConfiguration));

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

  private void dotWebStackConfigurationMock() {
    when(dotWebStackConfiguration.getObjectTypes()).thenReturn(objectTypesMock);
    lenient().when(objectTypesMock.get(null))
        .thenReturn(null);
  }
}

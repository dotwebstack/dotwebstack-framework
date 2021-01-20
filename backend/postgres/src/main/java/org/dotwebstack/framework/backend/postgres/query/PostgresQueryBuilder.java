package org.dotwebstack.framework.backend.postgres.query;

import static java.util.Collections.emptyList;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.notImplementedException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.lateral;
import static org.jooq.impl.DSL.trueCondition;

import graphql.schema.GraphQLTypeUtil;
import graphql.schema.SelectedField;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.dotwebstack.framework.backend.postgres.config.JoinColumn;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.LoadEnvironment;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectJoinStep;
import org.jooq.Table;
import org.jooq.TableLike;

public class PostgresQueryBuilder {

  private final DotWebStackConfiguration dotWebStackConfiguration;

  private final DSLContext dslContext;

  private final AtomicInteger tableCounter = new AtomicInteger();

  private final AtomicInteger selectCounter = new AtomicInteger();

  public PostgresQueryBuilder(DotWebStackConfiguration dotWebStackConfiguration, DSLContext dslContext) {
    this.dotWebStackConfiguration = dotWebStackConfiguration;
    this.dslContext = dslContext;
  }

  public PostgresQueryHolder build(PostgresTypeConfiguration typeConfiguration, LoadEnvironment loadEnvironment,
      Object key) {
    return build(typeConfiguration, loadEnvironment, null, key);
  }

  private PostgresQueryHolder build(PostgresTypeConfiguration typeConfiguration, LoadEnvironment loadEnvironment,
      JoinInformation joinInformation, Object key) {
    Table<Record> fromTable = typeConfiguration.getSqlTable()
        .as(newTableAlias());
    Map<String, Object> fieldAliasMap = new HashMap<>();

    List<Field<Object>> selectedColumns =
        getDirectFields(typeConfiguration, loadEnvironment.getSelectedFields(), fieldAliasMap);

    List<NestedQueryResult> nestedQueryResults = getNestedResults(typeConfiguration,
        loadEnvironment.getSelectedFields(), fromTable.getName(), fieldAliasMap, selectedColumns);

    SelectJoinStep<Record> query = dslContext.select(selectedColumns)
        .from(fromTable);

    for (NestedQueryResult nestedResult : nestedQueryResults) {
      Table<Record> nestedQuery = nestedResult.getTable();
      query.leftJoin(lateral(nestedQuery))
          .on(trueCondition());
    }

    if (joinInformation != null) {
      Field<Object> self = field(fromTable.getName()
          .concat(".")
          .concat(joinInformation.getReferencedField()));
      query.where(joinInformation.getParent()
          .eq(self));
    }

    if (loadEnvironment.getKeyArguments() != null) {
      loadEnvironment.getKeyArguments()
          .forEach(keyArgument -> query.where(field(fromTable.getName()
              .concat(".")
              .concat(keyArgument.getName())).eq(keyArgument.getValue())));
    }

    return PostgresQueryHolder.builder()
        .query(query)
        .fieldAliasMap(fieldAliasMap)
        .build();
  }

  private List<NestedQueryResult> getNestedResults(PostgresTypeConfiguration typeConfiguration,
      List<SelectedField> selectedFields, String tableName, Map<String, Object> fieldAliasMap,
      List<Field<Object>> selectedColumns) {
    return selectedFields.stream()
        .filter(selectedField -> !GraphQLTypeUtil.isLeaf(selectedField.getFieldDefinition()
            .getType()))
        .map(selectedField -> processNested(getJoinInformation(typeConfiguration, selectedField, tableName),
            selectedField))
        .map(Optional::get)
        .peek(nestedQueryResult -> {
          fieldAliasMap.put(nestedQueryResult.getSelectedField()
              .getResultKey(), nestedQueryResult.getColumnAliasMap());
          selectedColumns.add(nestedQueryResult.getSelectedColumn());
        })
        .collect(Collectors.toList());
  }

  private JoinInformation getJoinInformation(PostgresTypeConfiguration typeConfiguration, SelectedField selectedField,
      String tableName) {
    PostgresFieldConfiguration postgresFieldConfiguration = typeConfiguration.getFields()
        .get(selectedField.getName());

    if (postgresFieldConfiguration.getMappedBy() != null) {
      throw notImplementedException("The 'mappedBy' configuration item is not implemented yet!");
    }

    if (postgresFieldConfiguration.getJoinColumns() == null) {
      throw invalidConfigurationException("Missing relation configuration for table '{}' and field `{}`", tableName,
          selectedField.getName());
    }

    if (postgresFieldConfiguration.getJoinColumns()
        .size() > 1) {
      throw unsupportedOperationException("Multiple joinColumns for a single property aren't supported yet!");
    }

    JoinColumn joinColumn = postgresFieldConfiguration.getJoinColumns()
        .get(0);

    return JoinInformation.builder()
        .parent(field(tableName.concat(".")
            .concat(joinColumn.getName())))
        .referencedField(joinColumn.getReferencedField())
        .build();
  }

  private List<Field<Object>> getDirectFields(PostgresTypeConfiguration typeConfiguration,
      List<SelectedField> selectedFields, Map<String, Object> fieldAliasMap) {
    return selectedFields.stream()
        .filter(selectedField -> GraphQLTypeUtil.isLeaf(selectedField.getFieldDefinition()
            .getType()))
        .map(selectedField -> {
          Field<Object> column = createSelectedColumn(typeConfiguration, selectedField);
          fieldAliasMap.put(selectedField.getResultKey(), column.getName());
          return column;
        })
        .collect(Collectors.toList());
  }

  @SuppressWarnings("unchecked")
  private Optional<NestedQueryResult> processNested(JoinInformation joinInformation, SelectedField nestedField) {
    String nestedName = GraphQLTypeUtil.unwrapAll(nestedField.getFieldDefinition()
        .getType())
        .getName();

    TypeConfiguration<?> nestedTypeConfiguration = dotWebStackConfiguration.getTypeMapping()
        .get(nestedName);

    // Non-Postgres-backed objects can never be eager-loaded
    if (!(nestedTypeConfiguration instanceof PostgresTypeConfiguration)) {
      return Optional.empty();
    }

    List<SelectedField> nestedSelectedFields = nestedField.getSelectionSet()
        .getImmediateFields();

    LoadEnvironment loadEnvironment = LoadEnvironment.builder()
        .selectedFields(nestedSelectedFields)
        .keyArguments(emptyList())
        .build();

    PostgresQueryHolder queryHolder =
        build((PostgresTypeConfiguration) nestedTypeConfiguration, loadEnvironment, joinInformation, null);

    String joinAlias = newTableAlias();

    return Optional.of(NestedQueryResult.builder()
        .selectedField(nestedField)
        .selectedColumn(field(joinAlias.concat(".*")))
        .typeConfiguration((PostgresTypeConfiguration) nestedTypeConfiguration)
        .table(((TableLike<Record>) queryHolder.getQuery()).asTable(joinAlias))
        .columnAliasMap(queryHolder.getFieldAliasMap())
        .build());
  }

  private Field<Object> createSelectedColumn(PostgresTypeConfiguration typeConfiguration, SelectedField selectedField) {
    PostgresFieldConfiguration fieldConfiguration = typeConfiguration.getFields()
        .get(selectedField.getName());

    return field(fieldConfiguration.getSqlColumnName()).as(newSelectAlias());
  }

  private String newTableAlias() {
    return "t".concat(String.valueOf(tableCounter.incrementAndGet()));
  }

  private String newSelectAlias() {
    return "x".concat(String.valueOf(selectCounter.incrementAndGet()));
  }
}

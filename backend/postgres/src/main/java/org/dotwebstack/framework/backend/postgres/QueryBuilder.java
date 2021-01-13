package org.dotwebstack.framework.backend.postgres;

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
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectJoinStep;
import org.jooq.Table;
import org.jooq.TableLike;

class QueryBuilder {

  private final DotWebStackConfiguration dotWebStackConfiguration;

  private final DSLContext dslContext;

  private final AtomicInteger tableCounter = new AtomicInteger();

  private final AtomicInteger selectCounter = new AtomicInteger();

  public QueryBuilder(DotWebStackConfiguration dotWebStackConfiguration, DSLContext dslContext) {
    this.dotWebStackConfiguration = dotWebStackConfiguration;
    this.dslContext = dslContext;
  }

  public QueryWithAliasMap build(PostgresTypeConfiguration typeConfiguration, List<SelectedField> selectedFields) {
    return build(typeConfiguration, selectedFields, null);
  }

  private QueryWithAliasMap build(PostgresTypeConfiguration typeConfiguration, List<SelectedField> selectedFields,
      JoinInformation joinInformation) {
    Table<Record> fromTable = typeConfiguration.getSqlTable()
        .as(newTableAlias());
    Map<String, Object> columnAliasMap = new HashMap<>();

    List<Field<Object>> selectedColumns = getDirectFields(typeConfiguration, selectedFields, columnAliasMap);

    List<NestedQueryResult> nestedQueryResults =
        getNestedResults(typeConfiguration, selectedFields, fromTable, columnAliasMap, selectedColumns);

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

    return QueryWithAliasMap.builder()
        .query(query)
        .columnAliasMap(columnAliasMap)
        .build();
  }

  private List<NestedQueryResult> getNestedResults(PostgresTypeConfiguration typeConfiguration,
      List<SelectedField> selectedFields, Table<Record> fromTable, Map<String, Object> columnAliasMap,
      List<Field<Object>> selectedColumns) {
    return selectedFields.stream()
        .filter(selectedField -> !GraphQLTypeUtil.isLeaf(selectedField.getFieldDefinition()
            .getType()))
        .map(selectedField -> processNested(getJoinColumn(typeConfiguration, selectedField, fromTable), selectedField))
        .map(Optional::get)
        .peek(nestedQueryResult -> {
          columnAliasMap.put(nestedQueryResult.getSelectedField()
              .getResultKey(), nestedQueryResult.getColumnAliasMap());
          selectedColumns.add(nestedQueryResult.getSelectedColumn());
        })
        .collect(Collectors.toList());
  }

  private JoinInformation getJoinColumn(PostgresTypeConfiguration typeConfiguration, SelectedField selectedField,
      Table<Record> table) {
    PostgresFieldConfiguration postgresFieldConfiguration = typeConfiguration.getFields()
        .get(selectedField.getName());
    JoinColumn joinColumn = postgresFieldConfiguration.getJoinColumns()
        .get(0);

    return JoinInformation.builder()
        .parent(field(table.getName()
            .concat(".")
            .concat(joinColumn.getName())))
        .referencedField(joinColumn.getReferencedField())
        .build();
  }

  private List<Field<Object>> getDirectFields(PostgresTypeConfiguration typeConfiguration,
      List<SelectedField> selectedFields, Map<String, Object> columnAliasMap) {
    return selectedFields.stream()
        .filter(selectedField -> GraphQLTypeUtil.isLeaf(selectedField.getFieldDefinition()
            .getType()))
        .map(selectedField -> {
          Field<Object> column = createSelectedColumn(typeConfiguration, selectedField);
          columnAliasMap.put(selectedField.getResultKey(), column.getName());
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

    QueryWithAliasMap queryWithAliasMap =
        build((PostgresTypeConfiguration) nestedTypeConfiguration, nestedSelectedFields, joinInformation);

    String joinAlias = newTableAlias();

    return Optional.of(NestedQueryResult.builder()
        .selectedField(nestedField)
        .selectedColumn(field(joinAlias.concat(".*")))
        .typeConfiguration((PostgresTypeConfiguration) nestedTypeConfiguration)
        .table(((TableLike<Record>) queryWithAliasMap.getQuery()).asTable(joinAlias))
        .columnAliasMap(queryWithAliasMap.getColumnAliasMap())
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

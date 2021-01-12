package org.dotwebstack.framework.backend.postgres;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.lateral;
import static org.jooq.impl.DSL.trueCondition;

import graphql.schema.GraphQLTypeUtil;
import graphql.schema.SelectedField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
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
    return internalBuild(typeConfiguration, selectedFields);
  }

  public QueryWithAliasMap internalBuild(PostgresTypeConfiguration typeConfiguration, List<SelectedField> selectedFields) {
    Table<Record> fromTable = typeConfiguration.getSqlTable().as(newTableAlias());
    Map<String, Object> columnAliasMap = new HashMap<>();

    List<Field<Object>> selectedColumns = selectedFields.stream()
        .filter(selectedField -> GraphQLTypeUtil.isLeaf(selectedField.getFieldDefinition().getType()))
        .map(selectedField -> {
          Field<Object> column = createSelectedColumn(typeConfiguration, selectedField);
          columnAliasMap.put(selectedField.getResultKey(), column.getName());
          return column;
        })
        .collect(Collectors.toList());

    List<Table<Record>> joinTables = new ArrayList<>();

    selectedFields.stream()
        .filter(selectedField -> !GraphQLTypeUtil.isLeaf(selectedField.getFieldDefinition().getType()))
        .forEach(nestedField -> {
          String nestedName = GraphQLTypeUtil.unwrapAll(nestedField.getFieldDefinition()
              .getType())
              .getName();

          TypeConfiguration<?> nestedTypeConfiguration = dotWebStackConfiguration.getTypeMapping()
              .get(nestedName);

          // Non-Postgres-backed objects can never be eager-loaded
          if (!(nestedTypeConfiguration instanceof PostgresTypeConfiguration)) {
            return;
          }

          List<SelectedField> nestedSelectedFields = nestedField.getSelectionSet()
              .getImmediateFields();

          QueryWithAliasMap queryWithAliasMap = internalBuild(
              (PostgresTypeConfiguration) nestedTypeConfiguration, nestedSelectedFields);

          String joinAlias = newTableAlias();

          joinTables.add(lateral(
              ((TableLike<Record>) queryWithAliasMap.getQuery()).asTable(joinAlias)));

          selectedColumns.add(field(joinAlias.concat(".*")));
          columnAliasMap.put(nestedField.getResultKey(), queryWithAliasMap.getColumnAliasMap());
        });

    SelectJoinStep<Record> query = dslContext.select(selectedColumns)
        .from(fromTable);

    for (Table<Record> joinTable : joinTables) {
      query = query.leftJoin(joinTable)
          .on(trueCondition());
    }

    return QueryWithAliasMap.builder()
        .query(query)
        .columnAliasMap(columnAliasMap)
        .build();
  }

  private Field<Object> createSelectedColumn(PostgresTypeConfiguration typeConfiguration, SelectedField selectedField) {
    PostgresFieldConfiguration fieldConfiguration = typeConfiguration.getFields()
        .get(selectedField.getName());

    return fieldConfiguration.getSqlField().as(newSelectAlias());
  }

  private String newTableAlias() {
    return "t".concat(String.valueOf(tableCounter.incrementAndGet()));
  }

  private String newSelectAlias() {
    return "x".concat(String.valueOf(selectCounter.incrementAndGet()));
  }
}

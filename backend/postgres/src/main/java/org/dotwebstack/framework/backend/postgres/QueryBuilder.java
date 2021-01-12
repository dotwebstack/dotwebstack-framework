package org.dotwebstack.framework.backend.postgres;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.lateral;

import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.SelectedField;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.TableLike;

public class QueryBuilder {

  private final DotWebStackConfiguration dotWebStackConfiguration;

  private final DSLContext dslContext;

  private final AtomicInteger tableCounter = new AtomicInteger();

  private final AtomicInteger varCounter = new AtomicInteger();

  public QueryBuilder(DotWebStackConfiguration dotWebStackConfiguration, DSLContext dslContext) {
    this.dotWebStackConfiguration = dotWebStackConfiguration;
    this.dslContext = dslContext;
  }

  public QueryWithAliasMap build(PostgresTypeConfiguration typeConfiguration, List<SelectedField> selectedFields) {
    return internalBuild(typeConfiguration, selectedFields);
  }

  public QueryWithAliasMap internalBuild(PostgresTypeConfiguration typeConfiguration,
      List<SelectedField> selectedFields) {
    Table<Record> table = typeConfiguration.tableAlias(tableCounter.incrementAndGet());

    Map<Object, Object> columnAliasMap = new HashMap<>();

    List<Field<?>> selectedColumns = selectedFields.stream()
        .filter(selectedField -> !(GraphQLTypeUtil.unwrapAll(selectedField.getFieldDefinition()
            .getType()) instanceof GraphQLObjectType))
        .map(selectedField -> createSelectedColumn(table, selectedField, columnAliasMap))
        .collect(Collectors.toList());

    List<SelectedField> nestedObjects = selectedFields.stream()
        .filter(selectedField -> (GraphQLTypeUtil.unwrapAll(selectedField.getFieldDefinition()
            .getType()) instanceof GraphQLObjectType))
        .collect(Collectors.toList());

    TableLike<?> joinQuery = null;
    for (SelectedField nestedObjectField : nestedObjects) {
      String nestedName = GraphQLTypeUtil.unwrapAll(nestedObjectField.getFieldDefinition()
          .getType())
          .getName();
      PostgresTypeConfiguration nestedTypeConfiguration =
          (PostgresTypeConfiguration) dotWebStackConfiguration.getTypeMapping()
              .get(nestedName);

      List<SelectedField> nestedSelectedFields = nestedObjectField.getSelectionSet()
          .getImmediateFields();

      QueryWithAliasMap queryWithAliasMap = internalBuild(nestedTypeConfiguration, nestedSelectedFields);

      String joinAlias = "t" + tableCounter.incrementAndGet();
      joinQuery = ((TableLike<?>) queryWithAliasMap.getQuery()).asTable(joinAlias);

      selectedColumns.add(field(String.format("%s.*", joinAlias)));

      columnAliasMap.put(nestedObjectField.getName(), queryWithAliasMap.getColumnAliasMap());
    }

    Query query;
    if (joinQuery != null) {
      query = dslContext.select(selectedColumns)
          .from(table)
          .leftJoin(lateral(joinQuery))
          .on(true);
    } else {
      query = dslContext.select(selectedColumns)
          .from(table);
    }

    return QueryWithAliasMap.builder()
        .query(query)
        .columnAliasMap(columnAliasMap)
        .build();
  }

  private Field<?> createSelectedColumn(Table<Record> table, SelectedField selectedField,
      Map<Object, Object> columnAliasMap) {
    String columnName = String.format("%s.%s", table.getName(), selectedField.getName());
    // TODO: Get column name from
    String columnAlias = String.format("x%s", varCounter.incrementAndGet());

    columnAliasMap.put(selectedField.getName(), columnAlias);

    return field(columnName).as(columnAlias);
  }
}

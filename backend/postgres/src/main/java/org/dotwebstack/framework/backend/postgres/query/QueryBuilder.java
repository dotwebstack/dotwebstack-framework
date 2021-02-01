package org.dotwebstack.framework.backend.postgres.query;

import static org.jooq.impl.DSL.trueCondition;

import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.GraphQLUnmodifiedType;
import graphql.schema.SelectedField;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.backend.postgres.ColumnKeyCondition;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.KeyCondition;
import org.dotwebstack.framework.core.datafetchers.LoadEnvironment;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.RowN;
import org.jooq.Select;
import org.jooq.SelectJoinStep;
import org.jooq.SelectOnConditionStep;
import org.jooq.Table;
import org.jooq.impl.DSL;

public class QueryBuilder {

  private static final int HARD_LIMIT = 10;

  private final DotWebStackConfiguration dotWebStackConfiguration;

  private final DSLContext dslContext;

  private final LoadEnvironment loadEnvironment;

  private final AtomicInteger tableCounter = new AtomicInteger();

  private final AtomicInteger selectCounter = new AtomicInteger();

  public QueryBuilder(DotWebStackConfiguration dotWebStackConfiguration, DSLContext dslContext,
      LoadEnvironment loadEnvironment) {
    this.dotWebStackConfiguration = dotWebStackConfiguration;
    this.dslContext = dslContext;
    this.loadEnvironment = loadEnvironment;
  }

  private QueryHolder build(Select<Record> query, Function<Map<String, Object>, Map<String, Object>> rowAssembler) {
    return QueryHolder.builder()
        .query(query)
        .rowAssembler(rowAssembler)
        .build();
  }

  private QueryHolder build(Select<Record> query, Map<String, String> keyColumnNames,
      Function<Map<String, Object>, Map<String, Object>> rowAssembler) {
    return QueryHolder.builder()
        .query(query)
        .rowAssembler(rowAssembler)
        .keyColumnNames(keyColumnNames)
        .build();
  }

  public QueryHolder build(PostgresTypeConfiguration typeConfiguration, KeyCondition keyCondition) {
    return build(typeConfiguration, Optional.ofNullable(keyCondition)
        .map(List::of)
        .orElse(List.of()));
  }

  public QueryHolder build(PostgresTypeConfiguration typeConfiguration, Collection<KeyCondition> keyConditions) {
    SelectWrapper selectWrapper = selectTable(typeConfiguration, "");

    if (keyConditions.isEmpty()) {
      return build(selectWrapper.getQuery()
          .limit(HARD_LIMIT), selectWrapper.getRowAssembler());
    }

    RowN[] valuesTableRows = keyConditions.stream()
        .map(ColumnKeyCondition.class::cast)
        .map(columnKeyCondition -> DSL.row(columnKeyCondition.getValueMap()
            .values()))
        .toArray(RowN[]::new);

    Map<String, String> keyColumnNames = keyConditions.stream()
        .findAny()
        .map(ColumnKeyCondition.class::cast)
        .orElseThrow()
        .getValueMap()
        .keySet()
        .stream()
        .collect(Collectors.toMap(Function.identity(), keyColumnName -> newSelectAlias()));

    Table<Record> valuesTable = DSL.values(valuesTableRows)
        .as(newTableAlias(), keyColumnNames.values()
            .toArray(String[]::new));

    Condition joinCondition = keyColumnNames.entrySet()
        .stream()
        .map(entry -> DSL.field(entry.getKey())
            .eq(DSL.field(DSL.name(valuesTable.getName(), entry.getValue()))))
        .reduce(DSL.noCondition(), Condition::and);

    Table<Record> lateralTable = DSL.lateral(selectWrapper.getQuery()
        .where(joinCondition)
        .limit(HARD_LIMIT))
        .asTable(newTableAlias());

    SelectOnConditionStep<Record> valuesQuery = dslContext.select()
        .from(valuesTable)
        .join(lateralTable)
        .on(trueCondition());

    return build(valuesQuery, keyColumnNames, selectWrapper.getRowAssembler());
  }

  private SelectWrapper selectTable(PostgresTypeConfiguration typeConfiguration, String fieldPathPrefix) {
    Table<Record> fromTable = DSL.table(typeConfiguration.getTable())
        .as(newTableAlias());

    List<Field<Object>> selectColumns = new ArrayList<>();
    List<Table<Record>> joinTables = new ArrayList<>();
    Map<String, Function<Map<String, Object>, Object>> assembleFns = new HashMap<>();

    Field<Object> keyColumn = DSL.field(DSL.name(fromTable.getName(), typeConfiguration.getKeys()
        .get(0)
        .getField()))
        .as(newSelectAlias());

    selectColumns.add(keyColumn);

    loadEnvironment.getSelectionSet()
        .getFields(fieldPathPrefix.concat("*"))
        .forEach(selectedField -> {
          String fieldName = selectedField.getName();

          PostgresFieldConfiguration fieldConfiguration = Optional.ofNullable(typeConfiguration.getFields()
              .get(fieldName))
              .orElseThrow(() -> new IllegalStateException(String.format("Field '%s' is unknown.", fieldName)));

          if (fieldConfiguration.isForeignType()) {
            if (fieldConfiguration.getMappedBy() != null) {
              return;
            }

            joinTable(selectedField, fieldConfiguration, fromTable).ifPresent(joinTableWrapper -> {
              selectColumns.add(DSL.field(joinTableWrapper.getTable()
                  .getName()
                  .concat(".*")));

              joinTables.add(joinTableWrapper.getTable());

              assembleFns.put(fieldName, joinTableWrapper.getRowAssembler()::apply);
            });

            return;
          }

          Field<Object> column = DSL.field(DSL.name(fromTable.getName(), fieldConfiguration.getColumn()))
              .as(newSelectAlias());

          selectColumns.add(column);
          assembleFns.put(fieldName, row -> row.get(column.getName()));
        });

    SelectJoinStep<Record> query = dslContext.select(selectColumns)
        .from(fromTable);

    for (Table<Record> joinTable : joinTables) {
      query = query.leftJoin(joinTable)
          .on(trueCondition());
    }

    return SelectWrapper.builder()
        .query(query)
        .rowAssembler(row -> {
          if (row.get(keyColumn.getName()) == null) {
            return null;
          }

          return assembleFns.entrySet()
              .stream()
              .collect(HashMap::new, (acc, entry) -> acc.put(entry.getKey(), entry.getValue()
                  .apply(row)), HashMap::putAll);
        })
        .build();
  }

  private Optional<TableWrapper> joinTable(SelectedField selectedField, PostgresFieldConfiguration fieldConfiguration,
      Table<Record> fromTable) {
    GraphQLUnmodifiedType foreignType = GraphQLTypeUtil.unwrapAll(selectedField.getFieldDefinition()
        .getType());

    if (!(foreignType instanceof GraphQLObjectType)) {
      throw new IllegalStateException("Foreign output type is not an object type.");
    }

    TypeConfiguration<?> typeConfiguration = Optional.ofNullable(dotWebStackConfiguration.getTypeMapping()
        .get(foreignType.getName()))
        .orElseThrow(() -> new IllegalStateException("Output type is unknown."));

    // Non-Postgres backends can never be eager loaded
    if (!(typeConfiguration instanceof PostgresTypeConfiguration)) {
      return Optional.empty();
    }

    SelectWrapper selectWrapper =
        selectTable((PostgresTypeConfiguration) typeConfiguration, selectedField.getQualifiedName()
            .concat("/"));

    if (fieldConfiguration.getJoinColumns() != null) {
      Condition whereCondition = fieldConfiguration.getJoinColumns()
          .stream()
          .map(joinColumn -> {
            PostgresFieldConfiguration rightFieldConfiguration =
                ((PostgresTypeConfiguration) typeConfiguration).getFields()
                    .get(joinColumn.getReferencedField());

            Field<Object> leftColumn = DSL.field(DSL.name(fromTable.getName(), joinColumn.getName()));
            Field<Object> rightColumn = DSL.field(DSL.name(rightFieldConfiguration.getColumn()));

            return leftColumn.eq(rightColumn);
          })
          .reduce(DSL.noCondition(), Condition::and);

      TableWrapper tableWrapper = TableWrapper.builder()
          .table(DSL.lateral(selectWrapper.getQuery()
              .where(whereCondition)
              .limit(1))
              .asTable(newTableAlias()))
          .rowAssembler(selectWrapper.getRowAssembler()::apply)
          .build();

      return Optional.of(tableWrapper);
    }

    throw new UnsupportedOperationException("JoinTable is not yet supported.");
  }

  private String newTableAlias() {
    return "t".concat(String.valueOf(tableCounter.incrementAndGet()));
  }

  private String newSelectAlias() {
    return "x".concat(String.valueOf(selectCounter.incrementAndGet()));
  }

  @Builder
  @Getter
  static class SelectWrapper {

    private final SelectJoinStep<Record> query;

    private final Function<Map<String, Object>, Map<String, Object>> rowAssembler;
  }

  @Builder
  @Getter
  static class TableWrapper {

    private final Table<Record> table;

    private final Function<Map<String, Object>, Map<String, Object>> rowAssembler;
  }
}

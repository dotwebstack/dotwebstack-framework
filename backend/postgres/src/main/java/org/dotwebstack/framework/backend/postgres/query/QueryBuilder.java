package org.dotwebstack.framework.backend.postgres.query;

import static org.jooq.impl.DSL.trueCondition;

import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.GraphQLUnmodifiedType;
import graphql.schema.SelectedField;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.dotwebstack.framework.backend.postgres.ColumnKeyCondition;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.backend.postgres.query.RowAssembler.RowAssemblerBuilder;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.KeyConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.KeyCondition;
import org.dotwebstack.framework.core.datafetchers.LoadEnvironment;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.RowN;
import org.jooq.Select;
import org.jooq.SelectFieldOrAsterisk;
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

  private final RowAssemblerBuilder rowAssemblerBuilder = RowAssembler.builder();

  public QueryBuilder(DotWebStackConfiguration dotWebStackConfiguration, DSLContext dslContext,
      LoadEnvironment loadEnvironment) {
    this.dotWebStackConfiguration = dotWebStackConfiguration;
    this.dslContext = dslContext;
    this.loadEnvironment = loadEnvironment;
  }

  private QueryHolder build(Select<Record> query) {
    return QueryHolder.builder()
        .query(query)
        .rowAssembler(rowAssemblerBuilder.build())
        .build();
  }

  private QueryHolder build(Select<Record> query, Map<String, String> keyColumnNames) {
    return QueryHolder.builder()
        .query(query)
        .rowAssembler(rowAssemblerBuilder.build())
        .keyColumnNames(keyColumnNames)
        .build();
  }

  public QueryHolder build(PostgresTypeConfiguration typeConfiguration, KeyCondition keyCondition) {
    return build(typeConfiguration, Optional.ofNullable(keyCondition)
        .map(List::of)
        .orElse(List.of()));
  }

  public QueryHolder build(PostgresTypeConfiguration typeConfiguration, Collection<KeyCondition> keyConditions) {
    Table<Record> fromTable = DSL.table(typeConfiguration.getTable())
        .as(newTableAlias());

    Map<String, Field<Object>> selectColumnMap =
        createSelectColumnMap(typeConfiguration, loadEnvironment.getSelectionSet()
            .getImmediateFields(), fromTable);

    selectColumnMap.forEach((fieldName, column) -> rowAssemblerBuilder.step(fieldName,
        row -> Optional.ofNullable(row.get(column.getName()))));

    List<Table<Record>> joinTables = createJoinTables(typeConfiguration, "", fromTable);

    List<SelectFieldOrAsterisk> select = new ArrayList<>(selectColumnMap.values());

    select.addAll(joinTables.stream()
        .map(joinTable -> DSL.field(joinTable.getName()
            .concat(".*")))
        .collect(Collectors.toList()));

    SelectJoinStep<Record> query = dslContext.select(select)
        .from(fromTable);

    for (Table<Record> joinTable : joinTables) {
      query = query.leftJoin(joinTable)
          .on(trueCondition());
    }

    if (keyConditions.isEmpty()) {
      return build(query.limit(HARD_LIMIT));
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
        .map(entry -> DSL.field(DSL.name(fromTable.getName(), entry.getKey()))
            .eq(DSL.field(DSL.name(valuesTable.getName(), entry.getValue()))))
        .reduce(DSL.noCondition(), Condition::and);

    Table<Record> lateralTable = DSL.lateral(query.where(joinCondition)
        .limit(HARD_LIMIT))
        .asTable(newTableAlias());

    SelectOnConditionStep<Record> valuesQuery = dslContext.select()
        .from(valuesTable)
        .join(lateralTable)
        .on(trueCondition());

    return build(valuesQuery, keyColumnNames);
  }

  private Map<String, Field<Object>> createSelectColumnMap(PostgresTypeConfiguration typeConfiguration,
      List<SelectedField> selectedFields, Table<Record> table) {
    Stream<String> keyFieldNames = typeConfiguration.getKeys()
        .stream()
        .map(KeyConfiguration::getField);

    Stream<String> selectedFieldNames = selectedFields.stream()
        .map(SelectedField::getName);

    return Stream.concat(keyFieldNames, selectedFieldNames)
        .distinct()
        .flatMap(fieldName -> {
          PostgresFieldConfiguration fieldConfiguration = Optional.ofNullable(typeConfiguration.getFields()
              .get(fieldName))
              .orElseThrow(() -> new IllegalArgumentException("Field '{}' unknown."));

          if (fieldConfiguration.isForeignType()) {
            return Stream.empty();
          }

          Field<Object> column = DSL.field(DSL.name(table.getName(), fieldConfiguration.getColumn()))
              .as(newSelectAlias());

          return Stream.of(Map.entry(fieldName, column));
        })
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private List<Table<Record>> createJoinTables(PostgresTypeConfiguration typeConfiguration, String fieldPathPrefix,
      Table<Record> fromTable) {
    return loadEnvironment.getSelectionSet()
        .getFields(fieldPathPrefix.concat("*"))
        .stream()
        .flatMap(selectedField -> {
          PostgresFieldConfiguration fieldConfiguration = Optional.ofNullable(typeConfiguration.getFields()
              .get(selectedField.getName()))
              .orElseThrow(() -> new IllegalArgumentException("Field '{}' unknown."));

          if (!fieldConfiguration.isForeignType()) {
            return Stream.empty();
          }

          GraphQLUnmodifiedType foreignType = GraphQLTypeUtil.unwrapAll(selectedField.getFieldDefinition()
              .getType());

          if (!(foreignType instanceof GraphQLObjectType)) {
            throw new IllegalStateException("Foreign output type is not an object type.");
          }

          TypeConfiguration<?> foreignTypeConfiguration = Optional.ofNullable(dotWebStackConfiguration.getTypeMapping()
              .get(((GraphQLObjectType) foreignType).getName()))
              .orElseThrow(() -> new IllegalStateException("Output type is unknown."));

          // Non-Postgres backends cannot be eager loaded
          if (!(foreignTypeConfiguration instanceof PostgresTypeConfiguration)) {
            return Stream.empty();
          }

          Table<Record> foreignTable = DSL.table(((PostgresTypeConfiguration) foreignTypeConfiguration).getTable())
              .as(newTableAlias());

          Map<String, Field<Object>> foreignSelectColumnMap =
              createSelectColumnMap(((PostgresTypeConfiguration) foreignTypeConfiguration),
                  loadEnvironment.getSelectionSet()
                      .getFields(selectedField.getQualifiedName()
                          .concat("/*")),
                  foreignTable);

          List<Table<Record>> joinTables = createJoinTables((PostgresTypeConfiguration) foreignTypeConfiguration,
              fieldPathPrefix.concat(selectedField.getName())
                  .concat("/"),
              foreignTable);

          List<SelectFieldOrAsterisk> select = new ArrayList<>(foreignSelectColumnMap.values());

          select.addAll(joinTables.stream()
              .map(joinTable -> DSL.field(joinTable.getName()
                  .concat(".*")))
              .collect(Collectors.toList()));

          Field<Object> firstKeyColumn = foreignSelectColumnMap.get(typeConfiguration.getKeys()
              .get(0)
              .getField());

          rowAssemblerBuilder.step(selectedField.getName(), row -> {
            if (row.get(firstKeyColumn.getName()) == null) {
              return Optional.empty();
            }

            return foreignSelectColumnMap.entrySet()
                .stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> row.get(entry.getValue()
                    .getName())));
          });

          if (fieldConfiguration.getJoinColumns() != null) {
            Condition whereCondition = fieldConfiguration.getJoinColumns()
                .stream()
                .map(joinColumn -> {
                  PostgresFieldConfiguration rightFieldConfiguration = typeConfiguration.getFields()
                      .get(joinColumn.getReferencedField());

                  Field<Object> leftColumn = DSL.field(DSL.name(fromTable.getName(), joinColumn.getName()));

                  Field<Object> rightColumn =
                      DSL.field(DSL.name(foreignTable.getName(), rightFieldConfiguration.getColumn()));

                  return leftColumn.eq(rightColumn);
                })
                .reduce(DSL.noCondition(), Condition::and);

            SelectJoinStep<Record> subQuery = dslContext.select(select)
                .from(foreignTable);

            for (Table<Record> joinTable : joinTables) {
              subQuery = subQuery.leftJoin(joinTable)
                  .on(trueCondition());
            }

            return Stream.of(DSL.lateral(subQuery.where(whereCondition)
                .limit(1))
                .asTable(newTableAlias()));
          }

          throw new UnsupportedOperationException("JoinTable is not yet supported.");
        })
        .collect(Collectors.toList());
  }

  private String newTableAlias() {
    return "t".concat(String.valueOf(tableCounter.incrementAndGet()));
  }

  private String newSelectAlias() {
    return "x".concat(String.valueOf(selectCounter.incrementAndGet()));
  }
}

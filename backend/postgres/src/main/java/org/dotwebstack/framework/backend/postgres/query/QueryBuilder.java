package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;
import static org.jooq.impl.DSL.trueCondition;

import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.GraphQLUnmodifiedType;
import graphql.schema.SelectedField;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.backend.postgres.ColumnKeyCondition;
import org.dotwebstack.framework.backend.postgres.config.JoinTable;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.KeyConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.KeyCondition;
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
import org.springframework.stereotype.Component;

@Component
public class QueryBuilder {

  private static final int HARD_LIMIT = 10;

  private final DotWebStackConfiguration dotWebStackConfiguration;

  private final DSLContext dslContext;

  public QueryBuilder(DotWebStackConfiguration dotWebStackConfiguration, DSLContext dslContext) {
    this.dotWebStackConfiguration = dotWebStackConfiguration;
    this.dslContext = dslContext;
  }

  private QueryHolder build(Select<Record> query, UnaryOperator<Map<String, Object>> rowAssembler) {
    return QueryHolder.builder()
        .query(query)
        .rowAssembler(rowAssembler)
        .build();
  }

  private QueryHolder build(Select<Record> query, Map<String, String> keyColumnNames,
      UnaryOperator<Map<String, Object>> rowAssembler) {
    return QueryHolder.builder()
        .query(query)
        .rowAssembler(rowAssembler)
        .keyColumnNames(keyColumnNames)
        .build();
  }

  public QueryHolder build(PostgresTypeConfiguration typeConfiguration, KeyCondition keyCondition,
      DataFetchingFieldSelectionSet selectionSet) {
    return build(new QueryContext(), typeConfiguration, Optional.ofNullable(keyCondition)
        .map(List::of)
        .orElse(List.of()), selectionSet);
  }

  public QueryHolder build(PostgresTypeConfiguration typeConfiguration, Collection<KeyCondition> keyConditions,
      DataFetchingFieldSelectionSet selectionSet) {
    return build(new QueryContext(), typeConfiguration, keyConditions, selectionSet);
  }

  public QueryHolder build(QueryContext queryContext, PostgresTypeConfiguration typeConfiguration,
      Collection<KeyCondition> keyConditions, DataFetchingFieldSelectionSet selectionSet) {
    JoinTable joinTable = keyConditions.stream()
        .map(ColumnKeyCondition.class::cast)
        .map(ColumnKeyCondition::getJoinTable)
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(null);

    SelectWrapper selectWrapper = selectTable(queryContext, typeConfiguration, "", joinTable, selectionSet);


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
        .collect(Collectors.toMap(Function.identity(), keyColumnName -> queryContext.newSelectAlias()));

    Table<Record> valuesTable = DSL.values(valuesTableRows)
        .as(queryContext.newTableAlias(), keyColumnNames.values()
            .toArray(String[]::new));

    Condition joinCondition = keyColumnNames.entrySet()
        .stream()
        .map(entry -> DSL.field(entry.getKey())
            .eq(DSL.field(DSL.name(valuesTable.getName(), entry.getValue()))))
        .reduce(DSL.noCondition(), Condition::and);

    Table<Record> lateralTable = DSL.lateral(selectWrapper.getQuery()
        .where(joinCondition)
        .limit(HARD_LIMIT))
        .asTable(queryContext.newTableAlias());

    List<Field<Object>> selectedColumns = Stream.concat(keyColumnNames.values()
        .stream()
        .map(DSL::field),
        Set.of(DSL.field(lateralTable.getName()
            .concat(".*")))
            .stream())
        .collect(Collectors.toList());

    SelectOnConditionStep<Record> valuesQuery = dslContext.select(selectedColumns)
        .from(valuesTable)
        .join(lateralTable)
        .on(trueCondition());

    return build(valuesQuery, keyColumnNames, selectWrapper.getRowAssembler());
  }

  private SelectWrapper selectTable(QueryContext queryContext, PostgresTypeConfiguration typeConfiguration,
      String fieldPathPrefix, JoinTable parentJoinTable, DataFetchingFieldSelectionSet selectionSet) {
    Table<Record> fromTable = DSL.table(typeConfiguration.getTable())
        .as(queryContext.newTableAlias());

    List<SelectFieldOrAsterisk> selectColumns = new ArrayList<>();
    List<Table<Record>> joinTables = new ArrayList<>();
    Map<String, Function<Map<String, Object>, Object>> assembleFns = new HashMap<>();

    Map<String, SelectedField> selectedFields = selectionSet.getFields(fieldPathPrefix.concat("*.*"))
        .stream()
        .collect(Collectors.toMap(SelectedField::getName, Function.identity()));

    AtomicReference<String> keyAlias = new AtomicReference<>();

    getFieldNames(typeConfiguration, selectedFields.values()).forEach(fieldName -> {
      PostgresFieldConfiguration fieldConfiguration = Optional.ofNullable(typeConfiguration.getFields()
          .get(fieldName))
          .orElseThrow(() -> illegalStateException("Field '{}' is unknown.", fieldName));

      if (!fieldConfiguration.isScalar()) {
        joinTable(queryContext, selectedFields.get(fieldName), fieldConfiguration, fromTable, selectionSet)
            .ifPresent(joinTableWrapper -> {
              selectColumns.add(DSL.field(joinTableWrapper.getTable()
                  .getName()
                  .concat(".*")));

              joinTables.add(joinTableWrapper.getTable());

              assembleFns.put(fieldName, joinTableWrapper.getRowAssembler()::apply);
            });

        return;
      }

      String columnAlias = queryContext.newSelectAlias();
      Field<Object> column = DSL.field(DSL.name(fromTable.getName(), fieldConfiguration.getColumn()))
          .as(columnAlias);

      if (typeConfiguration.getKeys()
          .stream()
          .anyMatch(keyConfiguration -> Objects.equals(keyConfiguration.getField(), fieldName))) {
        keyAlias.set(columnAlias);
      }

      selectColumns.add(column);
      assembleFns.put(fieldName, row -> row.get(column.getName()));
    });

    SelectJoinStep<Record> query = dslContext.select(selectColumns)
        .from(fromTable);

    if (parentJoinTable != null) {
      Table<Record> parentTable = DSL.table(parentJoinTable.getName())
          .as(queryContext.newTableAlias());

      Condition[] parentConditions = parentJoinTable.getInverseJoinColumns()
          .stream()
          .map(inverseJoinColumn -> DSL.field(DSL.name(parentTable.getName(), inverseJoinColumn.getName()))
              .eq(DSL.field(DSL.name(fromTable.getName(), typeConfiguration.getFields()
                  .get(inverseJoinColumn.getReferencedField())
                  .getColumn()))))
          .toArray(Condition[]::new);

      query.innerJoin(parentTable)
          .on(parentConditions);
    }

    for (Table<Record> joinTable : joinTables) {
      query = query.leftJoin(joinTable)
          .on(trueCondition());
    }

    return SelectWrapper.builder()
        .query(query)
        .rowAssembler(row -> {
          if (row.get(keyAlias.get()) == null) {
            return null;
          }

          return assembleFns.entrySet()
              .stream()
              .collect(HashMap::new, (acc, entry) -> acc.put(entry.getKey(), entry.getValue()
                  .apply(row)), HashMap::putAll);
        })
        .build();
  }

  private Set<String> getFieldNames(PostgresTypeConfiguration typeConfiguration,
      Collection<SelectedField> selectedFields) {
    return Stream.concat(typeConfiguration.getKeys()
        .stream()
        .map(KeyConfiguration::getField),
        selectedFields.stream()
            .map(SelectedField::getName))
        .collect(Collectors.toSet());
  }


  private Optional<TableWrapper> joinTable(QueryContext queryContext, SelectedField selectedField,
      PostgresFieldConfiguration fieldConfiguration, Table<Record> fromTable,
      DataFetchingFieldSelectionSet selectionSet) {
    GraphQLUnmodifiedType foreignType = GraphQLTypeUtil.unwrapAll(selectedField.getFieldDefinition()
        .getType());

    if (!(foreignType instanceof GraphQLObjectType)) {
      throw illegalStateException("Foreign output type is not an object type.");
    }

    TypeConfiguration<?> typeConfiguration = dotWebStackConfiguration.getTypeConfiguration(foreignType.getName());

    // Non-Postgres backends can never be eager loaded
    if (!(typeConfiguration instanceof PostgresTypeConfiguration)) {
      return Optional.empty();
    }

    SelectWrapper selectWrapper =
        selectTable(queryContext, (PostgresTypeConfiguration) typeConfiguration, selectedField.getFullyQualifiedName()
            .concat("/"), null, selectionSet);

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
              .asTable(queryContext.newTableAlias()))
          .rowAssembler(selectWrapper.getRowAssembler())
          .build();

      return Optional.of(tableWrapper);
    }

    if (fieldConfiguration.getJoinTable() != null || fieldConfiguration.getMappedBy() != null) {
      return Optional.empty();
    }

    throw unsupportedOperationException("Unsupported field configuration!");
  }

  @Builder
  @Getter
  static class SelectWrapper {

    private final SelectJoinStep<Record> query;

    private final UnaryOperator<Map<String, Object>> rowAssembler;
  }

  @Builder
  @Getter
  static class TableWrapper {

    private final Table<Record> table;

    private final UnaryOperator<Map<String, Object>> rowAssembler;
  }
}

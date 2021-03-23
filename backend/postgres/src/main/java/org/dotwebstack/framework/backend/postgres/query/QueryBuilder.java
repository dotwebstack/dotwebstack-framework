package org.dotwebstack.framework.backend.postgres.query;

import static org.jooq.impl.DSL.trueCondition;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.backend.postgres.ColumnKeyCondition;
import org.dotwebstack.framework.backend.postgres.config.JoinTable;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.RowN;
import org.jooq.Select;
import org.jooq.SelectConnectByStep;
import org.jooq.SelectOnConditionStep;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

@Component
public class QueryBuilder {

  private final SelectWrapperBuilderFactory selectWrapperBuilderFactory;

  private final DSLContext dslContext;

  public QueryBuilder(SelectWrapperBuilderFactory selectWrapperBuilderFactory, DSLContext dslContext) {
    this.selectWrapperBuilderFactory = selectWrapperBuilderFactory;
    this.dslContext = dslContext;
  }

  private QueryHolder build(Select<Record> query, UnaryOperator<Map<String, Object>> rowAssembler) {
    return QueryHolder.builder()
        .query(query)
        .mapAssembler(rowAssembler)
        .build();
  }

  private QueryHolder build(Select<Record> query, Map<String, String> keyColumnNames,
      UnaryOperator<Map<String, Object>> rowAssembler) {
    return QueryHolder.builder()
        .query(query)
        .mapAssembler(rowAssembler)
        .keyColumnNames(keyColumnNames)
        .build();
  }

  private QueryHolder build(QueryContext queryContext, PostgresTypeConfiguration typeConfiguration,
      QueryParameters queryParameters) {
    JoinTable joinTable = queryParameters.getKeyConditions()
        .stream()
        .map(ColumnKeyCondition.class::cast)
        .map(ColumnKeyCondition::getJoinTable)
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(null);

    SelectWrapperBuilder selectWrapperBuilder = selectWrapperBuilderFactory.getSelectWrapperBuilder();

    SelectWrapper selectWrapper =
        selectWrapperBuilder.build(new SelectContext(queryContext), typeConfiguration, "", joinTable);

    if (queryParameters.getKeyConditions()
        .isEmpty()) {
      return build(limit(selectWrapper.getQuery(), queryParameters.getPage()), selectWrapper.getRowAssembler());
    }

    RowN[] valuesTableRows = queryParameters.getKeyConditions()
        .stream()
        .map(ColumnKeyCondition.class::cast)
        .map(columnKeyCondition -> DSL.row(columnKeyCondition.getValueMap()
            .values()))
        .toArray(RowN[]::new);

    Map<String, String> keyColumnNames = queryParameters.getKeyConditions()
        .stream()
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
        .map(entry -> DSL.field(DSL.name(selectWrapper.getTable()
            .getName(), entry.getKey()))
            .eq(DSL.field(DSL.name(valuesTable.getName(), entry.getValue()))))
        .reduce(DSL.noCondition(), Condition::and);

    Table<Record> lateralTable = DSL.lateral(limit(selectWrapper.getQuery()
        .where(joinCondition), queryParameters.getPage()))
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

  public QueryHolder build(PostgresTypeConfiguration typeConfiguration, QueryParameters queryParameters) {
    return build(new QueryContext(queryParameters.getSelectionSet()), typeConfiguration, queryParameters);
  }

  private Select<Record> limit(SelectConnectByStep<Record> query, Page page) {
    if (page != null) {
      query.limit(page.getOffset(), page.getSize());
    }

    return query;
  }

  @Builder
  @Getter
  static class TableWrapper {

    private final Table<Record> table;

    private final UnaryOperator<Map<String, Object>> rowAssembler;
  }
}

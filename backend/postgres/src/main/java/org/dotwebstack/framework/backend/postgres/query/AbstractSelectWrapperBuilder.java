package org.dotwebstack.framework.backend.postgres.query;

import static org.jooq.impl.DSL.trueCondition;

import graphql.schema.SelectedField;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.backend.postgres.config.JoinTable;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectJoinStep;
import org.jooq.Table;
import org.jooq.impl.DSL;

public abstract class AbstractSelectWrapperBuilder implements SelectWrapperBuilder {

  private final DSLContext dslContext;

  protected AbstractSelectWrapperBuilder(DSLContext dslContext) {
    this.dslContext = dslContext;
  }

  public abstract void addFields(SelectContext selectContext, PostgresTypeConfiguration typeConfiguration,
      Table<Record> fromTable, Map<String, SelectedField> selectedFields);

  @Override
  public SelectWrapper build(SelectContext selectContext, PostgresTypeConfiguration typeConfiguration,
      String fieldPathPrefix, JoinTable parentJoinTable) {

    Map<String, SelectedField> selectedFields = selectContext.getQueryContext()
        .getSelectionSet()
        .getFields(fieldPathPrefix.concat("*.*"))
        .stream()
        .collect(Collectors.toMap(field -> Optional.ofNullable(field.getAlias())
            .orElse(field.getName()), Function.identity()));
    return build(selectContext, typeConfiguration, parentJoinTable, selectedFields);
  }

  @Override
  public SelectWrapper build(SelectContext selectContext, PostgresTypeConfiguration typeConfiguration,
      JoinTable parentJoinTable, Map<String, SelectedField> selectedFields) {
    Table<Record> fromTable = DSL.table(typeConfiguration.getTable())
        .as(selectContext.getQueryContext()
            .newTableAlias());

    addFields(selectContext, typeConfiguration, fromTable, selectedFields);

    SelectJoinStep<Record> query = dslContext.select(selectContext.getSelectColumns())
        .from(fromTable);

    for (CrossJoin crossJoin : selectContext.getCrossJoinTables()) {
      query.crossJoin(DSL.unnest(DSL.field(DSL.name(crossJoin.getFromTable()
          .getName(), crossJoin.getColumnName()), String[].class))
          .as(crossJoin.getAlias()));
    }

    Table<Record> parentTable = null;
    if (parentJoinTable != null) {
      parentTable = DSL.table(parentJoinTable.getName())
          .as(selectContext.getQueryContext()
              .newTableAlias());

      Table<Record> finalParentTable = parentTable;
      Condition[] parentConditions = parentJoinTable.getInverseJoinColumns()
          .stream()
          .map(inverseJoinColumn -> DSL.field(DSL.name(finalParentTable.getName(), inverseJoinColumn.getName()))
              .eq(DSL.field(DSL.name(fromTable.getName(), typeConfiguration.getFields()
                  .get(inverseJoinColumn.getReferencedField())
                  .getColumn()))))
          .toArray(Condition[]::new);

      query.innerJoin(parentTable)
          .on(parentConditions);
    }

    for (Table<Record> joinTable : selectContext.getJoinTables()) {
      query = query.leftJoin(joinTable)
          .on(trueCondition());
    }

    return SelectWrapper.builder()
        .query(query)
        .table((parentTable != null ? parentTable : fromTable))
        .rowAssembler(row -> {
          if (!StringUtils.isEmpty(selectContext.getCheckNullAlias()
              .get()) && row.get(
                  selectContext.getCheckNullAlias()
                      .get()) == null) {
            return null;
          }


          return selectContext.getAssembleFns()
              .entrySet()
              .stream()
              .collect(HashMap::new, (acc, entry) -> acc.put(entry.getKey(), entry.getValue()
                  .apply(row)), HashMap::putAll);
        })
        .build();
  }
}

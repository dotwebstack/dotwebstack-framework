package org.dotwebstack.framework.backend.postgres.query;

import static org.jooq.impl.DSL.trueCondition;

import graphql.schema.DataFetchingFieldSelectionSet;
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

  public AbstractSelectWrapperBuilder(DSLContext dslContext) {
    this.dslContext = dslContext;
  }

  public abstract void addFields(SelectContext selectContext, PostgresTypeConfiguration typeConfiguration,
      Table<Record> fromTable, Map<String, SelectedField> selectedFields, DataFetchingFieldSelectionSet selectionSet);

  @Override
  public SelectWrapper build(SelectContext selectContext, PostgresTypeConfiguration typeConfiguration,
      String fieldPathPrefix, JoinTable parentJoinTable, DataFetchingFieldSelectionSet selectionSet) {
    Table<Record> fromTable = DSL.table(typeConfiguration.getTable())
        .as(selectContext.getQueryContext()
            .newTableAlias());

    Map<String, SelectedField> selectedFields = selectionSet.getFields(fieldPathPrefix.concat("*.*"))
        .stream()
        .collect(Collectors.toMap(field -> Optional.ofNullable(field.getAlias())
            .orElse(field.getName()), Function.identity()));

    addFields(selectContext, typeConfiguration, fromTable, selectedFields, selectionSet);

    SelectJoinStep<Record> query = dslContext.select(selectContext.getSelectColumns())
        .from(fromTable);

    if (parentJoinTable != null) {
      Table<Record> parentTable = DSL.table(parentJoinTable.getName())
          .as(selectContext.getQueryContext()
              .newTableAlias());

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

    for (Table<Record> joinTable : selectContext.getJoinTables()) {
      query = query.leftJoin(joinTable)
          .on(trueCondition());
    }

    return SelectWrapper.builder()
        .query(query)
        .rowAssembler(row -> {
          if (!StringUtils.isEmpty(selectContext.getCheckNullAlias()
              .get())) {
            if (row.get(selectContext.getCheckNullAlias()
                .get()) == null) {
              return null;
            }
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

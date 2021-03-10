package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FIELD_ARGUMENT;

import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.SelectedField;
import java.math.BigDecimal;
import java.util.Map;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;

public class AggregateSelectWrapperBuilder extends AbstractSelectWrapperBuilder {

  public AggregateSelectWrapperBuilder(DSLContext dslContext) {
    super(dslContext);
  }

  @Override
  public void addFields(SelectContext selectContext, PostgresTypeConfiguration typeConfiguration,
      Table<Record> fromTable, Map<String, SelectedField> selectedFields, DataFetchingFieldSelectionSet selectionSet) {
    selectedFields.values()
        .stream()
        .filter(AggregateHelper::isAggregateField)
        .forEach(selectedField -> addAggregateField(typeConfiguration, selectContext, fromTable, selectedField));
  }

  private void addAggregateField(PostgresTypeConfiguration typeConfiguration, SelectContext selectContext,
      Table<Record> fromTable, SelectedField selectedField) {
    String columnAlias = selectContext.getQueryContext()
        .newSelectAlias();
    String aggregateFieldName = (String) selectedField.getArguments()
        .get(FIELD_ARGUMENT);
    String columnName = typeConfiguration.getFields()
        .get(aggregateFieldName)
        .getColumn();

    // TODO add distinct
    // TODO add all aggregate functions

    Field<BigDecimal> column =
        DSL.coalesce(DSL.sum(DSL.field(DSL.name(fromTable.getName(), columnName), Integer.class)), BigDecimal.ZERO)
            .as(columnAlias);

    selectContext.addField(selectedField, column);

    selectContext.getCheckNullAlias()
        .set(columnAlias);
  }

}

package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FIELD_ARGUMENT;

import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.SelectedField;
import java.util.Map;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;

public class AggregateSelectWrapperBuilder extends AbstractSelectWrapperBuilder {

  private final AggregateFieldFactory aggregateFieldFactory;

  public AggregateSelectWrapperBuilder(DSLContext dslContext, AggregateFieldFactory aggregateFieldFactory) {
    super(dslContext);
    this.aggregateFieldFactory = aggregateFieldFactory;
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

    Field<?> aggregateField = aggregateFieldFactory.create(selectedField, fromTable.getName(), columnName)
        .as(columnAlias);

    selectContext.addField(selectedField, aggregateField);

  }

}

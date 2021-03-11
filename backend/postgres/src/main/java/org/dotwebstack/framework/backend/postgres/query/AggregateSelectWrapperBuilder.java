package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.COUNT_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FIELD_ARGUMENT;

import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.SelectedField;
import java.util.Map;
import java.util.Objects;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
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
    PostgresFieldConfiguration aggregateFieldConfiguration = typeConfiguration.getFields()
        .get(aggregateFieldName);
    String columnName = aggregateFieldConfiguration.getColumn();

    validate(aggregateFieldConfiguration, selectedField, aggregateFieldName);

    Field<?> aggregateField = aggregateFieldFactory.create(selectedField, fromTable.getName(), columnName)
        .as(columnAlias);

    selectContext.addField(selectedField, aggregateField);

  }

  private void validate(PostgresFieldConfiguration aggregateFieldConfiguration, SelectedField selectedField,
      String aggregateFieldName) {

    if (!Objects.equals(COUNT_FIELD, selectedField.getName()) && !aggregateFieldConfiguration.getIsNumeric()) {
      throw new IllegalArgumentException(
          String.format("Numeric aggregation for non-numeric field %s is not supported.", aggregateFieldName));
    }
  }
}

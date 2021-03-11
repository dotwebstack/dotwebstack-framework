package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper.isAggregate;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;

import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.GraphQLUnmodifiedType;
import graphql.schema.SelectedField;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.KeyConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.helpers.TypeHelper;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;

public class DefaultSelectWrapperBuilder extends AbstractSelectWrapperBuilder {

  private final SelectWrapperBuilderFactory factory;

  private final DotWebStackConfiguration dotWebStackConfiguration;

  public DefaultSelectWrapperBuilder(DSLContext dslContext, SelectWrapperBuilderFactory factory,
      DotWebStackConfiguration dotWebStackConfiguration) {
    super(dslContext);
    this.factory = factory;
    this.dotWebStackConfiguration = dotWebStackConfiguration;
  }

  @Override
  public void addFields(SelectContext selectContext, PostgresTypeConfiguration typeConfiguration,
      Table<Record> fromTable, Map<String, SelectedField> selectedFields, DataFetchingFieldSelectionSet selectionSet) {
    Map<String, PostgresFieldConfiguration> fieldNamesConfigurations =
        getFieldNames(typeConfiguration, selectedFields.values());

    // add nested objects
    fieldNamesConfigurations.entrySet()
        .stream()
        .filter(entry -> !entry.getValue()
            .isScalar())
        .forEach(entry -> addJoinTable(selectionSet, selectContext, fromTable, selectedFields.get(entry.getKey()),
            entry.getValue()));

    // add direct fields
    fieldNamesConfigurations.entrySet()
        .stream()
        .filter(entry -> entry.getValue()
            .isScalar())
        .forEach(entry -> addField(selectContext, typeConfiguration, fromTable, entry));
  }

  private void addJoinTable(DataFetchingFieldSelectionSet selectionSet, SelectContext selectContext,
      Table<Record> fromTable, SelectedField selectedField, PostgresFieldConfiguration fieldConfiguration) {
    joinTable(selectContext.getQueryContext(), selectedField, fieldConfiguration, fromTable, selectionSet)
        .ifPresent(joinTableWrapper -> {
          selectContext.getSelectColumns()
              .add(DSL.field(joinTableWrapper.getTable()
                  .getName()
                  .concat(".*")));

          selectContext.getJoinTables()
              .add(joinTableWrapper.getTable());

          selectContext.getAssembleFns()
              .put(selectedField.getName(), joinTableWrapper.getRowAssembler()::apply);
        });
  }

  private void addField(SelectContext selectContext, PostgresTypeConfiguration typeConfiguration,
      Table<Record> fromTable, Map.Entry<String, PostgresFieldConfiguration> fieldNameConfiguration) {
    String columnAlias = selectContext.getQueryContext()
        .newSelectAlias();

    Field<Object> column = DSL.field(DSL.name(fromTable.getName(), fieldNameConfiguration.getValue()
        .getColumn()))
        .as(columnAlias);

    selectContext.addField(fieldNameConfiguration.getKey(), column);

    if (typeConfiguration.getKeys()
        .stream()
        .anyMatch(keyConfiguration -> Objects.equals(keyConfiguration.getField(), fieldNameConfiguration.getKey()))) {
      selectContext.getCheckNullAlias()
          .set(columnAlias);
    }
  }

  private Map<String, PostgresFieldConfiguration> getFieldNames(PostgresTypeConfiguration typeConfiguration,
      Collection<SelectedField> selectedFields) {

    return Stream.concat(typeConfiguration.getKeys()
        .stream()
        .map(KeyConfiguration::getField),
        selectedFields.stream()
            .map(SelectedField::getName))
        .collect(Collectors.toMap(key -> key, key -> typeConfiguration.getFields()
            .get(key), (a, b) -> a));
  }

  private Optional<QueryBuilder.TableWrapper> joinTable(QueryContext queryContext, SelectedField selectedField,
      PostgresFieldConfiguration fieldConfiguration, Table<Record> fromTable,
      DataFetchingFieldSelectionSet selectionSet) {
    GraphQLUnmodifiedType foreignType = getForeignType(selectedField, fieldConfiguration);

    if (!(foreignType instanceof GraphQLObjectType)) {
      throw illegalStateException("Foreign output type is not an object type.");
    }

    TypeConfiguration<?> typeConfiguration = dotWebStackConfiguration.getTypeConfiguration(foreignType.getName());

    // Non-Postgres backends can never be eager loaded
    if (!(typeConfiguration instanceof PostgresTypeConfiguration)) {
      return Optional.empty();
    }

    SelectWrapperBuilder selectWrapperBuilder = factory.getSelectWrapperBuilder(fieldConfiguration);

    SelectWrapper selectWrapper =
        selectWrapperBuilder
            .build(new SelectContext(queryContext), (PostgresTypeConfiguration) typeConfiguration,
                selectedField.getFullyQualifiedName()
                    .concat("/"),
                isAggregate(fieldConfiguration) ? fieldConfiguration.getJoinTable() : null, selectionSet);

    final PostgresTypeConfiguration typeConfigurationForCondition = getPostgresTypeConfigurationForCondition(
        fieldConfiguration, selectedField, (PostgresTypeConfiguration) typeConfiguration);

    if (fieldConfiguration.getJoinColumns() != null) {
      Condition whereCondition = fieldConfiguration.getJoinColumns()
          .stream()
          .map(joinColumn -> createJoinTableCondition(typeConfigurationForCondition, fieldConfiguration, fromTable,
              joinColumn))
          .reduce(DSL.noCondition(), Condition::and);

      QueryBuilder.TableWrapper tableWrapper = QueryBuilder.TableWrapper.builder()
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

  private Condition createJoinTableCondition(PostgresTypeConfiguration typeConfigurationForCondition,
      PostgresFieldConfiguration fieldConfiguration, Table<Record> fromTable,
      org.dotwebstack.framework.backend.postgres.config.JoinColumn joinColumn) {
    PostgresFieldConfiguration rightFieldConfiguration = typeConfigurationForCondition.getFields()
        .get(joinColumn.getReferencedField());

    Field<Object> leftColumn;
    Field<Object> rightColumn;

    if (isAggregate(fieldConfiguration)) {
      leftColumn = DSL.field(DSL.name(joinColumn.getName()));
      rightColumn = DSL.field(DSL.name(fromTable.getName(), rightFieldConfiguration.getColumn()));
    } else {
      leftColumn = DSL.field(DSL.name(fromTable.getName(), joinColumn.getName()));
      rightColumn = DSL.field(DSL.name(rightFieldConfiguration.getColumn()));
    }

    return leftColumn.eq(rightColumn);
  }

  private GraphQLUnmodifiedType getForeignType(SelectedField selectedField,
      PostgresFieldConfiguration fieldConfiguration) {
    GraphQLOutputType foreignType;
    if (isAggregate(fieldConfiguration)) {
      foreignType = selectedField.getObjectType()
          .getFieldDefinition(fieldConfiguration.getAggregationOf())
          .getType();
    } else {
      foreignType = selectedField.getFieldDefinition()
          .getType();
    }
    return GraphQLTypeUtil.unwrapAll(foreignType);
  }

  private PostgresTypeConfiguration getPostgresTypeConfigurationForCondition(
      PostgresFieldConfiguration fieldConfiguration, SelectedField selectedField,
      PostgresTypeConfiguration rightTypeConfiguration) {
    if (isAggregate(fieldConfiguration)) {
      GraphQLType type = getForeignType(selectedField, fieldConfiguration);
      return dotWebStackConfiguration.getTypeConfiguration(TypeHelper.getTypeName(type));
    }

    return rightTypeConfiguration;
  }
}

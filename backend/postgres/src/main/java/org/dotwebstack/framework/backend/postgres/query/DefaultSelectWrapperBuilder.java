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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.dotwebstack.framework.backend.postgres.config.JoinColumn;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.KeyConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants;
import org.dotwebstack.framework.core.helpers.TypeHelper;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Name;
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
    List<UnaryOperator<Map<String, Object>>> assembleFnsList = new ArrayList<>();

    Map<String, SelectedField> selectedStringJoinFieldsByName =
        getSelectedAggregateFieldsByName(selectedField, selectionSet, true);
    selectedStringJoinFieldsByName.forEach((name, field) -> processJoinTable(assembleFnsList, selectionSet,
        selectContext, fromTable, selectedField, fieldConfiguration, Collections.singletonMap(name, field)));
    Map<String, SelectedField> otherAggregateFieldsByName =
        getSelectedAggregateFieldsByName(selectedField, selectionSet, false);
    if (!otherAggregateFieldsByName.isEmpty()) {
      processJoinTable(assembleFnsList, selectionSet, selectContext, fromTable, selectedField, fieldConfiguration,
          otherAggregateFieldsByName);
    }

    if (!assembleFnsList.isEmpty()) {
      selectContext.getAssembleFns()
          .put(selectedField.getName(), multiSelectRowAssembler(assembleFnsList)::apply);
    }
  }

  private void processJoinTable(List<UnaryOperator<Map<String, Object>>> assembleFnsList,
      DataFetchingFieldSelectionSet selectionSet, SelectContext selectContext, Table<Record> fromTable,
      SelectedField selectedField, PostgresFieldConfiguration fieldConfiguration,
      Map<String, SelectedField> selectedFields) {
    joinTable(selectContext.getQueryContext(), selectedField, fieldConfiguration, fromTable, selectionSet,
        selectedFields).ifPresent(joinTableWrapper -> {
          selectContext.getSelectColumns()
              .add(DSL.field(joinTableWrapper.getTable()
                  .getName()
                  .concat(".*")));

          selectContext.getJoinTables()
              .add(joinTableWrapper.getTable());

          assembleFnsList.add(joinTableWrapper.getRowAssembler());
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
      DataFetchingFieldSelectionSet selectionSet, Map<String, SelectedField> selectedFields) {
    GraphQLUnmodifiedType foreignType = getForeignType(selectedField, fieldConfiguration);

    if (!(foreignType instanceof GraphQLObjectType)) {
      throw illegalStateException("Foreign output type is not an object type.");
    }

    TypeConfiguration<?> typeConfiguration = dotWebStackConfiguration.getTypeConfiguration(foreignType.getName());

    // Non-Postgres backends can never be eager loaded
    if (!(typeConfiguration instanceof PostgresTypeConfiguration)) {
      return Optional.empty();
    }

    if (fieldConfiguration.getJoinColumns() != null) {
      SelectWrapperBuilder selectWrapperBuilder = factory.getSelectWrapperBuilder(fieldConfiguration);

      SelectWrapper selectWrapper =
          selectWrapperBuilder.build(new SelectContext(queryContext), (PostgresTypeConfiguration) typeConfiguration,
              isAggregate(fieldConfiguration) ? fieldConfiguration.getJoinTable() : null, selectionSet, selectedFields);

      final PostgresTypeConfiguration otherSideTypeConfiguration = getPostgresTypeConfigurationForCondition(
          fieldConfiguration, selectedField, (PostgresTypeConfiguration) typeConfiguration);

      Condition whereCondition = fieldConfiguration.getJoinColumns()
          .stream()
          .map(joinColumn -> createJoinTableCondition(otherSideTypeConfiguration, fieldConfiguration, joinColumn,
              fromTable, selectWrapper.getTable()))
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


  private Condition createJoinTableCondition(PostgresTypeConfiguration otherSideTypeConfiguration,
      PostgresFieldConfiguration fieldConfiguration, JoinColumn joinColumn, Table<Record> leftTable,
      Table<Record> rightTable) {
    PostgresFieldConfiguration otherSideFieldConfiguration = otherSideTypeConfiguration.getFields()
        .get(joinColumn.getReferencedField());

    Name leftColumn = DSL.name(leftTable.getName(),
        fieldConfiguration.isAggregate() ? otherSideFieldConfiguration.getColumn() : joinColumn.getName());
    Name rightColumn = DSL.name(rightTable.getName(),
        fieldConfiguration.isAggregate() ? joinColumn.getName() : otherSideFieldConfiguration.getColumn());

    return DSL.field(leftColumn)
        .eq(DSL.field(rightColumn));
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

  private Map<String, SelectedField> getSelectedAggregateFieldsByName(SelectedField selectedField,
      DataFetchingFieldSelectionSet selectionSet, boolean stringJoin) {
    String fieldPathPrefix = selectedField.getFullyQualifiedName()
        .concat("/");
    return selectionSet.getFields(fieldPathPrefix.concat("*.*"))
        .stream()
        .filter(field -> AggregateConstants.STRING_JOIN_FIELD.equals(field.getName()) == stringJoin)
        .collect(Collectors.toMap(field -> Optional.ofNullable(field.getAlias())
            .orElse(field.getName()), Function.identity()));
  }

  private UnaryOperator<Map<String, Object>> multiSelectRowAssembler(
      List<UnaryOperator<Map<String, Object>>> assembleFnsList) {
    // TODO naam ok?

    return row -> assembleFnsList.stream()
        .collect(HashMap::new, (acc, assembler) -> acc.putAll(assembler.apply(row)), HashMap::putAll);
  }

}

package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FIELD_ARGUMENT;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;
import static org.jooq.impl.DSL.trueCondition;

import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.GraphQLUnmodifiedType;
import graphql.schema.SelectedField;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
import org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper;
import org.dotwebstack.framework.core.helpers.TypeHelper;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.RowN;
import org.jooq.Select;
import org.jooq.SelectConnectByStep;
import org.jooq.SelectJoinStep;
import org.jooq.SelectOnConditionStep;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.tools.StringUtils;
import org.springframework.stereotype.Component;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Component
public class QueryBuilder {

  private final DotWebStackConfiguration dotWebStackConfiguration;

  private final DSLContext dslContext;

  public QueryBuilder(DotWebStackConfiguration dotWebStackConfiguration, DSLContext dslContext) {
    this.dotWebStackConfiguration = dotWebStackConfiguration;
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

    SelectWrapper selectWrapper =
        selectTable(queryContext, typeConfiguration, "", joinTable, queryParameters.getSelectionSet(), false);

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
        .map(entry -> DSL.field(entry.getKey())
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
    return build(new QueryContext(), typeConfiguration, queryParameters);
  }

  private Select<Record> limit(SelectConnectByStep<Record> query, Page page) {
    if (page != null) {
      query.limit(page.getOffset(), page.getSize());
    }

    return query;
  }

  private SelectWrapper selectTable(QueryContext queryContext, PostgresTypeConfiguration typeConfiguration,
      String fieldPathPrefix, JoinTable parentJoinTable, DataFetchingFieldSelectionSet selectionSet,
      boolean aggregate) {

    SelectContext selectContext = new SelectContext(queryContext);

    Table<Record> fromTable = DSL.table(typeConfiguration.getTable())
        .as(queryContext.newTableAlias());

    Map<String, SelectedField> selectedFields = selectionSet.getFields(fieldPathPrefix.concat("*.*"))
        .stream()
        .collect(Collectors.toMap(SelectedField::getName, Function.identity()));

    if (aggregate) {
      // add aggregates
      selectedFields.values()
          .stream()
          .filter(AggregateHelper::isAggregate)
          .forEach(selectedField -> addAggregateField(typeConfiguration, selectContext, fromTable, selectedField));
    } else {
      List<Tuple2<String, PostgresFieldConfiguration>> fieldTuples =
          getFieldNames(typeConfiguration, selectedFields.values());

      // add nested objects
      fieldTuples.stream()
          .filter(tuple -> !tuple.getT2()
              .isScalar())
          .forEach(tuple -> addJoinTable(selectionSet, selectContext, fromTable, selectedFields.get(tuple.getT1()),
              tuple.getT2()));

      // add direct fields
      fieldTuples.stream()
          .filter(tuple -> tuple.getT2()
              .isScalar())
          .forEach(tuple -> addField(selectContext, typeConfiguration, fromTable, tuple));
    }

    SelectJoinStep<Record> query = dslContext.select(selectContext.getSelectColumns())
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

    for (Table<Record> joinTable : selectContext.getJoinTables()) {
      query = query.leftJoin(joinTable)
          .on(trueCondition());
    }

    return SelectWrapper.builder()
        .query(query)
        .rowAssembler(row -> {
          if (row.get(selectContext.getCheckNullAlias()
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
    Field<BigDecimal> column = DSL.sum(DSL.field(DSL.name(fromTable.getName(), columnName), Integer.class))
        .as(columnAlias);

    selectContext.addField(selectedField, column);

    selectContext.getCheckNullAlias()
        .set(columnAlias);
  }

  private void addField(SelectContext selectContext, PostgresTypeConfiguration typeConfiguration,
      Table<Record> fromTable, Tuple2<String, PostgresFieldConfiguration> fieldTuple) {
    String columnAlias = selectContext.getQueryContext()
        .newSelectAlias();

    Field<Object> column = DSL.field(DSL.name(fromTable.getName(), fieldTuple.getT2()
        .getColumn()))
        .as(columnAlias);

    selectContext.addField(fieldTuple.getT1(), column);

    if (typeConfiguration.getKeys()
        .stream()
        .anyMatch(keyConfiguration -> Objects.equals(keyConfiguration.getField(), fieldTuple.getT1()))) {
      selectContext.getCheckNullAlias()
          .set(columnAlias);
    }
  }

  private List<Tuple2<String, PostgresFieldConfiguration>> getFieldNames(PostgresTypeConfiguration typeConfiguration,
      Collection<SelectedField> selectedFields) {
    return Stream.concat(typeConfiguration.getKeys()
        .stream()
        .map(KeyConfiguration::getField),
        selectedFields.stream()
            .map(SelectedField::getName))
        .map(fieldName -> Tuples.of(fieldName, typeConfiguration.getFields()
            .get(fieldName)))
        .collect(Collectors.toList());
  }

  private GraphQLUnmodifiedType getForeignType(SelectedField selectedField,
      PostgresFieldConfiguration fieldConfiguration) {
    GraphQLOutputType foreignType;
    if (!StringUtils.isEmpty(fieldConfiguration.getAggregationOf())) {
      String aggregationOfField = fieldConfiguration.getAggregationOf();
      foreignType = selectedField.getObjectType()
          .getFieldDefinition(aggregationOfField)
          .getType();
    } else {
      foreignType = selectedField.getFieldDefinition()
          .getType();
    }
    return GraphQLTypeUtil.unwrapAll(foreignType);
  }

  private PostgresTypeConfiguration getPostgresTypeConfigurationForCondition(
      PostgresFieldConfiguration postgresFieldConfiguration, SelectedField selectedField,
      PostgresTypeConfiguration rightTypeConfiguration) {
    if (!StringUtils.isEmpty(postgresFieldConfiguration.getAggregationOf())) {
      GraphQLObjectType objectType = (GraphQLObjectType) GraphQLTypeUtil.unwrapAll(selectedField.getObjectType()
          .getFieldDefinition(postgresFieldConfiguration.getAggregationOf())
          .getType());
      String typeName = TypeHelper.getTypeName(objectType);

      return dotWebStackConfiguration.getTypeConfiguration(typeName);
    }

    return rightTypeConfiguration;
  }

  private Optional<TableWrapper> joinTable(QueryContext queryContext, SelectedField selectedField,
      PostgresFieldConfiguration fieldConfiguration, Table<Record> fromTable,
      DataFetchingFieldSelectionSet selectionSet) {
    GraphQLUnmodifiedType foreignType = getForeignType(selectedField, fieldConfiguration);

    // if foreignType is Aggregate bepaal foreignType obv aggregationOf relatie

    if (!(foreignType instanceof GraphQLObjectType)) {
      throw illegalStateException("Foreign output type is not an object type.");
    }

    TypeConfiguration<?> typeConfiguration = dotWebStackConfiguration.getTypeConfiguration(foreignType.getName());

    // Non-Postgres backends can never be eager loaded
    if (!(typeConfiguration instanceof PostgresTypeConfiguration)) {
      return Optional.empty();
    }

    boolean aggregrateContainer = !StringUtils.isEmpty(fieldConfiguration.getAggregationOf());

    SelectWrapper selectWrapper = selectTable(queryContext, (PostgresTypeConfiguration) typeConfiguration,
        selectedField.getFullyQualifiedName()
            .concat("/"),
        aggregrateContainer ? fieldConfiguration.getJoinTable() : null, selectionSet, aggregrateContainer);

    final PostgresTypeConfiguration typeConfigurationForCondition = getPostgresTypeConfigurationForCondition(
        fieldConfiguration, selectedField, (PostgresTypeConfiguration) typeConfiguration);

    if (fieldConfiguration.getJoinColumns() != null) {
      Condition whereCondition = fieldConfiguration.getJoinColumns()
          .stream()
          .map(joinColumn -> {
            PostgresFieldConfiguration rightFieldConfiguration = typeConfigurationForCondition.getFields()
                .get(joinColumn.getReferencedField());

            Field<Object> leftColumn;
            Field<Object> rightColumn;

            if (fieldConfiguration.getAggregationOf() != null) {
              leftColumn = DSL.field(DSL.name(joinColumn.getName()));
              rightColumn = DSL.field(DSL.name(fromTable.getName(), rightFieldConfiguration.getColumn()));
            } else {
              leftColumn = DSL.field(DSL.name(fromTable.getName(), joinColumn.getName()));
              rightColumn = DSL.field(DSL.name(rightFieldConfiguration.getColumn()));
            }

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

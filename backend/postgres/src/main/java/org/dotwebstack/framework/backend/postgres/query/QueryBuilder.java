package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.AGGREGATE_TYPE;
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
import org.dotwebstack.framework.core.helpers.TypeHelper;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.RowN;
import org.jooq.Select;
import org.jooq.SelectConnectByStep;
import org.jooq.SelectFieldOrAsterisk;
import org.jooq.SelectJoinStep;
import org.jooq.SelectOnConditionStep;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.tools.StringUtils;
import org.springframework.stereotype.Component;

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

    SelectWrapper selectWrapper = selectTable(queryContext, typeConfiguration, "", joinTable,
        queryParameters.getSelectionSet(), queryParameters.getGraphQlObjectType());


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
      GraphQLObjectType graphQlObjectType) {
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
      PostgresFieldConfiguration fieldConfiguration;
      Optional<PostgresFieldConfiguration> optionalFieldConfiguration =
          Optional.ofNullable(typeConfiguration.getFields()
              .get(fieldName));
      if (optionalFieldConfiguration.isPresent()) {
        fieldConfiguration = optionalFieldConfiguration.get();
      } else {
        SelectedField selectedField = selectedFields.get(fieldName);
        // check if it is an aggregate function
        // if selectedfield.parent is type of Aggregate

        // if true then create aggregate FieldConfiguration (beerAgg):
        // the aggegratefieldconfiguration contains the column to aggregate
        fieldConfiguration = createAggregateFieldConfiguration(selectedField);
      }

      // TODO aggregate query bouwen
      // indien aggregationOf, bepaal fieldConfiguration of aggregationOf -> beerAgg -> beers

      if (!fieldConfiguration.isScalar()) {
        joinTable(queryContext, selectedFields.get(fieldName), fieldConfiguration, fromTable, selectionSet,
            graphQlObjectType).ifPresent(joinTableWrapper -> {
              selectColumns.add(DSL.field(joinTableWrapper.getTable()
                  .getName()
                  .concat(".*")));

              joinTables.add(joinTableWrapper.getTable());

              assembleFns.put(fieldName, joinTableWrapper.getRowAssembler()::apply);
            });

        return;
      }

      String columnAlias = queryContext.newSelectAlias();

      if (fieldConfiguration.isAggregate()) {
        // TODO add distinct
        Field<BigDecimal> column =
            DSL.sum(DSL.field(DSL.name(fromTable.getName(), fieldConfiguration.getColumn()), Integer.class))
                .as(columnAlias);
        selectColumns.add(column);
        assembleFns.put(fieldName, row -> row.get(column.getName()));
        keyAlias.set(columnAlias);
      } else {
        Field<Object> column = DSL.field(DSL.name(fromTable.getName(), fieldConfiguration.getColumn()))
            .as(columnAlias);
        selectColumns.add(column);
        assembleFns.put(fieldName, row -> row.get(column.getName()));

        if (typeConfiguration.getKeys()
            .stream()
            .anyMatch(keyConfiguration -> Objects.equals(keyConfiguration.getField(), fieldName))) {
          keyAlias.set(columnAlias);
        }
      }
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

    if (!selectedFields.isEmpty() && TypeHelper.getTypeName(selectedFields.iterator()
        .next()
        .getObjectType())
        .equals(AGGREGATE_TYPE)) {
      return selectedFields.stream()
          .map(SelectedField::getName)
          .collect(Collectors.toSet());
    }

    return Stream.concat(typeConfiguration.getKeys()
        .stream()
        .map(KeyConfiguration::getField),
        selectedFields.stream()
            .map(SelectedField::getName))
        .collect(Collectors.toSet());
  }

  private GraphQLUnmodifiedType getForeignType(SelectedField selectedField,
      PostgresFieldConfiguration fieldConfiguration, GraphQLObjectType graphQlObjectType) {
    GraphQLOutputType foreignType;
    if (fieldConfiguration.getAggregationOf() != null) {
      String aggregationOfField = fieldConfiguration.getAggregationOf();
      foreignType = graphQlObjectType.getFieldDefinition(aggregationOfField)
          .getType();
    } else {
      foreignType = selectedField.getFieldDefinition()
          .getType();
    }
    return GraphQLTypeUtil.unwrapAll(foreignType);
  }

  private PostgresFieldConfiguration createAggregateFieldConfiguration(SelectedField selectedField) {
    String column = selectedField.getArguments()
        .get(FIELD_ARGUMENT)
        .toString();
    return new PostgresFieldConfiguration(column, true);
  }

  private PostgresTypeConfiguration getPostgresTypeConfigurationForCondition(
      PostgresFieldConfiguration postgresFieldConfiguration, GraphQLObjectType graphQlObjectType,
      PostgresTypeConfiguration rightTypeConfiguration) {
    if (!StringUtils.isEmpty(postgresFieldConfiguration.getAggregationOf())) {
      String typeName = TypeHelper.getTypeName(graphQlObjectType);

      return dotWebStackConfiguration.getTypeConfiguration(typeName);
    }

    return rightTypeConfiguration;
  }

  private Optional<TableWrapper> joinTable(QueryContext queryContext, SelectedField selectedField,
      PostgresFieldConfiguration fieldConfiguration, Table<Record> fromTable,
      DataFetchingFieldSelectionSet selectionSet, GraphQLObjectType graphQlObjectType) {
    GraphQLUnmodifiedType foreignType = getForeignType(selectedField, fieldConfiguration, graphQlObjectType);

    // if foreignType is Aggregate bepaal foreignType obv aggregationOf relatie

    if (!(foreignType instanceof GraphQLObjectType)) {
      throw illegalStateException("Foreign output type is not an object type.");
    }

    TypeConfiguration<?> typeConfiguration = dotWebStackConfiguration.getTypeConfiguration(foreignType.getName());

    // Non-Postgres backends can never be eager loaded
    if (!(typeConfiguration instanceof PostgresTypeConfiguration)) {
      return Optional.empty();
    }

    SelectWrapper selectWrapper = selectTable(queryContext, (PostgresTypeConfiguration) typeConfiguration,
        selectedField.getFullyQualifiedName()
            .concat("/"),
        !StringUtils.isEmpty(fieldConfiguration.getAggregationOf()) ? fieldConfiguration.getJoinTable() : null,
        selectionSet, graphQlObjectType);

    final PostgresTypeConfiguration typeConfigurationForCondition = getPostgresTypeConfigurationForCondition(
        fieldConfiguration, graphQlObjectType, (PostgresTypeConfiguration) typeConfiguration);

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

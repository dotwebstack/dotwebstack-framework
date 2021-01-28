package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.query.QueryConstants.AGGREGATE_KEY;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.trueCondition;

import graphql.schema.GraphQLTypeUtil;
import graphql.schema.SelectedField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.dotwebstack.framework.backend.postgres.config.JoinColumn;
import org.dotwebstack.framework.backend.postgres.config.JoinTable;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.LoadEnvironment;
import org.dotwebstack.framework.core.datafetchers.filters.FieldFilter;
import org.dotwebstack.framework.core.datafetchers.filters.Filter;
import org.dotwebstack.framework.core.helpers.TypeHelper;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectJoinStep;
import org.jooq.Table;
import org.jooq.TableLike;
import org.jooq.impl.DSL;

public class PostgresQueryBuilder {

  private final DotWebStackConfiguration dotWebStackConfiguration;

  private final DSLContext dslContext;

  private final AtomicInteger tableCounter = new AtomicInteger();

  private final AtomicInteger selectCounter = new AtomicInteger();

  public PostgresQueryBuilder(DotWebStackConfiguration dotWebStackConfiguration, DSLContext dslContext) {
    this.dotWebStackConfiguration = dotWebStackConfiguration;
    this.dslContext = dslContext;
  }

  public PostgresQueryHolder build(PostgresTypeConfiguration typeConfiguration, LoadEnvironment loadEnvironment,
      List<Filter> filters) {

    //TODO: filters hier implementeren en niet delegeren
    /* SELECT t1.identifier AS x1, t2.*
          FROM (
            VALUES ('b0e7cf18-e3ce-439b-a63e-034c8452f59c')
          ) AS t1 (identifier)
          LEFT JOIN LATERAL ( innerQuery ) */

    return build(typeConfiguration, loadEnvironment, null, filters);
  }

  //TODO: deze methode verder opknippen
  private PostgresQueryHolder build(PostgresTypeConfiguration typeConfiguration, LoadEnvironment loadEnvironment,
      JoinInformation joinInformation, List<Filter> filters) {

    Map<String, Object> fieldAliasMap = new HashMap<>();
    List<Table<Record>> fromTables = new ArrayList<>();

    Optional<JoinTable> joinTable = getJoinTable(loadEnvironment);

    List<Field<?>> selectedColumns =
        getDirectFields(typeConfiguration, loadEnvironment.getSelectedFields(), fieldAliasMap);

    Table<Record> fromTable = typeConfiguration.getSqlTable()
        .as(newTableAlias());
    fromTables.add(fromTable);

    final Table<Record> fromJoinTable;
    if (joinTable.isPresent()) {
      fromJoinTable = DSL.table(joinTable.get()
          .getName())
          .as(newTableAlias());

      // TODO: alle joinColumns verwerken, niet enkel 0
      Field<?> aggregateKey = QueryHelper.field(fromJoinTable, joinTable.get()
          .getJoinColumns()
          .get(0)
          .getName(), AGGREGATE_KEY);
      selectedColumns.add(0, aggregateKey);

      fromTables.add(fromJoinTable);
    } else {
      fromJoinTable = null;
    }

    List<TableLike<?>> leftJoins = getNestedResults(typeConfiguration, loadEnvironment.getSelectedFields(),
        fromTable.getName(), fieldAliasMap, selectedColumns).stream()
            .map(NestedQueryResult::getTable)
            .map(DSL::lateral)
            .collect(Collectors.toList());

    List<Condition> conditions =
        joinTable.map(table -> createJoinTableConditions(table, fromTable, fromJoinTable, filters))
            .orElse(new ArrayList<>());

    if (joinInformation != null) {
      //TODO: QueryHelper gebruiken om field te maken
      Field<Object> self = field(fromTable.getName()
          .concat(".")
          .concat(joinInformation.getReferencedField()));
      conditions.add(joinInformation.getParent()
          .eq(self));
    }

    if (filters != null && filters.size() > 0 && joinTable.isEmpty()) {
      // TODO: Filters moeten hier weg zie andere build methode
      String field = Optional.of(filters.get(0))
          .map(FieldFilter.class::cast)
          .map(FieldFilter::getField)
          .orElseThrow(() -> illegalStateException("No filter field name found!"));

      Object[] values = filters.stream()
          .map(FieldFilter.class::cast)
          .map(FieldFilter::getValue)
          .toArray(Object[]::new);

      Field<Object> matchField = QueryHelper.field(fromTable, field);

      selectedColumns.add(0, matchField.as(AGGREGATE_KEY));

      conditions.add(matchField.in(values));
    }

    SelectJoinStep<Record> query = dslContext.select(selectedColumns)
        .from(fromTables);

    leftJoins.forEach(leftJoin -> query.leftJoin(leftJoin)
        .on(trueCondition()));

    conditions.forEach(query::where);

    return PostgresQueryHolder.builder()
        .query(query)
        .fieldAliasMap(fieldAliasMap)
        .build();
  }

  private Optional<JoinTable> getJoinTable(LoadEnvironment loadEnvironment) {
    if (loadEnvironment.getExecutionStepInfo()
        .getParent()
        .getFieldDefinition() != null) {

      String parentTypeName = TypeHelper.getTypeName(loadEnvironment.getExecutionStepInfo()
          .getParent()
          .getFieldDefinition()
          .getType());

      TypeConfiguration<?> parentType = dotWebStackConfiguration.getTypeMapping()
          .get(parentTypeName);

      String fieldName = loadEnvironment.getExecutionStepInfo()
          .getFieldDefinition()
          .getName();

      PostgresFieldConfiguration fieldConfiguration = (PostgresFieldConfiguration) parentType.getFields()
          .get(fieldName);

      if (fieldConfiguration.getJoinTable() != null) {
        return Optional.of(fieldConfiguration.getJoinTable());

      }
    }
    return Optional.empty();
  }

  private List<Condition> createJoinTableConditions(JoinTable joinTable, Table<Record> fromTable,
      Table<Record> fromJoinTable, List<Filter> filters) {
    List<Condition> conditions = new ArrayList<>();

    if (joinTable != null) {
      for (JoinColumn joinColumn : joinTable.getJoinColumns()) {
        Field<Object> left = DSL.field(fromJoinTable.getName()
            .concat(".")
            .concat(joinColumn.getName()));

        Object[] values = filters.stream()
            .map(FieldFilter.class::cast)
            .map(FieldFilter::getValue)
            .toArray(Object[]::new);

        conditions.add(left.in(values));
      }

      for (JoinColumn joinColumn : joinTable.getInverseJoinColumns()) {
        Field<Object> left = DSL.field(fromJoinTable.getName()
            .concat(".")
            .concat(joinColumn.getName()));
        Field<Object> right = DSL.field(fromTable.getName()
            .concat(".")
            .concat(joinColumn.getReferencedField()));

        conditions.add(left.eq(right));
      }
    }

    return conditions;
  }

  private List<NestedQueryResult> getNestedResults(PostgresTypeConfiguration typeConfiguration,
      List<SelectedField> selectedFields, String tableName, Map<String, Object> fieldAliasMap,
      List<Field<?>> selectedColumns) {
    return selectedFields.stream()
        .filter(selectedField -> !GraphQLTypeUtil.isLeaf(selectedField.getFieldDefinition()
            .getType()))
        .filter(
            selectedField -> !GraphQLTypeUtil.isList(GraphQLTypeUtil.unwrapNonNull(selectedField.getFieldDefinition()
                .getType())))
        .map(selectedField -> processNested(getJoinInformation(typeConfiguration, selectedField, tableName),
            selectedField))
        .map(Optional::get)
        .peek(nestedQueryResult -> {
          fieldAliasMap.put(nestedQueryResult.getSelectedField()
              .getResultKey(), nestedQueryResult.getColumnAliasMap());
          selectedColumns.add(nestedQueryResult.getSelectedColumn());
        })
        .collect(Collectors.toList());
  }

  private JoinInformation getJoinInformation(PostgresTypeConfiguration typeConfiguration, SelectedField selectedField,
      String tableName) {
    PostgresFieldConfiguration postgresFieldConfiguration = typeConfiguration.getFields()
        .get(selectedField.getName());

    if (postgresFieldConfiguration.getMappedBy() != null) {
      // throw notImplementedException("The 'mappedBy' configuration item is not implemented yet!");
      return null;
    }

    if (postgresFieldConfiguration.getJoinColumns() == null) {
      throw invalidConfigurationException("Missing relation configuration for table '{}' and field `{}`", tableName,
          selectedField.getName());
    }

    if (postgresFieldConfiguration.getJoinColumns()
        .size() > 1) {
      throw unsupportedOperationException("Multiple joinColumns for a single property aren't supported yet!");
    }

    JoinColumn joinColumn = postgresFieldConfiguration.getJoinColumns()
        .get(0);

    return JoinInformation.builder()
        .parent(field(tableName.concat(".")
            .concat(joinColumn.getName())))
        .referencedField(joinColumn.getReferencedField())
        .build();
  }

  private List<Field<?>> getDirectFields(PostgresTypeConfiguration typeConfiguration,
      List<SelectedField> selectedFields, Map<String, Object> fieldAliasMap) {
    return selectedFields.stream()
        .filter(selectedField -> GraphQLTypeUtil.isLeaf(selectedField.getFieldDefinition()
            .getType()))
        .map(selectedField -> {
          Field<Object> column = createSelectedColumn(typeConfiguration, selectedField);
          fieldAliasMap.put(selectedField.getResultKey(), column.getName());
          return column;
        })
        .collect(Collectors.toList());
  }

  @SuppressWarnings("unchecked")
  private Optional<NestedQueryResult> processNested(JoinInformation joinInformation, SelectedField nestedField) {
    String nestedName = GraphQLTypeUtil.unwrapAll(nestedField.getFieldDefinition()
        .getType())
        .getName();

    TypeConfiguration<?> nestedTypeConfiguration = dotWebStackConfiguration.getTypeMapping()
        .get(nestedName);

    // Non-Postgres-backed objects can never be eager-loaded
    if (!(nestedTypeConfiguration instanceof PostgresTypeConfiguration)) {
      return Optional.empty();
    }

    List<SelectedField> nestedSelectedFields = nestedField.getSelectionSet()
        .getImmediateFields();

    LoadEnvironment loadEnvironment = LoadEnvironment.builder()
        .selectedFields(nestedSelectedFields)
        .build();

    PostgresQueryHolder queryHolder =
        build((PostgresTypeConfiguration) nestedTypeConfiguration, loadEnvironment, joinInformation, null);

    String joinAlias = newTableAlias();

    return Optional.of(NestedQueryResult.builder()
        .selectedField(nestedField)
        .selectedColumn(field(joinAlias.concat(".*")))
        .typeConfiguration((PostgresTypeConfiguration) nestedTypeConfiguration)
        .table(((TableLike<Record>) queryHolder.getQuery()).asTable(joinAlias))
        .columnAliasMap(queryHolder.getFieldAliasMap())
        .build());
  }

  private Field<Object> createSelectedColumn(PostgresTypeConfiguration typeConfiguration, SelectedField selectedField) {
    PostgresFieldConfiguration fieldConfiguration = typeConfiguration.getFields()
        .get(selectedField.getName());

    return field(fieldConfiguration.getSqlColumnName()).as(newSelectAlias());
  }

  private String newTableAlias() {
    return "t".concat(String.valueOf(tableCounter.incrementAndGet()));
  }

  private String newSelectAlias() {
    return "x".concat(String.valueOf(selectCounter.incrementAndGet()));
  }
}

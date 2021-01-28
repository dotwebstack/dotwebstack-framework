package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.trueCondition;

import graphql.execution.ExecutionStepInfo;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.SelectedField;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
import org.jooq.Row1;
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
      Collection<Filter> filters) {

    if (filters != null && filters.size() > 0) {
      String tableName = newTableAlias();
      String fieldName = ((FieldFilter) filters.iterator()
          .next()).getField();
      String identifierColumn = typeConfiguration.getFields()
          .get(fieldName)
          .getSqlColumnName();

      Field<?> fieldIdentifier = QueryHelper.field(DSL.table(tableName), identifierColumn);

      Row1[] rows = filters.stream()
          .map(FieldFilter.class::cast)
          .map(filter -> DSL.row(filter.getValue()))
          .toArray(Row1[]::new);

      TableLike<?> fromTable = DSL.values(rows)
          .as(tableName, identifierColumn);

      JoinInformation joinInformation = JoinInformation.builder()
          .parent(QueryHelper.field(DSL.table(tableName), identifierColumn))
          .referencedField(((FieldFilter) filters.iterator()
              .next()).getField())
          .build();

      PostgresQueryHolder queryHolder = buildJoin(typeConfiguration, loadEnvironment, joinInformation);

      String joinAlias = newTableAlias();

      SelectJoinStep<Record> query = dslContext.select()
          .select(fieldIdentifier, DSL.field(joinAlias.concat(".")
              .concat("*")))
          .from(fromTable)
          .innerJoin(DSL.lateral((TableLike<Record>) queryHolder.getQuery())
              .asTable(joinAlias))
          .on(trueCondition());

      return PostgresQueryHolder.builder()
          .fieldAliasMap(queryHolder.getFieldAliasMap())
          .query(query)
          .build();
    }

    return buildJoin(typeConfiguration, loadEnvironment, null);
  }

  // TODO: deze methode verder opknippen
  private PostgresQueryHolder buildJoin(PostgresTypeConfiguration typeConfiguration, LoadEnvironment loadEnvironment,
      JoinInformation joinInformation) {

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

      fromTables.add(fromJoinTable);
    } else {
      fromJoinTable = null;
    }

    List<TableLike<?>> leftJoins =
        getNestedResults(typeConfiguration, loadEnvironment, fromTable.getName(), fieldAliasMap, selectedColumns)
            .stream()
            .map(NestedQueryResult::getTable)
            .map(DSL::lateral)
            .collect(Collectors.toList());

    List<Condition> conditions = new ArrayList<>();

    joinTable.ifPresent(
        table -> conditions.addAll(createJoinTableConditions(table, fromTable, fromJoinTable, joinInformation)));

    if (joinInformation != null && joinTable.isEmpty()) {
      Field<Object> matchField = QueryHelper.field(fromTable, joinInformation.getReferencedField());
      conditions.add(matchField.in(joinInformation.getParent()));
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
    return Optional.ofNullable(loadEnvironment.getExecutionStepInfo())
        .map(ExecutionStepInfo::getParent)
        .map(ExecutionStepInfo::getFieldDefinition)
        .map(fieldDefinition -> {
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
            return fieldConfiguration.getJoinTable();
          }

          return null;
        });
  }

  private List<Condition> createJoinTableConditions(JoinTable joinTable, Table<Record> fromTable,
      Table<Record> fromJoinTable, JoinInformation joinInformation) {
    Stream<Condition> joinConditions = joinTable.getJoinColumns()
        .stream()
        .map(joinColumn -> QueryHelper.field(fromJoinTable, joinColumn.getName()))
        .map(field -> field.eq(joinInformation.getParent()));

    Stream<Condition> inverseJoinConditions = joinTable.getInverseJoinColumns()
        .stream()
        .map(joinColumn -> QueryHelper.field(fromJoinTable, joinColumn.getName())
            .eq(QueryHelper.field(fromTable, joinColumn.getReferencedField())));

    return Stream.concat(joinConditions, inverseJoinConditions)
        .collect(Collectors.toList());
  }

  private List<NestedQueryResult> getNestedResults(PostgresTypeConfiguration typeConfiguration,
      LoadEnvironment loadEnvironment, String tableName, Map<String, Object> fieldAliasMap,
      List<Field<?>> selectedColumns) {
    return loadEnvironment.getSelectedFields()
        .stream()
        .filter(selectedField -> !GraphQLTypeUtil.isLeaf(selectedField.getFieldDefinition()
            .getType()))
        .filter(
            selectedField -> !GraphQLTypeUtil.isList(GraphQLTypeUtil.unwrapNonNull(selectedField.getFieldDefinition()
                .getType())))
        .map(selectedField -> processNested(getJoinInformation(typeConfiguration, selectedField, tableName),
            selectedField, loadEnvironment.getExecutionStepInfo()))
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
        .parent(QueryHelper.field(DSL.table(tableName), joinColumn.getName()))
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
  private Optional<NestedQueryResult> processNested(JoinInformation joinInformation, SelectedField nestedField,
      ExecutionStepInfo executionStepInfo) {
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
        .executionStepInfo(executionStepInfo)
        .build();

    PostgresQueryHolder queryHolder =
        buildJoin((PostgresTypeConfiguration) nestedTypeConfiguration, loadEnvironment, joinInformation);

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

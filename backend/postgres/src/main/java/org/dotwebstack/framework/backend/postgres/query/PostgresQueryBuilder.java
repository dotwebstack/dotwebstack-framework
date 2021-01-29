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
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.backend.postgres.ColumnKeyCondition;
import org.dotwebstack.framework.backend.postgres.config.JoinColumn;
import org.dotwebstack.framework.backend.postgres.config.JoinTable;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.KeyConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.KeyCondition;
import org.dotwebstack.framework.core.datafetchers.LoadEnvironment;
import org.dotwebstack.framework.core.helpers.MapNode;
import org.dotwebstack.framework.core.helpers.TypeHelper;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.RowN;
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
      KeyCondition keyCondition) {
    return build(typeConfiguration, loadEnvironment, keyCondition != null ? List.of(keyCondition) : List.of());
  }

  public PostgresQueryHolder build(PostgresTypeConfiguration typeConfiguration, LoadEnvironment loadEnvironment,
      Collection<KeyCondition> keyConditions) {
    if (keyConditions != null && keyConditions.size() > 0) {
      String tableName = newTableAlias();

      ColumnKeyCondition first = (ColumnKeyCondition) keyConditions.iterator()
          .next();

      RowN[] rows = keyConditions.stream()
          .map(ColumnKeyCondition.class::cast)
          .map(columnKeyCondition -> DSL.row(columnKeyCondition.getColumnValues()
              .values()))
          .toArray(RowN[]::new);

      TableLike<?> fromTable = DSL.values(rows)
          .as(tableName, first.getColumnValues()
              .keySet()
              .toArray(new String[0]));

      String identifierColumn = first.getColumnValues()
          .keySet()
          .iterator()
          .next();

      JoinInformation joinInformation = JoinInformation.builder()
          .parent(QueryHelper.field(DSL.table(tableName), identifierColumn))
          .referencedField(identifierColumn)
          .build();

      PostgresQueryHolder queryHolder = buildJoin(typeConfiguration, loadEnvironment, joinInformation);

      String joinAlias = newTableAlias();

      Field<?> fieldIdentifier = QueryHelper.field(DSL.table(tableName), identifierColumn);

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

    Set<Field<?>> selectedColumns =
        getDirectFields(typeConfiguration, loadEnvironment.getSelectedFields(), fieldAliasMap);

    Table<Record> fromTable = typeConfiguration.getSqlTable()
        .as(newTableAlias());
    fromTables.add(fromTable);

    List<TableLike<?>> leftJoins =
        getNestedResults(typeConfiguration, loadEnvironment, fromTable.getName(), fieldAliasMap, selectedColumns)
            .stream()
            .map(NestedQueryResult::getTable)
            .map(DSL::lateral)
            .collect(Collectors.toList());

    List<Condition> conditions = new ArrayList<>();

    Optional<JoinTableInformation> joinTableInformation =
        getJoinTableInformation(loadEnvironment, fromTable, joinInformation);

    if (joinTableInformation.isPresent()) {
      JoinTableInformation joinTableInformation1 = joinTableInformation.get();
      fromTables.add(joinTableInformation1.getTable());
      conditions.addAll(joinTableInformation1.getConditions());
    } else if (joinInformation != null) {
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

  private Optional<JoinTableInformation> getJoinTableInformation(LoadEnvironment loadEnvironment,
      Table<Record> parentTable, JoinInformation joinInformation) {
    return getJoinTable(loadEnvironment).map(joinTable -> {
      Table<Record> table = DSL.table(joinTable.getName())
          .as(newTableAlias());
      return JoinTableInformation.builder()
          .table(table)
          .conditions(createJoinTableConditions(joinTable, parentTable, table, joinInformation))
          .build();
    });
  }

  @Builder
  @Getter
  private static class JoinTableInformation {
    private final Table<Record> table;

    private final List<Condition> conditions;
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
      Set<Field<?>> selectedColumns) {
    return loadEnvironment.getSelectedFields()
        .stream()
        .filter(selectedField -> !GraphQLTypeUtil.isLeaf(selectedField.getFieldDefinition()
            .getType()))
        .filter(
            selectedField -> !GraphQLTypeUtil.isList(GraphQLTypeUtil.unwrapNonNull(selectedField.getFieldDefinition()
                .getType())))
        .map(selectedField -> processNested(getJoinInformation(typeConfiguration, selectedField, tableName),
            selectedField, loadEnvironment))
        .map(Optional::get)
        .peek(nestedQueryResult -> {
          MapNode mapNode = MapNode.builder()
              .typeConfiguration(nestedQueryResult.getTypeConfiguration())
              .fieldAliasMap(nestedQueryResult.getColumnAliasMap())
              .build();

          fieldAliasMap.put(nestedQueryResult.getSelectedField()
              .getFieldDefinition()
              .getName(), mapNode);
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

  private Set<Field<?>> getDirectFields(PostgresTypeConfiguration typeConfiguration, List<SelectedField> selectedFields,
      Map<String, Object> fieldAliasMap) {
    Set<String> fieldNames = selectedFields.stream()
        .filter(selectedField -> GraphQLTypeUtil.isLeaf(selectedField.getFieldDefinition()
            .getType()))
        .map(SelectedField::getName)
        .collect(Collectors.toSet());

    typeConfiguration.getKeys()
        .stream()
        .map(KeyConfiguration::getField)
        .forEach(fieldNames::add);

    return fieldNames.stream()
        .map(fieldName -> {
          String aliasName = newSelectAlias();
          Field<?> field = createSqlField(typeConfiguration, fieldName).as(aliasName);
          fieldAliasMap.put(fieldName, aliasName);
          return field;
        })
        .collect(Collectors.toSet());
  }

  @SuppressWarnings("unchecked")
  private Optional<NestedQueryResult> processNested(JoinInformation joinInformation, SelectedField nestedField,
      LoadEnvironment environment) {
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

    LoadEnvironment childEnvironment = LoadEnvironment.builder()
        .selectedFields(nestedSelectedFields)
        .executionStepInfo(environment.getExecutionStepInfo())
        .build();

    PostgresQueryHolder queryHolder =
        buildJoin((PostgresTypeConfiguration) nestedTypeConfiguration, childEnvironment, joinInformation);

    String joinAlias = newTableAlias();

    return Optional.of(NestedQueryResult.builder()
        .selectedField(nestedField)
        .selectedColumn(field(joinAlias.concat(".*")))
        .typeConfiguration((PostgresTypeConfiguration) nestedTypeConfiguration)
        .table(((TableLike<Record>) queryHolder.getQuery()).asTable(joinAlias))
        .columnAliasMap(queryHolder.getFieldAliasMap())
        .build());
  }

  private Field<Object> createSqlField(PostgresTypeConfiguration typeConfiguration, String fieldName) {
    PostgresFieldConfiguration fieldConfiguration = typeConfiguration.getFields()
        .get(fieldName);

    return field(fieldConfiguration.getSqlColumnName());
  }

  private String newTableAlias() {
    return "t".concat(String.valueOf(tableCounter.incrementAndGet()));
  }

  private String newSelectAlias() {
    return "x".concat(String.valueOf(selectCounter.incrementAndGet()));
  }
}

package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.helpers.ValidationHelper.validateFields;
import static org.dotwebstack.framework.backend.postgres.query.JoinHelper.createJoinConditions;
import static org.dotwebstack.framework.backend.postgres.query.JoinHelper.invertOnList;
import static org.dotwebstack.framework.backend.postgres.query.Query.EXISTS_KEY;
import static org.dotwebstack.framework.backend.postgres.query.Query.GROUP_KEY;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.columnName;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.dotwebstack.framework.backend.postgres.model.JoinColumn;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.core.backend.query.AliasManager;
import org.dotwebstack.framework.core.backend.query.ObjectFieldMapper;
import org.dotwebstack.framework.core.query.model.ContextCriteria;
import org.jooq.DSLContext;
import org.jooq.JoinType;
import org.jooq.Record;
import org.jooq.RowN;
import org.jooq.SQLDialect;
import org.jooq.SelectQuery;
import org.jooq.Table;
import org.jooq.impl.DSL;

@Accessors(fluent = true)
@Setter
class BatchQueryBuilder {

  private final DSLContext dslContext = DSL.using(SQLDialect.POSTGRES);

  @NotNull
  private ObjectFieldMapper<Map<String, Object>> fieldMapper;

  @NotNull
  private AliasManager aliasManager;

  @NotNull
  private SelectQuery<Record> dataQuery;

  @NotNull
  private Set<Map<String, Object>> joinKeys;

  private JoinConfiguration joinConfiguration;

  private ContextCriteria contextCriteria;

  private Table<Record> table;

  private boolean fromUnion = false;

  private BatchQueryBuilder() {}

  static BatchQueryBuilder newBatchQuery() {
    return new BatchQueryBuilder();
  }

  SelectQuery<Record> build() {
    validateFields(this);

    if (joinConfiguration == null) {
      return batchQueryWithKeys();
    }

    if (joinConfiguration.getMappedBy() != null) {
      var mappedBy = joinConfiguration.getMappedBy();

      var mappedByJoinConfiguration = JoinConfiguration.builder()
          .objectField(mappedBy)
          .joinTable(JoinHelper.invert(mappedBy.getJoinTable()))
          .joinColumns(mappedBy.getJoinColumns())
          .objectType((PostgresObjectType) joinConfiguration.getObjectField()
              .getObjectType())
          .targetType((PostgresObjectType) mappedBy.getObjectType())
          .build();

      return newBatchQuery().joinConfiguration(mappedByJoinConfiguration)
          .contextCriteria(contextCriteria)
          .aliasManager(aliasManager)
          .fieldMapper(fieldMapper)
          .dataQuery(dataQuery)
          .table(table)
          .joinKeys(joinKeys)
          .build();
    }

    if (!joinConfiguration.getJoinColumns()
        .isEmpty()) {
      return batchQuery(invertOnList(joinConfiguration.getObjectField(), joinConfiguration.getJoinColumns()), table);
    }

    if (joinConfiguration.getJoinTable() != null) {
      return batchQueryWithJoinTable();
    }

    throw illegalArgumentException("Object field '{}' has no relation configuration!",
        joinConfiguration.getObjectField()
            .getName());
  }

  private SelectQuery<Record> batchQueryWithKeys() {
    var columnAliases = joinKeys.stream()
        .findFirst()
        .stream()
        .flatMap(map -> map.keySet()
            .stream())
        .collect(Collectors.toMap(Function.identity(), field -> aliasManager.newAlias()));

    var keyTable = createValuesTable(columnAliases, joinKeys);

    columnAliases.entrySet()
        .stream()
        .map(entry -> QueryHelper.column(entry.getKey())
            .equal(QueryHelper.column(keyTable, entry.getValue())))
        .forEach(dataQuery::addConditions);

    addExists(dataQuery, columnAliases.keySet(), table);

    return batchQuery(keyTable);
  }

  private SelectQuery<Record> batchQuery(List<JoinColumn> joinColumns, Table<Record> joinConditionTable) {
    var objectType = joinConfiguration.getObjectType();

    var keyJoinColumnAliasMap = joinColumns.stream()
        .collect(Collectors.toMap(joinColumn -> joinColumn, joinColumn -> aliasManager.newAlias()));

    var keyColumnAliases = keyJoinColumnAliasMap.entrySet()
        .stream()
        .collect(Collectors.toMap(e -> columnName(e.getKey(), objectType), Map.Entry::getValue));

    var keyTable = createValuesTable(keyColumnAliases, joinKeys);

    keyJoinColumnAliasMap.entrySet()
        .stream()
        .map(entry -> QueryHelper.column(joinConditionTable, entry.getKey()
            .getName())
            .equal(DSL.field(DSL.name(keyTable.getName(), entry.getValue()))))
        .forEach(dataQuery::addConditions);

    addExistsJoinColumns(dataQuery, joinColumns, joinConditionTable);

    return batchQuery(keyTable);
  }

  private SelectQuery<Record> batchQuery(Table<Record> keyTable) {
    var batchQuery = dslContext.selectQuery(keyTable);

    batchQuery.addJoin(DSL.lateral(dataQuery.asTable(aliasManager.newAlias())), JoinType.LEFT_OUTER_JOIN);
    batchQuery.addSelect(DSL.asterisk());

    return batchQuery;
  }

  private SelectQuery<Record> batchQueryWithJoinTable() {
    var joinTable = joinConfiguration.getJoinTable();

    var junctionTable = QueryHelper.findTable(joinTable.getName(), contextCriteria)
        .as(aliasManager.newAlias());

    dataQuery.addFrom(junctionTable);

    var joinConditions = createJoinConditions(junctionTable, table, joinTable.getInverseJoinColumns(),
        joinConfiguration.getTargetType());

    dataQuery.addConditions(joinConditions);

    return batchQuery(joinTable.getJoinColumns(), junctionTable);
  }

  private Table<Record> createValuesTable(Map<String, String> keyColumnAliases, Collection<Map<String, Object>> keys) {
    var keyTableRows = keys.stream()
        .map(joinKey -> keyColumnAliases.keySet()
            .stream()
            .map(joinKey::get)
            .toArray())
        .map(DSL::row)
        .toArray(RowN[]::new);

    return createValuesTable(keyColumnAliases, keyTableRows);

  }

  private Table<Record> createValuesTable(Map<String, String> keyColumnAliases, RowN[] keyTableRows) {
    // Register field mapper for grouping rows per key
    if (!fromUnion) {
      register(GROUP_KEY, keyColumnAliases);
    }

    return DSL.values(keyTableRows)
        .as(aliasManager.newAlias(), keyColumnAliases.values()
            .toArray(String[]::new));
  }

  private void addExistsJoinColumns(SelectQuery<Record> dataQuery, List<JoinColumn> joinColumns, Table<Record> table) {
    var columnsNames = joinColumns.stream()
        .map(JoinColumn::getName)
        .collect(Collectors.toList());

    addExists(dataQuery, columnsNames, table);
  }

  private void addExists(SelectQuery<Record> dataQuery, Collection<String> columnNames, Table<Record> table) {
    var columnAliases = columnNames.stream()
        .collect(Collectors.toMap(Function.identity(), columnName -> aliasManager.newAlias()));

    columnAliases.entrySet()
        .stream()
        .map(entry -> QueryHelper.column(table, entry.getKey())
            .as(entry.getValue()))
        .forEach(dataQuery::addSelect);

    if (!fromUnion) {
      register(EXISTS_KEY, columnAliases);
    }
  }

  private void register(String name, Map<String, String> columnAliases) {
    fieldMapper.register(name, row -> columnAliases.entrySet()
        .stream()
        .filter(entry -> !Objects.isNull(row.get(entry.getValue())))
        .collect(Collectors.toMap(Map.Entry::getKey, entry -> row.get(entry.getValue()), (prev, next) -> next,
            HashMap::new)));
  }
}

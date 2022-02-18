package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.helpers.ValidationHelper.validateFields;
import static org.dotwebstack.framework.backend.postgres.query.JoinHelper.createJoinConditions;
import static org.dotwebstack.framework.backend.postgres.query.JoinHelper.invertOnList;
import static org.dotwebstack.framework.backend.postgres.query.Query.EXISTS_KEY;
import static org.dotwebstack.framework.backend.postgres.query.Query.GROUP_KEY;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.columnName;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
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
class BatchJoinBuilder {

  private final DSLContext dslContext = DSL.using(SQLDialect.POSTGRES);

  private JoinConfiguration joinConfiguration;

  private ContextCriteria contextCriteria;

  @NotNull
  private ObjectFieldMapper<Map<String, Object>> fieldMapper;

  @NotNull
  private AliasManager aliasManager;

  @NotNull
  private SelectQuery<Record> dataQuery;

  private Table<Record> table;

  @NotNull
  private Set<Map<String, Object>> joinKeys;

  private BatchJoinBuilder() {}

  static BatchJoinBuilder newBatchJoining() {
    return new BatchJoinBuilder();
  }

  SelectQuery<Record> build() {
    validateFields(this);

    if (joinConfiguration == null) {
      return batchJoinWithKeysOnly();
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

      return newBatchJoining().joinConfiguration(mappedByJoinConfiguration)
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
      return batchJoin(invertOnList(joinConfiguration.getObjectField(), joinConfiguration.getJoinColumns()), table);
    }

    if (joinConfiguration.getJoinTable() != null) {
      return batchJoinTable();
    }

    throw illegalArgumentException("Object field '{}' has no relation configuration!",
        joinConfiguration.getObjectField()
            .getName());
  }

  private SelectQuery<Record> batchJoinWithKeysOnly() {
    var keyColumnAliases = joinKeys.stream()
        .findFirst()
        .stream()
        .flatMap(map -> map.keySet()
            .stream())
        .collect(Collectors.toMap(Function.identity(), field -> aliasManager.newAlias()));

    var keyTable = createValuesTable(keyColumnAliases, joinKeys);

    keyColumnAliases.entrySet()
        .stream()
        .map(entry -> QueryHelper.column(null, entry.getKey())
            .equal(QueryHelper.column(keyTable, entry.getValue())))
        .forEach(dataQuery::addConditions);

    addExists(dataQuery, new ArrayList<>(keyColumnAliases.keySet()), table);

    return batchJoin(keyTable);
  }

  private SelectQuery<Record> batchJoin(List<JoinColumn> joinColumns, Table<Record> joinConditionTable) {
    var objectType = joinConfiguration.getObjectType();

    var keyJoinColumnAliasMap = joinColumns.stream()
        .collect(Collectors.toMap(joinColumn -> joinColumn, joinColumn -> aliasManager.newAlias()));

    var keyColumnAliasMap = keyJoinColumnAliasMap.entrySet()
        .stream()
        .collect(Collectors.toMap(e -> columnName(e.getKey(), objectType), Map.Entry::getValue));

    var keyTable = createValuesTable(keyColumnAliasMap, joinKeys);

    keyJoinColumnAliasMap.entrySet()
        .stream()
        .map(entry -> QueryHelper.column(joinConditionTable, entry.getKey()
            .getName())
            .equal(DSL.field(DSL.name(keyTable.getName(), entry.getValue()))))
        .forEach(dataQuery::addConditions);

    addExistsForJoinColumns(dataQuery, joinColumns, joinConditionTable);

    return batchJoin(keyTable);
  }

  private SelectQuery<Record> batchJoin(Table<Record> keyTable) {
    var batchQuery = dslContext.selectQuery(keyTable);

    batchQuery.addJoin(DSL.lateral(dataQuery.asTable(aliasManager.newAlias())), JoinType.LEFT_OUTER_JOIN);
    batchQuery.addSelect(DSL.asterisk());

    return batchQuery;
  }

  private SelectQuery<Record> batchJoinTable() {
    var joinTable = joinConfiguration.getJoinTable();

    var junctionTable = QueryHelper.findTable(joinTable.getName(), contextCriteria)
        .as(aliasManager.newAlias());

    dataQuery.addFrom(junctionTable);

    var joinConditions = createJoinConditions(junctionTable, table, joinTable.getInverseJoinColumns(),
        joinConfiguration.getTargetType());

    dataQuery.addConditions(joinConditions);

    return batchJoin(joinTable.getJoinColumns(), junctionTable);
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
    fieldMapper.register(GROUP_KEY, row -> keyColumnAliases.entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getKey, columnAlias -> row.get(columnAlias.getValue()),
            (prev, next) -> next, HashMap::new)));

    return DSL.values(keyTableRows)
        .as(aliasManager.newAlias(), keyColumnAliases.values()
            .toArray(String[]::new));
  }

  private void addExistsForJoinColumns(SelectQuery<Record> dataQuery, List<JoinColumn> joinColumns,
      Table<Record> table) {
    var existsColumnNames = joinColumns.stream()
        .map(JoinColumn::getName)
        .collect(Collectors.toList());

    addExists(dataQuery, existsColumnNames, table);
  }

  private void addExists(SelectQuery<Record> dataQuery, List<String> existsColumnNames, Table<Record> table) {
    existsColumnNames.stream()
        .map(existsColumn -> QueryHelper.column(table, existsColumn))
        .forEach(dataQuery::addSelect);

    // TODO: Hier dienen we ook een alias te gebruiken

    // Register field mapper for exist row columns
    fieldMapper.register(EXISTS_KEY, row -> existsColumnNames.stream()
        .filter(key -> !Objects.isNull(row.get(key)))
        .collect(Collectors.toMap(Function.identity(), row::get, (prev, next) -> next, HashMap::new)));
  }
}

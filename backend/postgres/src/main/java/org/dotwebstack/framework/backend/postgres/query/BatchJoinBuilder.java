package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.helpers.ValidationHelper.validateFields;
import static org.dotwebstack.framework.backend.postgres.query.JoinHelper.invertOnList;
import static org.dotwebstack.framework.backend.postgres.query.Query.EXISTS_KEY;
import static org.dotwebstack.framework.backend.postgres.query.Query.GROUP_KEY;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.columnName;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.createJoinConditions;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.dotwebstack.framework.backend.postgres.model.JoinColumn;
import org.dotwebstack.framework.backend.postgres.model.JoinTable;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.core.backend.query.AliasManager;
import org.dotwebstack.framework.core.backend.query.ObjectFieldMapper;
import org.dotwebstack.framework.core.query.model.ContextCriteria;
import org.dotwebstack.framework.core.query.model.JoinCriteria;
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

  @NotNull
  private PostgresObjectField objectField;

  @NotNull
  private PostgresObjectType targetObjectType;

  private ContextCriteria contextCriteria;

  @NotNull
  private ObjectFieldMapper<Map<String, Object>> fieldMapper;

  @NotNull
  private AliasManager aliasManager;

  @NotNull
  private SelectQuery<Record> dataQuery;

  private Table<Record> table;

  @NotNull
  private JoinCriteria joinCriteria;

  private BatchJoinBuilder() {}

  static BatchJoinBuilder newBatchJoining() {
    return new BatchJoinBuilder();
  }

  SelectQuery<Record> build() {
    validateFields(this);

    PostgresObjectField resolvedObjectField = getObjectField();

    if (!resolvedObjectField.getJoinColumns()
        .isEmpty()) {
      return batchJoin(invertOnList(resolvedObjectField, resolvedObjectField.getJoinColumns()));
    }

    if (resolvedObjectField.getJoinTable() != null) {
      return batchJoin(objectField, resolvedObjectField.getJoinTable(), targetObjectType);
    }

    return batchJoinWithKeysOnly();
  }

  private SelectQuery<Record> batchJoinWithKeysOnly() {
    var keyColumnNames = joinCriteria.getKeys()
        .stream()
        .findFirst()
        .stream()
        .flatMap(map -> map.keySet()
            .stream())
        .collect(Collectors.toMap(key -> key, key -> key));

    var keyTable = createValuesTable(keyColumnNames, joinCriteria.getKeys());

    keyColumnNames.values()
        .stream()
        .map(columnName -> QueryHelper.column(null, columnName)
            .equal(QueryHelper.column(keyTable, columnName)))
        .forEach(dataQuery::addConditions);

    return batchJoin(keyTable);
  }

  private SelectQuery<Record> batchJoin(List<JoinColumn> joinColumns, Table<Record> joinConditionTable) {

    var objectType = (PostgresObjectType) objectField.getObjectType();

    var keyJoinColumnAliasMap = joinColumns.stream()
        .collect(Collectors.toMap(joinColumn -> joinColumn, joinColumn -> aliasManager.newAlias()));

    var keyColumnAliasMap = keyJoinColumnAliasMap.entrySet()
        .stream()
        .collect(Collectors.toMap(e -> columnName(e.getKey(), objectType), Map.Entry::getValue));

    var keyTable = createValuesTable(keyColumnAliasMap, joinCriteria.getKeys());

    keyJoinColumnAliasMap.entrySet()
        .stream()
        .map(entry -> QueryHelper.column(joinConditionTable, entry.getKey()
            .getName())
            .equal(DSL.field(DSL.name(keyTable.getName(), entry.getValue()))))
        .forEach(dataQuery::addConditions);


    addExists(dataQuery, joinColumns, joinConditionTable);

    return batchJoin(keyTable);
  }

  private SelectQuery<Record> batchJoin(Table<Record> keyTable) {
    var batchQuery = dslContext.selectQuery(keyTable);

    batchQuery.addJoin(DSL.lateral(dataQuery.asTable(aliasManager.newAlias())), JoinType.LEFT_OUTER_JOIN);
    batchQuery.addSelect(DSL.asterisk());

    return batchQuery;
  }

  private SelectQuery<Record> batchJoin(List<JoinColumn> joinColumns) {
    return batchJoin(joinColumns, table);
  }

  private SelectQuery<Record> batchJoin(PostgresObjectField objectField, JoinTable joinTable,
      PostgresObjectType targetObjectType) {
    var junctionTable = QueryHelper.findTable(joinTable.getName(), contextCriteria)
        .as(aliasManager.newAlias());

    dataQuery.addFrom(junctionTable);

    var owningJoinColumns =
        objectField.getMappedByObjectField() != null ? joinTable.getJoinColumns() : joinTable.getInverseJoinColumns();

    var keyJoinColumns =
        objectField.getMappedByObjectField() != null ? joinTable.getInverseJoinColumns() : joinTable.getJoinColumns();

    dataQuery.addConditions(createJoinConditions(junctionTable, table, owningJoinColumns, targetObjectType));

    return batchJoin(keyJoinColumns, junctionTable);
  }

  private Table<Record> createValuesTable(Map<String, String> keyColumnNames, Collection<Map<String, Object>> keys) {
    var keyTableRows = keys.stream()
        .map(joinKey -> keyColumnNames.keySet()
            .stream()
            .map(joinKey::get)
            .toArray())
        .map(DSL::row)
        .toArray(RowN[]::new);

    return createValuesTable(keyColumnNames, keyTableRows);

  }

  private Table<Record> createValuesTable(Map<String, String> keyColumnAliases, RowN[] keyTableRows) {
    // Register field mapper for grouping rows per key
    fieldMapper.register(GROUP_KEY, row -> keyColumnAliases.entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getKey, columnAlias -> row.get(columnAlias.getValue()))));

    return DSL.values(keyTableRows)
        .as(aliasManager.newAlias(), keyColumnAliases.values()
            .toArray(String[]::new));
  }

  private void addExists(SelectQuery<Record> dataQuery, List<JoinColumn> joinColumns, Table<Record> table) {
    var existsColumnNames = joinColumns.stream()
        .map(JoinColumn::getName)
        .collect(Collectors.toList());

    existsColumnNames.stream()
        .map(existsColumn -> QueryHelper.column(table, existsColumn))
        .forEach(dataQuery::addSelect);

    // Register field mapper for exist row columns
    fieldMapper.register(EXISTS_KEY, row -> existsColumnNames.stream()
        .filter(key -> !Objects.isNull(row.get(key)))
        .collect(Collectors.toMap(Function.identity(), row::get, (prev, next) -> next, HashMap::new)));
  }

  private PostgresObjectField getObjectField() {
    return Optional.of(objectField)
        .map(PostgresObjectField::getMappedByObjectField)
        .orElse(objectField);
  }
}

package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.query.Query.EXISTS_KEY;
import static org.dotwebstack.framework.backend.postgres.query.Query.GROUP_KEY;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.columnName;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.createJoinConditions;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.getObjectType;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.dotwebstack.framework.backend.postgres.model.JoinColumn;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.core.backend.query.AliasManager;
import org.dotwebstack.framework.core.backend.query.ObjectFieldMapper;
import org.dotwebstack.framework.core.query.model.JoinCriteria;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.RequestContext;
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
  private RequestContext requestContext;

  @NotNull
  private ObjectFieldMapper<Map<String, Object>> fieldMapper;

  @NotNull
  private AliasManager aliasManager;

  @NotNull
  private SelectQuery<Record> dataQuery;

  @NotNull
  private Table<Record> table;

  @NotNull
  private JoinCriteria joinCriteria;

  @NotNull
  private ObjectRequest objectRequest;

  private BatchJoinBuilder() {}

  static BatchJoinBuilder newBatchJoining() {
    return new BatchJoinBuilder();
  }

  SelectQuery<Record> build() {
    var objectField = (PostgresObjectField) requestContext.getObjectField();

    if (objectField.getMappedByObjectField() != null) {
      objectField = objectField.getMappedByObjectField();
    }

    if (!objectField.getJoinColumns()
        .isEmpty()) {
      return batchJoin(objectField.getJoinColumns());
    }

    if (objectField.getJoinTable() != null) {
      var targetObjectType = getObjectType(objectRequest);
      return batchJoin(targetObjectType);
    }

    throw new UnsupportedOperationException();
  }

  private SelectQuery<Record> batchJoin(List<JoinColumn> joinColumns) {
    var objectType = (PostgresObjectType) requestContext.getObjectField()
        .getObjectType();

    // Create virtual table with static key values
    var keyTable = createValuesTable(objectType, joinColumns, joinCriteria.getKeys());

    var batchQuery = dslContext.selectQuery(keyTable);

    dataQuery.addConditions(createJoinConditions(table, keyTable, joinColumns, objectType));

    addExists(dataQuery, joinColumns, table);

    batchQuery.addJoin(DSL.lateral(dataQuery.asTable(aliasManager.newAlias())), JoinType.LEFT_OUTER_JOIN);
    batchQuery.addSelect(DSL.asterisk());

    return batchQuery;
  }

  private SelectQuery<Record> batchJoin(PostgresObjectType targetObjectType) {
    var objectField = (PostgresObjectField) requestContext.getObjectField();
    var objectType = (PostgresObjectType) objectField.getObjectType();
    var joinTable = objectField.getJoinTable();

    // Create virtual table with static key values
    var keyTable = createValuesTable(objectType, joinTable.getJoinColumns(), joinCriteria.getKeys());

    var junctionTable = DSL.table(joinTable.getName())
        .as(aliasManager.newAlias());

    dataQuery.addFrom(junctionTable);
    dataQuery.addConditions(createJoinConditions(junctionTable, keyTable, joinTable.getJoinColumns(), objectType));

    addExists(dataQuery, joinTable.getJoinColumns(), junctionTable);

    dataQuery
        .addConditions(createJoinConditions(junctionTable, table, joinTable.getInverseJoinColumns(), targetObjectType));

    var batchQuery = dslContext.selectQuery(keyTable);

    batchQuery.addJoin(DSL.lateral(dataQuery.asTable(aliasManager.newAlias())), JoinType.LEFT_OUTER_JOIN);
    batchQuery.addSelect(DSL.asterisk());

    return batchQuery;
  }

  private Table<Record> createValuesTable(PostgresObjectType objectType, List<JoinColumn> joinColumns,
      Collection<Map<String, Object>> keys) {
    var keyColumnNames = joinColumns.stream()
        .map(joinColumn -> columnName(joinColumn, objectType))
        .collect(Collectors.toList());

    var keyTableRows = keys.stream()
        .map(joinKey -> keyColumnNames.stream()
            .map(joinKey::get)
            .toArray())
        .map(DSL::row)
        .toArray(RowN[]::new);

    // Register field mapper for grouping rows per key
    fieldMapper.register(GROUP_KEY, row -> keyColumnNames.stream()
        .collect(Collectors.toMap(Function.identity(), row::get)));

    return DSL.values(keyTableRows)
        .as(aliasManager.newAlias(), keyColumnNames.toArray(String[]::new));
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
}

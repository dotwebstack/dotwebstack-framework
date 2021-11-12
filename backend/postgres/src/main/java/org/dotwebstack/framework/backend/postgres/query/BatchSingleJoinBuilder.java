package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.helpers.ValidationHelper.validateFields;
import static org.dotwebstack.framework.backend.postgres.query.Query.GROUP_KEY;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.dotwebstack.framework.core.backend.query.AliasManager;
import org.dotwebstack.framework.core.backend.query.ObjectFieldMapper;
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
public class BatchSingleJoinBuilder {

  private final DSLContext dslContext = DSL.using(SQLDialect.POSTGRES);

  @NotNull
  private ObjectFieldMapper<Map<String, Object>> fieldMapper;

  @NotNull
  private AliasManager aliasManager;

  @NotNull
  private SelectQuery<Record> dataQuery;

  @NotNull
  private Set<Map<String, Object>> keys;

  private BatchSingleJoinBuilder() {}

  static BatchSingleJoinBuilder newBatchSingleJoin() {
    return new BatchSingleJoinBuilder();
  }

  SelectQuery<Record> build() {
    validateFields(this);

    var keyColumnNames = keys.stream()
        .findFirst()
        .stream()
        .flatMap(map -> map.keySet()
            .stream())
        .collect(Collectors.toList());

    // Create virtual table with static key values
    var keyTable = createValuesTable(keyColumnNames, keys);

    keyColumnNames.stream()
        .map(columnName -> QueryHelper.column(null, columnName)
            .equal(QueryHelper.column(keyTable, columnName)))
        .forEach(dataQuery::addConditions);

    var batchQuery = dslContext.selectQuery(keyTable);

    batchQuery.addJoin(DSL.lateral(dataQuery.asTable(aliasManager.newAlias())), JoinType.LEFT_OUTER_JOIN);
    batchQuery.addSelect(DSL.asterisk());

    return batchQuery;
  }

  private Table<Record> createValuesTable(Collection<String> keyColumnNames, Collection<Map<String, Object>> keys) {
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


}
